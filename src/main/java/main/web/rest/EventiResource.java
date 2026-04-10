package main.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.EventiRepository;
import main.repository.PrenotazioneEventoPubblicoRepository;
import main.security.AuthoritiesConstants;
import main.service.EventiService;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniEmailDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/eventis")
public class EventiResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventiResource.class);
    private static final String ENTITY_NAME = "eventi";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventiService eventiService;
    private final EventiRepository eventiRepository;
    private final PrenotazioneEventoPubblicoRepository prenotazioneEventoPubblicoRepository;

    public EventiResource(
        EventiService eventiService,
        EventiRepository eventiRepository,
        PrenotazioneEventoPubblicoRepository prenotazioneEventoPubblicoRepository
    ) {
        this.eventiService = eventiService;
        this.eventiRepository = eventiRepository;
        this.prenotazioneEventoPubblicoRepository = prenotazioneEventoPubblicoRepository;
    }

    /**
     * Endpoint specifico per gli eventi pubblici (non richiede autenticazione).
     */
    @GetMapping("/pubblici")
    public List<EventiDTO> getPublicEventi() {
        LOG.debug("REST request to get public Eventi");
        return eventiService.findPublicEventi();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventiDTO> createEventi(@Valid @RequestBody EventiDTO eventiDTO) throws URISyntaxException {
        LOG.debug("REST request to save Eventi : {}", eventiDTO);
        validaNewEvento(eventiDTO);

        EventiDTO saved = eventiService.createEvento(eventiDTO);

        return ResponseEntity.created(new URI("/api/eventis/" + saved.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, saved.getId().toString()))
            .body(saved);
    }

    private void validaNewEvento(EventiDTO dto) {
        if (dto.getId() != null) {
            throw new BadRequestAlertException("Un nuovo evento non puo gia avere ID", ENTITY_NAME, "idexists");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<EventiDTO> updateEventi(
        @PathVariable(value = "id", required = false) UUID id,
        @Valid @RequestBody EventiDTO eventiDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Eventi : {}, {}", id, eventiDTO);
        validaIdPerUpdate(id, eventiDTO);

        EventiDTO result = eventiService.update(eventiDTO);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    private void validaIdPerUpdate(UUID id, EventiDTO eventiDTO) {
        if (eventiDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventiDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!eventiRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<EventiDTO> partialUpdateEventi(
        @PathVariable(value = "id", required = false) UUID id,
        @NotNull @RequestBody EventiDTO eventiDTO
    ) throws URISyntaxException {
        validaIdPerUpdate(id, eventiDTO);

        Optional<EventiDTO> result = eventiService.partialUpdate(eventiDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventiDTO.getId().toString())
        );
    }

    @GetMapping
    public ResponseEntity<List<EventiDTO>> getAllEventis(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<EventiDTO> page = eventiService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventiDTO> getEventi(@PathVariable("id") UUID id) {
        Optional<EventiDTO> eventiDTO = eventiService
            .findOne(id)
            .map(dto -> {
                long occupati = prenotazioneEventoPubblicoRepository.countByEventoId(id);
                dto.setPostiOccupati(occupati);
                if (dto.getNumPersone() != null) {
                    dto.setEventoPieno(occupati >= dto.getNumPersone());
                }
                return dto;
            });
        return ResponseUtil.wrapOrNotFound(eventiDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteEventi(@PathVariable("id") UUID id) {
        eventiService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/{id}/prenotazione-email")
    public ResponseEntity<Void> prenotazioneEmail(@PathVariable("id") UUID id, @RequestBody PrenotazioniEmailDTO dto) {
        eventiService.inviaEmailPrenotazione(id, dto);
        return ResponseEntity.ok().build();
    }
}
