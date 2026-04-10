import { Component, OnDestroy, OnInit, inject, signal, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';

import { EventiService } from 'app/entities/eventi/service/eventi.service';
import { IEventi } from 'app/entities/eventi/eventi.model';
import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  standalone: true,
  imports: [SharedModule, RouterModule, CommonModule],
})
export default class HomeComponent implements OnInit, OnDestroy {
  @ViewChild('heroViewport') heroViewport!: ElementRef<HTMLElement>;

  account = signal<Account | null>(null);
  eventi = signal<IEventi[]>([]);
  isLoading = signal<boolean>(true);

  activeSlide = 0;

  // ── Drag / Swipe ─────────────────────────────────────────
  isDragging = false;
  private dragStartX = 0;
  private touchStartX = 0;
  private readonly DRAG_THRESHOLD = 50; // px minimi per cambiare slide

  private readonly destroy$ = new Subject<void>();
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly eventiService = inject(EventiService);
  private readonly notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.caricaEventi();

    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => this.account.set(account));
  }

  goToSlide(index: number): void {
    this.activeSlide = index;
    const track = this.heroViewport?.nativeElement?.querySelector('.hero-track') as HTMLElement | null;
    if (track) {
      track.style.transform = `translateX(-${index * 100}%)`;
    }
  }

  caricaEventi(): void {
    this.isLoading.set(true);
    this.eventiService
      .getEventiPubblici()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: data => {
          this.eventi.set(data);
          this.activeSlide = 0;
          setTimeout(() => this.goToSlide(0), 0);
        },
        error: () => {
          this.notificationService.show('Errore nel caricamento degli eventi pubblici', 'error');
        },
      });
  }

  vaiADettagli(evento: IEventi): void {
    this.router.navigate(['/eventi', evento.id, 'view']);
  }

  vaiAlleMiePrenotazioni(): void {
    this.gestisciNavigazioneProtetta('/mie-prenotazioni');
  }

  private gestisciNavigazioneProtetta(destinazione: string): void {
    if (this.account() === null) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: destinazione } });
    } else {
      this.router.navigate([destinazione]);
    }
  }

  prevSlide(): void {
    if (this.activeSlide > 0) this.goToSlide(this.activeSlide - 1);
  }

  nextSlide(): void {
    if (this.activeSlide < this.eventi().length - 1) this.goToSlide(this.activeSlide + 1);
  }

  // ── Mouse drag ────────────────────────────────────────────

  onDragStart(e: MouseEvent): void {
    this.isDragging = true;
    this.dragStartX = e.clientX;
    e.preventDefault(); // evita selezione testo durante il drag
  }

  onDragMove(e: MouseEvent): void {
    if (!this.isDragging) return;
    // Feedback visivo opzionale: si potrebbe spostare il track in tempo reale
    // Per semplicità gestiamo solo il release
  }

  onDragEnd(e?: MouseEvent): void {
    if (!this.isDragging) return;
    this.isDragging = false;
    if (!e) return;
    const delta = e.clientX - this.dragStartX;
    if (Math.abs(delta) < this.DRAG_THRESHOLD) return;
    if (delta < 0)
      this.nextSlide(); // trascinato verso sinistra → slide successiva
    else this.prevSlide(); // trascinato verso destra  → slide precedente
  }

  // ── Touch swipe ───────────────────────────────────────────

  onTouchStart(e: TouchEvent): void {
    this.touchStartX = e.touches[0].clientX;
  }

  onTouchEnd(e: TouchEvent): void {
    const delta = e.changedTouches[0].clientX - this.touchStartX;
    if (Math.abs(delta) < this.DRAG_THRESHOLD) return;
    if (delta < 0) this.nextSlide();
    else this.prevSlide();
  }

  /** Fallback carosello: se l'immagine non carica mostra il placeholder verde */
  onHeroImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const placeholder = img.closest('.hero-img-col')?.querySelector('.hero-img-placeholder') as HTMLElement | null;
    if (placeholder) placeholder.style.display = 'flex';
  }

  /** Fallback card evento: se l'immagine non carica mostra il placeholder emoji */
  onCardImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    const placeholder = img.closest('.evento-card__img-wrap')?.querySelector('.evento-card__img-placeholder') as HTMLElement | null;
    if (placeholder) placeholder.style.display = 'flex';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
