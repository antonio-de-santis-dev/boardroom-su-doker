import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { IEventi } from '../eventi.model';
import SharedModule from 'app/shared/shared.module';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: true,
  selector: 'jhi-eventi-detail',
  templateUrl: './eventi-detail.component.html',
  styleUrl: './eventi-detail.component.scss',
  imports: [SharedModule, RouterModule, CommonModule, FormsModule],
})
export class EventiDetailComponent implements OnInit {
  eventi = signal<IEventi | null>(null);
  showForm = false;

  prenotazione = {
    nome: '',
    cognome: '',
    email: '',
  };

  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);

  ngOnInit(): void {
    window.scrollTo(0, 0);
    this.activatedRoute.data.subscribe(({ eventi }) => {
      this.eventi.set(eventi);
    });
  }

  previousState(): void {
    window.history.back();
  }

  apriForm(): void {
    this.showForm = true;
  }

  chiudiForm(): void {
    this.showForm = false;
    this.prenotazione = { nome: '', cognome: '', email: '' };
  }

  confermaPrenotazione(): void {
    if (!this.eventi()) {
      return;
    }

    this.http
      .post(`/api/eventis/${this.eventi()!.id}/prenotazione-email`, {
        nome: this.prenotazione.nome,
        cognome: this.prenotazione.cognome,
        email: this.prenotazione.email,
      })
      .subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: () => {
          alert("Errore durante l'invio dell'email");
        },
      });
  }
}
