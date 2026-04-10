package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.PrenotazioniRepository;
import main.security.AuthoritiesConstants;
import main.service.PrenotazioniService;
import main.service.dto.PrenotazioniDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link main.domain.Prenotazioni}.
 */
@RestController
@RequestMapping("/api/prenotazionis")
public class PrenotazioniResource {

    private static final Logger LOG = LoggerFactory.getLogger(PrenotazioniResource.class);

    private static final String ENTITY_NAME = "prenotazioni";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PrenotazioniService prenotazioniService;

    private final PrenotazioniRepository prenotazioniRepository;
    private final main.service.WaitlistService waitlistService;

    public PrenotazioniResource(
        PrenotazioniService prenotazioniService,
        PrenotazioniRepository prenotazioniRepository,
        main.service.WaitlistService waitlistService
    ) {
        this.prenotazioniService = prenotazioniService;
        this.prenotazioniRepository = prenotazioniRepository;
        this.waitlistService = waitlistService;
    }

    /**
     * {@code POST /prenotazionis} : Creazione diretta riservata agli amministratori.
     * Imposta lo stato a CONFIRMED solo se non esistono conflitti con prenotazioni già confermate.
     * Gli utenti devono usare POST /prenotta.
     */
    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<PrenotazioniDTO> createPrenotazioni(@Valid @RequestBody PrenotazioniDTO prenotazioniDTO) {
        LOG.debug("REST request to save Prenotazioni (admin) : {}", prenotazioniDTO);
        if (prenotazioniDTO.getId() != null) {
            throw new BadRequestAlertException("A new prenotazioni cannot already have an ID", ENTITY_NAME, "idexists");
        }
        try {
            PrenotazioniDTO result = prenotazioniService.save(prenotazioniDTO);
            return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.getId()).toUri()
            )
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
        } catch (IllegalStateException e) {
            LOG.warn("Conflitto rilevato durante la creazione admin della prenotazione: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "conflitto");
        } catch (EntityNotFoundException e) {
            LOG.warn("Risorsa non trovata durante la creazione admin della prenotazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * {@code POST /prenotazionis/prenotta} : Creazione prenotazione per utente autenticato.
     * Imposta lo stato iniziale a WAITING, verifica sovrapposizioni e collega automaticamente
     * l'utente autenticato. Questo è l'unico endpoint da usare per il flusso utente.
     */
    @PostMapping("/prenotta")
    public ResponseEntity<PrenotazioniDTO> nuovaPrenotazione(@Valid @RequestBody PrenotazioniDTO dto) {
        try {
            PrenotazioniDTO result = prenotazioniService.nuovoPrenotazioni(dto);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            LOG.warn("Risorsa non trovata durante la creazione della prenotazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Input non valido durante la creazione della prenotazione: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "invalidinput");
        }
    }

    /**
     * {@code PUT  /prenotazionis/:id} : Updates an existing prenotazioni.
     *
     * @param id the id of the prenotazioniDTO to save.
     * @param prenotazioniDTO the prenotazioniDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prenotazioniDTO,
     * or with status {@code 400 (Bad Request)} if the prenotazioniDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prenotazioniDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> updatePrenotazioni(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody PrenotazioniDTO prenotazioniDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Prenotazioni : {}, {}", id, prenotazioniDTO);
        validaPrenoUpdate(id, prenotazioniDTO);

        PrenotazioniDTO result = prenotazioniService.update(prenotazioniDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prenotazioniDTO.getId().toString()))
            .body(result);
    }

    private void validaPrenoUpdate(UUID id, PrenotazioniDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dto.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!prenotazioniRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
    }

    /**
     * {@code PATCH  /prenotazionis/:id} : Partial updates given fields of an existing prenotazioni, field will ignore if it is null
     *
     * @param id the id of the prenotazioniDTO to save.
     * @param prenotazioniDTO the prenotazioniDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prenotazioniDTO,
     * or with status {@code 404 (Not Found)} if the prenotazioniDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the prenotazioniDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PrenotazioniDTO> partialUpdatePrenotazioni(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody PrenotazioniDTO prenotazioniDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Prenotazioni partially : {}, {}", id, prenotazioniDTO);
        validaPrenoUpdate(id, prenotazioniDTO);

        Optional<PrenotazioniDTO> result = prenotazioniService.partialUpdate(prenotazioniDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prenotazioniDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /prenotazionis} : get all the prenotazionis.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of prenotazionis in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PrenotazioniDTO>> getAllPrenotazionis(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        @RequestParam(name = "salaId", required = false) UUID salaId
    ) {
        LOG.debug("REST request to get a page of Prenotazionis. Filter salaId: {}", salaId);
        Page<PrenotazioniDTO> page = prenotazioniService.getAll(pageable, eagerload, salaId);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /prenotazionis/:id} : get the "id" prenotazioni.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> getPrenotazioni(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Prenotazioni : {}", id);
        Optional<PrenotazioniDTO> prenotazioniDTO = prenotazioniService.findOne(id);
        return ResponseUtil.wrapOrNotFound(prenotazioniDTO);
    }

    @GetMapping("/storico")
    public ResponseEntity<List<PrenotazioniDTO>> getStoricoPrenotazioni() {
        LOG.debug("REST request to get storico prenotazioni");
        return ResponseEntity.ok(prenotazioniService.getStoricoPrenotazioni());
    }

    @GetMapping("/odierne")
    public ResponseEntity<List<PrenotazioniDTO>> getPrenotazioniOdierne() {
        LOG.debug("REST request to get odierne prenotazioni");
        return ResponseEntity.ok(prenotazioniService.getPrenotazioniOdierne());
    }

    // ── CALENDARIO ────────────────────────────────────────────────────────────
    /**
     * {@code GET /prenotazionis/calendario} : recupera tutte le prenotazioni in un
     * intervallo di date per la vista calendario mensile.
     *
     * Esempio: GET /api/prenotazionis/calendario?dataInizio=2026-03-01&dataFine=2026-03-31
     *
     * Admin: riceve tutte le prenotazioni di tutte le sale.
     * Utente: riceve solo le proprie prenotazioni.
     *
     * @param dataInizio primo giorno dell'intervallo (formato ISO: YYYY-MM-DD)
     * @param dataFine   ultimo giorno dell'intervallo (formato ISO: YYYY-MM-DD)
     * @return lista di PrenotazioniDTO
     */
    @GetMapping("/calendario")
    public ResponseEntity<List<PrenotazioniDTO>> getPrenotazioniCalendario(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine
    ) {
        LOG.debug("REST request to get prenotazioni calendario da {} a {}", dataInizio, dataFine);

        if (dataInizio == null || dataFine == null) {
            throw new BadRequestAlertException("dataInizio e dataFine sono obbligatori", ENTITY_NAME, "missingparams");
        }
        if (dataFine.isBefore(dataInizio)) {
            throw new BadRequestAlertException("dataFine non può essere precedente a dataInizio", ENTITY_NAME, "invalidrange");
        }

        List<PrenotazioniDTO> result = prenotazioniService.findByDataBetween(dataInizio, dataFine);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@code DELETE /prenotazionis/:id} : cancellazione logica per l'utente proprietario.
     * Imposta lo stato a CANCELLED. Solo il proprietario della prenotazione può eseguire questa operazione.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancellaPrenotazione(@PathVariable("id") UUID id) {
        LOG.debug("REST request to cancel Prenotazioni : {}", id);
        try {
            prenotazioniService.deletePrenotazione(id);
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * {@code DELETE /prenotazionis/:id/admin} : cancellazione fisica riservata agli amministratori.
     * Rimuove definitivamente il record dal DB.
     */
    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deletePrenotazioneAdmin(@PathVariable("id") UUID id) {
        LOG.debug("REST request to hard-delete Prenotazioni (admin) : {}", id);
        try {
            prenotazioniService.deleteAsAdmin(id);
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                .build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint per la conferma della prenotazione
     */
    @PostMapping("/{id}/conferma")
    public ResponseEntity<PrenotazioniDTO> confermaPrenotazioni(@PathVariable UUID id) {
        PrenotazioniDTO dto = prenotazioniService.confermaPrenotazione(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /prenotazionis/verifica-qr/{codice}
     */
    @GetMapping("/verifica-qr/{codice}")
    public ResponseEntity<PrenotazioniDTO> verificaQrCode(@PathVariable String codice) {
        LOG.debug("REST request to verify QR code : {}", codice);
        return prenotazioniService.findByCodiceQr(codice).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /prenotazionis/{id}/conferma-promozione
     * L'utente promosso dalla waitlist conferma di voler occupare lo slot.
     * Deve essere chiamato entro 15 minuti dalla promozione.
     */
    @PostMapping("/{id}/conferma-promozione")
    public ResponseEntity<PrenotazioniDTO> confermaPromozione(@PathVariable UUID id) {
        LOG.debug("REST request to confirm waitlist promotion for prenotazione : {}", id);
        try {
            main.domain.Prenotazioni pren = waitlistService.confermaPromozione(id);
            return ResponseEntity.ok(prenotazioniService.findOne(id).orElseThrow());
        } catch (IllegalStateException e) {
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "waitlist.scaduta");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
