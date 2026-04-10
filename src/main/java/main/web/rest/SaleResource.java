package main.web.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.security.AuthoritiesConstants;
import main.service.SaleService;
import main.service.dto.SaleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleResource.class);
    private final SaleService saleService;

    public SaleResource(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleDTO saleDTO) {
        LOG.debug("REST request to save Sale : {}", saleDTO);
        SaleDTO result = saleService.save(saleDTO);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<SaleDTO> updateSale(@PathVariable(value = "id") final UUID id, @RequestBody SaleDTO saleDTO) {
        LOG.debug("REST request to update Sale : {}, {}", id, saleDTO);
        saleDTO.setId(id);
        SaleDTO result = saleService.update(saleDTO);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllSales() {
        LOG.debug("REST request to get all Sales");
        return ResponseEntity.ok().body(saleService.findAll(Pageable.unpaged()).getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getSale(@PathVariable UUID id) {
        LOG.debug("REST request to get Sale : {}", id);
        return saleService.findOne(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disponibili")
    public ResponseEntity<List<SaleDTO>> getAllFreeSales(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime inizio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime fine,
        @RequestParam(required = false) Integer capienza
    ) {
        LOG.debug("REST request to get free Sale for date: {}, from: {} to: {}", data, inizio, fine, capienza);
        List<SaleDTO> result = saleService.findAllFreeSales(data, inizio, fine, capienza);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteSale(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Sale : {}", id);
        saleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
