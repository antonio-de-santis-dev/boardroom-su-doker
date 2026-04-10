package main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Sale;
import main.repository.SaleRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleService saleService;

    private UUID salaId;
    private Sale sala;
    private SaleDTO salaDTO;

    @BeforeEach
    void setUp() {
        salaId = UUID.randomUUID();

        sala = new Sale();
        sala.setId(salaId);
        sala.setNome("Sala Riunioni A");
        sala.setCapienza(20);

        salaDTO = new SaleDTO();
        salaDTO.setId(salaId);
        salaDTO.setNome("Sala Riunioni A");
        salaDTO.setCapienza(20);
    }

    // ─────────────────────────────────────────────────────────────
    // save()
    // ─────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDTO() {
        when(saleMapper.toEntity(salaDTO)).thenReturn(sala);
        when(saleRepository.save(sala)).thenReturn(sala);
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        SaleDTO result = saleService.save(salaDTO);

        assertThat(result).isNotNull();
        assertThat(result.getNome()).isEqualTo("Sala Riunioni A");
        verify(saleRepository).save(sala);
    }

    // ─────────────────────────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────────────────────────

    @Test
    void update_shouldSaveAndReturnUpdatedDTO() {
        salaDTO.setNome("Sala Aggiornata");

        when(saleMapper.toEntity(salaDTO)).thenReturn(sala);
        when(saleRepository.save(sala)).thenReturn(sala);
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        SaleDTO result = saleService.update(salaDTO);

        assertThat(result).isNotNull();
        verify(saleRepository).save(sala);
    }

    // ─────────────────────────────────────────────────────────────
    // findAll()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnPageOfSaleDTO() {
        when(saleRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sala)));
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        var result = saleService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Sala Riunioni A");
    }

    @Test
    void findAll_shouldReturnEmpty_whenNoSaleExist() {
        when(saleRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        var result = saleService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // findOne()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findOne_shouldReturnDTO_whenSalaExists() {
        when(saleRepository.findById(salaId)).thenReturn(Optional.of(sala));
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        Optional<SaleDTO> result = saleService.findOne(salaId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(salaId);
    }

    @Test
    void findOne_shouldReturnEmpty_whenSalaDoesNotExist() {
        when(saleRepository.findById(salaId)).thenReturn(Optional.empty());

        Optional<SaleDTO> result = saleService.findOne(salaId);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────────────────────────

    @Test
    void delete_shouldCallDeleteById() {
        saleService.delete(salaId);

        verify(saleRepository).deleteById(salaId);
    }

    // ─────────────────────────────────────────────────────────────
    // findAllFreeSales()
    // ─────────────────────────────────────────────────────────────

    @Test
    void findAllFreeSales_shouldReturnAvailableSale() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inizio = LocalTime.of(9, 0);
        LocalTime fine = LocalTime.of(10, 0);

        when(saleRepository.findFreeSales(data, inizio, fine, null)).thenReturn(List.of(sala));
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        List<SaleDTO> result = saleService.findAllFreeSales(data, inizio, fine, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Sala Riunioni A");
        verify(saleRepository).findFreeSales(data, inizio, fine, null);
    }

    @Test
    void findAllFreeSales_shouldFilterByCapienza_whenProvided() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inizio = LocalTime.of(9, 0);
        LocalTime fine = LocalTime.of(10, 0);
        Integer capienza = 15;

        when(saleRepository.findFreeSales(data, inizio, fine, capienza)).thenReturn(List.of(sala));
        when(saleMapper.toDto(sala)).thenReturn(salaDTO);

        List<SaleDTO> result = saleService.findAllFreeSales(data, inizio, fine, capienza);

        assertThat(result).hasSize(1);
        verify(saleRepository).findFreeSales(data, inizio, fine, capienza);
    }

    @Test
    void findAllFreeSales_shouldReturnEmpty_whenNoSaleAvailable() {
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inizio = LocalTime.of(9, 0);
        LocalTime fine = LocalTime.of(10, 0);

        when(saleRepository.findFreeSales(data, inizio, fine, null)).thenReturn(List.of());

        List<SaleDTO> result = saleService.findAllFreeSales(data, inizio, fine, null);

        assertThat(result).isEmpty();
    }
}
