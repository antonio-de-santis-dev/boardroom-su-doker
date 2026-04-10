package main.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.StatiPrenotazione;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.StatiPrenotazioneDTO;
import main.service.mapper.StatiPrenotazioneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link main.domain.StatiPrenotazione}.
 */
@Service
@Transactional
public class StatiPrenotazioneService {

    private static final Logger LOG = LoggerFactory.getLogger(StatiPrenotazioneService.class);

    private final StatiPrenotazioneRepository statiPrenotazioneRepository;

    private final StatiPrenotazioneMapper statiPrenotazioneMapper;

    public StatiPrenotazioneService(
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        StatiPrenotazioneMapper statiPrenotazioneMapper
    ) {
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.statiPrenotazioneMapper = statiPrenotazioneMapper;
    }

    /**
     * Save a statiPrenotazione.
     *
     * @param dto the entity to save.
     * @return the persisted entity.
     */
    public StatiPrenotazioneDTO save(StatiPrenotazioneDTO dto) {
        LOG.debug("Request to save StatiPrenotazione : {}", dto);
        StatiPrenotazione entity = statiPrenotazioneMapper.toEntity(dto);
        entity = statiPrenotazioneRepository.save(entity);
        return statiPrenotazioneMapper.toDto(entity);
    }

    /**
     * Update a statiPrenotazione.
     *
     * @param dto the entity to save.
     * @return the persisted entity.
     */
    public StatiPrenotazioneDTO update(StatiPrenotazioneDTO dto) {
        LOG.debug("Request to update StatiPrenotazione : {}", dto);
        StatiPrenotazione entity = statiPrenotazioneMapper.toEntity(dto);
        entity = statiPrenotazioneRepository.save(entity);
        return statiPrenotazioneMapper.toDto(entity);
    }

    /**
     * Partially update a statiPrenotazione.
     *
     * @param dto the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<StatiPrenotazioneDTO> partialUpdate(StatiPrenotazioneDTO dto) {
        LOG.debug("Request to partially update StatiPrenotazione : {}", dto);

        return statiPrenotazioneRepository
            .findById(dto.getId())
            .map(existingStatiPrenotazione -> {
                statiPrenotazioneMapper.partialUpdate(existingStatiPrenotazione, dto);

                return existingStatiPrenotazione;
            })
            .map(statiPrenotazioneRepository::save)
            .map(statiPrenotazioneMapper::toDto);
    }

    /**
     * Get all the statiPrenotaziones.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<StatiPrenotazioneDTO> findAll() {
        LOG.debug("Request to get all StatiPrenotaziones");
        return statiPrenotazioneRepository.findAll().stream().map(statiPrenotazioneMapper::toDto).toList();
    }

    /**
     * Get one statiPrenotazione by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<StatiPrenotazioneDTO> findOne(UUID id) {
        LOG.debug("Request to get StatiPrenotazione : {}", id);
        return statiPrenotazioneRepository.findById(id).map(statiPrenotazioneMapper::toDto);
    }

    /**
     * Delete the statiPrenotazione by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete StatiPrenotazione : {}", id);
        statiPrenotazioneRepository.deleteById(id);
    }
}
