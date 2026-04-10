package main.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Sale;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sale s WHERE s.id = :id")
    Optional<Sale> findByIdWithLock(@Param("id") UUID id);

    @Query(
        """
        SELECT s
        FROM Sale s
        LEFT JOIN Prenotazioni p
          ON p.sala = s
         AND p.data = :data
         AND p.oraInizio < :fine
         AND p.oraFine > :inizio
         AND p.stato.codice IN (
               main.domain.enumeration.StatoCodice.CONFIRMED,
               main.domain.enumeration.StatoCodice.WAITING
             )
        WHERE p.id IS NULL
          AND (:capienza IS NULL OR s.capienza >= :capienza)
        ORDER BY s.capienza DESC
        """
    )
    List<Sale> findFreeSales(
        @Param("data") LocalDate data,
        @Param("inizio") LocalTime inizio,
        @Param("fine") LocalTime fine,
        @Param("capienza") Integer capienza
    );

    @Cacheable("sale-list")
    @Query("SELECT s FROM Sale s ORDER BY s.nome ASC")
    List<Sale> findAllCached();
}
