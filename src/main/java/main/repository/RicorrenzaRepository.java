package main.repository;

import java.util.List;
import java.util.UUID;
import main.domain.Ricorrenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RicorrenzaRepository extends JpaRepository<Ricorrenza, UUID> {
    /** Tutte le ricorrenze dell'utente autenticato */
    @Query(
        """
        SELECT r FROM Ricorrenza r
        LEFT JOIN FETCH r.sala
        WHERE r.utente.user.login = :login
        ORDER BY r.dataInizio DESC
        """
    )
    List<Ricorrenza> findByUtente_User_Login(@Param("login") String login);

    /** Tutte le ricorrenze (admin) */
    @Query(
        """
        SELECT r FROM Ricorrenza r
        LEFT JOIN FETCH r.sala
        LEFT JOIN FETCH r.utente
        ORDER BY r.dataInizio DESC
        """
    )
    List<Ricorrenza> findAllWithDetails();
}
