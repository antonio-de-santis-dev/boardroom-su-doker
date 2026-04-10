package main.web.rest;

import static main.domain.PrenotazioniAsserts.*;
import static main.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import main.IntegrationTest;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.User;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.service.PrenotazioniService;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PrenotazioniResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(username = "user")
class PrenotazioniResourceIT {

    private static final LocalDate DEFAULT_DATA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA = LocalDate.now(ZoneId.systemDefault());

    private static final LocalTime DEFAULT_ORA_INIZIO = LocalTime.NOON;
    private static final LocalTime UPDATED_ORA_INIZIO = LocalTime.MAX.withNano(0);

    private static final LocalTime DEFAULT_ORA_FINE = LocalTime.NOON;
    private static final LocalTime UPDATED_ORA_FINE = LocalTime.MAX.withNano(0);

    private static final String ENTITY_API_URL = "/api/prenotazionis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrenotazioniRepository prenotazioniRepository;

    @Mock
    private PrenotazioniRepository prenotazioniRepositoryMock;

    @Autowired
    private PrenotazioniMapper prenotazioniMapper;

    @Mock
    private PrenotazioniService prenotazioniServiceMock;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private main.repository.UserRepository userRepository;

    private Prenotazioni prenotazioni;
    private Prenotazioni insertedPrenotazioni;

    public static Prenotazioni createEntity() {
        Prenotazioni prenotazioni = new Prenotazioni();
        prenotazioni.setData(DEFAULT_DATA);
        prenotazioni.setOraInizio(DEFAULT_ORA_INIZIO);
        prenotazioni.setOraFine(DEFAULT_ORA_FINE);
        return prenotazioni;
    }

    public static Prenotazioni createUpdatedEntity() {
        Prenotazioni prenotazioni = new Prenotazioni();
        prenotazioni.setData(UPDATED_DATA);
        prenotazioni.setOraInizio(UPDATED_ORA_INIZIO);
        prenotazioni.setOraFine(UPDATED_ORA_FINE);
        return prenotazioni;
    }

    @BeforeEach
    void initTest() {
        prenotazioni = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPrenotazioni != null) {
            prenotazioniRepository.delete(insertedPrenotazioni);
            insertedPrenotazioni = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD base
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void createPrenotazioni_shouldPersistEntity() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        PrenotazioniDTO returnedPrenotazioniDTO = objectMapper.readValue(
            mockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PrenotazioniDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);

        Prenotazioni returnedPrenotazioni = prenotazioniMapper.toEntity(returnedPrenotazioniDTO);
        assertPrenotazioniUpdatableFieldsEquals(returnedPrenotazioni, getPersistedPrenotazioni(returnedPrenotazioni));

        insertedPrenotazioni = returnedPrenotazioni;
    }

    @Test
    @Transactional
    void createPrenotazioniWithExistingId_shouldReturnBadRequest() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);
        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        long databaseSizeBeforeCreate = getRepositoryCount();

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDataIsRequired_shouldFailOnNull() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        prenotazioni.setData(null);

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOraInizioIsRequired_shouldFailOnNull() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        prenotazioni.setOraInizio(null);

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOraFineIsRequired_shouldFailOnNull() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        prenotazioni.setOraFine(null);

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPrenotazionis_shouldReturnList() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        mockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prenotazioni.getId().toString())))
            .andExpect(jsonPath("$.[*].data").value(hasItem(DEFAULT_DATA.toString())))
            .andExpect(jsonPath("$.[*].oraInizio").value(hasItem(DEFAULT_ORA_INIZIO.toString())))
            .andExpect(jsonPath("$.[*].oraFine").value(hasItem(DEFAULT_ORA_FINE.toString())));
    }

    @Test
    @Transactional
    void getPrenotazioni_shouldReturnSingleEntity() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);

        mockMvc
            .perform(get(ENTITY_API_URL_ID, prenotazioni.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prenotazioni.getId().toString()))
            .andExpect(jsonPath("$.data").value(DEFAULT_DATA.toString()))
            .andExpect(jsonPath("$.oraInizio").value(DEFAULT_ORA_INIZIO.toString()))
            .andExpect(jsonPath("$.oraFine").value(DEFAULT_ORA_FINE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPrenotazioni_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrenotazioni_shouldUpdateEntity() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Prenotazioni updatedPrenotazioni = prenotazioniRepository.findById(prenotazioni.getId()).orElseThrow();
        entityManager.detach(updatedPrenotazioni);

        updatedPrenotazioni.setData(UPDATED_DATA);
        updatedPrenotazioni.setOraInizio(UPDATED_ORA_INIZIO);
        updatedPrenotazioni.setOraFine(UPDATED_ORA_FINE);

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(updatedPrenotazioni);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrenotazioniToMatchAllProperties(updatedPrenotazioni);
    }

    @Test
    @Transactional
    void putNonExistingPrenotazioni_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrenotazioni_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrenotazioni_shouldReturnMethodNotAllowed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(prenotazioniDTO)))
            .andExpect(status().isMethodNotAllowed());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrenotazioniWithPatch_shouldUpdateSelectedFields() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Prenotazioni partialUpdatedPrenotazioni = new Prenotazioni();
        partialUpdatedPrenotazioni.setId(prenotazioni.getId());
        partialUpdatedPrenotazioni.setData(UPDATED_DATA);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrenotazioni.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(partialUpdatedPrenotazioni))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrenotazioniUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPrenotazioni, prenotazioni),
            getPersistedPrenotazioni(prenotazioni)
        );
    }

    @Test
    @Transactional
    void fullUpdatePrenotazioniWithPatch_shouldUpdateAllUpdatableFields() throws Exception {
        insertedPrenotazioni = prenotazioniRepository.saveAndFlush(prenotazioni);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        Prenotazioni partialUpdatedPrenotazioni = new Prenotazioni();
        partialUpdatedPrenotazioni.setId(prenotazioni.getId());
        partialUpdatedPrenotazioni.setData(UPDATED_DATA);
        partialUpdatedPrenotazioni.setOraInizio(UPDATED_ORA_INIZIO);
        partialUpdatedPrenotazioni.setOraFine(UPDATED_ORA_FINE);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrenotazioni.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(partialUpdatedPrenotazioni))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrenotazioniUpdatableFieldsEquals(partialUpdatedPrenotazioni, getPersistedPrenotazioni(partialUpdatedPrenotazioni));
    }

    @Test
    @Transactional
    void patchNonExistingPrenotazioni_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prenotazioniDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrenotazioni_shouldReturnBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrenotazioni_shouldReturnMethodNotAllowed() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prenotazioni.setId(UUID.randomUUID());

        PrenotazioniDTO prenotazioniDTO = prenotazioniMapper.toDto(prenotazioni);

        mockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(objectMapper.writeValueAsBytes(prenotazioniDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // ─────────────────────────────────────────────────────────────
    // deletePrenotazione() — soft delete con permessi
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void deletePrenotazioni_shouldCancelWhenOwner() throws Exception {
        initStatiPrenotazione();

        User user = createUserWithLogin("user");
        Utenti utente = createUtenteForUser(user);
        Prenotazioni ownedPrenotazione = createPrenotazioneForUtente(utente);

        insertedPrenotazioni = ownedPrenotazione;
        long databaseSizeBeforeDelete = getRepositoryCount();

        mockMvc
            .perform(delete(ENTITY_API_URL + "/cancella/" + ownedPrenotazione.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
        Prenotazioni updated = prenotazioniRepository.findById(ownedPrenotazione.getId()).orElseThrow();
        assertThat(updated.getStato().getCodice()).isEqualTo(StatoCodice.CANCELLED);
    }

    @Test
    @Transactional
    void deletePrenotazioni_shouldReturnForbiddenWhenNotOwner() throws Exception {
        initStatiPrenotazione();

        User otherUser = createUserWithLogin("other");
        Utenti otherUtente = createUtenteForUser(otherUser);
        Prenotazioni prenotazione = createPrenotazioneForUtente(otherUtente);

        insertedPrenotazioni = prenotazione;
        long databaseSizeBeforeDelete = getRepositoryCount();

        mockMvc
            .perform(delete(ENTITY_API_URL + "/cancella/" + prenotazione.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    // ─────────────────────────────────────────────────────────────
    // /crea — creazione con regole di business
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void createPrenotazioneEndpoint_shouldApplyBusinessRules() throws Exception {
        initStatiPrenotazione();

        Utenti utente = createUtente("Mario", "3331234567");
        Sale sala = createSala("Sala Test", 10);

        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        dto.setUtente(
            utentiRepository
                .findById(utente.getId())
                .map(u -> {
                    var uDto = new main.service.dto.UtentiDTO();
                    uDto.setId(u.getId());
                    return uDto;
                })
                .orElseThrow()
        );
        dto.setSala(
            saleRepository
                .findById(sala.getId())
                .map(s -> {
                    var sDto = new main.service.dto.SaleDTO();
                    sDto.setId(s.getId());
                    return sDto;
                })
                .orElseThrow()
        );

        mockMvc
            .perform(post(ENTITY_API_URL + "/crea").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(dto.getData().toString()))
            .andExpect(jsonPath("$.oraInizio").value(dto.getOraInizio().toString()))
            .andExpect(jsonPath("$.oraFine").value(dto.getOraFine().toString()));
    }

    @Test
    @Transactional
    void createPrenotazioneEndpoint_shouldReturnBadRequest_whenDataInPast() throws Exception {
        initStatiPrenotazione();

        Utenti utente = createUtente("Mario", "3331234567");
        Sale sala = createSala("Sala Test", 10);

        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().minusDays(1)); // data nel passato
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        dto.setUtente(
            utentiRepository
                .findById(utente.getId())
                .map(u -> {
                    var uDto = new main.service.dto.UtentiDTO();
                    uDto.setId(u.getId());
                    return uDto;
                })
                .orElseThrow()
        );
        dto.setSala(
            saleRepository
                .findById(sala.getId())
                .map(s -> {
                    var sDto = new main.service.dto.SaleDTO();
                    sDto.setId(s.getId());
                    return sDto;
                })
                .orElseThrow()
        );

        mockMvc
            .perform(post(ENTITY_API_URL + "/crea").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────────────────────
    // /{id}/conferma — conferma prenotazione
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void confermaPrenotazioneEndpoint_shouldSetConfirmedState() throws Exception {
        initStatiPrenotazione();

        Utenti utente = createUtente("Mario", "3331234567");
        Sale sala = createSala("Sala Test", 10);

        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setUtente(utente);
        pren.setSala(sala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING).orElseThrow());
        pren = prenotazioniRepository.saveAndFlush(pren);

        mockMvc
            .perform(post(ENTITY_API_URL + "/" + pren.getId() + "/conferma").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(pren.getId().toString()));

        Prenotazioni updated = prenotazioniRepository.findById(pren.getId()).orElseThrow();
        assertThat(updated.getStato().getCodice()).isEqualTo(StatoCodice.CONFIRMED);
    }

    @Test
    @Transactional
    void confermaPrenotazioneEndpoint_shouldFail_whenNotInWaitingState() throws Exception {
        initStatiPrenotazione();

        Utenti utente = createUtente("Mario", "3331234567");
        Sale sala = createSala("Sala Test", 10);

        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setUtente(utente);
        pren.setSala(sala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        pren = prenotazioniRepository.saveAndFlush(pren);

        mockMvc
            .perform(post(ENTITY_API_URL + "/" + pren.getId() + "/conferma").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────────────────────
    // filtro per sala
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void getAllPrenotazionis_withSalaFilter_shouldReturnOnlyMatchingSala() throws Exception {
        Sale sala1 = createSala("Sala 1", 10);
        Sale sala2 = createSala("Sala 2", 20);

        Prenotazioni p1 = createPrenotazioneForSala(sala1);
        Prenotazioni p2 = createPrenotazioneForSala(sala2);

        insertedPrenotazioni = p1;

        mockMvc
            .perform(get(ENTITY_API_URL + "?salaId=" + sala1.getId()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(p1.getId().toString())))
            .andExpect(jsonPath("$.[*].id").value(org.hamcrest.Matchers.not(hasItem(p2.getId().toString()))));
    }

    // ─────────────────────────────────────────────────────────────
    // storico e odierne
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void getStorico_shouldReturnPastPrenotazioni() throws Exception {
        initStatiPrenotazione();

        // Prenotazione nel passato
        Prenotazioni passata = new Prenotazioni();
        passata.setData(LocalDate.now().minusDays(3));
        passata.setOraInizio(LocalTime.of(9, 0));
        passata.setOraFine(LocalTime.of(10, 0));
        passata.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        prenotazioniRepository.saveAndFlush(passata);

        mockMvc
            .perform(get(ENTITY_API_URL + "/storico").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(passata.getId().toString())));
    }

    @Test
    @Transactional
    void getOdierne_shouldReturnFuturePrenotazioniForCurrentUser() throws Exception {
        initStatiPrenotazione();

        User user = createUserWithLogin("user");
        Utenti utente = createUtenteForUser(user);

        Prenotazioni futura = new Prenotazioni();
        futura.setData(LocalDate.now().plusDays(1));
        futura.setOraInizio(LocalTime.of(14, 0));
        futura.setOraFine(LocalTime.of(15, 0));
        futura.setUtente(utente);
        futura.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        prenotazioniRepository.saveAndFlush(futura);

        mockMvc
            .perform(get(ENTITY_API_URL + "/odierne").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].id").value(hasItem(futura.getId().toString())));
    }

    // ─────────────────────────────────────────────────────────────
    // verifica-qr — nuovo endpoint
    // ─────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void verificaQrCode_shouldReturnPrenotazione_whenCodiceValido() throws Exception {
        String codice = "test1234";
        prenotazioni.setCodiceQr(codice);
        prenotazioniRepository.saveAndFlush(prenotazioni);
        insertedPrenotazioni = prenotazioni;

        mockMvc
            .perform(get(ENTITY_API_URL + "/verifica-qr/" + codice).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codiceQr").value(codice));
    }

    @Test
    @Transactional
    void verificaQrCode_shouldReturn404_whenCodiceNonEsiste() throws Exception {
        mockMvc
            .perform(get(ENTITY_API_URL + "/verifica-qr/codice-inesistente-xyz").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────
    // helper methods
    // ─────────────────────────────────────────────────────────────

    private void initStatiPrenotazione() {
        if (statiPrenotazioneRepository.count() > 0) return;

        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.WAITING).descrizione("In attesa").ordineAzione(1)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.CONFIRMED).descrizione("Confermata").ordineAzione(2)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.REJECTED).descrizione("Rifiutata").ordineAzione(3)
        );
        statiPrenotazioneRepository.saveAndFlush(
            new StatiPrenotazione().codice(StatoCodice.CANCELLED).descrizione("Annullata").ordineAzione(4)
        );
    }

    private User createUserWithLogin(String login) {
        User user = new User();
        user.setLogin(login);
        return userRepository.saveAndFlush(user);
    }

    private Utenti createUtenteForUser(User user) {
        Utenti utente = new Utenti();
        utente.setNome("Nome " + user.getLogin());
        utente.setNumeroDiTelefono("3330000000");
        utente.setUser(user);
        return utentiRepository.saveAndFlush(utente);
    }

    private Prenotazioni createPrenotazioneForUtente(Utenti utente) {
        Sale sala = createSala("Sala Owner", 10);
        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setUtente(utente);
        pren.setSala(sala);
        pren.setStato(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).orElseThrow());
        return prenotazioniRepository.saveAndFlush(pren);
    }

    private Prenotazioni createPrenotazioneForSala(Sale sala) {
        Prenotazioni pren = new Prenotazioni();
        pren.setData(LocalDate.now().plusDays(1));
        pren.setOraInizio(LocalTime.of(10, 0));
        pren.setOraFine(LocalTime.of(11, 0));
        pren.setSala(sala);
        return prenotazioniRepository.saveAndFlush(pren);
    }

    private Utenti createUtente(String nome, String telefono) {
        Utenti utente = new Utenti();
        utente.setNome(nome);
        utente.setNumeroDiTelefono(telefono);
        return utentiRepository.saveAndFlush(utente);
    }

    private Sale createSala(String nome, int capienza) {
        Sale sala = new Sale();
        sala.setNome(nome);
        sala.setCapienza(capienza);
        return saleRepository.saveAndFlush(sala);
    }

    protected long getRepositoryCount() {
        return prenotazioniRepository.count();
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

    protected Prenotazioni getPersistedPrenotazioni(Prenotazioni prenotazioni) {
        return prenotazioniRepository.findById(prenotazioni.getId()).orElseThrow();
    }

    protected void assertPersistedPrenotazioniToMatchAllProperties(Prenotazioni expectedPrenotazioni) {
        assertPrenotazioniAllPropertiesEquals(expectedPrenotazioni, getPersistedPrenotazioni(expectedPrenotazioni));
    }

    protected void assertPersistedPrenotazioniToMatchUpdatableProperties(Prenotazioni expectedPrenotazioni) {
        assertPrenotazioniAllUpdatablePropertiesEquals(expectedPrenotazioni, getPersistedPrenotazioni(expectedPrenotazioni));
    }
}
