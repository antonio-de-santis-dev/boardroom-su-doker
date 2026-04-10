package main.web.rest;

import static main.domain.UtentiAsserts.*;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Utenti;
import main.repository.UserRepository;
import main.repository.UtentiRepository;
import main.service.dto.UtentiDTO;
import main.service.mapper.UtentiMapper;
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
 * Integration tests for the {@link UtentiResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UtentiResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_NOME_AZIENDA = "AAAAAAAAAA";
    private static final String UPDATED_NOME_AZIENDA = "BBBBBBBBBB";

    private static final String DEFAULT_NUMERO_DI_TELEFONO = "AAAAAAAAAA";
    private static final String UPDATED_NUMERO_DI_TELEFONO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/utentis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UtentiMapper utentiMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUtentiMockMvc;

    private Utenti utenti;

    private Utenti insertedUtenti;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Utenti createEntity() {
        return new Utenti().nome(DEFAULT_NOME).nomeAzienda(DEFAULT_NOME_AZIENDA).numeroDiTelefono(DEFAULT_NUMERO_DI_TELEFONO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Utenti createUpdatedEntity() {
        return new Utenti().nome(UPDATED_NOME).nomeAzienda(UPDATED_NOME_AZIENDA).numeroDiTelefono(UPDATED_NUMERO_DI_TELEFONO);
    }

    @BeforeEach
    void initTest() {
        utenti = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUtenti != null) {
            utentiRepository.delete(insertedUtenti);
            insertedUtenti = null;
        }
    }

    @Test
    @Transactional
    void createUtenti() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);
        var returnedUtentiDTO = om.readValue(
            restUtentiMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UtentiDTO.class
        );

        // Validate the Utenti in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedUtenti = utentiMapper.toEntity(returnedUtentiDTO);
        assertUtentiUpdatableFieldsEquals(returnedUtenti, getPersistedUtenti(returnedUtenti));

        insertedUtenti = returnedUtenti;
    }

    @Test
    @Transactional
    void createUtentiWithExistingId() throws Exception {
        // Create the Utenti with an existing ID
        insertedUtenti = utentiRepository.saveAndFlush(utenti);
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUtentiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        utenti.setNome(null);

        // Create the Utenti, which fails.
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        restUtentiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNumeroDiTelefonoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        utenti.setNumeroDiTelefono(null);

        // Create the Utenti, which fails.
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        restUtentiMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllUtentis() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        // Get all the utentiList
        restUtentiMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(utenti.getId().toString())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].nomeAzienda").value(hasItem(DEFAULT_NOME_AZIENDA)))
            .andExpect(jsonPath("$.[*].numeroDiTelefono").value(hasItem(DEFAULT_NUMERO_DI_TELEFONO)));
    }

    @Test
    @Transactional
    void getUtenti() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        // Get the utenti
        restUtentiMockMvc
            .perform(get(ENTITY_API_URL_ID, utenti.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(utenti.getId().toString()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.nomeAzienda").value(DEFAULT_NOME_AZIENDA))
            .andExpect(jsonPath("$.numeroDiTelefono").value(DEFAULT_NUMERO_DI_TELEFONO));
    }

    @Test
    @Transactional
    void getNonExistingUtenti() throws Exception {
        // Get the utenti
        restUtentiMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingUtenti() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utenti
        Utenti updatedUtenti = utentiRepository.findById(utenti.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedUtenti are not directly saved in db
        em.detach(updatedUtenti);
        updatedUtenti.nome(UPDATED_NOME).nomeAzienda(UPDATED_NOME_AZIENDA).numeroDiTelefono(UPDATED_NUMERO_DI_TELEFONO);
        UtentiDTO utentiDTO = utentiMapper.toDto(updatedUtenti);

        restUtentiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, utentiDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO))
            )
            .andExpect(status().isOk());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUtentiToMatchAllProperties(updatedUtenti);
    }

    @Test
    @Transactional
    void putNonExistingUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, utentiDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(utentiDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUtentiWithPatch() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utenti using partial update
        Utenti partialUpdatedUtenti = new Utenti();
        partialUpdatedUtenti.setId(utenti.getId());

        partialUpdatedUtenti.nome(UPDATED_NOME).nomeAzienda(UPDATED_NOME_AZIENDA);

        restUtentiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUtenti.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUtenti))
            )
            .andExpect(status().isOk());

        // Validate the Utenti in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUtentiUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedUtenti, utenti), getPersistedUtenti(utenti));
    }

    @Test
    @Transactional
    void fullUpdateUtentiWithPatch() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the utenti using partial update
        Utenti partialUpdatedUtenti = new Utenti();
        partialUpdatedUtenti.setId(utenti.getId());

        partialUpdatedUtenti.nome(UPDATED_NOME).nomeAzienda(UPDATED_NOME_AZIENDA).numeroDiTelefono(UPDATED_NUMERO_DI_TELEFONO);

        restUtentiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUtenti.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUtenti))
            )
            .andExpect(status().isOk());

        // Validate the Utenti in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUtentiUpdatableFieldsEquals(partialUpdatedUtenti, getPersistedUtenti(partialUpdatedUtenti));
    }

    @Test
    @Transactional
    void patchNonExistingUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, utentiDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(utentiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(utentiDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUtenti() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        utenti.setId(UUID.randomUUID());

        // Create the Utenti
        UtentiDTO utentiDTO = utentiMapper.toDto(utenti);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUtentiMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(utentiDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Utenti in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUtenti() throws Exception {
        // Initialize the database
        insertedUtenti = utentiRepository.saveAndFlush(utenti);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the utenti
        restUtentiMockMvc
            .perform(delete(ENTITY_API_URL_ID, utenti.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return utentiRepository.count();
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

    protected Utenti getPersistedUtenti(Utenti utenti) {
        return utentiRepository.findById(utenti.getId()).orElseThrow();
    }

    protected void assertPersistedUtentiToMatchAllProperties(Utenti expectedUtenti) {
        assertUtentiAllPropertiesEquals(expectedUtenti, getPersistedUtenti(expectedUtenti));
    }

    protected void assertPersistedUtentiToMatchUpdatableProperties(Utenti expectedUtenti) {
        assertUtentiAllUpdatablePropertiesEquals(expectedUtenti, getPersistedUtenti(expectedUtenti));
    }
}
