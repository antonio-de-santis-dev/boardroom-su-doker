package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import main.security.AuthoritiesConstants;
import main.service.SaleService;
import main.service.dto.SaleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller per la gestione delle immagini delle sale.
 *
 * Strategia: l'immagine viene salvata come data URL Base64 direttamente
 * nel campo image_url del database (colonna LONGTEXT).
 *
 * Formato: "data:image/jpeg;base64,/9j/4AAQ..."
 *
 * Vantaggi rispetto al filesystem:
 * - Zero dipendenze da path Windows/Linux
 * - Zero configurazione di cartelle
 * - Zero problemi di permessi
 * - Il frontend usa [src]="sala.imageUrl" direttamente senza chiamate extra
 *
 * Endpoint:
 *   POST   /api/sales/{id}/immagine   → upload immagine (solo admin)
 *   DELETE /api/sales/{id}/immagine   → rimuovi immagine (solo admin)
 */
@RestController
@RequestMapping("/api/sales")
public class SaleImmagineResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleImmagineResource.class);

    /** Dimensione massima: 2 MB (in base64 diventa ~2.7 MB nella colonna LONGTEXT) */
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024;

    /** Formati accettati */
    private static final List<String> TIPI_ACCETTATI = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    private final SaleService saleService;

    public SaleImmagineResource(SaleService saleService) {
        this.saleService = saleService;
    }

    /**
     * POST /api/sales/{id}/immagine
     * Carica e associa un'immagine a una sala salvandola come Base64 nel DB.
     */
    @PostMapping(value = "/{id}/immagine", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<SaleDTO> uploadImmagine(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        LOG.debug("REST request to upload immagine (base64) for sale: {}", id);

        // ── Validazione ──────────────────────────────────────
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File vuoto");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File troppo grande. Massimo 2 MB per le immagini delle sale.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TIPI_ACCETTATI.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato non supportato. Accettati: JPEG, PNG, WEBP");
        }

        // ── Converti in Base64 data URL ───────────────────────
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + contentType + ";base64," + base64;

            // ── Salva nel DB ──────────────────────────────────
            SaleDTO dto = saleService.findOne(id).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + id));
            dto.setImageUrl(dataUrl);
            SaleDTO risultato = saleService.update(dto);

            LOG.info("Immagine sala {} salvata come base64 ({} bytes -> {} char)", id, bytes.length, dataUrl.length());
            return ResponseEntity.ok(risultato);
        } catch (IOException e) {
            LOG.error("Errore lettura file per sala {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante la lettura del file");
        }
    }

    /**
     * DELETE /api/sales/{id}/immagine
     * Rimuove l'immagine di una sala (azzera il campo nel DB).
     */
    @DeleteMapping("/{id}/immagine")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> eliminaImmagine(@PathVariable UUID id) {
        LOG.debug("REST request to delete immagine for sale: {}", id);

        SaleDTO dto = saleService.findOne(id).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + id));

        if (dto.getImageUrl() != null) {
            dto.setImageUrl(null);
            saleService.update(dto);
        }

        return ResponseEntity.noContent().build();
    }
}
