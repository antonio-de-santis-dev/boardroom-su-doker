import { Component, OnInit, OnDestroy, AfterViewInit, inject, signal, ElementRef, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { StatsDashboardService, StatsDashboard, SalaAdmin, Periodo } from './stats-dashboard.service';
import { NotificationService } from 'app/shared/notification/notification.service';

declare const Chart: any;

@Component({
  selector: 'jhi-stats-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stats-dashboard.component.html',
  styleUrl: './stats-dashboard.component.scss',
})
export default class StatsDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private statsService = inject(StatsDashboardService);
  private notify = inject(NotificationService);
  private http = inject(HttpClient);

  isLoading = signal(true);
  hasError = signal(false);
  stats = signal<StatsDashboard | null>(null);
  periodo = signal<Periodo>('1m');

  sale = signal<SalaAdmin[]>([]);
  showSalaForm = signal(false);
  editingSala = signal<SalaAdmin | null>(null);
  salaForm: SalaAdmin = { nome: '', capienza: 10, descrizione: '' };
  salaLoading = signal(false);
  showDeleteConfirm = signal<string | null>(null);

  salaFotoSelezionata = signal<(SalaAdmin & { imageUrl?: string | null }) | null>(null);
  fotoLoading = signal(false);
  fotoErrore = signal<string | null>(null);
  fileSelezionato: File | null = null;
  isDragOver = false;

  private charts: any[] = [];
  private chartJsLoaded = false;

  readonly periodoOptions: { value: Periodo; label: string }[] = [
    { value: '1m', label: 'Ultimo mese' },
    { value: '3m', label: 'Ultimi 3 mesi' },
    { value: '1y', label: 'Ultimo anno' },
  ];

  ngOnInit(): void {
    this.caricaStats();
    this.caricaSale();
  }

  ngAfterViewInit(): void {
    this.caricaChartJs().then(() => {
      if (this.stats()) this.renderCharts();
    });
  }

  ngOnDestroy(): void {
    this.distruggiCharts();
  }

  caricaStats(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.statsService.getStats(this.periodo()).subscribe({
      next: data => {
        this.stats.set(data);
        this.isLoading.set(false);

        setTimeout(() => this.renderCharts(), 50);
      },
      error: () => {
        this.isLoading.set(false);
        this.hasError.set(true);
        this.notify.show('Errore nel caricamento delle statistiche', 'error');
      },
    });
  }

  onPeriodoChange(p: Periodo): void {
    this.periodo.set(p);
    this.distruggiCharts();
    this.caricaStats();
  }

  private async caricaChartJs(): Promise<void> {
    if (this.chartJsLoaded || typeof Chart !== 'undefined') {
      this.chartJsLoaded = true;
      return;
    }
    return new Promise(resolve => {
      const script = document.createElement('script');
      script.src = 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js';
      script.onload = () => {
        this.chartJsLoaded = true;
        resolve();
      };
      document.head.appendChild(script);
    });
  }

  private renderCharts(): void {
    const data = this.stats();
    if (!data || typeof Chart === 'undefined') return;
    this.distruggiCharts();

    const PALETTE = ['#3B82F6', '#8B5CF6', '#10B981', '#F59E0B', '#EF4444', '#06B6D4', '#84CC16', '#EC4899'];
    const gridColor = 'rgba(0,0,0,0.06)';
    const font = { family: "'Inter', system-ui, sans-serif", size: 12 };

    const defaults = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false }, tooltip: { cornerRadius: 8 } },
      scales: {
        x: { grid: { color: gridColor }, ticks: { font } },
        y: { grid: { color: gridColor }, ticks: { font }, beginAtZero: true },
      },
    };

    this.creaChart('chartSale', {
      type: 'bar',
      data: {
        labels: data.prenotazioniPerSala.map(d => d.label),
        datasets: [
          {
            label: 'Prenotazioni',
            data: data.prenotazioniPerSala.map(d => d.value),
            backgroundColor: PALETTE,
            borderRadius: 6,
          },
        ],
      },
      options: {
        ...defaults,
        indexAxis: 'y',
        plugins: { ...defaults.plugins, legend: { display: false } },
        scales: {
          x: { ...defaults.scales.x, beginAtZero: true },
          y: { grid: { display: false }, ticks: { font } },
        },
      },
    });

    this.creaChart('chartOre', {
      type: 'line',
      data: {
        labels: data.oreRichieste.map(d => d.label),
        datasets: [
          {
            label: 'Prenotazioni',
            data: data.oreRichieste.map(d => d.value),
            borderColor: '#3B82F6',
            backgroundColor: 'rgba(59,130,246,0.08)',
            borderWidth: 2.5,
            tension: 0.4,
            fill: true,
            pointBackgroundColor: '#3B82F6',
            pointRadius: 4,
          },
        ],
      },
      options: defaults,
    });

    this.creaChart('chartMesi', {
      type: 'line',
      data: {
        labels: data.occupazionePerMese.map(d => d.label),
        datasets: [
          {
            label: 'Occupazione %',
            data: data.occupazionePerMese.map(d => d.value),
            borderColor: '#10B981',
            backgroundColor: 'rgba(16,185,129,0.08)',
            borderWidth: 2.5,
            tension: 0.4,
            fill: true,
            pointBackgroundColor: '#10B981',
            pointRadius: 4,
          },
        ],
      },
      options: {
        ...defaults,
        scales: {
          ...defaults.scales,
          y: { ...defaults.scales.y, max: 100, ticks: { ...font, callback: (v: number) => v + '%' } },
        },
      },
    });

    this.creaChart('chartUtenti', {
      type: 'bar',
      data: {
        labels: data.topUtenti.map(d => d.label),
        datasets: [
          {
            label: 'Prenotazioni',
            data: data.topUtenti.map(d => d.value),
            backgroundColor: PALETTE.slice(0, 5),
            borderRadius: 6,
          },
        ],
      },
      options: defaults,
    });

    this.creaChart('chartGiorni', {
      type: 'doughnut',
      data: {
        labels: data.prenotazioniPerGiorno.map(d => d.label),
        datasets: [
          {
            data: data.prenotazioniPerGiorno.map(d => d.value),
            backgroundColor: PALETTE,
            hoverOffset: 6,
            borderWidth: 2,
            borderColor: '#fff',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: {
            display: true,
            position: 'right',
            labels: { font, boxWidth: 12, padding: 14 },
          },
          tooltip: { cornerRadius: 8 },
        },
      },
    });
  }

  private creaChart(canvasId: string, config: any): void {
    const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
    if (!canvas) return;
    const chart = new Chart(canvas, config);
    this.charts.push(chart);
  }

  private distruggiCharts(): void {
    this.charts.forEach(c => {
      try {
        c.destroy();
      } catch (_) {}
    });
    this.charts = [];
  }

  caricaSale(): void {
    this.statsService.getSale().subscribe({
      next: list => this.sale.set(list),
      error: () => {},
    });
  }

  aprireSalaForm(sala?: SalaAdmin): void {
    if (sala) {
      this.editingSala.set(sala);
      this.salaForm = { ...sala };
    } else {
      this.editingSala.set(null);
      this.salaForm = { nome: '', capienza: 10, descrizione: '' };
    }
    this.showSalaForm.set(true);
  }

  chiudiSalaForm(): void {
    this.showSalaForm.set(false);
    this.editingSala.set(null);
  }

  salvaSala(): void {
    if (!this.salaForm.nome.trim()) {
      this.notify.show('Inserisci il nome della sala', 'error');
      return;
    }
    if (!this.salaForm.capienza || this.salaForm.capienza < 1) {
      this.notify.show('Inserisci una capienza valida', 'error');
      return;
    }

    this.salaLoading.set(true);
    const editing = this.editingSala();

    const op$ = editing?.id ? this.statsService.modificaSala(editing.id, this.salaForm) : this.statsService.creaSala(this.salaForm);

    op$.subscribe({
      next: () => {
        this.salaLoading.set(false);
        this.notify.show(editing ? 'Sala modificata' : 'Sala creata', 'success');
        this.chiudiSalaForm();
        this.caricaSale();
      },
      error: () => {
        this.salaLoading.set(false);
        this.notify.show('Errore nel salvataggio', 'error');
      },
    });
  }

  chiediConfermaElimina(id: string): void {
    this.showDeleteConfirm.set(id);
  }

  confermaElimina(): void {
    const id = this.showDeleteConfirm();
    if (!id) return;
    this.statsService.eliminaSala(id).subscribe({
      next: () => {
        this.notify.show('Sala eliminata', 'success');
        this.showDeleteConfirm.set(null);
        this.caricaSale();
      },
      error: () => this.notify.show('Errore eliminazione sala', 'error'),
    });
  }

  apriUploadFoto(sala: SalaAdmin): void {
    this.salaFotoSelezionata.set({ ...sala });
    this.fileSelezionato = null;
    this.fotoErrore.set(null);
    this.isDragOver = false;
  }

  chiudiUploadFoto(): void {
    if (this.fotoLoading()) return;
    this.salaFotoSelezionata.set(null);
    this.fileSelezionato = null;
    this.fotoErrore.set(null);
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.impostaFile(input.files[0]);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) this.impostaFile(file);
  }

  private impostaFile(file: File): void {
    const tipiOk = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!tipiOk.includes(file.type)) {
      this.fotoErrore.set('Formato non supportato. Usa JPG, PNG o WEBP.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.fotoErrore.set('File troppo grande. Massimo 5 MB.');
      return;
    }
    this.fotoErrore.set(null);
    this.fileSelezionato = file;
  }

  confermaUploadFoto(): void {
    const sala = this.salaFotoSelezionata();
    if (!sala?.id || !this.fileSelezionato) return;
    this.fotoLoading.set(true);

    const formData = new FormData();
    formData.append('file', this.fileSelezionato);

    this.http.post<SalaAdmin & { imageUrl?: string | null }>(`/api/sales/${sala.id}/immagine`, formData).subscribe({
      next: updated => {
        this.fotoLoading.set(false);
        this.notify.show('Foto caricata con successo! 📷', 'success');
        this.fileSelezionato = null;

        this.caricaSale();

        const upd = updated as SalaAdmin & { imageUrl?: string | null };
        this.salaFotoSelezionata.set({ ...sala, imageUrl: upd.imageUrl ?? null });
      },
      error: () => {
        this.fotoLoading.set(false);
        this.fotoErrore.set('Errore durante il caricamento. Riprova.');
      },
    });
  }

  confermaRimuoviFoto(): void {
    const sala = this.salaFotoSelezionata();
    if (!sala?.id) return;
    this.fotoLoading.set(true);

    this.http.delete(`/api/sales/${sala.id}/immagine`).subscribe({
      next: () => {
        this.fotoLoading.set(false);
        this.notify.show('Foto rimossa', 'success');
        this.salaFotoSelezionata.set({ ...sala, imageUrl: null });
        this.caricaSale();
      },
      error: () => {
        this.fotoLoading.set(false);
        this.fotoErrore.set('Errore durante la rimozione.');
      },
    });
  }

  formatPercent(v: number): string {
    return v.toFixed(1) + '%';
  }
}
