package main.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.UtentiRepository;
import main.security.AuthoritiesConstants;
import main.service.UtentiService;
import main.service.dto.UtentiDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link main.domain.Utenti}.
 */
@RestController
@RequestMapping("/api/utentis")
public class UtentiResource {

    private static final Logger LOG = LoggerFactory.getLogger(UtentiResource.class);

    private static final String ENTITY_NAME = "utenti";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UtentiService utentiService;

    private final UtentiRepository utentiRepository;

    public UtentiResource(UtentiService utentiService, UtentiRepository utentiRepository) {
        this.utentiService = utentiService;
        this.utentiRepository = utentiRepository;
    }

    /**
     * {@code POST  /utentis} : Create a new utenti.
     *
     * @param utentiDTO the utentiDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new utentiDTO, or with status {@code 400 (Bad Request)} if the utenti has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UtentiDTO> createUtenti(@Valid @RequestBody UtentiDTO utentiDTO) throws URISyntaxException {
        LOG.debug("REST request to save Utenti : {}", utentiDTO);
        if (utentiDTO.getId() != null) {
            throw new BadRequestAlertException("A new utenti cannot already have an ID", ENTITY_NAME, "idexists");
        }
        utentiDTO = utentiService.save(utentiDTO);
        return ResponseEntity.created(new URI("/api/utentis/" + utentiDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, utentiDTO.getId().toString()))
            .body(utentiDTO);
    }

    /**
     * {@code PUT  /utentis/:id} : Updates an existing utenti.
     *
     * @param id the id of the utentiDTO to save.
     * @param utentiDTO the utentiDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated utentiDTO,
     * or with status {@code 400 (Bad Request)} if the utentiDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the utentiDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UtentiDTO> updateUtenti(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody UtentiDTO utentiDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Utenti : {}, {}", id, utentiDTO);
        if (utentiDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, utentiDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!utentiRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        utentiDTO = utentiService.update(utentiDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, utentiDTO.getId().toString()))
            .body(utentiDTO);
    }

    /**
     * {@code PATCH  /utentis/:id} : Partial updates given fields of an existing utenti, field will ignore if it is null
     *
     * @param id the id of the utentiDTO to save.
     * @param utentiDTO the utentiDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated utentiDTO,
     * or with status {@code 400 (Bad Request)} if the utentiDTO is not valid,
     * or with status {@code 404 (Not Found)} if the utentiDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the utentiDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UtentiDTO> partialUpdateUtenti(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody UtentiDTO utentiDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Utenti partially : {}, {}", id, utentiDTO);
        if (utentiDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, utentiDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!utentiRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<UtentiDTO> result = utentiService.partialUpdate(utentiDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, utentiDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /utentis} : get all the utentis.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of utentis in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<UtentiDTO> getAllUtentis() {
        LOG.debug("REST request to get all Utentis");
        return utentiService.findAll();
    }

    /**
     * {@code GET  /utentis/:id} : get the "id" utenti.
     *
     * @param id the id of the utentiDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the utentiDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UtentiDTO> getUtenti(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Utenti : {}", id);
        Optional<UtentiDTO> utentiDTO = utentiService.findOne(id);
        return ResponseUtil.wrapOrNotFound(utentiDTO);
    }

    /**
     * {@code DELETE  /utentis/:id} : delete the "id" utenti.
     *
     * @param id the id of the utentiDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUtenti(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Utenti : {}", id);
        utentiService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
