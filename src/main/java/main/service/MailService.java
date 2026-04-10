package main.service;

import java.util.Base64;
import java.util.List;
import java.util.Locale;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.User;
import main.domain.enumeration.TipoEvento;
import main.service.dto.PrenotazioniEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tech.jhipster.config.JHipsterProperties;

@Service
public class MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;
    private final EmailDispatcher emailDispatcher;

    // Bytes del logo caricati una volta sola all'avvio
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

        String content = templateEngine.process(templateName, ctx);
        String subject = messageSource.getMessage(titleKey, null, locale);

        emailDispatcher.enqueue(
            new EmailTask(user.getEmail(), subject, content, List.of(new InlineAttachment("logoimg", logoBytes, "image/png")))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Email conferma prenotazione (scattata al passaggio di stato → CONFIRMED)
    // Gestisce sia eventi pubblici (con prezzo + biglietto) che privati/sala
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Invia l'email di conferma quando una prenotazione passa a stato CONFIRMED.
     *
     * @param prenotazione  l'entità prenotazione appena confermata (con sala, utente, evento caricati)
     * @param dto           dati anagrafici dell'utente (nome, cognome, email)
     * @param codicePre     codice prenotazione alfanumerico
     * @param qrCodeBase64  QR code in formato base64
     */
    public void sendConfermaPrenotazione(Prenotazioni prenotazione, PrenotazioniEmailDTO dto, String codicePre, String qrCodeBase64) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            LOG.warn("Email destinatario assente per prenotazione {}, skip invio conferma", prenotazione.getId());
            return;
        }

        Context ctx = new Context();

        // ── Dati prenotazione ─────────────────────────────────────────────
        ctx.setVariable("nome", dto.getNome());
        ctx.setVariable("cognome", dto.getCognome());
        ctx.setVariable("codicePre", codicePre);
        ctx.setVariable("dataPrenotazione", prenotazione.getData());
        ctx.setVariable("oraInizio", prenotazione.getOraInizio());
        ctx.setVariable("oraFine", prenotazione.getOraFine());

        // ── Dati sala ─────────────────────────────────────────────────────
        String salaNome = prenotazione.getSala() != null ? prenotazione.getSala().getNome() : "N/D";
        int salaCapienza = prenotazione.getSala() != null ? prenotazione.getSala().getCapienza() : 0;
        String salaImageUrl = prenotazione.getSala() != null ? prenotazione.getSala().getImageUrl() : null;

        ctx.setVariable("salaNome", salaNome);
        ctx.setVariable("salaCapienza", salaCapienza);
        ctx.setVariable("salaImageUrl", salaImageUrl);

        // ── Dati evento (se collegato) ────────────────────────────────────
        Eventi evento = prenotazione.getEvento();
        boolean eventoPubblico = false;

        if (evento != null) {
            ctx.setVariable("titoloEvento", evento.getTitolo());
            ctx.setVariable("tipoEvento", evento.getTipo() != null ? evento.getTipo().name() : "PRIVATO");
            ctx.setVariable("prezzoEvento", evento.getPrezzo());
            eventoPubblico = TipoEvento.PUBBLICO.equals(evento.getTipo());
        } else {
            // Prenotazione sala senza evento associato
            ctx.setVariable("titoloEvento", "Prenotazione sala " + salaNome);
            ctx.setVariable("tipoEvento", "PRIVATO");
            ctx.setVariable("prezzoEvento", null);
        }

        ctx.setVariable("eventoPubblico", eventoPubblico);

        // ── Render template ───────────────────────────────────────────────
        // L'immagine sala è un data URL base64 (es: "data:image/jpeg;base64,...")
        // Gmail blocca i data URL inline → la passiamo come allegato cid:salaimg
        boolean haSalaImage = salaImageUrl != null && !salaImageUrl.isBlank();
        byte[] salaImgBytes = new byte[0];
        String salaImgMime = "image/jpeg";
        if (haSalaImage) {
            try {
                // Estrae la parte base64 pura dopo la virgola (es: "data:image/png;base64,ABC..." → "ABC...")
                String raw = salaImageUrl.contains(",") ? salaImageUrl.substring(salaImageUrl.indexOf(',') + 1) : salaImageUrl;
                salaImgBytes = Base64.getDecoder().decode(raw.trim());
                if (salaImageUrl.contains("image/png")) salaImgMime = "image/png";
                else if (salaImageUrl.contains("image/webp")) salaImgMime = "image/webp";
                // Sostituisce il data URL con il riferimento cid nel contesto
                ctx.setVariable("salaImageUrl", null); // evita che Thymeleaf metta il base64 nell'img src
            } catch (Exception e) {
                LOG.warn("Impossibile decodificare immagine sala: {}", e.getMessage());
                haSalaImage = false;
            }
        }
        ctx.setVariable("haSalaImage", haSalaImage);

        String content = templateEngine.process("mail/confermaPrenotazioneEmail", ctx);

        // ── Accoda l'invio (senza QR, con immagine sala come cid) ────────
        java.util.List<InlineAttachment> allegati = new java.util.ArrayList<>();
        allegati.add(new InlineAttachment("logoimg", logoBytes, "image/png"));
        if (haSalaImage) {
            allegati.add(new InlineAttachment("salaimg", salaImgBytes, salaImgMime));
        }

        emailDispatcher.enqueue(new EmailTask(dto.getEmail(), "Prenotazione confermata — " + salaNome, content, allegati));

        LOG.debug("Email conferma prenotazione accodata per '{}' — prenotazione {}", dto.getEmail(), prenotazione.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Email prenotazione evento pubblico (flusso precedente — mantenuto)
    // ─────────────────────────────────────────────────────────────────────────

    public void sendPrenotazioneEventoPublico(Eventi evento, PrenotazioniEmailDTO dto, String codicePre, String qrCod) {
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

        // ── Usa il template originale del biglietto evento ────────────────
        String content = templateEngine.process("mail/eventoPrenotazioneEmail", ctx);
        byte[] qrBytes = Base64.getDecoder().decode(qrCod);

        emailDispatcher.enqueue(
            new EmailTask(
                dto.getEmail(),
                "Biglietto evento — " + evento.getTitolo(),
                content,
                List.of(new InlineAttachment("qrcode", qrBytes, "image/png"), new InlineAttachment("logoimg", logoBytes, "image/png"))
            )
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Promemoria prenotazioni
    // ─────────────────────────────────────────────────────────────────────────

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
        Context ctx = new Context();
        ctx.setVariable("nomeUtente", nomeUtente);
        ctx.setVariable("titoloEvento", titoloEvento);
        ctx.setVariable("dataPrenotazione", data);
        ctx.setVariable("oraInizio", oraInizio);
        ctx.setVariable("oraFine", oraFine);
        ctx.setVariable("nomeSala", nomeSala);
        ctx.setVariable("etichettaTipo", etichettaTipo);

        String content = templateEngine.process("mail/promemoriaEmail", ctx);

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

    // ─────────────────────────────────────────────────────────────────────────
    // Email waitlist
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Notifica all'utente che è stato inserito in lista di attesa.
     */
    public void sendWaitlistNotifica(
        String email,
        String nomeUtente,
        String nomeSala,
        String data,
        String oraInizio,
        String oraFine,
        int posizione
    ) {
        Context ctx = new Context();
        ctx.setVariable("nomeUtente", nomeUtente);
        ctx.setVariable("nomeSala", nomeSala);
        ctx.setVariable("dataPrenotazione", data);
        ctx.setVariable("oraInizio", oraInizio);
        ctx.setVariable("oraFine", oraFine);
        ctx.setVariable("posizione", posizione);

        String content = templateEngine.process("mail/waitlistNotificaEmail", ctx);

        emailDispatcher.enqueue(
            new EmailTask(email, "Lista di attesa — " + nomeSala, content, List.of(new InlineAttachment("logoimg", logoBytes, "image/png")))
        );

        LOG.debug("Email waitlist accodata per '{}' — posizione {}", email, posizione);
    }

    /**
     * Notifica all'utente che è stato promosso dalla waitlist e ha 15 minuti per confermare.
     */
    public void sendWaitlistPromossa(
        String email,
        String nomeUtente,
        String nomeSala,
        String data,
        String oraInizio,
        String oraFine,
        String prenotazioneId,
        int minutiConferma
    ) {
        Context ctx = new Context();
        ctx.setVariable("nomeUtente", nomeUtente);
        ctx.setVariable("nomeSala", nomeSala);
        ctx.setVariable("dataPrenotazione", data);
        ctx.setVariable("oraInizio", oraInizio);
        ctx.setVariable("oraFine", oraFine);
        ctx.setVariable("prenotazioneId", prenotazioneId);
        ctx.setVariable("minutiConferma", minutiConferma);
        ctx.setVariable("baseUrl", jHipsterProperties.getMail().getBaseUrl());

        String content = templateEngine.process("mail/waitlistPromossaEmail", ctx);

        emailDispatcher.enqueue(
            new EmailTask(
                email,
                "Posto disponibile! Conferma entro " + minutiConferma + " minuti — " + nomeSala,
                content,
                List.of(new InlineAttachment("logoimg", logoBytes, "image/png"))
            )
        );

        LOG.debug("Email promozione waitlist accodata per '{}' — prenotazione {}", email, prenotazioneId);
    }
}
