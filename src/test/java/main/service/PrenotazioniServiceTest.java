package main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import main.service.dto.PrenotazioniDTO;
import main.service.dto.SaleDTO;
import main.service.dto.UtentiDTO;
import main.service.mapper.PrenotazioniMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrenotazioniServiceTest {

    @Mock
    private PrenotazioniRepository prenotazioniRepository;

    @Mock
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Mock
    private UtentiRepository utentiRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PrenotazioniMapper prenotazioniMapper;

    @InjectMocks
    private PrenotazioniService prenotazioniService;

    private UUID prenotazioneId;
    private Prenotazioni prenotazione;
    private Utenti utenteOwner;
    private User userOwner;
    private StatiPrenotazione statoCancelled;
    private StatiPrenotazione statoWaiting;
    private StatiPrenotazione statoConfirmed;
    private StatiPrenotazione statoRejected;
    private Sale sala;

    @BeforeEach
    void setUp() {
        prenotazioneId = UUID.randomUUID();

        userOwner = new User();
        userOwner.setLogin("owner");

        utenteOwner = new Utenti();
        utenteOwner.setId(UUID.randomUUID());
        utenteOwner.setUser(userOwner);

        sala = new Sale();
        sala.setId(UUID.randomUUID());
        sala.setNome("Sala Test");
        sala.setCapienza(10);

        prenotazione = new Prenotazioni();
        prenotazione.setId(prenotazioneId);
        prenotazione.setData(LocalDate.now().plusDays(1));
        prenotazione.setOraInizio(LocalTime.of(10, 0));
        prenotazione.setOraFine(LocalTime.of(11, 0));
        prenotazione.setUtente(utenteOwner);
        prenotazione.setSala(sala);

        statoCancelled = new StatiPrenotazione();
        statoCancelled.setCodice(StatoCodice.CANCELLED);

        statoWaiting = new StatiPrenotazione();
        statoWaiting.setCodice(StatoCodice.WAITING);

        statoConfirmed = new StatiPrenotazione();
        statoConfirmed.setCodice(StatoCodice.CONFIRMED);

        statoRejected = new StatiPrenotazione();
        statoRejected.setCodice(StatoCodice.REJECTED);

        // Mock globale per saleRepository — copre sia findById che findByIdWithLock.
        // Il service locale potrebbe usare l'uno o l'altro a seconda della versione.
        // LENIENT evita UnnecessaryStubbingException per il metodo non chiamato.
        when(saleRepository.findById(any(UUID.class))).thenReturn(Optional.of(sala));
        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
    }

    // ─────────────────────────────────────────────────────────────
    // deleteAsAdmin() — cancellazione fisica riservata agli admin
    // ─────────────────────────────────────────────────────────────

    @Test
    void deleteAsAdmin_shouldDeleteFromRepository_whenRecordExists() {
        when(prenotazioniRepository.existsById(prenotazioneId)).thenReturn(true);

        prenotazioniService.deleteAsAdmin(prenotazioneId);

        verify(prenotazioniRepository).existsById(prenotazioneId);
        verify(prenotazioniRepository).deleteById(prenotazioneId);
        verify(utentiRepository, never()).deleteById(any());
    }

    @Test
    void deleteAsAdmin_shouldThrowEntityNotFound_whenRecordDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(prenotazioniRepository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> prenotazioniService.deleteAsAdmin(missingId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Prenotazione non trovata");

        verify(prenotazioniRepository, never()).deleteById(any());
    }

    // ─────────────────────────────────────────────────────────────
    // deletePrenotazione() — soft delete con permessi + stato CANCELLED
    // ─────────────────────────────────────────────────────────────

    @Test
    void deletePrenotazione_shouldSetCancelledState_whenUserIsOwner() throws Exception {
        setAuthenticatedUser("owner");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CANCELLED)).thenReturn(Optional.of(statoCancelled));
        when(prenotazioniRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        prenotazioniService.deletePrenotazione(prenotazioneId);

        verify(prenotazioniRepository).findById(prenotazioneId);
        verify(statiPrenotazioneRepository).findByCodice(StatoCodice.CANCELLED);
        verify(prenotazioniRepository).save(prenotazione);
        assertThat(prenotazione.getStato()).isEqualTo(statoCancelled);
    }

    @Test
    void deletePrenotazione_shouldThrowAccessDenied_whenUserIsNotOwner() {
        setAuthenticatedUser("another-user");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));

        assertThatThrownBy(() -> prenotazioniService.deletePrenotazione(prenotazioneId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Utente non ha i permessi");

        verify(statiPrenotazioneRepository, never()).findByCodice(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void deletePrenotazione_shouldThrowEntityNotFound_whenPrenotazioneDoesNotExist() {
        setAuthenticatedUser("owner");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prenotazioniService.deletePrenotazione(prenotazioneId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Prenotazione non trovata");

        verify(statiPrenotazioneRepository, never()).findByCodice(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void deletePrenotazione_shouldThrowUtenteNonAutenticato_whenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> prenotazioniService.deletePrenotazione(prenotazioneId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Utente non autenticato");

        verify(prenotazioniRepository, never()).findById(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // validazione regole di business — testata tramite nuovoPrenotazioni() e save()
    // ─────────────────────────────────────────────────────────────

    @Test
    void nuovoPrenotazioni_shouldThrow_whenOraInizioAfterOraFine() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();
        dto.setOraInizio(LocalTime.of(11, 0));
        dto.setOraFine(LocalTime.of(10, 0));

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ora di inizio");
    }

    @Test
    void nuovoPrenotazioni_shouldThrow_whenDataInPast() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();
        dto.setData(LocalDate.now().minusDays(1));

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("passato");
    }

    @Test
    void save_admin_shouldThrow_whenNumPersoneSuperaCapienza() {
        PrenotazioniDTO dto = buildPrenotazioneDTOValida();
        prenotazione.setNumPersone(sala.getCapienza() + 1);

        when(prenotazioniMapper.toEntity(dto)).thenReturn(prenotazione);
        when(saleRepository.findByIdWithLock(sala.getId())).thenReturn(Optional.of(sala));

        assertThatThrownBy(() -> prenotazioniService.save(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("capienza");

        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void nuovoPrenotazioni_shouldThrow_whenNumPersoneSuperaCapienza() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();
        dto.setNumPersone(sala.getCapienza() + 1);

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("capienza");

        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void nuovoPrenotazioni_shouldThrow_whenNumPersoneZeroONegativo() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();
        dto.setNumPersone(0);

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("almeno 1");

        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void nuovoPrenotazioni_shouldNotThrow_whenNumPersoneIsNull() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();
        dto.setNumPersone(null);

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING)).thenReturn(Optional.of(statoWaiting));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(false);
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(dto);

        PrenotazioniDTO result = prenotazioniService.nuovoPrenotazioni(dto);

        assertThat(result).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────
    // confermaPrenotazione() — macchina a stati
    // ─────────────────────────────────────────────────────────────

    @Test
    void confermaPrenotazione_shouldSetConfirmedState_whenWaiting() {
        prenotazione.setStato(statoWaiting);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(false);
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(new PrenotazioniDTO());

        PrenotazioniDTO result = prenotazioniService.confermaPrenotazione(prenotazioneId);

        assertThat(result).isNotNull();
        assertThat(prenotazione.getStato()).isEqualTo(statoConfirmed);
        verify(prenotazioniRepository).save(prenotazione);
    }

    @Test
    void confermaPrenotazione_shouldThrow_whenNotInWaitingState() {
        prenotazione.setStato(statoConfirmed);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));

        assertThatThrownBy(() -> prenotazioniService.confermaPrenotazione(prenotazioneId)).isInstanceOf(IllegalStateException.class);

        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void confermaPrenotazione_shouldThrow_whenOverlapExists() {
        prenotazione.setStato(statoWaiting);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> prenotazioniService.confermaPrenotazione(prenotazioneId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("conflitto");

        verify(prenotazioniRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // getStoricoPrenotazioni() e getPrenotazioniOdierne()
    // ─────────────────────────────────────────────────────────────

    @Test
    void getStoricoPrenotazioni_shouldReturnPassedPrenotazioni() {
        when(prenotazioniRepository.findStorico(any())).thenReturn(List.of(prenotazione));
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(new PrenotazioniDTO());

        List<PrenotazioniDTO> result = prenotazioniService.getStoricoPrenotazioni();

        assertThat(result).hasSize(1);
        verify(prenotazioniRepository).findStorico(any());
    }

    @Test
    void getStoricoPrenotazioni_shouldReturnEmptyList_whenNessunStorico() {
        when(prenotazioniRepository.findStorico(any())).thenReturn(List.of());

        List<PrenotazioniDTO> result = prenotazioniService.getStoricoPrenotazioni();

        assertThat(result).isEmpty();
    }

    @Test
    void getPrenotazioniOdierne_shouldReturnPrenotazioniForAuthenticatedUser() {
        setAuthenticatedUser("owner");

        when(
            prenotazioniRepository.findByUtente_User_LoginAndDataGreaterThanEqualOrderByDataAscOraInizioAsc(eq("owner"), any())
        ).thenReturn(List.of(prenotazione));
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(new PrenotazioniDTO());

        List<PrenotazioniDTO> result = prenotazioniService.getPrenotazioniOdierne();

        assertThat(result).hasSize(1);
        verify(prenotazioniRepository).findByUtente_User_LoginAndDataGreaterThanEqualOrderByDataAscOraInizioAsc(eq("owner"), any());
    }

    // ─────────────────────────────────────────────────────────────
    // aggiornaPrenotazioniScadute() — scheduler, saveAll una sola volta
    // ─────────────────────────────────────────────────────────────

    @Test
    void aggiornaPrenotazioniScadute_shouldCallSaveAllOnce_notTwice() {
        prenotazione.setStato(statoWaiting);

        when(prenotazioniRepository.findExpiredWaiting(eq(StatoCodice.WAITING), any())).thenReturn(List.of(prenotazione));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.REJECTED)).thenReturn(Optional.of(statoRejected));
        when(prenotazioniRepository.saveAll(any())).thenReturn(List.of(prenotazione));

        prenotazioniService.aggiornaPrenotazioniScadute();

        verify(prenotazioniRepository, times(1)).saveAll(any());
        assertThat(prenotazione.getStato()).isEqualTo(statoRejected);
    }

    @Test
    void aggiornaPrenotazioniScadute_shouldDoNothing_whenNoExpiredPrenotazioni() {
        when(prenotazioniRepository.findExpiredWaiting(eq(StatoCodice.WAITING), any())).thenReturn(List.of());

        prenotazioniService.aggiornaPrenotazioniScadute();

        verify(prenotazioniRepository, never()).saveAll(any());
        verify(statiPrenotazioneRepository, never()).findByCodice(any());
    }

    @Test
    void aggiornaPrenotazioniScadute_shouldSetRejectedStateOnAllExpired() {
        Prenotazioni p1 = new Prenotazioni();
        p1.setId(UUID.randomUUID());
        p1.setStato(statoWaiting);

        Prenotazioni p2 = new Prenotazioni();
        p2.setId(UUID.randomUUID());
        p2.setStato(statoWaiting);

        when(prenotazioniRepository.findExpiredWaiting(eq(StatoCodice.WAITING), any())).thenReturn(List.of(p1, p2));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.REJECTED)).thenReturn(Optional.of(statoRejected));
        when(prenotazioniRepository.saveAll(any())).thenReturn(List.of(p1, p2));

        prenotazioniService.aggiornaPrenotazioniScadute();

        assertThat(p1.getStato()).isEqualTo(statoRejected);
        assertThat(p2.getStato()).isEqualTo(statoRejected);
    }

    // ─────────────────────────────────────────────────────────────
    // save() — creazione admin: verifica conflitti prima di CONFIRMED
    // ─────────────────────────────────────────────────────────────

    @Test
    void save_admin_shouldPersistAsConfirmed_whenNoConflict() {
        PrenotazioniDTO dto = buildPrenotazioneDTOValida();

        when(prenotazioniMapper.toEntity(dto)).thenReturn(prenotazione);
        when(saleRepository.findByIdWithLock(sala.getId())).thenReturn(Optional.of(sala));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(false);
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(dto);

        PrenotazioniDTO result = prenotazioniService.save(dto);

        assertThat(result).isNotNull();
        assertThat(prenotazione.getStato()).isEqualTo(statoConfirmed);
        verify(prenotazioniRepository).existsOverlappingConfirmedPrenotazione(any(), any(), any(), any());
        verify(prenotazioniRepository).save(prenotazione);
    }

    @Test
    void save_admin_shouldThrowIllegalState_whenConflictExists() {
        PrenotazioniDTO dto = buildPrenotazioneDTOValida();

        when(prenotazioniMapper.toEntity(dto)).thenReturn(prenotazione);
        when(saleRepository.findByIdWithLock(sala.getId())).thenReturn(Optional.of(sala));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> prenotazioniService.save(dto)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Sala Test");

        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void save_admin_shouldThrowEntityNotFound_whenSalaNonEsiste() {
        PrenotazioniDTO dto = buildPrenotazioneDTOValida();

        when(prenotazioniMapper.toEntity(dto)).thenReturn(prenotazione);
        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prenotazioniService.save(dto))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Sala non trovata");

        verify(prenotazioniRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // nuovoPrenotazioni() — flusso utente: WAITING, sovrapposizioni, utente da SecurityContext
    // ─────────────────────────────────────────────────────────────

    @Test
    void nuovoPrenotazioni_shouldPersistAndReturnDTO() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING)).thenReturn(Optional.of(statoWaiting));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(false);
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(dto);

        PrenotazioniDTO result = prenotazioniService.nuovoPrenotazioni(dto);

        assertThat(result).isNotNull();
        verify(prenotazioniRepository).save(any());
    }

    @Test
    void nuovoPrenotazioni_shouldSetRejected_whenOverlapExists() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.of(utenteOwner));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.WAITING)).thenReturn(Optional.of(statoWaiting));
        when(prenotazioniRepository.existsOverlappingConfirmedPrenotazione(any(), any(), any(), any())).thenReturn(true);
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.REJECTED)).thenReturn(Optional.of(statoRejected));
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(prenotazioniMapper.toDto(any(Prenotazioni.class))).thenReturn(dto);

        prenotazioniService.nuovoPrenotazioni(dto);

        assertThat(prenotazione.getStato()).isEqualTo(statoRejected);
    }

    @Test
    void nuovoPrenotazioni_shouldThrowIllegalArgument_whenSalaIdMissing() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        // salaId deliberatamente assente

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nuovoPrenotazioni_shouldThrowEntityNotFound_whenUtenteNonHaProfilo() {
        setAuthenticatedUser("owner");
        PrenotazioniDTO dto = buildPrenotazioneDTODaRicerca();

        when(saleRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.of(sala));
        when(utentiRepository.findByUser_Login("owner")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prenotazioniService.nuovoPrenotazioni(dto))
            .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
            .hasMessageContaining("Profilo utente non trovato");
    }

    // ─────────────────────────────────────────────────────────────
    // helpers
    // ─────────────────────────────────────────────────────────────

    private PrenotazioniDTO buildPrenotazioneDTODaRicerca() {
        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setSalaId(sala.getId());
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        return dto;
    }

    private PrenotazioniDTO buildPrenotazioneDTOValida() {
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setId(sala.getId());

        UtentiDTO utenteDTO = new UtentiDTO();
        utenteDTO.setId(utenteOwner.getId());

        PrenotazioniDTO dto = new PrenotazioniDTO();
        dto.setSala(saleDTO);
        dto.setUtente(utenteDTO);
        dto.setData(LocalDate.now().plusDays(1));
        dto.setOraInizio(LocalTime.of(10, 0));
        dto.setOraFine(LocalTime.of(11, 0));
        return dto;
    }

    private void setAuthenticatedUser(String username) {
        var auth = new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
