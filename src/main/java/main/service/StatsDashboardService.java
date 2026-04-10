package main.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import main.repository.StatsRepository;
import main.security.AuthoritiesConstants;
import main.service.dto.StatsDashboardDTO;
import main.service.dto.StatsDashboardDTO.LabelDoubleDTO;
import main.service.dto.StatsDashboardDTO.LabelValueDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsDashboardService {

    private static final List<String> GIORNI_SETTIMANA = Arrays.asList(
        "Domenica",
        "Lunedì",
        "Martedì",
        "Mercoledì",
        "Giovedì",
        "Venerdì",
        "Sabato"
    );

    private final StatsRepository statsRepository;

    public StatsDashboardService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Calcola tutte le statistiche aggregate per il periodo specificato.
     * Accessibile solo da ROLE_ADMIN.
     *
     * @param dal  data inizio intervallo (inclusa)
     * @param al   data fine intervallo (inclusa)
     */
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public StatsDashboardDTO getDashboard(LocalDate dal, LocalDate al) {
        StatsDashboardDTO dto = new StatsDashboardDTO();

        // ── KPI ───────────────────────────────────────────────
        long totale = statsRepository.countTotale(dal, al);
        long confermate = statsRepository.countConfermate(dal, al);
        long utentiAttivi = statsRepository.countUtentiAttivi(dal, al);

        dto.setTotalePrenotazioni(totale);
        dto.setPrenotazioniConfermate(confermate);
        dto.setUtentiAttivi(utentiAttivi);
        dto.setTassoOccupazionePercentuale(totale > 0 ? Math.round(((double) confermate / totale) * 1000.0) / 10.0 : 0.0);

        // ── Grafico 1: Per sala ───────────────────────────────
        dto.setPrenotazioniPerSala(
            statsRepository
                .countPerSala(dal, al)
                .stream()
                .map(row -> new LabelValueDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList())
        );

        // ── Grafico 2: Per ora ────────────────────────────────
        // Costruiamo mappa ora→count inizializzata a 0 per tutte le ore 8-19
        Map<Integer, Long> oraMap = new LinkedHashMap<>();
        for (int h = 8; h <= 19; h++) oraMap.put(h, 0L);

        statsRepository
            .countPerOra(dal, al)
            .forEach(row -> {
                int ora = ((Number) row[0]).intValue();
                long cnt = ((Number) row[1]).longValue();
                oraMap.put(ora, cnt);
            });

        dto.setOreRichieste(
            oraMap
                .entrySet()
                .stream()
                .map(e -> new LabelValueDTO(String.format("%02d:00", e.getKey()), e.getValue()))
                .collect(Collectors.toList())
        );

        // ── Grafico 3: Occupazione per mese ──────────────────
        dto.setOccupazionePerMese(
            statsRepository
                .occupazionePerMese(dal, al)
                .stream()
                .map(row -> {
                    int anno = ((Number) row[0]).intValue();
                    int mese = ((Number) row[1]).intValue();
                    long conf = ((Number) row[2]).longValue();
                    long tot = ((Number) row[3]).longValue();
                    String label = Month.of(mese).getDisplayName(TextStyle.SHORT, Locale.ITALIAN) + " " + anno;
                    double tasso = tot > 0 ? Math.round(((double) conf / tot) * 1000.0) / 10.0 : 0.0;
                    return new LabelDoubleDTO(label, tasso);
                })
                .collect(Collectors.toList())
        );

        // ── Grafico 4: Top 5 utenti ───────────────────────────
        dto.setTopUtenti(
            statsRepository
                .top5Utenti(dal, al)
                .stream()
                .limit(5)
                .map(row -> new LabelValueDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList())
        );

        // ── Grafico 5: Per giorno settimana ───────────────────
        // DAYOFWEEK MySQL: 1=Dom, 2=Lun, ..., 7=Sab → mappiamo su label italiano
        Map<Integer, Long> giornoMap = new LinkedHashMap<>();
        for (int i = 1; i <= 7; i++) giornoMap.put(i, 0L);

        statsRepository
            .countPerGiornoSettimana(dal, al)
            .forEach(row -> {
                int giorno = ((Number) row[0]).intValue();
                long cnt = ((Number) row[1]).longValue();
                giornoMap.put(giorno, cnt);
            });

        dto.setPrenotazioniPerGiorno(
            giornoMap
                .entrySet()
                .stream()
                .map(e -> new LabelValueDTO(GIORNI_SETTIMANA.get(e.getKey() - 1), e.getValue()))
                .collect(Collectors.toList())
        );

        return dto;
    }
}
