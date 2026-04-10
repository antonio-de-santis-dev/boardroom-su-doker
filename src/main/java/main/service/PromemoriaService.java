package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Prenotazioni;
import main.domain.Promemoria;
import main.domain.enumeration.TipoPromemoria;
import main.repository.PrenotazioniRepository;
import main.repository.PromemoriaRepository;
import main.security.AuthoritiesConstants;
import main.service.dto.PromemoriaDTO;
import main.service.dto.PromemoriaDTO.TipoStatoDTO;
import main.web.rest.errors.UtenteNonAutenticatoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromemoriaService {

    private static final Logger LOG = LoggerFactory.getLogger(PromemoriaService.class);

    private final PromemoriaRepository proRepository;
    private final PrenotazioniRepository prenotazioniRepository;

    public PromemoriaService(PromemoriaRepository proRepository, PrenotazioniRepository prenotazioniRepository) {
        this.proRepository = proRepository;
        this.prenotazioniRepository = prenotazioniRepository;
    }

    // ── LETTURA ───────────────────────────────────────────────

    /**
     * Ritorna la configurazione corrente dei promemoria per una prenotazione.
     * Inizializza tutti e 4 i tipi se non esistono ancora.
     */
    @Transactional(readOnly = true)
    public PromemoriaDTO getPromemoria(UUID prenotazioneId) {
        verificaProprietario(prenotazioneId);

        List<Promemoria> esistenti = proRepository.findByPrenotazioneId(prenotazioneId);

        // Costruisce il riepilogo per tutti i tipi possibili
        List<TipoStatoDTO> riepilogo = Arrays.stream(TipoPromemoria.values())
            .map(tipo -> {
                boolean abilitato = esistenti.stream().anyMatch(p -> p.getTipo() == tipo && p.isAbilitato());
                boolean inviato = esistenti.stream().anyMatch(p -> p.getTipo() == tipo && p.isInviato());
                return new TipoStatoDTO(tipo, abilitato, inviato);
            })
            .collect(Collectors.toList());

        List<TipoPromemoria> tipiAttivi = esistenti
            .stream()
            .filter(Promemoria::isAbilitato)
            .map(Promemoria::getTipo)
            .collect(Collectors.toList());

        PromemoriaDTO dto = new PromemoriaDTO();
        dto.setPrenotazioneId(prenotazioneId);
        dto.setTipiAbilitati(tipiAttivi);
        dto.setRiepilogo(riepilogo);
        return dto;
    }

    // ── SALVATAGGIO ───────────────────────────────────────────

    /**
     * Salva/aggiorna le preferenze promemoria per una prenotazione.
     * Strategia: upsert per ogni tipo.
     * - Se il tipo è nella lista tipiAbilitati → crea o abilita il record
     * - Se il tipo NON è nella lista → disabilita il record (non lo cancella)
     *   in modo da preservare lo storico degli invii
     */
    public PromemoriaDTO salvaPromemoria(PromemoriaDTO dto) {
        UUID prenotazioneId = dto.getPrenotazioneId();
        verificaProprietario(prenotazioneId);

        Prenotazioni prenotazione = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata: " + prenotazioneId));

        List<TipoPromemoria> tipiRichiesti = dto.getTipiAbilitati() != null ? dto.getTipiAbilitati() : List.of();

        // Upsert per tutti e 4 i tipi
        for (TipoPromemoria tipo : TipoPromemoria.values()) {
            boolean deveEssereAttivo = tipiRichiesti.contains(tipo);

            Promemoria promemoria = proRepository
                .findByPrenotazioneIdAndTipo(prenotazioneId, tipo)
                .orElseGet(() -> {
                    Promemoria nuovo = new Promemoria();
                    nuovo.setPrenotazione(prenotazione);
                    nuovo.setTipo(tipo);
                    return nuovo;
                });

            promemoria.setAbilitato(deveEssereAttivo);

            // Se riabilitato dopo essere stato già inviato → reset per permettere reinvio
            // (utile se la prenotazione viene spostata)
            if (deveEssereAttivo && promemoria.isInviato()) {
                LOG.debug("Reset stato inviato per tipo {} su prenotazione {}", tipo, prenotazioneId);
                promemoria.setInviato(false);
                promemoria.setInviatoAlle(null);
            }

            proRepository.save(promemoria);
        }

        LOG.debug("Promemoria aggiornati per prenotazione {}: tipi={}", prenotazioneId, tipiRichiesti);
        return getPromemoria(prenotazioneId);
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Verifica che l'utente autenticato sia il proprietario della prenotazione
     * oppure un admin.
     */
    private void verificaProprietario(UUID prenotazioneId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new UtenteNonAutenticatoException("Utente non autenticato");

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getAuthority()));
        if (isAdmin) return;

        Prenotazioni p = prenotazioniRepository
            .findById(prenotazioneId)
            .orElseThrow(() -> new EntityNotFoundException("Prenotazione non trovata"));

        String login = auth.getName();
        boolean isOwner = p.getUtente() != null && p.getUtente().getUser() != null && login.equals(p.getUtente().getUser().getLogin());

        if (!isOwner) throw new AccessDeniedException("Non autorizzato");
    }
}
