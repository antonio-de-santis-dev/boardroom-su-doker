package main.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Utenti;
import main.repository.UtentiRepository;
import main.service.dto.UtentiDTO;
import main.service.mapper.UtentiMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link main.domain.Utenti}.
 */
@Service
@Transactional
public class UtentiService {

    private static final Logger LOG = LoggerFactory.getLogger(UtentiService.class);

    private final UtentiRepository utentiRepository;

    private final UtentiMapper utentiMapper;

    public UtentiService(UtentiRepository utentiRepository, UtentiMapper utentiMapper) {
        this.utentiRepository = utentiRepository;
        this.utentiMapper = utentiMapper;
    }

    /**
     * Save a utenti.
     *
     * @param utentiDTO the entity to save.
     * @return the persisted entity.
     */
    public UtentiDTO save(UtentiDTO utentiDTO) {
        LOG.debug("Request to save Utenti : {}", utentiDTO);
        Utenti utenti = utentiMapper.toEntity(utentiDTO);
        utenti = utentiRepository.save(utenti);
        return utentiMapper.toDto(utenti);
    }

    /**
     * Update a utenti.
     *
     * @param utentiDTO the entity to save.
     * @return the persisted entity.
     */
    public UtentiDTO update(UtentiDTO utentiDTO) {
        LOG.debug("Request to update Utenti : {}", utentiDTO);
        Utenti utenti = utentiMapper.toEntity(utentiDTO);
        utenti = utentiRepository.save(utenti);
        return utentiMapper.toDto(utenti);
    }

    /**
     * Partially update a utenti.
     *
     * @param utentiDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<UtentiDTO> partialUpdate(UtentiDTO utentiDTO) {
        LOG.debug("Request to partially update Utenti : {}", utentiDTO);

        return utentiRepository
            .findById(utentiDTO.getId())
            .map(existingUtenti -> {
                utentiMapper.partialUpdate(existingUtenti, utentiDTO);

                return existingUtenti;
            })
            .map(utentiRepository::save)
            .map(utentiMapper::toDto);
    }

    /**
     * Get all the utentis.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<UtentiDTO> findAll() {
        LOG.debug("Request to get all Utentis");
        return utentiRepository.findAll().stream().map(utentiMapper::toDto).toList();
    }

    /**
     * Get one utenti by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<UtentiDTO> findOne(UUID id) {
        LOG.debug("Request to get Utenti : {}", id);
        return utentiRepository.findById(id).map(utentiMapper::toDto);
    }

    /**
     * Delete the utenti by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Utenti : {}", id);
        utentiRepository.deleteById(id);
    }
}
