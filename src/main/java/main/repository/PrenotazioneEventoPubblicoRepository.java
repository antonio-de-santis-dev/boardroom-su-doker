package main.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.PrenotazioneEventoPubblico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrenotazioneEventoPubblicoRepository extends JpaRepository<PrenotazioneEventoPubblico, UUID> {
    /**
     * Conta il numero di posti già prenotati per un evento pubblico.
     */
    @Query("SELECT COUNT(p) FROM PrenotazioneEventoPubblico p WHERE p.evento.id = :eventoId")
    long countByEventoId(@Param("eventoId") UUID eventoId);

    /**
     * Conta i posti occupati per una lista di eventi in una sola query.
     * Ritorna una mappa eventoId -> count, evitando il problema N+1.
     */
    @Query("SELECT p.evento.id, COUNT(p) FROM PrenotazioneEventoPubblico p " + "WHERE p.evento.id IN :eventoIds GROUP BY p.evento.id")
    List<Object[]> countByEventoIdIn(@Param("eventoIds") List<UUID> eventoIds);

    /**
     * Converte il risultato di countByEventoIdIn in una mappa eventoId -> count.
     */
    default Map<UUID, Long> conteggioPerEventi(List<UUID> eventoIds) {
        if (eventoIds == null || eventoIds.isEmpty()) {
            return Map.of();
        }
        return countByEventoIdIn(eventoIds).stream().collect(Collectors.toMap(r -> (UUID) r[0], r -> (Long) r[1]));
    }

    /**
     * Restituisce la sequenza del prossimo partecipante (1, 2, 3...).
     * Usata per generare il codice persona: A01, A02, ecc.
     */
    default long nextSequenzaPerEvento(UUID eventoId) {
        return countByEventoId(eventoId) + 1;
    }
}
