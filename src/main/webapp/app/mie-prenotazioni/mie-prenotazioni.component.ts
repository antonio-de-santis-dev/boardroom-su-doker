import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import SharedModule from 'app/shared/shared.module';
import { PrenotazioneDTO, PrenotazioniApiService } from '../services/prenotazioni-api.service';
import { NotificationService } from 'app/shared/notification/notification.service';
import { RicorrenzaService } from '../entities/ricorrenti/ricorrenza.service';
import { IRicorrenza } from '../entities/ricorrenti/ricorrenza.model';
import { PromemoriaService, PromemoriaDTO, TipoPromemoria, TipoStatoDTO } from '../services/promemoria.service';

type FiltroType = 'tutte' | 'settimana' | 'mese' | 'confermate' | 'attesa';

interface GruppoPrenotazioni {
  stato: string;
  items: PrenotazioneDTO[];
}

@Component({
  standalone: true,
  selector: 'jhi-mie-prenotazioni',
  templateUrl: './mie-prenotazioni.component.html',
  styleUrl: './mie-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class MiePrenotazioniComponent implements OnInit {
  prenotazioni = signal<PrenotazioneDTO[]>([]);
  expandedRows = signal<Set<string>>(new Set());
  showDeleteModal = signal(false);
  prenotazioneIdDaEliminare = signal<string | undefined>(undefined);

  ricorrenze = signal<IRicorrenza[]>([]);
  showCancellaSerieModal = signal(false);
  ricorrenzaIdDaCancellare = signal<string | undefined>(undefined);
  cancellaSoloFuture = signal(true);

  showPromemoriaModal = signal(false);
  proPrenotazioneId = signal<string | undefined>(undefined);
  proRiepilogo = signal<TipoStatoDTO[]>([]);
  proSelezionati = signal<Set<TipoPromemoria>>(new Set());
  proLoading = signal(false);
  proSaving = signal(false);

  filtroAttivo = signal<FiltroType>('tutte');

  readonly proOpzioni: { tipo: TipoPromemoria; label: string; desc: string }[] = [
    { tipo: 'WEEK_BEFORE', label: '1 settimana prima', desc: "7 giorni prima dell'evento" },
    { tipo: 'TWO_DAYS_BEFORE', label: '2 giorni prima', desc: "48 ore prima dell'evento" },
    { tipo: 'DAY_BEFORE', label: '1 giorno prima', desc: 'La sera precedente' },
    { tipo: 'SAME_DAY', label: 'Il giorno stesso', desc: "La mattina dell'evento alle 08:00" },
  ];

  private prenotazioniApi = inject(PrenotazioniApiService);
  private notificationService = inject(NotificationService);
  private ricorrenzaService = inject(RicorrenzaService);
  private proService = inject(PromemoriaService);

  ngOnInit(): void {
    this.caricaLeMiePrenotazioni();
    this.caricaRicorrenze();
  }

  caricaLeMiePrenotazioni(): void {
    this.prenotazioniApi.getMiePrenotazioni().subscribe({
      next: (res: PrenotazioneDTO[]) => this.prenotazioni.set(res),
      error: () => this.notificationService.show('Errore nel caricamento delle prenotazioni', 'error'),
    });
  }

  setFiltro(filtro: FiltroType): void {
    this.filtroAttivo.set(filtro);
  }

  gruppiPrenotazioni = computed<GruppoPrenotazioni[]>(() => {
    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    const filtro = this.filtroAttivo();

    const filtrate = this.prenotazioni().filter(pren => {
      const dataPren = pren.data ? new Date(pren.data) : null;

      switch (filtro) {
        case 'settimana': {
          if (!dataPren) return false;
          const fine = new Date(oggi);
          fine.setDate(fine.getDate() + 7);
          return dataPren >= oggi && dataPren <= fine;
        }
        case 'mese': {
          if (!dataPren) return false;
          const fine = new Date(oggi);
          fine.setMonth(fine.getMonth() + 1);
          return dataPren >= oggi && dataPren <= fine;
        }
        case 'confermate':
          return pren.stato?.codice === 'CONFIRMED' || pren.stato?.codice?.toLowerCase() === 'confermata';
        case 'attesa':
          return pren.stato?.codice === 'WAITING' || pren.stato?.codice?.toLowerCase().includes('attesa');
        default:
          return true;
      }
    });

    const mappaGruppi = new Map<string, PrenotazioneDTO[]>();

    for (const pren of filtrate) {
      const statoLabel = this.getStatoLabel(pren);
      if (!mappaGruppi.has(statoLabel)) {
        mappaGruppi.set(statoLabel, []);
      }
      mappaGruppi.get(statoLabel)!.push(pren);
    }

    return Array.from(mappaGruppi.entries()).map(([stato, items]) => ({ stato, items }));
  });

  private getStatoLabel(pren: PrenotazioneDTO): string {
    const codice = pren.stato?.codice?.toUpperCase() ?? '';
    const mappa: Record<string, string> = {
      CONFIRMED: 'Confermate',
      CONFERMATA: 'Confermate',
      CANCELLED: 'Cancellate',
      CANCELLATA: 'Cancellate',
      ANNULLATA: 'Cancellate',
      REJECTED: 'Rifiutate',
      RIFIUTATA: 'Rifiutate',
      WAITING: 'In attesa',
    };
    return mappa[codice] ?? pren.stato?.descrizione ?? 'Altre';
  }

  getTitolo(pren: PrenotazioneDTO): string {
    if (pren.evento?.titolo) return pren.evento.titolo;
    if (pren.ricorrenzaId) return `${pren.sala?.nome ?? 'Sala'} — Ricorrente`;
    return pren.sala?.nome ?? 'Prenotazione';
  }

  isCancellata(pren: PrenotazioneDTO): boolean {
    return pren.stato?.codice === 'CANCELLED';
  }

  isRicorrente(pren: PrenotazioneDTO): boolean {
    return !!pren.ricorrenzaId;
  }

  getStatusAttr(pren: PrenotazioneDTO): string {
    return pren.stato?.codice?.toLowerCase() ?? '';
  }

  getFrequenzaLabel(f: string): string {
    const map: Record<string, string> = {
      WEEKLY: 'Settimanale',
      BIWEEKLY: 'Bisettimanale',
      MONTHLY: 'Mensile',
    };
    return map[f] ?? f;
  }

  getGiorniLabel(giorni: string[] | undefined): string {
    if (!giorni?.length) return '';
    const nomi: Record<string, string> = {
      MONDAY: 'Lun',
      TUESDAY: 'Mar',
      WEDNESDAY: 'Mer',
      THURSDAY: 'Gio',
      FRIDAY: 'Ven',
      SATURDAY: 'Sab',
      SUNDAY: 'Dom',
    };
    return giorni.map(g => nomi[g] ?? g).join(' · ');
  }

  apriPromemoria(prenotazioneId: string | undefined): void {
    if (!prenotazioneId) return;
    this.proPrenotazioneId.set(prenotazioneId);
    this.proLoading.set(true);
    this.showPromemoriaModal.set(true);

    this.proService.get(prenotazioneId).subscribe({
      next: (dto: PromemoriaDTO) => {
        this.proRiepilogo.set(dto.riepilogo ?? []);
        this.proSelezionati.set(new Set(dto.tipiAbilitati ?? []));
        this.proLoading.set(false);
      },
      error: () => {
        this.proLoading.set(false);
        this.proRiepilogo.set([]);
        this.proSelezionati.set(new Set());
      },
    });
  }

  chiudiPromemoria(): void {
    this.showPromemoriaModal.set(false);
    this.proPrenotazioneId.set(undefined);
  }

  toggleTipo(tipo: TipoPromemoria): void {
    const set = new Set(this.proSelezionati());
    if (set.has(tipo)) {
      set.delete(tipo);
    } else {
      set.add(tipo);
    }
    this.proSelezionati.set(set);
  }

  isTipoSelezionato(tipo: TipoPromemoria): boolean {
    return this.proSelezionati().has(tipo);
  }

  isTipoInviato(tipo: TipoPromemoria): boolean {
    return this.proRiepilogo().some(r => r.tipo === tipo && r.inviato);
  }

  salvaPromemoria(): void {
    const id = this.proPrenotazioneId();
    if (!id) return;

    this.proSaving.set(true);
    this.proService
      .salva({
        prenotazioneId: id,
        tipiAbilitati: Array.from(this.proSelezionati()),
      })
      .subscribe({
        next: () => {
          this.proSaving.set(false);
          this.chiudiPromemoria();
          const count = this.proSelezionati().size;
          this.notificationService.show(
            count > 0 ? `Promemoria salvati: riceverai ${count} notifica${count > 1 ? 'he' : ''}` : 'Promemoria disattivati',
            'success',
          );
        },
        error: () => {
          this.proSaving.set(false);
          this.notificationService.show('Errore nel salvataggio dei promemoria', 'error');
        },
      });
  }

  hasPromemoriaAttivo(prenotazioneId: string | undefined): boolean {
    return false;
  }

  caricaRicorrenze(): void {
    this.ricorrenzaService.getMie().subscribe({
      next: list => this.ricorrenze.set(list),
      error: () => {},
    });
  }

  get ricorrenzeAttive(): IRicorrenza[] {
    return this.ricorrenze().filter(r => !this.isSerieInteramenteCancellata(r.id));
  }

  isSerieInteramenteCancellata(ricorrenzaId: string | undefined): boolean {
    if (!ricorrenzaId) return false;
    const prenotazioniSerie = this.prenotazioni().filter(p => p.ricorrenzaId === ricorrenzaId);
    if (prenotazioniSerie.length === 0) return false;
    return prenotazioniSerie.every(p => p.stato?.codice === 'CANCELLED');
  }

  chiediCancellazioneSerie(id: string | undefined, soloFuture: boolean): void {
    if (!id) return;
    this.ricorrenzaIdDaCancellare.set(id);
    this.cancellaSoloFuture.set(soloFuture);
    this.showCancellaSerieModal.set(true);
  }

  annullaCancellazioneSerie(): void {
    this.showCancellaSerieModal.set(false);
    this.ricorrenzaIdDaCancellare.set(undefined);
  }

  confermaCancellazioneSerie(): void {
    const id = this.ricorrenzaIdDaCancellare();
    if (!id) return;
    this.showCancellaSerieModal.set(false);

    const op$ = this.cancellaSoloFuture() ? this.ricorrenzaService.cancellaFuture(id) : this.ricorrenzaService.cancellaTutta(id);

    op$.subscribe({
      next: () => {
        this.notificationService.show('Serie cancellata con successo', 'success');
        this.caricaLeMiePrenotazioni();
        this.caricaRicorrenze();
      },
      error: () => this.notificationService.show('Errore nella cancellazione della serie', 'error'),
    });
    this.ricorrenzaIdDaCancellare.set(undefined);
  }

  toggleDetails(id: string | undefined): void {
    if (!id) return;
    const newSet = new Set(this.expandedRows());
    newSet.has(id) ? newSet.delete(id) : newSet.add(id);
    this.expandedRows.set(newSet);
  }

  isExpanded(id: string | undefined): boolean {
    return !!id && this.expandedRows().has(id);
  }

  chiediConfermaEliminazione(id: string | undefined): void {
    if (!id) return;
    this.prenotazioneIdDaEliminare.set(id);
    this.showDeleteModal.set(true);
  }

  annullaEliminazione(): void {
    this.showDeleteModal.set(false);
    this.prenotazioneIdDaEliminare.set(undefined);
  }

  confermaEliminazione(): void {
    const id = this.prenotazioneIdDaEliminare();
    if (!id) return;
    this.showDeleteModal.set(false);

    this.prenotazioniApi.cancellaPrenotazione(id).subscribe({
      next: () => {
        this.notificationService.show('Prenotazione eliminata con successo', 'success');
        this.caricaLeMiePrenotazioni();
      },
      error: () => this.notificationService.show("Errore durante l'eliminazione", 'error'),
    });
    this.prenotazioneIdDaEliminare.set(undefined);
  }
}
