import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import dayjs from 'dayjs/esm';
import isoWeek from 'dayjs/esm/plugin/isoWeek';
import 'dayjs/esm/locale/it';

import { AccountService } from 'app/core/auth/account.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IPrenotazioni } from '../prenotazioni.model';
import { ISale } from 'app/entities/sale/sale.model';
import { SaleService } from 'app/entities/sale/service/sale.service';
import { StatoCodice } from 'app/entities/enumerations/stato-codice.model';
import { PrenotazioniCalendarDetailComponent } from './prenotazioni-calendar-detail.component';

dayjs.extend(isoWeek);
dayjs.locale('it');

export interface CalendarDay {
  date: dayjs.Dayjs;
  isCurrentMonth: boolean;
  isToday: boolean;
  prenotazioni: IPrenotazioni[];
}

const SALA_COLORS = [
  { bg: '#3B82F6', light: '#EFF6FF', text: '#1D4ED8' },
  { bg: '#8B5CF6', light: '#F5F3FF', text: '#6D28D9' },
  { bg: '#EC4899', light: '#FDF2F8', text: '#BE185D' },
  { bg: '#F59E0B', light: '#FFFBEB', text: '#B45309' },
  { bg: '#10B981', light: '#ECFDF5', text: '#065F46' },
  { bg: '#EF4444', light: '#FEF2F2', text: '#B91C1C' },
  { bg: '#06B6D4', light: '#ECFEFF', text: '#0E7490' },
  { bg: '#84CC16', light: '#F7FEE7', text: '#3F6212' },
];

const STATO_COLORS: Record<string, { bg: string; light: string; text: string }> = {
  [StatoCodice.CONFIRMED]: { bg: '#10B981', light: '#ECFDF5', text: '#065F46' },
  [StatoCodice.WAITING]: { bg: '#F59E0B', light: '#FFFBEB', text: '#B45309' },
  [StatoCodice.REJECTED]: { bg: '#EF4444', light: '#FEF2F2', text: '#B91C1C' },
  [StatoCodice.CANCELLED]: { bg: '#6B7280', light: '#F9FAFB', text: '#374151' },
};

@Component({
  selector: 'jhi-prenotazioni-calendar',
  templateUrl: './prenotazioni-calendar.component.html',
  styleUrls: ['./prenotazioni-calendar.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
})
export class PrenotazioniCalendarComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);
  private saleService = inject(SaleService);
  private accountService = inject(AccountService);
  private modalService = inject(NgbModal);

  private readonly calendarioUrl = this.appConfig.getEndpointFor('api/prenotazionis/calendario');

  currentMonth = signal<dayjs.Dayjs>(dayjs().startOf('month'));
  prenotazioni = signal<IPrenotazioni[]>([]);
  sale = signal<ISale[]>([]);
  isLoading = signal(false);
  isAdmin = signal(false);

  selectedSalaId = signal<string | null>(null);
  selectedStato = signal<string | null>(null);
  colorMode = signal<'sala' | 'stato'>('sala');

  private salaColorMap = new Map<string, number>();

  private subscription: Subscription | null = null;

  readonly weekDays = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'];
  readonly statiOptions = Object.values(StatoCodice);

  readonly calendarDays = computed<CalendarDay[]>(() => {
    const month = this.currentMonth();
    const startOfMonth = month.startOf('month');
    const endOfMonth = month.endOf('month');

    const startCell = startOfMonth.isoWeekday() === 1 ? startOfMonth : startOfMonth.subtract(startOfMonth.isoWeekday() - 1, 'day');

    const endCell = endOfMonth.isoWeekday() === 7 ? endOfMonth : endOfMonth.add(7 - endOfMonth.isoWeekday(), 'day');

    const days: CalendarDay[] = [];
    let current = startCell;
    const today = dayjs().startOf('day');

    while (current.isBefore(endCell) || current.isSame(endCell, 'day')) {
      const dayDate = current;
      days.push({
        date: dayDate,
        isCurrentMonth: dayDate.month() === month.month(),
        isToday: dayDate.isSame(today, 'day'),
        prenotazioni: this.getPrenotazioniForDay(dayDate),
      });
      current = current.add(1, 'day');
    }
    return days;
  });

  readonly monthLabel = computed(() => this.currentMonth().format('MMMM YYYY'));

  readonly monthName = computed(
    () => this.currentMonth().format('MMMM'), // es: "marzo"
  );

  readonly monthYear = computed(() => this.currentMonth().format('YYYY'));

  readonly filteredPrenotazioni = computed(() => {
    let list = this.prenotazioni();
    const sala = this.selectedSalaId();
    const stato = this.selectedStato();

    list = list.filter(p => p.stato?.codice !== 'CANCELLED');
    if (sala) list = list.filter(p => p.sala?.id === sala);
    if (stato) list = list.filter(p => p.stato?.codice === stato);
    return list;
  });

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.isAdmin.set(account?.authorities?.includes('ROLE_ADMIN') ?? false);
    });
    this.loadSale();
    this.loadPrenotazioni();
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  prevMonth(): void {
    this.currentMonth.update(m => m.subtract(1, 'month'));
    this.loadPrenotazioni();
  }

  nextMonth(): void {
    this.currentMonth.update(m => m.add(1, 'month'));
    this.loadPrenotazioni();
  }

  goToToday(): void {
    this.currentMonth.set(dayjs().startOf('month'));
    this.loadPrenotazioni();
  }

  loadSale(): void {
    this.saleService.query({ size: 100 }).subscribe(res => {
      const saleList = res.body ?? [];
      this.sale.set(saleList);
      saleList.forEach((s, i) => {
        if (s.id) this.salaColorMap.set(s.id, i % SALA_COLORS.length);
      });
    });
  }

  loadPrenotazioni(): void {
    this.isLoading.set(true);
    const month = this.currentMonth();
    const dataInizio = month.startOf('month').format('YYYY-MM-DD');
    const dataFine = month.endOf('month').format('YYYY-MM-DD');

    this.http
      .get<IPrenotazioni[]>(this.calendarioUrl, {
        params: { dataInizio, dataFine },
      })
      .subscribe({
        next: data => {
          const converted = data.map(p => ({
            ...p,
            data: p.data ? dayjs(p.data as any) : null,
          }));
          this.prenotazioni.set(converted as IPrenotazioni[]);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  getPrenotazioniForDay(day: dayjs.Dayjs): IPrenotazioni[] {
    return this.filteredPrenotazioni().filter(p => p.data && dayjs(p.data).isSame(day, 'day'));
  }

  getEventColor(prenotazione: IPrenotazioni): { bg: string; light: string; text: string } {
    if (this.colorMode() === 'stato') {
      const codice = prenotazione.stato?.codice ?? '';
      return STATO_COLORS[codice] ?? STATO_COLORS[StatoCodice.WAITING];
    }
    const salaId = prenotazione.sala?.id;
    if (salaId && this.salaColorMap.has(salaId)) {
      return SALA_COLORS[this.salaColorMap.get(salaId)!];
    }
    return SALA_COLORS[0];
  }

  openDetail(prenotazione: IPrenotazioni, event: Event): void {
    event.stopPropagation();
    const modalRef = this.modalService.open(PrenotazioniCalendarDetailComponent, {
      size: 'md',
      centered: true,
    });
    modalRef.componentInstance.prenotazione = prenotazione;
    modalRef.componentInstance.color = this.getEventColor(prenotazione);
  }

  trackDay = (_: number, day: CalendarDay) => day.date.valueOf();
  trackPrenotazione = (_: number, p: IPrenotazioni) => p.id;

  getStato(p: IPrenotazioni): string {
    return p.stato?.codice ?? '';
  }

  onSalaChange(id: string): void {
    this.selectedSalaId.update(v => (v === id ? null : id));
  }

  onStatoChange(stato: string): void {
    this.selectedStato.update(v => (v === stato ? null : stato));
  }

  clearFilters(): void {
    this.selectedSalaId.set(null);
    this.selectedStato.set(null);
  }

  getLegendColorBySala(salaId: string | null | undefined): { bg: string; light: string; text: string } {
    if (salaId && this.salaColorMap.has(salaId)) {
      return SALA_COLORS[this.salaColorMap.get(salaId)!];
    }
    return SALA_COLORS[0];
  }

  getLegendColorByStato(codice: string): { bg: string; light: string; text: string } {
    return STATO_COLORS[codice] ?? STATO_COLORS[StatoCodice.WAITING];
  }

  getStatoLabel(codice: string): string {
    const labels: Record<string, string> = {
      CONFIRMED: 'Confermata',
      WAITING: 'In attesa',
      REJECTED: 'Rifiutata',
      CANCELLED: 'Cancellata',
    };
    return labels[codice] ?? codice;
  }
}
