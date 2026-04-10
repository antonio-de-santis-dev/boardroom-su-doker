import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import dayjs from 'dayjs/esm';
import { IPrenotazioni } from '../prenotazioni.model';

@Component({
  selector: 'jhi-prenotazioni-calendar-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="modal-container">
      <!-- Stripe colorata in cima -->
      <div class="modal-stripe" [style.background-color]="color.bg"></div>

      <div class="modal-body-content">
        <!-- Header -->
        <div class="modal-header-row">
          <div class="modal-sala-badge" [style.background-color]="color.light" [style.color]="color.text">
            <span class="modal-sala-dot" [style.background-color]="color.bg"></span>
            {{ prenotazione.sala?.nome ?? 'Sala non specificata' }}
          </div>
          <button class="modal-close" (click)="close()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <!-- Data principale -->
        <div class="modal-date-block">
          <div class="modal-date-main">{{ formatDate(prenotazione.data) }}</div>
          @if (prenotazione.oraInizio) {
            <div class="modal-time">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              {{ prenotazione.oraInizio }}
              @if (prenotazione.oraFine) {
                &nbsp;–&nbsp;{{ prenotazione.oraFine }}
              }
            </div>
          }
        </div>

        <!-- Dettagli -->
        <div class="modal-details">
          @if (prenotazione.stato?.codice) {
            <div class="modal-detail-row">
              <span class="modal-detail-label">Stato</span>
              <span class="modal-stato-badge" [attr.data-stato]="prenotazione.stato!.codice">
                {{ getStatoLabel(prenotazione.stato!.codice!) }}
              </span>
            </div>
          }

          @if (prenotazione.utente?.nome) {
            <div class="modal-detail-row">
              <span class="modal-detail-label">Utente</span>
              <span class="modal-detail-value">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                {{ prenotazione.utente!.nome }}
              </span>
            </div>
          }
        </div>

        <!-- Azioni -->
        <div class="modal-actions">
          <a [routerLink]="['/prenotazioni', prenotazione.id, 'view']" class="modal-btn modal-btn--secondary" (click)="close()">
            Visualizza dettaglio
          </a>
          <a
            [routerLink]="['/prenotazioni', prenotazione.id, 'edit']"
            class="modal-btn modal-btn--primary"
            [style.background-color]="color.bg"
            (click)="close()"
          >
            Modifica
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .modal-container {
        border-radius: 12px;
        overflow: hidden;
        font-family: 'Inter', system-ui, sans-serif;
      }

      .modal-stripe {
        height: 5px;
      }

      .modal-body-content {
        padding: 20px 22px 22px;
      }

      .modal-header-row {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        margin-bottom: 16px;
      }

      .modal-sala-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 0.8125rem;
        font-weight: 600;
      }

      .modal-sala-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
      }

      .modal-close {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 30px;
        height: 30px;
        border: none;
        background: #f3f4f6;
        border-radius: 50%;
        color: #6b7280;
        cursor: pointer;
        transition: all 0.15s;

        &:hover {
          background: #e5e7eb;
          color: #111827;
        }
      }

      .modal-date-block {
        margin-bottom: 18px;
      }

      .modal-date-main {
        font-size: 1.3rem;
        font-weight: 700;
        color: #111827;
        letter-spacing: -0.02em;
        text-transform: capitalize;
      }

      .modal-time {
        display: flex;
        align-items: center;
        gap: 5px;
        margin-top: 4px;
        font-size: 0.9rem;
        color: #4b5563;
        font-weight: 500;
      }

      .modal-details {
        display: flex;
        flex-direction: column;
        gap: 10px;
        padding: 14px 0;
        border-top: 1px solid #f3f4f6;
        border-bottom: 1px solid #f3f4f6;
        margin-bottom: 18px;
      }

      .modal-detail-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .modal-detail-label {
        font-size: 0.8125rem;
        color: #9ca3af;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
      }

      .modal-detail-value {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 0.875rem;
        color: #374151;
        font-weight: 500;
      }

      .modal-stato-badge {
        font-size: 0.8125rem;
        font-weight: 600;
        padding: 3px 10px;
        border-radius: 20px;

        &[data-stato='CONFIRMED'] {
          background: #ecfdf5;
          color: #065f46;
        }
        &[data-stato='WAITING'] {
          background: #fffbeb;
          color: #b45309;
        }
        &[data-stato='REJECTED'] {
          background: #fef2f2;
          color: #b91c1c;
        }
        &[data-stato='CANCELLED'] {
          background: #f9fafb;
          color: #374151;
        }
      }

      .modal-actions {
        display: flex;
        gap: 10px;
      }

      .modal-btn {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 9px 16px;
        border-radius: 8px;
        font-size: 0.875rem;
        font-weight: 600;
        text-decoration: none;
        text-align: center;
        transition: all 0.15s;
        cursor: pointer;

        &--secondary {
          background: #f3f4f6;
          color: #374151;
          border: none;

          &:hover {
            background: #e5e7eb;
            color: #111827;
          }
        }

        &--primary {
          color: white;
          border: none;

          &:hover {
            filter: brightness(1.1);
          }
        }
      }
    `,
  ],
})
export class PrenotazioniCalendarDetailComponent {
  @Input() prenotazione!: IPrenotazioni;
  @Input() color!: { bg: string; light: string; text: string };

  activeModal = inject(NgbActiveModal);

  close(): void {
    this.activeModal.dismiss();
  }

  formatDate(date: any): string {
    if (!date) return '';
    return dayjs(date).locale('it').format('dddd D MMMM YYYY');
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
