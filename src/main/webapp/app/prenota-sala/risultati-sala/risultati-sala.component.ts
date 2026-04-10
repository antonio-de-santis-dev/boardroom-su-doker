import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SaleApiService, ISalaDTO } from 'app/services/sale-api.service';
import { PrenotazioniApiService } from 'app/services/prenotazioni-api.service';
import { NotificationService } from 'app/shared/notification/notification.service';

export interface Sala {
  id: string;
  nome: string;
  capienza: number;
  descrizione?: string;
  imageUrl?: string | null;
  features?: string[];
}

@Component({
  selector: 'jhi-risultati-sala',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './risultati-sala.component.html',
  styleUrl: './risultati-sala.component.scss',
})
export class RisultatiSalaComponent implements OnInit {
  dataRicerca = '';
  oraRicerca = '';
  capienzaRicerca = 0;
  numPersoneRicerca = 1;
  sale: Sala[] = [];

  isLoading = false;
  showPrivacyModal = false;
  salaSelezionata: Sala | null = null;

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private saleApiService = inject(SaleApiService);
  private prenotazioniApi = inject(PrenotazioniApiService);
  private notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.dataRicerca = params['data'] ?? '';
      this.oraRicerca = params['ora'] ?? '';
      this.capienzaRicerca = Number(params['capienza'] ?? 0);
      this.numPersoneRicerca = Math.max(1, Number(params['numPersone'] ?? 1));
      this.caricaSaleDisponibili();
    });
  }

  tornaIndietro(): void {
    this.router.navigate(['/prenota-sala']);
  }

  apriSceltaPrivacy(sala: Sala): void {
    this.salaSelezionata = sala;
    this.showPrivacyModal = true;
  }

  chiudiModal(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.showPrivacyModal = false;
    }
  }

  confermaEProcedi(isPubblico: boolean): void {
    if (!this.salaSelezionata || this.isLoading) return;

    this.isLoading = true;
    const [oraInizio, oraFine] = this.oraRicerca.split('-').map(o => o.trim());

    this.prenotazioniApi
      .creaPrenotazione({
        data: this.dataRicerca,
        oraInizio: this.normalizzaOra(oraInizio),
        oraFine: this.normalizzaOra(oraFine),
        salaId: this.salaSelezionata.id,
        numPersone: this.numPersoneRicerca,
      })
      .subscribe({
        next: pren => {
          this.showPrivacyModal = false;
          this.router
            .navigate(['/prenota-sala/crea-evento'], {
              queryParams: {
                salaId: this.salaSelezionata?.id,
                nomeSala: this.salaSelezionata?.nome,
                data: this.dataRicerca,
                ora: this.oraRicerca,
                pubblico: isPubblico,
                prenotazioneId: pren.id,
              },
            })
            .finally(() => (this.isLoading = false));
        },
        error: () => {
          this.isLoading = false;
          this.showPrivacyModal = false;
          this.notificationService.show('Errore durante la creazione della prenotazione. Riprova.', 'error');
        },
      });
  }

  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const placeholder = img.closest('.room-img-col')?.querySelector('.room-img-placeholder') as HTMLElement | null;
    if (placeholder) placeholder.style.display = 'flex';
  }

  private caricaSaleDisponibili(): void {
    if (!this.dataRicerca || !this.oraRicerca) return;

    this.isLoading = true;
    const parts = this.oraRicerca.split('-');
    if (parts.length < 2) {
      this.isLoading = false;
      return;
    }

    const inizio = this.normalizzaOra(parts[0].trim());
    const fine = this.normalizzaOra(parts[1].trim());

    this.saleApiService.getSaleDisponibili(this.dataRicerca, inizio, fine, this.capienzaRicerca).subscribe({
      next: (saleDto: ISalaDTO[]) => {
        this.sale = saleDto
          .filter(s => s && s.id)
          .map(s => ({
            id: s.id!.toString(),
            nome: s.nome || 'Sala Executive',
            capienza: s.capienza || 0,
            descrizione: s.descrizione ?? undefined,
            imageUrl: s.imageUrl ?? null,

            features: (s as any).features ?? (s as any).dotazioni ?? [],
          }));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.show('Impossibile caricare le sale. Riprova.', 'error');
      },
    });
  }

  private normalizzaOra(ora: string): string {
    if (!ora) return '00:00';
    const [h, m] = ora.split(':');
    return `${h.padStart(2, '0')}:${m ?? '00'}`;
  }
}
