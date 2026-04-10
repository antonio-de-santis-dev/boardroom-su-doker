package main.web.rest;

import static main.domain.StatiPrenotazioneAsserts.*;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.StatiPrenotazioneDTO;
import main.service.mapper.StatiPrenotazioneMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link StatiPrenotazioneResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class StatiPrenotazioneResourceIT {

    private static final String DEFAULT_DESCRIZIONE = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIZIONE = "BBBBBBBBBB";

    private static final StatoCodice DEFAULT_CODICE = StatoCodice.WAITING;
    private static final StatoCodice UPDATED_CODICE = StatoCodice.CONFIRMED;

    private static final Integer DEFAULT_ORDINE_AZIONE = 1;
    private static final Integer UPDATED_ORDINE_AZIONE = 2;

    private static final String ENTITY_API_URL = "/api/stati-prenotaziones";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private StatiPrenotazioneMapper statiPrenotazioneMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restStatiPrenotazioneMockMvc;

    private StatiPrenotazione statiPrenotazione;

    private StatiPrenotazione insertedStatiPrenotazione;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StatiPrenotazione createEntity() {
        return new StatiPrenotazione().descrizione(DEFAULT_DESCRIZIONE).codice(DEFAULT_CODICE).ordineAzione(DEFAULT_ORDINE_AZIONE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StatiPrenotazione createUpdatedEntity() {
        return new StatiPrenotazione().descrizione(UPDATED_DESCRIZIONE).codice(UPDATED_CODICE).ordineAzione(UPDATED_ORDINE_AZIONE);
    }

    @BeforeEach
    void initTest() {
        statiPrenotazione = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedStatiPrenotazione != null) {
            statiPrenotazioneRepository.delete(insertedStatiPrenotazione);
            insertedStatiPrenotazione = null;
        }
    }

    @Test
    @Transactional
    void createStatiPrenotazione() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);
        var returnedStatiPrenotazioneDTO = om.readValue(
            restStatiPrenotazioneMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            StatiPrenotazioneDTO.class
        );

        // Validate the StatiPrenotazione in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedStatiPrenotazione = statiPrenotazioneMapper.toEntity(returnedStatiPrenotazioneDTO);
        assertStatiPrenotazioneUpdatableFieldsEquals(returnedStatiPrenotazione, getPersistedStatiPrenotazione(returnedStatiPrenotazione));

        insertedStatiPrenotazione = returnedStatiPrenotazione;
    }

    @Test
    @Transactional
    void createStatiPrenotazioneWithExistingId() throws Exception {
        // Create the StatiPrenotazione with an existing ID
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restStatiPrenotazioneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isBadRequest());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescrizioneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        statiPrenotazione.setDescrizione(null);

        // Create the StatiPrenotazione, which fails.
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        restStatiPrenotazioneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCodiceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        statiPrenotazione.setCodice(null);

        // Create the StatiPrenotazione, which fails.
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        restStatiPrenotazioneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrdineAzioneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        statiPrenotazione.setOrdineAzione(null);

        // Create the StatiPrenotazione, which fails.
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        restStatiPrenotazioneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllStatiPrenotaziones() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        // Get all the statiPrenotazioneList
        restStatiPrenotazioneMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(statiPrenotazione.getId().toString())))
            .andExpect(jsonPath("$.[*].descrizione").value(hasItem(DEFAULT_DESCRIZIONE)))
            .andExpect(jsonPath("$.[*].codice").value(hasItem(DEFAULT_CODICE.toString())))
            .andExpect(jsonPath("$.[*].ordineAzione").value(hasItem(DEFAULT_ORDINE_AZIONE)));
    }

    @Test
    @Transactional
    void getStatiPrenotazione() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        // Get the statiPrenotazione
        restStatiPrenotazioneMockMvc
            .perform(get(ENTITY_API_URL_ID, statiPrenotazione.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(statiPrenotazione.getId().toString()))
            .andExpect(jsonPath("$.descrizione").value(DEFAULT_DESCRIZIONE))
            .andExpect(jsonPath("$.codice").value(DEFAULT_CODICE.toString()))
            .andExpect(jsonPath("$.ordineAzione").value(DEFAULT_ORDINE_AZIONE));
    }

    @Test
    @Transactional
    void getNonExistingStatiPrenotazione() throws Exception {
        // Get the statiPrenotazione
        restStatiPrenotazioneMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingStatiPrenotazione() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the statiPrenotazione
        StatiPrenotazione updatedStatiPrenotazione = statiPrenotazioneRepository.findById(statiPrenotazione.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedStatiPrenotazione are not directly saved in db
        em.detach(updatedStatiPrenotazione);
        updatedStatiPrenotazione.descrizione(UPDATED_DESCRIZIONE).codice(UPDATED_CODICE).ordineAzione(UPDATED_ORDINE_AZIONE);
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(updatedStatiPrenotazione);

        restStatiPrenotazioneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, statiPrenotazioneDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statiPrenotazioneDTO))
            )
            .andExpect(status().isOk());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedStatiPrenotazioneToMatchAllProperties(updatedStatiPrenotazione);
    }

    @Test
    @Transactional
    void putNonExistingStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, statiPrenotazioneDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statiPrenotazioneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statiPrenotazioneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateStatiPrenotazioneWithPatch() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the statiPrenotazione using partial update
        StatiPrenotazione partialUpdatedStatiPrenotazione = new StatiPrenotazione();
        partialUpdatedStatiPrenotazione.setId(statiPrenotazione.getId());

        partialUpdatedStatiPrenotazione.codice(UPDATED_CODICE).ordineAzione(UPDATED_ORDINE_AZIONE);

        restStatiPrenotazioneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStatiPrenotazione.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedStatiPrenotazione))
            )
            .andExpect(status().isOk());

        // Validate the StatiPrenotazione in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStatiPrenotazioneUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedStatiPrenotazione, statiPrenotazione),
            getPersistedStatiPrenotazione(statiPrenotazione)
        );
    }

    @Test
    @Transactional
    void fullUpdateStatiPrenotazioneWithPatch() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the statiPrenotazione using partial update
        StatiPrenotazione partialUpdatedStatiPrenotazione = new StatiPrenotazione();
        partialUpdatedStatiPrenotazione.setId(statiPrenotazione.getId());

        partialUpdatedStatiPrenotazione.descrizione(UPDATED_DESCRIZIONE).codice(UPDATED_CODICE).ordineAzione(UPDATED_ORDINE_AZIONE);

        restStatiPrenotazioneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStatiPrenotazione.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedStatiPrenotazione))
            )
            .andExpect(status().isOk());

        // Validate the StatiPrenotazione in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertStatiPrenotazioneUpdatableFieldsEquals(
            partialUpdatedStatiPrenotazione,
            getPersistedStatiPrenotazione(partialUpdatedStatiPrenotazione)
        );
    }

    @Test
    @Transactional
    void patchNonExistingStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, statiPrenotazioneDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(statiPrenotazioneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(statiPrenotazioneDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamStatiPrenotazione() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        statiPrenotazione.setId(UUID.randomUUID());

        // Create the StatiPrenotazione
        StatiPrenotazioneDTO statiPrenotazioneDTO = statiPrenotazioneMapper.toDto(statiPrenotazione);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStatiPrenotazioneMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(statiPrenotazioneDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the StatiPrenotazione in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteStatiPrenotazione() throws Exception {
        // Initialize the database
        insertedStatiPrenotazione = statiPrenotazioneRepository.saveAndFlush(statiPrenotazione);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the statiPrenotazione
        restStatiPrenotazioneMockMvc
            .perform(delete(ENTITY_API_URL_ID, statiPrenotazione.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return statiPrenotazioneRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected StatiPrenotazione getPersistedStatiPrenotazione(StatiPrenotazione statiPrenotazione) {
        return statiPrenotazioneRepository.findById(statiPrenotazione.getId()).orElseThrow();
    }

    protected void assertPersistedStatiPrenotazioneToMatchAllProperties(StatiPrenotazione expectedStatiPrenotazione) {
        assertStatiPrenotazioneAllPropertiesEquals(expectedStatiPrenotazione, getPersistedStatiPrenotazione(expectedStatiPrenotazione));
    }

    protected void assertPersistedStatiPrenotazioneToMatchUpdatableProperties(StatiPrenotazione expectedStatiPrenotazione) {
        assertStatiPrenotazioneAllUpdatablePropertiesEquals(
            expectedStatiPrenotazione,
            getPersistedStatiPrenotazione(expectedStatiPrenotazione)
        );
    }
}
