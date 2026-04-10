package main.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterProperties;

@Service
public class EmailDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(EmailDispatcher.class);

    private static final int QUEUE_CAPACITY = 500;
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 2_000L;

    private final BlockingQueue<EmailTask> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final JavaMailSender mailSender;
    private final JHipsterProperties jHipsterProperties;
    private Thread workerThread;

    public EmailDispatcher(JavaMailSender mailSender, JHipsterProperties jHipsterProperties) {
        this.mailSender = mailSender;
        this.jHipsterProperties = jHipsterProperties;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @PostConstruct
    public void start() {
        running.set(true);
        workerThread = new Thread(this::processLoop, "email-dispatcher");
        workerThread.setDaemon(true);
        workerThread.start();
        LOG.info("EmailDispatcher avviato (coda max {} messaggi)", QUEUE_CAPACITY);
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        LOG.info("EmailDispatcher: shutdown — svuoto la coda ({} messaggi rimasti)", queue.size());
        running.set(false);
        workerThread.interrupt();
        workerThread.join(10_000); // aspetta max 10s
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pubblica
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Accoda un'email già renderizzata per l'invio asincrono.
     * Ritorna immediatamente — non blocca mai.
     *
     * @param task email task con HTML già generato
     */
    public void enqueue(EmailTask task) {
        if (!queue.offer(task)) {
            LOG.error("Coda email piena! Email a '{}' scartata. Aumentare QUEUE_CAPACITY.", task.to());
        } else {
            LOG.debug("Email accodata per '{}' — coda: {}/{}", task.to(), queue.size(), QUEUE_CAPACITY);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Worker loop
    // ─────────────────────────────────────────────────────────────────────────

    private void processLoop() {
        LOG.debug("EmailDispatcher worker avviato");
        while (running.get() || !queue.isEmpty()) {
            try {
                EmailTask task = queue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    sendWithRetry(task, 0);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        LOG.debug("EmailDispatcher worker terminato");
    }

    private void sendWithRetry(EmailTask task, int attempt) {
        try {
            doSend(task);
            LOG.debug("Email inviata a '{}' (tentativo {})", task.to(), attempt + 1);
        } catch (Exception e) {
            if (attempt < MAX_RETRIES - 1) {
                long delay = BASE_DELAY_MS * (1L << attempt); // 2s, 4s, 8s
                LOG.warn(
                    "Invio email a '{}' fallito (tentativo {}/{}), retry tra {}ms: {}",
                    task.to(),
                    attempt + 1,
                    MAX_RETRIES,
                    delay,
                    e.getMessage()
                );
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                sendWithRetry(task, attempt + 1);
            } else {
                // Dead Letter: log completo, nessun retry
                LOG.error(
                    "Email a '{}' definitivamente fallita dopo {} tentativi — soggetto: '{}'",
                    task.to(),
                    MAX_RETRIES,
                    task.subject(),
                    e
                );
                // TODO produzione: salvare in tabella dead_letter_email per riprocesso manuale
            }
        }
    }

    private void doSend(EmailTask task) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, !task.inlineAttachments().isEmpty(), "UTF-8");

        h.setTo(task.to());
        h.setFrom(jHipsterProperties.getMail().getFrom());
        h.setSubject(task.subject());
        h.setText(task.htmlContent(), true);

        for (InlineAttachment att : task.inlineAttachments()) {
            h.addInline(att.contentId(), new ByteArrayResource(att.data()), att.contentType());
        }

        mailSender.send(mime);
    }
}
