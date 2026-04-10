package main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Eventi;
import main.domain.enumeration.StatoCodice;
import main.domain.enumeration.TipoEvento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventiRepository extends JpaRepository<Eventi, UUID> {
    @Query(
        """
        SELECT e FROM Eventi e
        LEFT JOIN FETCH e.prenotazione p
        LEFT JOIN FETCH p.sala
        LEFT JOIN FETCH p.stato s
        WHERE e.tipo = :tipo
          AND (p IS NULL OR s.codice = :stato)
        """
    )
    List<Eventi> findPublicConfirmed(@Param("tipo") TipoEvento tipo, @Param("stato") StatoCodice stato);

    @Query(
        """
        SELECT e FROM Eventi e
        JOIN FETCH e.prenotazione p
        JOIN FETCH p.sala
        WHERE e.id = :id
        """
    )
    Optional<Eventi> findByIdWithPrenotazioneAndSala(UUID id);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Eventi e WHERE e.id = :id")
    Optional<Eventi> findByIdWithLock(@Param("id") UUID id);
}
