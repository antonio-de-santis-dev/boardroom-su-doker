package main.service.dto;

import java.util.List;
import java.util.UUID;
import main.domain.enumeration.TipoPromemoria;

/**
 * DTO per la configurazione dei promemoria di una prenotazione.
 *
 * Payload salvataggio (POST/PUT):
 * {
 *   "prenotazioneId": "uuid",
 *   "tipiAbilitati": ["DAY_BEFORE", "SAME_DAY"]
 * }
 *
 * Response lettura (GET):
 * {
 *   "prenotazioneId": "uuid",
 *   "tipiAbilitati": ["DAY_BEFORE", "SAME_DAY"],
 *   "riepilogo": [
 *     { "tipo": "DAY_BEFORE", "etichetta": "1 giorno prima", "abilitato": true, "inviato": false },
 *     ...
 *   ]
 * }
 */
public class PromemoriaDTO {

    private UUID prenotazioneId;

    /** Lista dei tipi attualmente attivati dall'utente */
    private List<TipoPromemoria> tipiAbilitati;

    /** Riepilogo completo di tutti i tipi con stato (per il frontend) */
    private List<TipoStatoDTO> riepilogo;

    public static class TipoStatoDTO {

        private TipoPromemoria tipo;
        private String etichetta;
        private boolean abilitato;
        private boolean inviato;

        public TipoStatoDTO(TipoPromemoria tipo, boolean abilitato, boolean inviato) {
            this.tipo = tipo;
            this.etichetta = tipo.getEtichetta();
            this.abilitato = abilitato;
            this.inviato = inviato;
        }

        public TipoPromemoria getTipo() {
            return tipo;
        }

        public String getEtichetta() {
            return etichetta;
        }

        public boolean isAbilitato() {
            return abilitato;
        }

        public boolean isInviato() {
            return inviato;
        }
    }

    public UUID getPrenotazioneId() {
        return prenotazioneId;
    }

    public void setPrenotazioneId(UUID prenotazioneId) {
        this.prenotazioneId = prenotazioneId;
    }

    public List<TipoPromemoria> getTipiAbilitati() {
        return tipiAbilitati;
    }

    public void setTipiAbilitati(List<TipoPromemoria> tipiAbilitati) {
        this.tipiAbilitati = tipiAbilitati;
    }

    public List<TipoStatoDTO> getRiepilogo() {
        return riepilogo;
    }

    public void setRiepilogo(List<TipoStatoDTO> riepilogo) {
        this.riepilogo = riepilogo;
    }
}
