package main.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.User;
import main.service.dto.PrenotazioniEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;
    private final EmailDispatcher emailDispatcher;

    // Bytes del logo caricati una volta sola all'avvio (evita I/O ripetuto)
    private final byte[] logoBytes;

    public MailService(
        JHipsterProperties jHipsterProperties,
        MessageSource messageSource,
        SpringTemplateEngine templateEngine,
        EmailDispatcher emailDispatcher
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.emailDispatcher = emailDispatcher;

        // Precarica il logo una volta sola → evita ClassPathResource I/O ad ogni email
        byte[] bytes;
        try {
            bytes = new ClassPathResource("imags/logo-jhipster.png").getInputStream().readAllBytes();
        } catch (Exception e) {
            LOG.warn("Logo email non trovato: {}", e.getMessage());
            bytes = new byte[0];
        }
        this.logoBytes = bytes;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Email di sistema (attivazione, reset password, creazione account)
    // ─────────────────────────────────────────────────────────────────────────

    public void sendActivationEmail(User user) {
        LOG.debug("Accodamento email attivazione per '{}'", user.getEmail());
        enqueueFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    public void sendCreationEmail(User user) {
        LOG.debug("Accodamento email creazione per '{}'", user.getEmail());
        enqueueFromTemplate(user, "mail/creationEmail", "email.activation.title");
    }

    public void sendPasswordResetMail(User user) {
        LOG.debug("Accodamento email reset password per '{}'", user.getEmail());
        enqueueFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }

    private void enqueueFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            LOG.debug("Email non presente per user '{}', skip", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context ctx = new Context(locale);
        ctx.setVariable(USER, user);
        ctx.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());

        String content = templateEngine.process(templateName, ctx); // CPU only
        String subject = messageSource.getMessage(titleKey, null, locale);

        emailDispatcher.enqueue(
            new EmailTask(user.getEmail(), subject, content, List.of(new InlineAttachment("logoimg", logoBytes, "image/png")))
        );
    }

    public void sendPrenotazioneEventoPublico(Eventi evento, PrenotazioniEmailDTO dto, String codicePre, String qrCod) {
        // 1. Render (CPU, veloce — nessun I/O)
        Context ctx = new Context();
        ctx.setVariable("titoloEvento", evento.getTitolo());
        ctx.setVariable("descrizioneEvento", evento.getDescrizione());
        ctx.setVariable("tipoEvento", evento.getTipo().name());
        ctx.setVariable("prezzoEvento", evento.getPrezzo());

        Prenotazioni p = evento.getPrenotazione();
        ctx.setVariable("dataPrenotazione", p.getData());
        ctx.setVariable("oraInizio", p.getOraInizio());
        ctx.setVariable("oraFine", p.getOraFine());
        ctx.setVariable("salaNome", p.getSala().getNome());
        ctx.setVariable("salaCapienza", p.getSala().getCapienza());
        ctx.setVariable("nome", dto.getNome());
        ctx.setVariable("cognome", dto.getCognome());
        ctx.setVariable("email", dto.getEmail());
        ctx.setVariable("codicePre", codicePre);

        String content = templateEngine.process("mail/eventoPrenotazioneEmail", ctx);
        byte[] qrBytes = Base64.getDecoder().decode(qrCod);

        // 2. Accoda (ritorna immediatamente)
        emailDispatcher.enqueue(
            new EmailTask(
                dto.getEmail(),
                "Conferma Prenotazione Evento",
                content,
                List.of(new InlineAttachment("qrcode", qrBytes, "image/png"), new InlineAttachment("logoimg", logoBytes, "image/png"))
            )
        );
    }

    public void sendPromemoriaPrenotazione(
        String email,
        String nomeUtente,
        String titoloEvento,
        String data,
        String oraInizio,
        String oraFine,
        String nomeSala,
        String etichettaTipo
    ) {
        // 1. Render (CPU only)
        Context ctx = new Context();
        ctx.setVariable("nomeUtente", nomeUtente);
        ctx.setVariable("titoloEvento", titoloEvento);
        ctx.setVariable("dataPrenotazione", data);
        ctx.setVariable("oraInizio", oraInizio);
        ctx.setVariable("oraFine", oraFine);
        ctx.setVariable("nomeSala", nomeSala);
        ctx.setVariable("etichettaTipo", etichettaTipo);

        String content = templateEngine.process("mail/promemoriaEmail", ctx);

        // 2. Accoda
        emailDispatcher.enqueue(
            new EmailTask(
                email,
                "Promemoria: " + titoloEvento + " — " + etichettaTipo,
                content,
                List.of(new InlineAttachment("logoimg", logoBytes, "image/png"))
            )
        );

        LOG.debug("Promemoria accodato per '{}' — tipo: {}", email, etichettaTipo);
    }
}
