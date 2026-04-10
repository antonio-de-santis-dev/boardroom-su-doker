package main.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.domain.StatiPrenotazione;
import main.domain.enumeration.StatoCodice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prenotazioni entity.
 */
@Repository
public interface PrenotazioniRepository extends JpaRepository<Prenotazioni, UUID> {
    Page<Prenotazioni> findBySalaId(UUID salaId, Pageable pageable);

    default Optional<Prenotazioni> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default Page<Prenotazioni> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select prenotazioni from Prenotazioni prenotazioni left join fetch prenotazioni.stato left join fetch prenotazioni.utente left join fetch prenotazioni.sala",
        countQuery = "select count(prenotazioni) from Prenotazioni prenotazioni"
    )
    Page<Prenotazioni> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select prenotazioni from Prenotazioni prenotazioni left join fetch prenotazioni.stato left join fetch prenotazioni.utente left join fetch prenotazioni.sala where prenotazioni.id =:id"
    )
    Optional<Prenotazioni> findOneWithToOneRelationships(@Param("id") UUID id);

    @Query(
        "SELECT COUNT(p) > 0 FROM Prenotazioni p WHERE p.sala = :sala AND p.data = :data " +
        "AND ((p.oraInizio < :oraFine) AND (p.oraFine > :oraInizio)) " +
        "AND p.stato.codice = main.domain.enumeration.StatoCodice.CONFIRMED"
    )
    boolean existsOverlappingConfirmedPrenotazione(
        @Param("sala") Sale sala,
        @Param("data") LocalDate data,
        @Param("oraInizio") LocalTime oraInizio,
        @Param("oraFine") LocalTime oraFine
    );

    @Query(
        "SELECT COUNT(p) > 0 FROM Prenotazioni p WHERE p.sala = :sala AND p.data = :data " +
        "AND ((p.oraInizio < :oraFine) AND (p.oraFine > :oraInizio)) " +
        "AND p.stato.codice = main.domain.enumeration.StatoCodice.CONFIRMED " +
        "AND p.id <> :excludeId"
    )
    boolean existsOverlappingConfirmedExcluding(
        @Param("sala") Sale sala,
        @Param("data") LocalDate data,
        @Param("oraInizio") LocalTime oraInizio,
        @Param("oraFine") LocalTime oraFine,
        @Param("excludeId") UUID excludeId
    );

    @Query(
        """
        SELECT p FROM Prenotazioni p
        LEFT JOIN FETCH p.stato
        LEFT JOIN FETCH p.utente u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH p.sala
        WHERE p.data < :oggi
        ORDER BY p.data DESC, p.oraInizio DESC
        """
    )
    List<Prenotazioni> findStorico(@Param("oggi") LocalDate oggi);

    @Query(
        """
        SELECT p FROM Prenotazioni p
        LEFT JOIN FETCH p.stato
        LEFT JOIN FETCH p.utente u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH p.sala
        WHERE p.data < :oggi
        AND u.user.login = :login
        ORDER BY p.data DESC, p.oraInizio DESC
        """
    )
    List<Prenotazioni> findStoricoByLogin(@Param("login") String login, @Param("oggi") LocalDate oggi);

    @Query(
        """
        SELECT p FROM Prenotazioni p
        LEFT JOIN FETCH p.stato
        LEFT JOIN FETCH p.utente u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH p.sala
        LEFT JOIN FETCH p.evento e
        LEFT JOIN FETCH p.ricorrenza
        WHERE p.utente.user.login = :login
        AND p.data >= :oggi
        ORDER BY p.data ASC, p.oraInizio ASC
        """
    )
    List<Prenotazioni> findByUtente_User_LoginAndDataGreaterThanEqualOrderByDataAscOraInizioAsc(
        @Param("login") String login,
        @Param("oggi") LocalDate oggi
    );

    @Query(
        """
        SELECT p
        FROM Prenotazioni p
        WHERE p.stato.codice = :stato
          AND p.createdAt < :limite
        """
    )
    List<Prenotazioni> findExpiredWaiting(@Param("stato") StatoCodice stato, @Param("limite") LocalDateTime limite);

    @Modifying
    @Query(
        """
        UPDATE Prenotazioni p
        SET p.stato = :statoRejected
        WHERE p.stato.codice = main.domain.enumeration.StatoCodice.WAITING
          AND p.createdAt < :limite
        """
    )
    int aggiornaScadute(@Param("statoRejected") StatiPrenotazione statoRejected, @Param("limite") LocalDateTime limite);

    Optional<Prenotazioni> findByCodiceQr(String codiceQr);

    // ── CALENDARIO ────────────────────────────────────────────────────────────
    @Query(
        """
        SELECT p FROM Prenotazioni p
        LEFT JOIN FETCH p.stato
        LEFT JOIN FETCH p.utente u
        LEFT JOIN FETCH u.user
        LEFT JOIN FETCH p.sala
        WHERE p.data >= :dataInizio
          AND p.data <= :dataFine
        ORDER BY p.data ASC, p.oraInizio ASC
        """
    )
    List<Prenotazioni> findByDataBetween(@Param("dataInizio") LocalDate dataInizio, @Param("dataFine") LocalDate dataFine);

    // ── RICORRENZA ────────────────────────────────────────────────────────────

    /** Tutte le istanze di una serie */
    List<Prenotazioni> findByRicorrenzaId(UUID ricorrenzaId);

    /** Istanze future (da oggi incluso) di una serie — usato per cancellazione parziale */
    @Query(
        """
        SELECT p FROM Prenotazioni p
        WHERE p.ricorrenza.id = :ricorrenzaId
          AND p.data >= :dataInizio
        ORDER BY p.data ASC
        """
    )
    List<Prenotazioni> findByRicorrenzaIdAndDataGreaterThanEqual(
        @Param("ricorrenzaId") UUID ricorrenzaId,
        @Param("dataInizio") LocalDate dataInizio
    );

    // ── WAITLIST ──────────────────────────────────────────────────────────────

    /** Verifica se l'utente è già in waitlist per lo stesso slot/sala. */
    @Query(
        """
        SELECT COUNT(p) > 0 FROM Prenotazioni p
        WHERE p.sala = :sala
          AND p.data = :data
          AND p.oraInizio = :oraInizio
          AND p.oraFine = :oraFine
          AND p.utente = :utente
          AND p.stato.codice = 'WAITLISTED'
        """
    )
    boolean existsWaitlistPerStessoSlot(
        @org.springframework.data.repository.query.Param("sala") main.domain.Sale sala,
        @org.springframework.data.repository.query.Param("data") java.time.LocalDate data,
        @org.springframework.data.repository.query.Param("oraInizio") java.time.LocalTime oraInizio,
        @org.springframework.data.repository.query.Param("oraFine") java.time.LocalTime oraFine,
        @org.springframework.data.repository.query.Param("utente") main.domain.Utenti utente
    );

    /** Restituisce la posizione massima occupata nella waitlist per uno slot. */
    @Query(
        """
        SELECT MAX(p.posizioneWaitlist) FROM Prenotazioni p
        WHERE p.sala = :sala
          AND p.data = :data
          AND p.oraInizio = :oraInizio
          AND p.oraFine = :oraFine
          AND p.stato.codice = 'WAITLISTED'
        """
    )
    java.util.Optional<Integer> maxPosizioneWaitlist(
        @org.springframework.data.repository.query.Param("sala") main.domain.Sale sala,
        @org.springframework.data.repository.query.Param("data") java.time.LocalDate data,
        @org.springframework.data.repository.query.Param("oraInizio") java.time.LocalTime oraInizio,
        @org.springframework.data.repository.query.Param("oraFine") java.time.LocalTime oraFine
    );

    /** Restituisce la waitlist ordinata per posizione (FIFO) per uno slot. */
    @Query(
        """
        SELECT p FROM Prenotazioni p
        WHERE p.sala = :sala
          AND p.data = :data
          AND p.oraInizio = :oraInizio
          AND p.oraFine = :oraFine
          AND p.stato.codice = 'WAITLISTED'
        ORDER BY p.posizioneWaitlist ASC
        """
    )
    java.util.List<Prenotazioni> findWaitlistOrdinata(
        @org.springframework.data.repository.query.Param("sala") main.domain.Sale sala,
        @org.springframework.data.repository.query.Param("data") java.time.LocalDate data,
        @org.springframework.data.repository.query.Param("oraInizio") java.time.LocalTime oraInizio,
        @org.springframework.data.repository.query.Param("oraFine") java.time.LocalTime oraFine
    );

    /** Restituisce le prenotazioni PROMOTED con timer scaduto. */
    @Query(
        """
        SELECT p FROM Prenotazioni p
        WHERE p.stato.codice = 'PROMOTED'
          AND p.promossaAt <= :limite
        """
    )
    java.util.List<Prenotazioni> findPromotedScadute(
        @org.springframework.data.repository.query.Param("limite") java.time.LocalDateTime limite
    );
}
