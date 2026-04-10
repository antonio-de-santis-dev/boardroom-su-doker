import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IStatiPrenotazione } from '../stati-prenotazione.model';
import { StatiPrenotazioneService } from '../service/stati-prenotazione.service';

const statiPrenotazioneResolve = (route: ActivatedRouteSnapshot): Observable<null | IStatiPrenotazione> => {
  const id = route.params.id;
  if (id) {
    return inject(StatiPrenotazioneService)
      .find(id)
      .pipe(
        mergeMap((statiPrenotazione: HttpResponse<IStatiPrenotazione>) => {
          if (statiPrenotazione.body) {
            return of(statiPrenotazione.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default statiPrenotazioneResolve;
