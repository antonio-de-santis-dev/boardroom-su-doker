package main.web.rest;

import java.time.LocalDate;
import java.util.List;
import main.security.AuthoritiesConstants;
import main.service.SaleService;
import main.service.StatsDashboardService;
import main.service.dto.SaleDTO;
import main.service.dto.StatsDashboardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller per la dashboard admin.
 *
 * Tutti gli endpoint sono protetti con @PreAuthorize ROLE_ADMIN.
 *
 * Endpoint:
 *   GET  /api/admin/stats?dal=2026-01-01&al=2026-03-31   → statistiche aggregate
 *   GET  /api/admin/sale                                  → lista sale (per gestione)
 *   POST /api/admin/sale                                  → crea sala
 *   PUT  /api/admin/sale/{id}                             → modifica sala
 *   DELETE /api/admin/sale/{id}                           → elimina sala
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class StatsDashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(StatsDashboardResource.class);

    private final StatsDashboardService statsService;
    private final SaleService saleService;

    public StatsDashboardResource(StatsDashboardService statsService, SaleService saleService) {
        this.statsService = statsService;
        this.saleService = saleService;
    }

    /**
     * GET /api/admin/stats
     *
     * Restituisce tutte le statistiche aggregate nel periodo.
     * Default: ultimo mese se i parametri non sono forniti.
     *
     * Esempi:
     *   /api/admin/stats                             → ultimo mese
     *   /api/admin/stats?periodo=3m                  → ultimi 3 mesi
     *   /api/admin/stats?periodo=1y                  → ultimo anno
     *   /api/admin/stats?dal=2026-01-01&al=2026-03-31 → intervallo custom
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsDashboardDTO> getStats(
        @RequestParam(required = false) String periodo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al
    ) {
        LocalDate fine = al != null ? al : LocalDate.now();
        LocalDate inizio;

        if (dal != null) {
            inizio = dal;
        } else if ("3m".equals(periodo)) {
            inizio = fine.minusMonths(3);
        } else if ("1y".equals(periodo)) {
            inizio = fine.minusYears(1);
        } else {
            inizio = fine.minusMonths(1); // default: ultimo mese
        }

        LOG.debug("REST request stats dashboard da {} a {}", inizio, fine);
        return ResponseEntity.ok(statsService.getDashboard(inizio, fine));
    }

    // ── Gestione sale ─────────────────────────────────────────

    /**
     * GET /api/admin/sale — lista tutte le sale
     */
    @GetMapping("/sale")
    public ResponseEntity<List<SaleDTO>> getSale() {
        return ResponseEntity.ok(saleService.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent());
    }

    /**
     * POST /api/admin/sale — crea nuova sala
     * Body: { "nome": "Sala Meeting", "capienza": 10, "descrizione": "..." }
     */
    @PostMapping("/sale")
    public ResponseEntity<SaleDTO> creaSala(@RequestBody SaleDTO dto) {
        LOG.debug("REST admin request to create Sale: {}", dto.getNome());
        dto.setId(null); // sicurezza: ignora id nel body
        return ResponseEntity.ok(saleService.save(dto));
    }

    /**
     * PUT /api/admin/sale/{id} — modifica sala
     */
    @PutMapping("/sale/{id}")
    public ResponseEntity<SaleDTO> modificaSala(@PathVariable java.util.UUID id, @RequestBody SaleDTO dto) {
        LOG.debug("REST admin request to update Sale: {}", id);
        dto.setId(id);
        return ResponseEntity.ok(saleService.update(dto));
    }

    /**
     * DELETE /api/admin/sale/{id} — elimina sala
     */
    @DeleteMapping("/sale/{id}")
    public ResponseEntity<Void> eliminaSala(@PathVariable java.util.UUID id) {
        LOG.debug("REST admin request to delete Sale: {}", id);
        saleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
