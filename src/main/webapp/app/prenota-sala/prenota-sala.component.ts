import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RicercaService } from '../services/ricerca.service';
import { NotificationService } from 'app/shared/notification/notification.service';

@Component({
  standalone: true,
  selector: 'jhi-prenota-sala',
  templateUrl: './prenota-sala.component.html',
  styleUrls: ['./prenota-sala.component.scss'],
  imports: [CommonModule, FormsModule],
})
export default class PrenotaSalaComponent implements OnInit {
  orari: string[] = [];
  capienza = 0;
  dataSelezionata = '';
  oraSelezionata = '';
  caricamento = false;

  constructor(
    public router: Router,
    private ricercaService: RicercaService,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.generaOrari();

    const salvati = this.ricercaService.recuperaRicerca();
    if (salvati.data) {
      this.dataSelezionata = salvati.data;
      this.oraSelezionata = salvati.ora;
      this.capienza = salvati.capienza ?? 0;
    }
  }

  generaOrari(): void {
    this.orari = [];
    for (let i = 8; i < 20; i++) {
      const fascia = `${i.toString().padStart(2, '0')}:00 - ${(i + 1).toString().padStart(2, '0')}:00`;
      this.orari.push(fascia);
    }
  }

  confermaPrenotazione(): void {
    if (!this.dataSelezionata) {
      this.notificationService.show('Seleziona una data per continuare', 'error');
      return;
    }
    if (!this.oraSelezionata) {
      this.notificationService.show('Seleziona una fascia oraria per continuare', 'error');
      return;
    }

    const oggi = new Date();
    oggi.setHours(0, 0, 0, 0);
    const dataScelta = new Date(this.dataSelezionata);
    if (dataScelta < oggi) {
      this.notificationService.show('La data selezionata è nel passato', 'error');
      return;
    }

    this.caricamento = true;
    this.ricercaService.salvaRicerca({
      data: this.dataSelezionata,
      ora: this.oraSelezionata,
      capienza: this.capienza,
    });

    this.router
      .navigate(['/risultati-sala'], {
        queryParams: {
          data: this.dataSelezionata,
          ora: this.oraSelezionata,
          capienza: this.capienza,
          numPersone: this.capienza,
        },
      })
      .then(() => {
        this.caricamento = false;
      });
  }
}
