package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import main.service.RicorrenzaService;
import main.service.dto.RicorrenzaDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller per la gestione delle prenotazioni ricorrenti.
 *
 * Endpoint:
 *   POST   /api/ricorrenze                          → crea serie + istanze
 *   GET    /api/ricorrenze                          → lista serie dell'utente (admin: tutte)
 *   DELETE /api/ricorrenze/{id}/future              → cancella istanze da oggi
 *   DELETE /api/ricorrenze/{id}                     → cancella tutta la serie
 */
@RestController
@RequestMapping("/api/ricorrenze")
public class RicorrenzaResource {

    private static final Logger LOG = LoggerFactory.getLogger(RicorrenzaResource.class);
    private static final String ENTITY_NAME = "ricorrenza";

    private final RicorrenzaService ricorrenzaService;

    public RicorrenzaResource(RicorrenzaService ricorrenzaService) {
        this.ricorrenzaService = ricorrenzaService;
    }

    /**
     * POST /api/ricorrenze
     * Crea una nuova regola di ricorrenza e genera tutte le occorrenze (strategia EAGER).
     *
     * Request body esempio:
     * {
     *   "salaId": "uuid-sala",
     *   "frequenza": "WEEKLY",
     *   "giorniSettimana": ["MONDAY", "WEDNESDAY", "FRIDAY"],
     *   "dataInizio": "2026-04-07",
     *   "dataFine": "2026-06-30",
     *   "oraInizio": "09:00",
     *   "oraFine": "10:30",
     *   "numPersone": 4
     * }
     *
     * Response: RicorrenzaDTO con istanzeCreate, istanzeConflitto, dateConflitto
     */
    @PostMapping
    public ResponseEntity<RicorrenzaDTO> creaRicorrenza(@Valid @RequestBody RicorrenzaDTO dto) {
        LOG.debug("REST request to create Ricorrenza: {}", dto.getFrequenza());
        try {
            RicorrenzaDTO result = ricorrenzaService.creaRicorrenza(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "invalidinput");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/ricorrenze
     * Restituisce le serie dell'utente autenticato (admin: tutte).
     */
    @GetMapping
    public ResponseEntity<List<RicorrenzaDTO>> getMieRicorrenze() {
        LOG.debug("REST request to get ricorrenze");
        return ResponseEntity.ok(ricorrenzaService.getMieRicorrenze());
    }

    /**
     * DELETE /api/ricorrenze/{id}/future
     * Cancella (logicamente, stato → CANCELLED) tutte le istanze future della serie.
     * Le prenotazioni passate rimangono intatte.
     */
    @DeleteMapping("/{id}/future")
    public ResponseEntity<Void> cancellaSerieFuture(@PathVariable UUID id) {
        LOG.debug("REST request to cancel future instances of ricorrenza: {}", id);
        try {
            ricorrenzaService.cancellaSerieDaOggi(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/ricorrenze/{id}
     * Cancella (logicamente) TUTTA la serie — passato e futuro.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancellaTuttaSerie(@PathVariable UUID id) {
        LOG.debug("REST request to cancel entire serie: {}", id);
        try {
            ricorrenzaService.cancellaTuttaSerie(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
