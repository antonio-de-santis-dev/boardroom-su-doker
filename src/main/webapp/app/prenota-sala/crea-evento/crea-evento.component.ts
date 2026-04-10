import { Component, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NotificationService } from 'app/shared/notification/notification.service';
import { EventiApiService } from 'app/services/eventi-api.service';
import { RicorrenzaService } from 'app/entities/ricorrenti/ricorrenza.service';
import { IRicorrenza, GiornoSettimana, GIORNI_OPTIONS, FREQUENZA_OPTIONS, Frequenza } from 'app/entities/ricorrenti/ricorrenza.model';
import { ISalaDTO } from 'app/services/sale-api.service';

const GIORNI_JS_TO_API: Record<number, GiornoSettimana> = {
  0: 'SUNDAY',
  1: 'MONDAY',
  2: 'TUESDAY',
  3: 'WEDNESDAY',
  4: 'THURSDAY',
  5: 'FRIDAY',
  6: 'SATURDAY',
};

export interface OpzioneConflitto {
  tipo: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala';
  data: string;
  oraInizio: string;
  oraFine: string;
  salaId: string;
  salaNome: string;
  etichetta: string;
}

export interface ConflittoData {
  data: string;
  opzioniGiorno: string[];
  opzioniOrario: { oraInizio: string; oraFine: string }[];
  opzioniSala: ISalaDTO[];
  giornoScelto: string | null;
  oraScelta: { oraInizio: string; oraFine: string } | null;
  salaScelta: ISalaDTO | null;
  tipoRisoluzione: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala' | null;
  caricamento: boolean;
}

@Component({
  selector: 'jhi-crea-evento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crea-evento.component.html',
  styleUrl: './crea-evento.component.scss',
})
export class CreaEventoComponent implements OnInit {
  isLoading = false;
  isPubblico = false;
  prenotazioneId = '';
  locandinaPreview: string | null = null;
  locandinaBase64: string | null = null;
  @ViewChild('locandinaInput') locandinaInput!: ElementRef<HTMLInputElement>;
  capienzaRicerca = 0;

  evento = {
    titolo: '',
    descrizione: '',
    prezzo: null as number | null,
    salaId: '',
    nomeSala: '',
    data: '',
    ora: '',
    oraInizio: '',
    oraFine: '',
  };

  mostraRicorrenza = false;
  showRicorrenzaModal = false;
  readonly frequenzaOptions = FREQUENZA_OPTIONS;
  readonly giorniOptions = GIORNI_OPTIONS;

  ricorrenza = {
    frequenza: 'WEEKLY' as Frequenza,
    giorniSelezionati: [] as GiornoSettimana[],
    tipoFine: 'mai' as 'mai' | 'data' | 'occorrenze',
    dataFine: '',
    numOccorrenze: null as number | null,
    oraInizio: '',
    oraFine: '',
  };

  fase: 'form' | 'verifica' | 'riepilogo' = 'form';
  conflitti = signal<ConflittoData[]>([]);
  dateLibere: string[] = [];

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private notificationService = inject(NotificationService);
  private eventiApi = inject(EventiApiService);
  private ricorrenzaService = inject(RicorrenzaService);

  readonly oggi = new Date().toISOString().split('T')[0];

  triggerLocandinaInput(): void {
    this.locandinaInput?.nativeElement?.click();
  }

  onLocandinaChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.elaboraLocandinaFile(file);
  }

  onLocandrinaDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file?.type.startsWith('image/')) this.elaboraLocandinaFile(file);
  }

  rimuoviLocandina(event: Event): void {
    event.stopPropagation();
    this.locandinaPreview = null;
    this.locandinaBase64 = null;
    if (this.locandinaInput?.nativeElement) this.locandinaInput.nativeElement.value = '';
  }

  private elaboraLocandinaFile(file: File): void {
    if (!file.type.startsWith('image/')) {
      this.notificationService.show('Formato non supportato. Usa JPG, PNG o WEBP.', 'error');
      return;
    }
    const reader = new FileReader();
    reader.onload = e => {
      const url = e.target?.result as string;
      const img = new Image();
      img.onload = () => {
        const MAX = 1200;
        let { width, height } = img;
        if (width > MAX || height > MAX) {
          if (width > height) {
            height = Math.round((height * MAX) / width);
            width = MAX;
          } else {
            width = Math.round((width * MAX) / height);
            height = MAX;
          }
        }
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d')!.drawImage(img, 0, 0, width, height);
        const compressed = canvas.toDataURL('image/jpeg', 0.82);
        this.locandinaPreview = compressed;
        this.locandinaBase64 = compressed;
      };
      img.src = url;
    };
    reader.readAsDataURL(file);
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.isPubblico = params['pubblico'] === 'true';
      this.evento.salaId = params['salaId'];
      this.evento.nomeSala = params['nomeSala'];
      this.evento.data = params['data'];
      this.evento.ora = params['ora'];
      this.prenotazioneId = params['prenotazioneId'];
      this.capienzaRicerca = params['numPersone'] ? Number(params['numPersone']) : 0;

      if (this.evento.ora) {
        const parts = this.evento.ora.split('-').map((s: string) => s.trim());
        this.evento.oraInizio = this.normalizzaOra(parts[0] ?? '');
        this.evento.oraFine = this.normalizzaOra(parts[1] ?? '');
      }

      this.ricorrenza.oraInizio = this.evento.oraInizio;
      this.ricorrenza.oraFine = this.evento.oraFine;

      if (this.evento.data) {
        const g = GIORNI_JS_TO_API[new Date(this.evento.data + 'T00:00:00').getDay()];
        if (g) this.ricorrenza.giorniSelezionati = [g];
      }

      if (!this.evento.salaId) this.router.navigate(['/prenota-sala']);
    });
  }

  get mostraGiorni(): boolean {
    return this.ricorrenza.frequenza !== 'MONTHLY';
  }

  toggleGiorno(g: GiornoSettimana): void {
    const idx = this.ricorrenza.giorniSelezionati.indexOf(g);
    this.ricorrenza.giorniSelezionati =
      idx >= 0 ? this.ricorrenza.giorniSelezionati.filter(x => x !== g) : [...this.ricorrenza.giorniSelezionati, g];
  }

  isGiornoSelezionato(g: GiornoSettimana): boolean {
    return this.ricorrenza.giorniSelezionati.includes(g);
  }

  onFrequenzaChange(): void {
    if (this.ricorrenza.frequenza === 'MONTHLY') this.ricorrenza.giorniSelezionati = [];
  }

  get ricorrenzaModalSummary(): string {
    return this.buildSummary();
  }

  get ricorrenzaSummary(): string {
    return this.mostraRicorrenza ? this.buildSummary() : '';
  }

  private buildSummary(): string {
    const freqLabel = this.frequenzaOptions.find(f => f.value === this.ricorrenza.frequenza)?.label ?? '';
    const giorni = this.ricorrenza.giorniSelezionati.map(g => this.giorniOptions.find(o => o.value === g)?.label ?? g).join(', ');
    const orario =
      this.ricorrenza.oraInizio && this.ricorrenza.oraFine ? `dalle ${this.ricorrenza.oraInizio} alle ${this.ricorrenza.oraFine}` : '';

    let fine = 'senza data di fine';
    if (this.ricorrenza.tipoFine === 'data' && this.ricorrenza.dataFine)
      fine = `fino al ${new Date(this.ricorrenza.dataFine + 'T00:00:00').toLocaleDateString('it-IT')}`;
    if (this.ricorrenza.tipoFine === 'occorrenze' && this.ricorrenza.numOccorrenze) fine = `per ${this.ricorrenza.numOccorrenze} volte`;

    const parti = [`${freqLabel}`];
    if (giorni) parti.push(`il ${giorni}`);
    if (orario) parti.push(orario);
    parti.push(fine);
    return parti.join(' ') + '.';
  }

  apriRicorrenzaModal(): void {
    this.showRicorrenzaModal = true;
  }

  chiudiRicorrenzaModal(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('ric-overlay')) {
      this.showRicorrenzaModal = false;
    }
  }

  confermaRicorrenza(): void {
    if (this.mostraGiorni && this.ricorrenza.giorniSelezionati.length === 0) {
      this.notificationService.show('Seleziona almeno un giorno della settimana', 'error');
      return;
    }
    if (this.ricorrenza.tipoFine === 'data' && !this.ricorrenza.dataFine) {
      this.notificationService.show('Inserisci la data di fine', 'error');
      return;
    }
    if (this.ricorrenza.tipoFine === 'occorrenze' && (!this.ricorrenza.numOccorrenze || this.ricorrenza.numOccorrenze < 1)) {
      this.notificationService.show('Inserisci il numero di ripetizioni', 'error');
      return;
    }
    this.mostraRicorrenza = true;
    this.showRicorrenzaModal = false;
  }

  conferma(): void {
    if (!this.evento.titolo) {
      this.notificationService.show("Inserire un titolo per l'evento", 'error');
      return;
    }
    if (this.isPubblico && (this.evento.prezzo == null || this.evento.prezzo < 0)) {
      this.notificationService.show('Inserire un prezzo valido (0 = gratuito)', 'error');
      return;
    }
    if (this.mostraRicorrenza) {
      this.verificaEProcedi();
    } else {
      this.isLoading = true;
      this.eventiApi
        .creaEvento({
          titolo: this.evento.titolo,
          descrizione: this.evento.descrizione,
          prezzo: this.isPubblico ? this.evento.prezzo : null,
          tipo: this.isPubblico ? 'PUBBLICO' : 'PRIVATO',
          prenotazioneId: this.prenotazioneId,
          locandinaUrl: this.isPubblico ? this.locandinaBase64 : null,
        })
        .subscribe({
          next: () => {
            this.isLoading = false;
            this.notificationService.show('Evento creato!', 'success');
            this.router.navigate(['/']);
          },
          error: () => {
            this.isLoading = false;
            this.notificationService.show('Errore creazione evento', 'error');
          },
        });
    }
  }

  annulla(): void {
    this.router.navigate(['/risultati-sala'], {
      queryParams: {
        data: this.evento.data,
        ora: this.evento.ora,
        numPersone: this.capienzaRicerca,
        capienza: this.capienzaRicerca,
      },
    });
  }

  async verificaEProcedi(): Promise<void> {
    this.fase = 'verifica';
    const dateCandidate = this.calcolaDate();
    const risultati: ConflittoData[] = [];
    this.dateLibere = [];

    for (const data of dateCandidate) {
      const libera = await this.verificaDisponibilita(data, this.ricorrenza.oraInizio, this.ricorrenza.oraFine, this.evento.salaId);
      if (libera) this.dateLibere.push(data);
      else risultati.push(await this.caricaOpzioniConflitto(data));
    }

    this.conflitti.set(risultati);
    if (risultati.length === 0) await this.creaRicorrenzaFinale();
    else this.fase = 'riepilogo';
  }

  private async caricaOpzioniConflitto(data: string): Promise<ConflittoData> {
    const [opzioniGiorno, opzioniOrario, opzioniSala] = await Promise.all([
      this.trovaTuttiGiorniLiberi(data, 5),
      this.trovaTutteFasceLibere(data),
      this.troveTutteleSaleDisponibili(data),
    ]);
    return {
      data,
      opzioniGiorno,
      opzioniOrario,
      opzioniSala,
      giornoScelto: null,
      oraScelta: null,
      salaScelta: null,
      tipoRisoluzione: null,
      caricamento: false,
    };
  }

  private async trovaTuttiGiorniLiberi(dataConflitto: string, max: number): Promise<string[]> {
    const risultati: string[] = [];
    const cursore = new Date(dataConflitto + 'T00:00:00');
    cursore.setDate(cursore.getDate() + 1);
    for (let i = 0; i < 14 && risultati.length < max; i++) {
      const dataStr = cursore.toISOString().split('T')[0];
      if (await this.verificaDisponibilita(dataStr, this.ricorrenza.oraInizio, this.ricorrenza.oraFine, this.evento.salaId))
        risultati.push(dataStr);
      cursore.setDate(cursore.getDate() + 1);
    }
    return risultati;
  }

  private async trovaTutteFasceLibere(data: string): Promise<{ oraInizio: string; oraFine: string }[]> {
    const durataOre = this.calcolaDurataOre();
    const risultati: { oraInizio: string; oraFine: string }[] = [];
    for (let h = 8; h <= 19 - durataOre; h++) {
      const oraI = `${String(h).padStart(2, '0')}:00`;
      const oraF = `${String(h + durataOre).padStart(2, '0')}:00`;
      if (oraI === this.ricorrenza.oraInizio) continue;
      if (await this.verificaDisponibilita(data, oraI, oraF, this.evento.salaId)) risultati.push({ oraInizio: oraI, oraFine: oraF });
    }
    return risultati;
  }

  private async troveTutteleSaleDisponibili(data: string): Promise<ISalaDTO[]> {
    try {
      const params = new HttpParams()
        .set('data', data)
        .set('inizio', this.ricorrenza.oraInizio)
        .set('fine', this.ricorrenza.oraFine)
        .set('capienza', this.capienzaRicerca.toString());
      const sale = await this.http.get<ISalaDTO[]>('/api/sales/disponibili', { params }).toPromise();
      return (sale ?? []).filter(s => s.id !== this.evento.salaId);
    } catch {
      return [];
    }
  }

  selezionaTipo(c: ConflittoData, tipo: 'cambio_giorno' | 'cambio_orario' | 'cambio_sala'): void {
    c.tipoRisoluzione = tipo;
    c.giornoScelto = null;
    c.oraScelta = null;
    c.salaScelta = null;
  }
  selezionaGiorno(c: ConflittoData, data: string): void {
    c.giornoScelto = c.giornoScelto === data ? null : data;
  }
  selezionaOrario(c: ConflittoData, slot: { oraInizio: string; oraFine: string }): void {
    c.oraScelta = c.oraScelta?.oraInizio === slot.oraInizio ? null : slot;
  }
  selezionaSala(c: ConflittoData, sala: ISalaDTO): void {
    c.salaScelta = c.salaScelta?.id === sala.id ? null : sala;
  }

  isConflittoRisolto(c: ConflittoData): boolean {
    if (!c.tipoRisoluzione) return false;
    if (c.tipoRisoluzione === 'cambio_giorno') return !!c.giornoScelto;
    if (c.tipoRisoluzione === 'cambio_orario') return !!c.oraScelta;
    if (c.tipoRisoluzione === 'cambio_sala') return !!c.salaScelta;
    return false;
  }
  saltaConflitto(c: ConflittoData): void {
    c.tipoRisoluzione = null;
    c.giornoScelto = null;
    c.oraScelta = null;
    c.salaScelta = null;
  }
  isConflittoSaltato(c: ConflittoData): boolean {
    return !c.tipoRisoluzione && !c.giornoScelto && !c.oraScelta && !c.salaScelta;
  }
  get conflittiRisoltiCount(): number {
    return this.conflitti().filter(c => this.isConflittoRisolto(c)).length;
  }
  get conflittiSaltatiCount(): number {
    return this.conflitti().filter(c => !this.isConflittoRisolto(c)).length;
  }
  get tuttiGestiti(): boolean {
    return this.conflitti().every(c => this.isConflittoRisolto(c) || this.isConflittoSaltato(c));
  }

  getRiepilogoConflitto(c: ConflittoData): string {
    if (!this.isConflittoRisolto(c)) return '';
    if (c.tipoRisoluzione === 'cambio_giorno' && c.giornoScelto)
      return `${this.formatData(c.giornoScelto)} · ${this.ricorrenza.oraInizio}–${this.ricorrenza.oraFine} · ${this.evento.nomeSala}`;
    if (c.tipoRisoluzione === 'cambio_orario' && c.oraScelta)
      return `${this.formatData(c.data)} · ${c.oraScelta.oraInizio}–${c.oraScelta.oraFine} · ${this.evento.nomeSala}`;
    if (c.tipoRisoluzione === 'cambio_sala' && c.salaScelta)
      return `${this.formatData(c.data)} · ${this.ricorrenza.oraInizio}–${this.ricorrenza.oraFine} · ${c.salaScelta.nome}`;
    return '';
  }

  async creaRicorrenzaFinale(): Promise<void> {
    this.isLoading = true;
    try {
      await this.eventiApi
        .creaEvento({
          titolo: this.evento.titolo,
          descrizione: this.evento.descrizione,
          prezzo: this.isPubblico ? this.evento.prezzo : null,
          tipo: this.isPubblico ? 'PUBBLICO' : 'PRIVATO',
          prenotazioneId: this.prenotazioneId,
          locandinaUrl: this.isPubblico ? this.locandinaBase64 : null,
        })
        .toPromise();
    } catch {
      this.isLoading = false;
      this.notificationService.show('Errore nella creazione evento', 'error');
      return;
    }

    if (!this.mostraRicorrenza) {
      this.isLoading = false;
      this.notificationService.show('Evento creato con successo!', 'success');
      this.router.navigate(['/']);
      return;
    }

    const dto: IRicorrenza = {
      salaId: this.evento.salaId,
      frequenza: this.ricorrenza.frequenza,
      giorniSettimana: this.mostraGiorni ? this.ricorrenza.giorniSelezionati : undefined,
      dataInizio: this.evento.data,
      dataFine: this.ricorrenza.tipoFine === 'data' ? this.ricorrenza.dataFine : undefined,
      numOccorrenze: this.ricorrenza.tipoFine === 'occorrenze' ? this.ricorrenza.numOccorrenze! : undefined,
      oraInizio: this.ricorrenza.oraInizio,
      oraFine: this.ricorrenza.oraFine,
      numPersone: this.capienzaRicerca,
      titoloEvento: this.evento.titolo || undefined,
      descrizioneEvento: this.evento.descrizione || undefined,
    };

    this.ricorrenzaService.crea(dto).subscribe({
      next: async res => {
        for (const c of this.conflitti().filter(c => this.isConflittoRisolto(c))) {
          await this.creaPrenotazioneConflittoRisolto(c);
        }
        this.isLoading = false;
        const saltati = this.conflitti().filter(c => !this.isConflittoRisolto(c)).length;
        this.notificationService.show(
          ` Serie creata: ${res.istanzeCreate} prenotazioni` + (saltati > 0 ? ` · ${saltati} saltati` : ''),
          'success',
        );
        this.router.navigate(['/mie-prenotazioni']);
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.show('Errore nella creazione della serie', 'error');
      },
    });
  }

  private async creaPrenotazioneConflittoRisolto(c: ConflittoData): Promise<void> {
    let data = c.data;
    let oraInizio = this.ricorrenza.oraInizio;
    let oraFine = this.ricorrenza.oraFine;
    let salaId = this.evento.salaId;
    if (c.tipoRisoluzione === 'cambio_giorno' && c.giornoScelto) data = c.giornoScelto;
    if (c.tipoRisoluzione === 'cambio_orario' && c.oraScelta) {
      oraInizio = c.oraScelta.oraInizio;
      oraFine = c.oraScelta.oraFine;
    }
    if (c.tipoRisoluzione === 'cambio_sala' && c.salaScelta) salaId = c.salaScelta.id;
    try {
      await this.http
        .post('/api/prenotazionis/prenotta', { data, oraInizio, oraFine, salaId, numPersone: this.capienzaRicerca })
        .toPromise();
    } catch {
      console.warn(`Prenotazione conflitto non creata per ${data}`);
    }
  }

  tornaAlForm(): void {
    this.fase = 'form';
    this.conflitti.set([]);
  }

  private calcolaDate(): string[] {
    const date: string[] = [];
    const inizio = new Date(this.evento.data + 'T00:00:00');
    let fine: Date | null = null;
    if (this.ricorrenza.tipoFine === 'data' && this.ricorrenza.dataFine) fine = new Date(this.ricorrenza.dataFine + 'T00:00:00');
    const maxOcc = this.ricorrenza.tipoFine === 'occorrenze' ? (this.ricorrenza.numOccorrenze ?? 1) : 365;
    const MAP: Record<string, number> = { SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6 };

    if (this.ricorrenza.frequenza === 'MONTHLY') {
      let cur = new Date(inizio);
      while (date.length < maxOcc && (!fine || cur <= fine)) {
        date.push(cur.toISOString().split('T')[0]);
        cur = new Date(cur);
        cur.setMonth(cur.getMonth() + 1);
      }
    } else {
      const step = this.ricorrenza.frequenza === 'BIWEEKLY' ? 14 : 7;
      const giorniTarget = this.ricorrenza.giorniSelezionati.map(g => MAP[g]).sort();
      const lunedi = new Date(inizio);
      const dow = lunedi.getDay() || 7;
      lunedi.setDate(lunedi.getDate() - (dow - 1));
      let settimana = new Date(lunedi);
      while (date.length < maxOcc && (!fine || settimana <= fine)) {
        for (const g of giorniTarget) {
          const d = new Date(settimana);
          d.setDate(settimana.getDate() + ((g + 6) % 7));
          if (d >= inizio && (!fine || d <= fine)) {
            date.push(d.toISOString().split('T')[0]);
            if (date.length >= maxOcc || date.length >= 365) break;
          }
        }
        settimana.setDate(settimana.getDate() + step);
        if (date.length >= 365) break;
      }
    }
    return date;
  }

  private async verificaDisponibilita(data: string, oraInizio: string, oraFine: string, salaId: string): Promise<boolean> {
    try {
      const params = new HttpParams()
        .set('data', data)
        .set('inizio', oraInizio)
        .set('fine', oraFine)
        .set('capienza', this.capienzaRicerca.toString());
      const sale = await this.http.get<ISalaDTO[]>('/api/sales/disponibili', { params }).toPromise();
      return (sale ?? []).some(s => s.id === salaId);
    } catch {
      return false;
    }
  }

  private calcolaDurataOre(): number {
    const [hI, mI] = this.ricorrenza.oraInizio.split(':').map(Number);
    const [hF, mF] = this.ricorrenza.oraFine.split(':').map(Number);
    return Math.max(1, Math.round((hF * 60 + mF - hI * 60 - mI) / 60));
  }

  private normalizzaOra(ora: string): string {
    if (!ora) return '00:00';
    const [h, m] = ora.split(':');
    return `${h.padStart(2, '0')}:${m ?? '00'}`;
  }

  formatData(data: string): string {
    if (!data) return '';
    return new Date(data + 'T00:00:00').toLocaleDateString('it-IT', { weekday: 'short', day: 'numeric', month: 'short' });
  }

  get stimaOccorrenze(): string {
    return this.buildSummary();
  }
}
