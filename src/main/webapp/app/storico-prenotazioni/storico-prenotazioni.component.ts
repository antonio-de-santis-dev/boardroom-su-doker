import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import SharedModule from 'app/shared/shared.module';
import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { PrenotazioniService } from 'app/entities/prenotazioni/service/prenotazioni.service';
import { faCalendar, faCalendarDays, faCalendarWeek, faUser, faTableColumns } from '@fortawesome/free-solid-svg-icons';
import dayjs from 'dayjs/esm';
import 'dayjs/esm/locale/it';

type FiltroModalita = 'tutte' | '30giorni' | 'annoCorrente' | 'singola' | 'range';

@Component({
  standalone: true,
  selector: 'jhi-storico-prenotazioni',
  templateUrl: './storico-prenotazioni.component.html',
  styleUrl: './storico-prenotazioni.component.scss',
  imports: [SharedModule, RouterModule, CommonModule, FormsModule],
})
export class StoricoPrenotazioniComponent implements OnInit {
  prenotazioniMostrate = signal<IPrenotazioni[]>([]);
  tutteLePrenotazioni: IPrenotazioni[] = [];
  filtroAttivo = signal<FiltroModalita>('tutte');
  isLoading = signal<boolean>(false);

  readonly annoCorrente = dayjs().year();
  readonly ieri = dayjs().subtract(1, 'day').format('YYYY-MM-DD');

  filtroDataSingola = '';
  filtroDataDal = '';
  filtroDataAl = '';

  faCalendar = faCalendar;
  faCalendarDays = faCalendarDays;
  faCalendarRange = faCalendarWeek;
  faUser = faUser;
  faTableColumns = faTableColumns;

  private prenotazioniService = inject(PrenotazioniService);

  gruppiMese = computed(() => {
    const mesi: { mese: string; items: IPrenotazioni[] }[] = [];
    for (const p of this.prenotazioniMostrate()) {
      const label = dayjs(p.data).locale('it').format('MMMM YYYY').toUpperCase();
      const existing = mesi.find(g => g.mese === label);
      if (existing) {
        existing.items.push(p);
      } else {
        mesi.push({ mese: label, items: [p] });
      }
    }
    return mesi;
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.prenotazioniService.getStorico().subscribe({
      next: res => {
        const dati = res.body ?? [];

        const ieridayjs = dayjs().subtract(1, 'day').endOf('day');
        this.tutteLePrenotazioni = dati
          .filter(p => (p.data && dayjs(p.data).isBefore(ieridayjs)) || dayjs(p.data).isSame(ieridayjs, 'day'))
          .sort((a, b) => dayjs(b.data).valueOf() - dayjs(a.data).valueOf());
        this.applicaFiltroCorrente();
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  applicaFiltro(tipo: FiltroModalita): void {
    this.filtroAttivo.set(tipo);
    if (tipo !== 'singola') this.filtroDataSingola = '';
    if (tipo !== 'range') {
      this.filtroDataDal = '';
      this.filtroDataAl = '';
    }
    this.applicaFiltroCorrente();
  }

  cercaPerData(): void {
    this.applicaFiltroCorrente();
  }

  resetFiltri(): void {
    this.filtroAttivo.set('tutte');
    this.filtroDataSingola = '';
    this.filtroDataDal = '';
    this.filtroDataAl = '';
    this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
  }

  private applicaFiltroCorrente(): void {
    const tipo = this.filtroAttivo();

    if (tipo === 'tutte') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni);
    } else if (tipo === '30giorni') {
      const dal = dayjs().subtract(30, 'days').startOf('day');
      const al = dayjs().subtract(1, 'day').endOf('day');
      this.prenotazioniMostrate.set(
        this.tutteLePrenotazioni.filter(p => p.data && !dayjs(p.data).isBefore(dal) && !dayjs(p.data).isAfter(al)),
      );
    } else if (tipo === 'annoCorrente') {
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).year() === this.annoCorrente));
    } else if (tipo === 'singola' && this.filtroDataSingola) {
      const target = dayjs(this.filtroDataSingola);
      this.prenotazioniMostrate.set(this.tutteLePrenotazioni.filter(p => p.data && dayjs(p.data).isSame(target, 'day')));
    } else if (tipo === 'range') {
      let r = [...this.tutteLePrenotazioni];
      if (this.filtroDataDal) {
        const dal = dayjs(this.filtroDataDal).startOf('day');
        r = r.filter(p => p.data && !dayjs(p.data).isBefore(dal));
      }
      if (this.filtroDataAl) {
        const al = dayjs(this.filtroDataAl).endOf('day');
        r = r.filter(p => p.data && !dayjs(p.data).isAfter(al));
      }
      this.prenotazioniMostrate.set(r);
    }
  }

  convertToDate(d: any): Date | null {
    return d ? dayjs(d).toDate() : null;
  }
}
