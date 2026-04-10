package main.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import main.domain.Prenotazioni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository dedicato alle query aggregate per la dashboard statistiche.
 * Separato da PrenotazioniRepository per mantenere la separazione delle responsabilità.
 */
@Repository
public interface StatsRepository extends JpaRepository<Prenotazioni, UUID> {
    // ── Contatori KPI ─────────────────────────────────────────

    @Query(
        """
        SELECT COUNT(p) FROM Prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
        """
    )
    long countTotale(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    @Query(
        """
        SELECT COUNT(p) FROM Prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
          AND p.stato.codice = main.domain.enumeration.StatoCodice.CONFIRMED
        """
    )
    long countConfermate(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    @Query(
        """
        SELECT COUNT(DISTINCT p.utente.id) FROM Prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
        """
    )
    long countUtentiAttivi(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Grafico 1: Prenotazioni per sala ──────────────────────
    /**
     * Ritorna [ [nomeSala, count], ... ] ordinato per count DESC.
     */
    @Query(
        """
        SELECT p.sala.nome, COUNT(p)
        FROM Prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
        GROUP BY p.sala.nome
        ORDER BY COUNT(p) DESC
        """
    )
    List<Object[]> countPerSala(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Grafico 2: Ore più richieste ──────────────────────────
    /**
     * Ritorna [ [oraInizio, count], ... ] ordinato per ora.
     * FUNCTION('HOUR', ...) funziona su MySQL; per H2 in test usiamo HOUR().
     */
    @Query(
        value = """
        SELECT HOUR(p.ora_inizio) AS ora, COUNT(*) AS cnt
        FROM prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
        GROUP BY HOUR(p.ora_inizio)
        ORDER BY ora ASC
        """,
        nativeQuery = true
    )
    List<Object[]> countPerOra(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Grafico 3: Occupazione per mese ──────────────────────
    /**
     * Ritorna [ [anno, mese, countConfermate, countTotali], ... ].
     * Usiamo YEAR/MONTH (MySQL native) per raggruppare per mese.
     */
    @Query(
        value = """
        SELECT YEAR(p.data) AS anno, MONTH(p.data) AS mese,
               SUM(CASE WHEN s.codice = 'CONFIRMED' THEN 1 ELSE 0 END) AS confermate,
               COUNT(*) AS totali
        FROM prenotazioni p
        JOIN stati_prenotazione s ON p.stato_id = s.id
        WHERE p.data BETWEEN :dal AND :al
        GROUP BY YEAR(p.data), MONTH(p.data)
        ORDER BY anno ASC, mese ASC
        """,
        nativeQuery = true
    )
    List<Object[]> occupazionePerMese(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Grafico 4: Top 5 utenti più attivi ───────────────────
    @Query(
        """
        SELECT p.utente.nome, COUNT(p)
        FROM Prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
          AND p.utente IS NOT NULL
        GROUP BY p.utente.nome
        ORDER BY COUNT(p) DESC
        """
    )
    List<Object[]> top5Utenti(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Grafico 5: Prenotazioni per giorno della settimana ────
    /**
     * DAYOFWEEK MySQL: 1=Dom, 2=Lun, ..., 7=Sab
     */
    @Query(
        value = """
        SELECT DAYOFWEEK(p.data) AS giorno, COUNT(*) AS cnt
        FROM prenotazioni p
        WHERE p.data BETWEEN :dal AND :al
        GROUP BY DAYOFWEEK(p.data)
        ORDER BY giorno ASC
        """,
        nativeQuery = true
    )
    List<Object[]> countPerGiornoSettimana(@Param("dal") LocalDate dal, @Param("al") LocalDate al);

    // ── Tasso occupazione: numero slot disponibili per sala ───
    /**
     * Conta le sale attive (per calcolare il denominatore del tasso occupazione).
     */
    @Query("SELECT COUNT(s) FROM Sale s")
    long countSaleTotali();
}
