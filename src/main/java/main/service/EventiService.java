package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Eventi;
import main.domain.PrenotazioneEventoPubblico;
import main.domain.Prenotazioni;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import main.repository.EventiRepository;
import main.repository.PrenotazioneEventoPubblicoRepository;
import main.repository.PrenotazioniRepository;
import main.repository.StatiPrenotazioneRepository;
import main.service.dto.EventiDTO;
import main.service.dto.PrenotazioniEmailDTO;
import main.service.mapper.EventiMapper;
import main.web.rest.errors.BadRequestAlertException;
import main.web.rest.errors.EventoPienoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventiService {

    private static final Logger LOG = LoggerFactory.getLogger(EventiService.class);

    private final EventiRepository eventiRepository;
    private final EventiMapper eventiMapper;
    private final PrenotazioniRepository prenotazioniRepository;
    private final StatiPrenotazioneRepository statiPrenotazioneRepository;
    private final MailService mailService;
    private final QrCodeGenerator qrCodeGenerator;
    private final PrenotazioneEventoPubblicoRepository prenotazioneEventoPubblicoRepository;

    public EventiService(
        EventiRepository eventiRepository,
        EventiMapper eventiMapper,
        PrenotazioniRepository prenotazioniRepository,
        StatiPrenotazioneRepository statiPrenotazioneRepository,
        MailService mailService,
        QrCodeGenerator qrCodeGenerator,
        PrenotazioneEventoPubblicoRepository prenotazioneEventoPubblicoRepository
    ) {
        this.eventiRepository = eventiRepository;
        this.eventiMapper = eventiMapper;
        this.prenotazioniRepository = prenotazioniRepository;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
        this.mailService = mailService;
        this.qrCodeGenerator = qrCodeGenerator;
        this.prenotazioneEventoPubblicoRepository = prenotazioneEventoPubblicoRepository;
    }

    @Transactional(readOnly = true)
    public List<EventiDTO> findPublicEventi() {
        LOG.debug("Request to get all public Eventi");
        List<Eventi> eventi = eventiRepository.findPublicConfirmed(TipoEvento.PUBBLICO, StatoCodice.CONFIRMED);

        if (eventi.isEmpty()) {
            return List.of();
        }

        // Una sola query aggregata per tutti gli eventi invece di N query COUNT separate
        List<UUID> ids = eventi.stream().map(Eventi::getId).toList();
        Map<UUID, Long> conteggioMap = prenotazioneEventoPubblicoRepository.conteggioPerEventi(ids);

        return eventi
            .stream()
            .map(e -> {
                EventiDTO dto = eventiMapper.toDto(e);
                long occupati = conteggioMap.getOrDefault(e.getId(), 0L);
                dto.setPostiOccupati(occupati);
                if (dto.getNumPersone() != null) {
                    dto.setEventoPieno(occupati >= dto.getNumPersone());
                }
                return dto;
            })
            .toList();
    }

    public EventiDTO createEvento(EventiDTO dto) {
        LOG.debug("REST request to save Eventi : {}", dto);

        Prenotazioni pren = null;

        if (dto.getPrenotazioneId() != null) {
            pren = prenotazioniRepository
                .findById(dto.getPrenotazioneId())
                .orElseThrow(() -> new BadRequestAlertException("Prenotazione non trovata", "eventi", "prenotazioneNotFound"));
        }

        Eventi eventi = buildEventoFromDto(dto, pren);
        setPrezzoInBaseAlTipo(dto.getTipo(), dto.getPrezzo(), eventi);

        eventi = eventiRepository.save(eventi);

        if (pren != null) {
            aggiornaStatoPrenotazioneConfermata(pren);
            inviaEmailConfermaPrenotazione(pren, eventi);
        }

        return eventiMapper.toDto(eventi);
    }

    @Transactional(readOnly = true)
    public Page<EventiDTO> findAll(Pageable pageable) {
        return eventiRepository.findAll(pageable).map(eventiMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<EventiDTO> findOne(UUID id) {
        // Usa la query con JOIN FETCH per caricare prenotazione e sala in eager.
        // findById standard lascerebbe prenotazione in lazy → numPersone = null nel DTO.
        return eventiRepository.findByIdWithPrenotazioneAndSala(id).map(eventiMapper::toDto);
    }

    public EventiDTO update(EventiDTO eventiDTO) {
        Eventi existing = eventiRepository.findById(eventiDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        existing.setTitolo(eventiDTO.getTitolo());
        existing.setDescrizione(eventiDTO.getDescrizione());

        // Preserva il tipo dal DB se il frontend non lo invia (colonna NOT NULL)
        TipoEvento tipoEffettivo = eventiDTO.getTipo() != null ? eventiDTO.getTipo() : existing.getTipo();
        existing.setTipo(tipoEffettivo);

        setPrezzoInBaseAlTipo(tipoEffettivo, eventiDTO.getPrezzo(), existing);

        return eventiMapper.toDto(eventiRepository.save(existing));
    }

    public Optional<EventiDTO> partialUpdate(EventiDTO eventiDTO) {
        return eventiRepository
            .findById(eventiDTO.getId())
            .map(existing -> {
                if (eventiDTO.getTitolo() != null) existing.setTitolo(eventiDTO.getTitolo());
                if (eventiDTO.getDescrizione() != null) existing.setDescrizione(eventiDTO.getDescrizione());
                if (eventiDTO.getTipo() != null) existing.setTipo(eventiDTO.getTipo());
                TipoEvento tipoEffettivo = existing.getTipo();
                if (tipoEffettivo == TipoEvento.PUBBLICO && eventiDTO.getPrezzo() != null) {
                    existing.setPrezzo(eventiDTO.getPrezzo());
                } else if (tipoEffettivo == TipoEvento.PRIVATO) {
                    existing.setPrezzo(BigDecimal.ZERO);
                }
                return eventiRepository.save(existing);
            })
            .map(eventiMapper::toDto);
    }

    public void delete(UUID id) {
        eventiRepository.deleteById(id);
    }

    private Eventi buildEventoFromDto(EventiDTO dto, Prenotazioni prenotazione) {
        Eventi evento = new Eventi();
        evento.setTitolo(dto.getTitolo());
        evento.setDescrizione(dto.getDescrizione());
        evento.setTipo(dto.getTipo());
        evento.setPrenotazione(prenotazione);
        evento.setLocandinaUrl(dto.getLocandinaUrl());
        return evento;
    }

    private void setPrezzoInBaseAlTipo(TipoEvento tipo, BigDecimal prezzoDto, Eventi evento) {
        if (tipo == TipoEvento.PUBBLICO) {
            evento.setPrezzo(prezzoDto != null ? prezzoDto : BigDecimal.ZERO);
        } else {
            evento.setPrezzo(BigDecimal.ZERO);
        }
    }

    private void aggiornaStatoPrenotazioneConfermata(Prenotazioni prenotazione) {
        statiPrenotazioneRepository.findByCodice(StatoCodice.CONFIRMED).ifPresent(prenotazione::setStato);
        prenotazioniRepository.save(prenotazione);
    }

    /**
     * Invia l'email di conferma prenotazione all'utente dopo che lo stato
     * è passato a CONFIRMED tramite la creazione dell'evento.
     * Non blocca mai il flusso principale: gli errori vengono solo loggati.
     */
    private void inviaEmailConfermaPrenotazione(Prenotazioni pren, Eventi evento) {
        try {
            // Genera il codice QR dal codice prenotazione
            if (pren.getCodiceQr() == null || pren.getCodiceQr().isBlank()) {
                String codice = "SALA-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                pren.setCodiceQr(codice);
                prenotazioniRepository.save(pren);
            }

            String qrBase64 = qrCodeGenerator.generateQRCodeBase64(pren.getCodiceQr());

            PrenotazioniEmailDTO emailDto = new PrenotazioniEmailDTO();
            if (pren.getUtente() != null) {
                emailDto.setNome(pren.getUtente().getNome() != null ? pren.getUtente().getNome() : "");
                emailDto.setCognome("");
                if (pren.getUtente().getUser() != null) {
                    emailDto.setEmail(pren.getUtente().getUser().getEmail());
                }
            }

            if (emailDto.getEmail() == null || emailDto.getEmail().isBlank()) {
                LOG.warn("Email utente non disponibile per prenotazione {}, skip invio conferma", pren.getId());
                return;
            }

            // Collega l'evento alla prenotazione per il template
            pren.setEvento(evento);

            mailService.sendConfermaPrenotazione(pren, emailDto, pren.getCodiceQr(), qrBase64);
            LOG.debug("Email conferma inviata per prenotazione {} (evento: {})", pren.getId(), evento.getId());
        } catch (Exception e) {
            LOG.warn("Errore invio email conferma per prenotazione {}: {}", pren.getId(), e.getMessage());
        }
    }

    @Transactional
    public void inviaEmailPrenotazione(UUID id, PrenotazioniEmailDTO dto) {
        // Acquisisce subito il lock pessimistico sull'evento per serializzare
        // le richieste concorrenti: il secondo thread attende finché il primo
        // non ha completato il salvataggio e fatto commit.
        eventiRepository.findByIdWithLock(id).orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        // Carica l'evento con le relazioni necessarie (prenotazione + sala)
        // all'interno della stessa transazione che detiene il lock.
        Eventi evento = eventiRepository
            .findByIdWithPrenotazioneAndSala(id)
            .orElseThrow(() -> new EntityNotFoundException("Evento non trovato"));

        // Controlla capienza: a questo punto siamo gli unici nel lock,
        // il conteggio riflette la situazione reale senza interferenze.
        Integer limitePartecipanti = evento.getPrenotazione() != null ? evento.getPrenotazione().getNumPersone() : null;
        if (limitePartecipanti != null) {
            long postiOccupati = prenotazioneEventoPubblicoRepository.countByEventoId(evento.getId());
            if (postiOccupati >= limitePartecipanti) {
                LOG.warn("Evento {} al completo ({}/{})", evento.getId(), postiOccupati, limitePartecipanti);
                throw new EventoPienoException();
            }
        }

        String codiceEvento = evento.getId().toString().replace("-", "").substring(0, 8);
        long sequenza = prenotazioneEventoPubblicoRepository.nextSequenzaPerEvento(evento.getId());
        String primaLettera = dto.getNome() != null && !dto.getNome().isEmpty()
            ? String.valueOf(dto.getNome().charAt(0)).toUpperCase()
            : "X";
        String codicePersona = primaLettera + String.format("%02d", sequenza);
        String codicePrenotazione = codiceEvento + "-" + codicePersona;

        PrenotazioneEventoPubblico prenPub = new PrenotazioneEventoPubblico();
        prenPub.setEvento(evento);
        prenPub.setNome(dto.getNome());
        prenPub.setCognome(dto.getCognome());
        prenPub.setEmail(dto.getEmail());
        prenPub.setCodicePrenotazione(codicePrenotazione);
        prenotazioneEventoPubblicoRepository.save(prenPub);
        // Il lock viene rilasciato al commit di questa transazione,
        // garantendo che nessun altro thread abbia inserito nel frattempo.

        String qrCod = qrCodeGenerator.generateQRCodeBase64(codicePrenotazione);
        mailService.sendPrenotazioneEventoPublico(evento, dto, codicePrenotazione, qrCod);

        LOG.info("Prenotazione evento pubblico {} salvata con codice {}", evento.getId(), codicePrenotazione);
    }
}
