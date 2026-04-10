package main.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import main.domain.Prenotazioni;
import main.domain.Promemoria;
import main.domain.User;
import main.repository.PromemoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler giornaliero per l'invio dei promemoria.
 *
 * Esecuzione: ogni giorno alle 08:00.
 * Strategia anti-duplicati: campo "inviato" impostato a true dopo l'invio.
 *
 * Flusso:
 *  1. Trova tutti i Promemoria con inviato=false, abilitato=true
 *     e data evento = oggi + giorniPrima(tipo)
 *  2. Per ognuno: invia email + imposta inviato=true + salva timestamp
 *  3. Log di ogni invio
 */
@Service
public class ReminderSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(ReminderSchedulerService.class);

    private static final DateTimeFormatter FORMATTER_IT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN);

    private final PromemoriaRepository proRepository;
    private final MailService mailService;

    public ReminderSchedulerService(PromemoriaRepository proRepository, MailService mailService) {
        this.proRepository = proRepository;
        this.mailService = mailService;
    }

    /**
     * Job schedulato: ogni giorno alle 08:00.
     * cron = "0 0 8 * * *"  →  secondi minuti ore giorno mese giorno-settimana
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void inviaPromemoriaGiornalieri() {
        LocalDate oggi = LocalDate.now();

        // FIX: calcolo in Java — JPQL non supporta :oggi + 7
        LocalDate dataUnGiorno = oggi.plusDays(1);
        LocalDate dataDueGiorni = oggi.plusDays(2);
        LocalDate dataSettimana = oggi.plusDays(7);

        LOG.info("=== ReminderScheduler avviato [{}] — target: +1={}, +2={}, +7={} ===", oggi, dataUnGiorno, dataDueGiorni, dataSettimana);

        List<Promemoria> daInviare = proRepository.findDaInviareOggi(oggi, dataUnGiorno, dataDueGiorni, dataSettimana);
        LOG.info("Trovati {} promemoria da inviare", daInviare.size());

        int inviati = 0;
        int errori = 0;

        for (Promemoria promemoria : daInviare) {
            try {
                inviaPromemoria(promemoria);
                // Marca come inviato (anti-duplicati)
                promemoria.setInviato(true);
                promemoria.setInviatoAlle(LocalDateTime.now());
                proRepository.save(promemoria);
                inviati++;
                LOG.debug(
                    "Promemoria {} inviato → prenotazione={}, tipo={}",
                    promemoria.getId(),
                    promemoria.getPrenotazione().getId(),
                    promemoria.getTipo()
                );
            } catch (Exception e) {
                errori++;
                LOG.error(
                    "Errore invio promemoria {} (prenotazione={}, tipo={}): {}",
                    promemoria.getId(),
                    promemoria.getPrenotazione().getId(),
                    promemoria.getTipo(),
                    e.getMessage()
                );
                // Non rilanciamo l'eccezione: continuiamo con i successivi
            }
        }

        LOG.info("=== ReminderScheduler completato: inviati={}, errori={} ===", inviati, errori);
    }

    private void inviaPromemoria(Promemoria promemoria) {
        Prenotazioni p = promemoria.getPrenotazione();

        // Recupera email utente
        if (p.getUtente() == null || p.getUtente().getUser() == null) {
            LOG.warn("Promemoria {} senza utente valido, skip", promemoria.getId());
            return;
        }

        User user = p.getUtente().getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            LOG.warn("Utente {} senza email, skip", user.getLogin());
            return;
        }

        // Costruisce i dati per il template
        String dataFormattata = p.getData().format(FORMATTER_IT);
        String nomeSala = p.getSala() != null ? p.getSala().getNome() : "N/D";
        String titoloEvento = p.getEvento() != null ? p.getEvento().getTitolo() : "Prenotazione sala " + nomeSala;
        String nomeUtente = user.getFirstName() != null ? user.getFirstName() : user.getLogin();
        String etichettaTipo = promemoria.getTipo().getEtichetta();

        mailService.sendPromemoriaPrenotazione(
            user.getEmail(),
            nomeUtente,
            titoloEvento,
            dataFormattata,
            p.getOraInizio().toString(),
            p.getOraFine().toString(),
            nomeSala,
            etichettaTipo
        );
    }
}
