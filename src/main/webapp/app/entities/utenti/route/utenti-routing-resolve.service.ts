import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IUtenti } from '../utenti.model';
import { UtentiService } from '../service/utenti.service';

const utentiResolve = (route: ActivatedRouteSnapshot): Observable<null | IUtenti> => {
  const id = route.params.id;
  if (id) {
    return inject(UtentiService)
      .find(id)
      .pipe(
        mergeMap((utenti: HttpResponse<IUtenti>) => {
          if (utenti.body) {
            return of(utenti.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default utentiResolve;
