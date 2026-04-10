package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.PrenotazioniEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestione completa della lista di attesa (waitlist) per le prenotazioni sala.
 *
 * Flusso:
 * 1. Utente prova a prenotare → slot occupato → entra in WAITLISTED con posizione FIFO
 * 2. Prenotazione confermata viene cancellata → promuoviPrimoInWaitlist()
 * 3. Utente promosso ha 15 minuti per confermare → stato PROMOTED
 * 4. Se non conferma → EXPIRED, si passa al prossimo in lista
 *
 * Anti race-condition: lock pessimistico sulla sala + transazione serializzata.
 */
@Service
@Transactional
public class WaitlistService {

    private static final Logger LOG = LoggerFactory.getLogger(WaitlistService.class);

    /** Minuti entro cui l'utente promosso deve confermare prima di perdere il posto. */
    private static final int MINUTI_CONFERMA = 15;

    private final PrenotazioniRepository prenotazioniRepository;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final SaleRepository saleRepository;
    private final MailService mailService;
    private final QrCodeGenerator qrCodeGenerator;

    public WaitlistService(
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        SaleRepository saleRepository,
        MailService mailService,
        QrCodeGenerator qrCodeGenerator
    ) {
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.saleRepository = saleRepository;
        this.mailService = mailService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INSERIMENTO IN WAITLIST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Aggiunge una prenotazione alla waitlist per lo slot richiesto.
     * Calcola automaticamente la posizione FIFO.
     * Chiamato da PrenotazioniService quando lo slot è occupato.
     */
    public Prenotazioni aggiungiAWaitlist(Prenotazioni prenotazione) {
        Sale sala = prenotazione.getSala();
        if (sala == null) throw new IllegalArgumentException("Sala obbligatoria");

        // Controlla che l'utente non sia già in waitlist per lo stesso slot
        boolean giaInCoda = prenotazioniRepository.existsWaitlistPerStessoSlot(
            sala,
            prenotazione.getData(),
            prenotazione.getOraInizio(),
            prenotazione.getOraFine(),
            prenotazione.getUtente()
        );
        if (giaInCoda) {
            throw new IllegalStateException("Sei già in lista di attesa per questo slot.");
        }

        // Calcola la prossima posizione disponibile (FIFO)
        int prossimaPos =
            prenotazioniRepository
                .maxPosizioneWaitlist(sala, prenotazione.getData(), prenotazione.getOraInizio(), prenotazione.getOraFine())
                .orElse(0) +
            1;

        StatiPrenotazione statoWaitlisted = getStato(StatoCodice.WAITLISTED);
        prenotazione.setStato(statoWaitlisted);
        prenotazione.setPosizioneWaitlist(prossimaPos);

        Prenotazioni salvata = prenotazioniRepository.save(prenotazione);

        // Notifica l'utente che è in lista di attesa
        inviaEmailWaitlist(salvata, prossimaPos);

        LOG.info(
            "Prenotazione {} aggiunta in waitlist pos {} per sala {} slot {}-{}",
            salvata.getId(),
            prossimaPos,
            sala.getNome(),
            prenotazione.getOraInizio(),
            prenotazione.getOraFine()
        );

        return salvata;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROMOZIONE DALLA WAITLIST (chiamato alla cancellazione)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Quando una prenotazione CONFIRMED viene cancellata, promuove automaticamente
     * il primo utente in waitlist per quello slot.
     *
     * Usa lock pessimistico sulla sala per evitare race condition
     * (es: due cancellazioni simultanee che promuovono lo stesso utente).
     *
     * @param prenotazioneCancellata la prenotazione appena cancellata
     */
    public void promuoviDaWaitlist(Prenotazioni prenotazioneCancellata) {
        Sale sala = prenotazioneCancellata.getSala();

        // Lock pessimistico sulla sala — serializza le promozioni concorrenti
        saleRepository.findByIdWithLock(sala.getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));

        // Carica la waitlist ordinata per posizione (FIFO)
        List<Prenotazioni> waitlist = prenotazioniRepository.findWaitlistOrdinata(
            sala,
            prenotazioneCancellata.getData(),
            prenotazioneCancellata.getOraInizio(),
            prenotazioneCancellata.getOraFine()
        );

        if (waitlist.isEmpty()) {
            LOG.debug("Nessun utente in waitlist per slot liberato di sala {}", sala.getNome());
            return;
        }

        Prenotazioni candidato = waitlist.get(0);
        promuoviCandidato(candidato);

        // Riscala le posizioni degli altri in coda (1, 2, 3, ...)
        for (int i = 1; i < waitlist.size(); i++) {
            Prenotazioni p = waitlist.get(i);
            p.setPosizioneWaitlist(i);
            prenotazioniRepository.save(p);
        }
    }

    /**
     * Porta la prenotazione dallo stato WAITLISTED a PROMOTED.
     * Imposta il timer di 15 minuti per la conferma.
     */
    private void promuoviCandidato(Prenotazioni candidato) {
        // Genera codice QR se non presente
        if (candidato.getCodiceQr() == null || candidato.getCodiceQr().isBlank()) {
            String codice = "SALA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            candidato.setCodiceQr(codice);
        }

        StatiPrenotazione statoPromoted = getStato(StatoCodice.PROMOTED);
        candidato.setStato(statoPromoted);
        candidato.setPosizioneWaitlist(null);
        candidato.setPromossaAt(LocalDateTime.now());

        prenotazioniRepository.save(candidato);

        // Notifica l'utente — ha 15 minuti per confermare
        inviaEmailPromozione(candidato);

        LOG.info(
            "Prenotazione {} promossa da waitlist a PROMOTED — scade alle {}",
            candidato.getId(),
            candidato.getPromossaAt().plusMinutes(MINUTI_CONFERMA)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONFERMA DA PARTE DELL'UTENTE PROMOSSO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * L'utente promosso clicca "Conferma" — passa a CONFIRMED.
     * Verifica che la finestra di 15 minuti non sia scaduta.
     */
    public Prenotazioni confermaPromozione(UUID prenotazioneId) {
        Prenotazioni pren = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata: " + prenotazioneId));

        if (pren.getStato().getCodice() != StatoCodice.PROMOTED) {
            throw new IllegalStateException("La prenotazione non è in stato PROMOTED.");
        }

        // Verifica scadenza 15 minuti
        LocalDateTime scadenza = pren.getPromossaAt().plusMinutes(MINUTI_CONFERMA);
        if (LocalDateTime.now().isAfter(scadenza)) {
            // Scaduta — segna come EXPIRED e passa al prossimo
            scadiEPromuoviSuccessivo(pren);
            throw new IllegalStateException("Il tempo per confermare è scaduto. Il posto è stato assegnato al prossimo in lista.");
        }

        // Lock sulla sala per evitare conferme doppie
        saleRepository.findByIdWithLock(pren.getSala().getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata"));

        // Verifica che nel frattempo non ci siano conflitti (altra prenotazione confermata)
        boolean conflitto = prenotazioniRepository.existsOverlappingConfirmedPrenotazione(
            pren.getSala(),
            pren.getData(),
            pren.getOraInizio(),
            pren.getOraFine()
        );
        if (conflitto) {
            scadiEPromuoviSuccessivo(pren);
            throw new IllegalStateException("Lo slot non è più disponibile.");
        }

        pren.setStato(getStato(StatoCodice.CONFIRMED));
        pren.setPromossaAt(null);
        prenotazioniRepository.save(pren);

        LOG.info("Prenotazione {} confermata dall'utente dopo promozione da waitlist", pren.getId());
        return pren;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCHEDULED: scade le PROMOTED non confermate in tempo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ogni minuto controlla le prenotazioni PROMOTED scadute.
     * Se l'utente non ha confermato entro 15 min → EXPIRED, si passa al prossimo.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void scadiPromozioniScadute() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTI_CONFERMA);

        List<Prenotazioni> scadute = prenotazioniRepository.findPromotedScadute(limite);

        for (Prenotazioni pren : scadute) {
            try {
                LOG.info("Promozione scaduta per prenotazione {} — passa al prossimo in waitlist", pren.getId());
                scadiEPromuoviSuccessivo(pren);
            } catch (Exception e) {
                LOG.error("Errore nella scadenza promozione {}: {}", pren.getId(), e.getMessage());
            }
        }
    }

    /**
     * Segna la prenotazione come EXPIRED e promuove il prossimo in lista.
     */
    private void scadiEPromuoviSuccessivo(Prenotazioni pren) {
        pren.setStato(getStato(StatoCodice.EXPIRED));
        pren.setPromossaAt(null);
        prenotazioniRepository.save(pren);

        // Promuovi il prossimo come se fosse una cancellazione
        promuoviDaWaitlist(pren);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NOTIFICHE EMAIL
    // ─────────────────────────────────────────────────────────────────────────

    private void inviaEmailWaitlist(Prenotazioni pren, int posizione) {
        try {
            if (pren.getUtente() == null || pren.getUtente().getUser() == null) return;
            String email = pren.getUtente().getUser().getEmail();
            String nome = pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "";

            mailService.sendWaitlistNotifica(
                email,
                nome,
                pren.getSala().getNome(),
                pren.getData().toString(),
                pren.getOraInizio().toString(),
                pren.getOraFine().toString(),
                posizione
            );
        } catch (Exception e) {
            LOG.warn("Errore invio email waitlist per prenotazione {}: {}", pren.getId(), e.getMessage());
        }
    }

    private void inviaEmailPromozione(Prenotazioni pren) {
        try {
            if (pren.getUtente() == null || pren.getUtente().getUser() == null) return;
            String email = pren.getUtente().getUser().getEmail();
            String nome = pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "";

            mailService.sendWaitlistPromossa(
                email,
                nome,
                pren.getSala().getNome(),
                pren.getData().toString(),
                pren.getOraInizio().toString(),
                pren.getOraFine().toString(),
                pren.getId().toString(),
                MINUTI_CONFERMA
            );
        } catch (Exception e) {
            LOG.warn("Errore invio email promozione per prenotazione {}: {}", pren.getId(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private StatiPrenotazione getStato(StatoCodice codice) {
        return statiPrenotazioneRepository
            .findByCodice(codice)
            .orElseThrow(() -> new EntityNotFoundException("Stato " + codice + " non trovato"));
    }
}
