package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Sale;
import main.repository.SaleRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private static final Logger LOG = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;

    public SaleService(SaleRepository saleRepository, SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleMapper = saleMapper;
    }

    // ---- Normalizzazione imageUrl -------------------------------------------

    /**
     * Con la strategia base64, i valori nel DB sono gia data URL corretti
     * (es: "data:image/jpeg;base64,...") oppure null.
     *
     * I vecchi valori legacy (path filesystem come /uploads/sale/uuid.jpg)
     * vengono azzerati: il frontend mostra il placeholder e l'admin
     * potra ricaricare l'immagine dal popup.
     */
    private SaleDTO normalizzaImageUrl(SaleDTO dto) {
        String url = dto.getImageUrl();
        if (url == null || url.isBlank()) return dto;

        // Formato corretto: data URL base64
        if (url.startsWith("data:image/")) return dto;

        // Formato legacy (path filesystem) - non servibile, azzeriamo
        LOG.debug("imageUrl legacy rimosso per sala {}", dto.getId());
        dto.setImageUrl(null);
        return dto;
    }

    // ---- CRUD ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SaleDTO> findAllFreeSales(LocalDate data, LocalTime inizio, LocalTime fine, Integer capienza) {
        LOG.debug("Request to get free sales for {} from {} to {}", data, inizio, fine, capienza);
        return saleRepository
            .findFreeSales(data, inizio, fine, capienza)
            .stream()
            .map(saleMapper::toDto)
            .map(this::normalizzaImageUrl)
            .toList();
    }

    public SaleDTO save(SaleDTO saleDTO) {
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return normalizzaImageUrl(saleMapper.toDto(sale));
    }

    public SaleDTO update(SaleDTO saleDTO) {
        LOG.debug("Request to update Sale : {}", saleDTO);
        saleRepository.findById(saleDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + saleDTO.getId()));
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return normalizzaImageUrl(saleMapper.toDto(sale));
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable).map(saleMapper::toDto).map(this::normalizzaImageUrl);
    }

    public void delete(UUID id) {
        saleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<SaleDTO> findOne(UUID id) {
        return saleRepository.findById(id).map(saleMapper::toDto).map(this::normalizzaImageUrl);
    }
}
