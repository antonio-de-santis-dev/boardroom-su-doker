import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPrenotazioni } from '../prenotazioni.model';
import { PrenotazioniService } from '../service/prenotazioni.service';

const prenotazioniResolve = (route: ActivatedRouteSnapshot): Observable<null | IPrenotazioni> => {
  const id = route.params.id;
  if (id) {
    return inject(PrenotazioniService)
      .find(id)
      .pipe(
        mergeMap((prenotazioni: HttpResponse<IPrenotazioni>) => {
          if (prenotazioni.body) {
            return of(prenotazioni.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default prenotazioniResolve;
