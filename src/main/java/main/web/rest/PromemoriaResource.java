package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import main.service.PromemoriaService;
import main.service.dto.PromemoriaDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller per la gestione dei promemoria prenotazione.
 *
 * GET  /api/promemoria/{prenotazioneId}          → leggi configurazione
 * POST /api/promemoria                            → salva/aggiorna configurazione
 */
@RestController
@RequestMapping("/api/promemoria")
public class PromemoriaResource {

    private static final Logger LOG = LoggerFactory.getLogger(PromemoriaResource.class);
    private static final String ENTITY_NAME = "promemoria";

    private final PromemoriaService proService;

    public PromemoriaResource(PromemoriaService proService) {
        this.proService = proService;
    }

    /**
     * GET /api/promemoria/{prenotazioneId}
     * Restituisce la configurazione corrente dei promemoria per la prenotazione.
     * Include il riepilogo di tutti e 4 i tipi con stato abilitato/inviato.
     */
    @GetMapping("/{prenotazioneId}")
    public ResponseEntity<PromemoriaDTO> getPromemoria(@PathVariable UUID prenotazioneId) {
        LOG.debug("REST request to get promemoria for prenotazione: {}", prenotazioneId);
        try {
            return ResponseEntity.ok(proService.getPromemoria(prenotazioneId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * POST /api/promemoria
     * Salva o aggiorna le preferenze promemoria.
     *
     * Body esempio:
     * {
     *   "prenotazioneId": "uuid",
     *   "tipiAbilitati": ["DAY_BEFORE", "SAME_DAY"]
     * }
     */
    @PostMapping
    public ResponseEntity<PromemoriaDTO> salvaPromemoria(@RequestBody PromemoriaDTO dto) {
        LOG.debug("REST request to save promemoria for prenotazione: {}", dto.getPrenotazioneId());
        if (dto.getPrenotazioneId() == null) {
            throw new BadRequestAlertException("prenotazioneId obbligatorio", ENTITY_NAME, "missingid");
        }
        try {
            return ResponseEntity.ok(proService.salvaPromemoria(dto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
