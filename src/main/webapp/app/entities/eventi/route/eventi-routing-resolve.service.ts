import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IEventi } from '../eventi.model';
import { EventiService } from '../service/eventi.service';

const eventiResolve = (route: ActivatedRouteSnapshot): Observable<null | IEventi> => {
  const id = route.params.id;
  if (id) {
    return inject(EventiService)
      .find(id)
      .pipe(
        mergeMap((eventi: HttpResponse<IEventi>) => {
          if (eventi.body) {
            return of(eventi.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default eventiResolve;
