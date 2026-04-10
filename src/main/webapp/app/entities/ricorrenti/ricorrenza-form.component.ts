import { Component, OnInit, inject, signal, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { NotificationService } from 'app/shared/notification/notification.service';
import { RicorrenzaService } from './ricorrenza.service';
import { IRicorrenza, Frequenza, GiornoSettimana, GIORNI_OPTIONS, FREQUENZA_OPTIONS } from './ricorrenza.model';
import { ISale } from 'app/entities/sale/sale.model';

@Component({
  selector: 'jhi-ricorrenza-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ricorrenza-form.component.html',
  styleUrls: ['./ricorrenza-form.component.scss'],
})
export class RicorrenzaFormComponent implements OnInit {
  /** Emesso dopo creazione riuscita con il risultato */
  creata = output<IRicorrenza>();
  /** Emesso se l'utente chiude/annulla il form */
  annullata = output<void>();

  private ricorrenzaService = inject(RicorrenzaService);
  private appConfig = inject(ApplicationConfigService);
  private http = inject(HttpClient);
  private notify = inject(NotificationService);

  // ── Stato form ────────────────────────────────────────
  sale = signal<ISale[]>([]);
  isLoading = signal(false);
  risultato = signal<IRicorrenza | null>(null);

  // Opzioni
  readonly frequenzaOptions = FREQUENZA_OPTIONS;
  readonly giorniOptions = GIORNI_OPTIONS;

  // Valori form
  salaId = '';
  frequenza: Frequenza = 'WEEKLY';
  giorniSelezionati: GiornoSettimana[] = [];
  dataInizio = '';
  usaDataFine = true; // true = dataFine, false = numOccorrenze
  dataFine = '';
  numOccorrenze: number | null = null;
  oraInizio = '09:00';
  oraFine = '10:00';
  numPersone: number | null = null;

  // Stato UI
  mostraGiorni = true; // false per MONTHLY

  readonly oggi = new Date().toISOString().split('T')[0];

  ngOnInit(): void {
    this.http.get<ISale[]>(this.appConfig.getEndpointFor('api/sales')).subscribe(list => this.sale.set(list));
  }

  onFrequenzaChange(): void {
    this.mostraGiorni = this.frequenza !== 'MONTHLY';
    if (!this.mostraGiorni) this.giorniSelezionati = [];
  }

  toggleGiorno(giorno: GiornoSettimana): void {
    const idx = this.giorniSelezionati.indexOf(giorno);
    if (idx >= 0) {
      this.giorniSelezionati = this.giorniSelezionati.filter(g => g !== giorno);
    } else {
      this.giorniSelezionati = [...this.giorniSelezionati, giorno];
    }
  }

  isGiornoSelezionato(giorno: GiornoSettimana): boolean {
    return this.giorniSelezionati.includes(giorno);
  }

  /** Anteprima: quante occorrenze verranno generate (stima) */
  get stimaOccorrenze(): string {
    if (!this.dataInizio) return '';
    if (this.usaDataFine && this.dataFine) {
      const inizio = new Date(this.dataInizio);
      const fine = new Date(this.dataFine);
      const giorni = Math.ceil((fine.getTime() - inizio.getTime()) / (1000 * 60 * 60 * 24));
      let stima = 0;
      if (this.frequenza === 'WEEKLY') stima = Math.ceil(giorni / 7) * this.giorniSelezionati.length;
      if (this.frequenza === 'BIWEEKLY') stima = Math.ceil(giorni / 14) * this.giorniSelezionati.length;
      if (this.frequenza === 'MONTHLY') stima = Math.ceil(giorni / 30);
      return `~${stima} prenotazioni`;
    }
    if (!this.usaDataFine && this.numOccorrenze) {
      return `${this.numOccorrenze} prenotazioni`;
    }
    return '';
  }

  invia(): void {
    if (!this.valida()) return;

    const dto: IRicorrenza = {
      salaId: this.salaId,
      frequenza: this.frequenza,
      giorniSettimana: this.mostraGiorni ? this.giorniSelezionati : undefined,
      dataInizio: this.dataInizio,
      dataFine: this.usaDataFine ? this.dataFine : undefined,
      numOccorrenze: !this.usaDataFine ? this.numOccorrenze! : undefined,
      oraInizio: this.oraInizio,
      oraFine: this.oraFine,
      numPersone: this.numPersone ?? undefined,
    };

    this.isLoading.set(true);
    this.ricorrenzaService.crea(dto).subscribe({
      next: res => {
        this.isLoading.set(false);
        this.risultato.set(res);
        if (res.istanzeCreate && res.istanzeCreate > 0) {
          this.notify.show(
            `✅ ${res.istanzeCreate} prenotazioni create` +
              (res.istanzeConflitto ? ` · ${res.istanzeConflitto} con conflitti (saltate)` : ''),
            'success',
          );
        }
        this.creata.emit(res);
      },
      error: () => {
        this.isLoading.set(false);
        this.notify.show('Errore nella creazione della serie ricorrente', 'error');
      },
    });
  }

  annulla(): void {
    this.annullata.emit();
  }

  private valida(): boolean {
    if (!this.salaId) {
      this.notify.show('Seleziona una sala', 'error');
      return false;
    }
    if (!this.dataInizio) {
      this.notify.show('Inserisci la data di inizio', 'error');
      return false;
    }
    if (this.usaDataFine && !this.dataFine) {
      this.notify.show('Inserisci la data di fine', 'error');
      return false;
    }
    if (!this.usaDataFine && (!this.numOccorrenze || this.numOccorrenze < 1)) {
      this.notify.show('Inserisci il numero di occorrenze', 'error');
      return false;
    }
    if (this.mostraGiorni && this.giorniSelezionati.length === 0) {
      this.notify.show('Seleziona almeno un giorno della settimana', 'error');
      return false;
    }
    if (this.oraInizio >= this.oraFine) {
      this.notify.show("L'ora di inizio deve essere prima dell'ora di fine", 'error');
      return false;
    }
    return true;
  }
}
