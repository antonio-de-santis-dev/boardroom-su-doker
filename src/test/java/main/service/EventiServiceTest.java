package main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioniRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.EventiDTO;
import main.service.mapper.EventiMapper;
import main.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventiServiceTest {

    @Mock
    private EventiRepository eventiRepository;

    @Mock
    private EventiMapper eventiMapper;

    @Mock
    private PrenotazioniRepository prenotazioniRepository;

    @Mock
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Mock
    private MailService mailService;

    @Mock
    private QrCodeGenerator qrCodeGenerator;

    @InjectMocks
    private EventiService eventiService;

    private UUID eventoId;
    private UUID prenotazioneId;
    private Eventi evento;
    private Prenotazioni prenotazione;
    private StatiPrenotazione statoConfirmed;

    @BeforeEach
    void setUp() {
        eventoId = UUID.randomUUID();
        prenotazioneId = UUID.randomUUID();

        Sale sala = new Sale();
        sala.setId(UUID.randomUUID());
        sala.setNome("Sala Conferenze");

        Utenti utente = new Utenti();
        utente.setId(UUID.randomUUID());

        statoConfirmed = new StatiPrenotazione();
        statoConfirmed.setCodice(StatoCodice.CONFIRMED);

        prenotazione = new Prenotazioni();
        prenotazione.setId(prenotazioneId);
        prenotazione.setSala(sala);
        prenotazione.setUtente(utente);
        prenotazione.setStato(statoConfirmed);

        evento = new Eventi();
        evento.setId(eventoId);
        evento.setTitolo("Evento Test");
        evento.setTipo(TipoEvento.PRIVATO);
        evento.setPrezzo(BigDecimal.ZERO);
        evento.setPrenotazione(prenotazione);
    }

    // ─────────────────────────────────────────────────────────────
    // createEvento()
    // ─────────────────────────────────────────────────────────────

    @Test
    void createEvento_shouldPersistAndReturnDTO_forPrivateEvent() {
        EventiDTO dto = buildEventoDTO(TipoEvento.PRIVATO, null);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(eventiRepository.save(any(Eventi.class))).thenReturn(evento);
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        EventiDTO result = eventiService.createEvento(dto);

        assertThat(result).isNotNull();
        verify(eventiRepository).save(any(Eventi.class));
        verify(prenotazioniRepository).save(prenotazione);
    }

    @Test
    void createEvento_shouldSetPrezzoToZero_forPrivateEvent() {
        EventiDTO dto = buildEventoDTO(TipoEvento.PRIVATO, BigDecimal.valueOf(50));

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(invocation -> {
            // FIX: cast corretto — classe è Eventi non Events
            Eventi saved = invocation.getArgument(0, Eventi.class);
            assertThat(saved.getPrezzo()).isEqualByComparingTo(BigDecimal.ZERO);
            return evento;
        });
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.createEvento(dto);
    }

    @Test
    void createEvento_shouldSetPrezzo_forPublicEvent() {
        BigDecimal prezzo = BigDecimal.valueOf(25);
        EventiDTO dto = buildEventoDTO(TipoEvento.PUBBLICO, prezzo);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(invocation -> {
            // FIX: cast corretto
            Eventi saved = invocation.getArgument(0, Eventi.class);
            assertThat(saved.getPrezzo()).isEqualByComparingTo(prezzo);
            return evento;
        });
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.createEvento(dto);
    }

    @Test
    void createEvento_shouldThrowBadRequest_whenPrenotazioneNotFound() {
        EventiDTO dto = buildEventoDTO(TipoEvento.PRIVATO, null);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventiService.createEvento(dto)).isInstanceOf(BadRequestAlertException.class);

        verify(eventiRepository, never()).save(any());
    }

    @Test
    void createEvento_shouldConfirmPrenotazione_afterSaving() {
        EventiDTO dto = buildEventoDTO(TipoEvento.PRIVATO, null);

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(eventiRepository.save(any(Eventi.class))).thenReturn(evento);
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED)).thenReturn(Optional.of(statoConfirmed));
        when(prenotazioniRepository.save(any())).thenReturn(prenotazione);
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.createEvento(dto);

        // La prenotazione deve essere confermata dopo la creazione dell'evento
        assertThat(prenotazione.getStato()).isEqualTo(statoConfirmed);
        verify(prenotazioniRepository).save(prenotazione);
    }

    // ─────────────────────────────────────────────────────────────
    // update() — Bug Fix: preserva tipo se non inviato dal frontend
    // ─────────────────────────────────────────────────────────────

    @Test
    void update_shouldPreserveTipo_whenTipoIsNullInDTO() {
        evento.setTipo(TipoEvento.PRIVATO); // tipo salvato nel DB
        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Titolo Aggiornato");
        dto.setTipo(null); // frontend non invia tipo

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.update(dto);

        // Il tipo NON deve diventare NULL (fix bug tipo=null 500 error)
        assertThat(evento.getTipo()).isEqualTo(TipoEvento.PRIVATO);
    }

    @Test
    void update_shouldUpdateTipo_whenTipoIsProvidedInDTO() {
        evento.setTipo(TipoEvento.PRIVATO);
        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Titolo Aggiornato");
        dto.setTipo(TipoEvento.PUBBLICO);
        dto.setPrezzo(BigDecimal.valueOf(15));

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.update(dto);

        assertThat(evento.getTipo()).isEqualTo(TipoEvento.PUBBLICO);
    }

    @Test
    void update_shouldUpdateTitolo_whenProvided() {
        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Nuovo Titolo");
        dto.setTipo(TipoEvento.PRIVATO);

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.update(dto);

        assertThat(evento.getTitolo()).isEqualTo("Nuovo Titolo");
    }

    @Test
    void update_shouldThrowEntityNotFound_whenEventoDoesNotExist() {
        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Titolo");
        dto.setTipo(TipoEvento.PRIVATO);

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventiService.update(dto))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Evento non trovato");

        verify(eventiRepository, never()).save(any());
    }

    @Test
    void update_shouldSetPrezzoZero_whenSwitchingFromPubblicoToPrivato() {
        evento.setTipo(TipoEvento.PUBBLICO);
        evento.setPrezzo(BigDecimal.valueOf(50));

        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Titolo");
        dto.setTipo(TipoEvento.PRIVATO);

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(dto);

        eventiService.update(dto);

        assertThat(evento.getPrezzo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─────────────────────────────────────────────────────────────
    // partialUpdate()
    // ─────────────────────────────────────────────────────────────

    @Test
    void partialUpdate_shouldUpdateOnlyTitolo_whenOnlyTitoloProvided() {
        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(new EventiDTO());

        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setTitolo("Solo Titolo Aggiornato");

        eventiService.partialUpdate(dto);

        assertThat(evento.getTitolo()).isEqualTo("Solo Titolo Aggiornato");
    }

    @Test
    void partialUpdate_shouldNotUpdatePrezzo_forPrivateEvent() {
        evento.setTipo(TipoEvento.PRIVATO);
        evento.setPrezzo(BigDecimal.ZERO);

        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setPrezzo(BigDecimal.valueOf(100)); // prova ad aggiornare prezzo su privato

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(new EventiDTO());

        eventiService.partialUpdate(dto);

        // Il prezzo su evento PRIVATO non deve cambiare
        assertThat(evento.getPrezzo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void partialUpdate_shouldUpdatePrezzo_forPublicEvent() {
        evento.setTipo(TipoEvento.PUBBLICO);
        evento.setPrezzo(BigDecimal.valueOf(10));

        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);
        dto.setPrezzo(BigDecimal.valueOf(30));

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(eventiRepository.save(any(Eventi.class))).thenAnswer(i -> i.getArgument(0));
        when(eventiMapper.toDto(any(Eventi.class))).thenReturn(new EventiDTO());

        eventiService.partialUpdate(dto);

        assertThat(evento.getPrezzo()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void partialUpdate_shouldReturnEmpty_whenEventoNotFound() {
        EventiDTO dto = new EventiDTO();
        dto.setId(eventoId);

        when(eventiRepository.findById(eventoId)).thenReturn(Optional.empty());

        Optional<EventiDTO> result = eventiService.partialUpdate(dto);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // findPublicEventi()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findPublicEventi_shouldReturnOnlyPublicConfirmedEvents() {
        evento.setTipo(TipoEvento.PUBBLICO);

        when(eventiRepository.findPublicConfirmed(TipoEvento.PUBBLICO, StatoCodice.CONFIRMED)).thenReturn(List.of(evento));
        // FIX: usa anyList() invece di passare un'istanza concreta al matcher
        when(eventiMapper.toDto(anyList())).thenReturn(List.of(new EventiDTO()));

        List<EventiDTO> result = eventiService.findPublicEventi();

        assertThat(result).hasSize(1);
        verify(eventiRepository).findPublicConfirmed(TipoEvento.PUBBLICO, StatoCodice.CONFIRMED);
    }

    @Test
    void findPublicEventi_shouldReturnEmptyList_whenNoneExist() {
        when(eventiRepository.findPublicConfirmed(any(), any())).thenReturn(List.of());
        // FIX: usa anyList() invece di passare un'istanza concreta al matcher
        when(eventiMapper.toDto(anyList())).thenReturn(List.of());

        List<EventiDTO> result = eventiService.findPublicEventi();

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────────────────────────

    @Test
    void delete_shouldCallDeleteById() {
        eventiService.delete(eventoId);

        verify(eventiRepository).deleteById(eventoId);
    }

    // ─────────────────────────────────────────────────────────────
    // helpers
    // ─────────────────────────────────────────────────────────────

    private EventiDTO buildEventoDTO(TipoEvento tipo, BigDecimal prezzo) {
        EventiDTO dto = new EventiDTO();
        dto.setPrenotazioneId(prenotazioneId);
        dto.setTitolo("Evento Test");
        dto.setTipo(tipo);
        dto.setPrezzo(prezzo);
        return dto;
    }
}
