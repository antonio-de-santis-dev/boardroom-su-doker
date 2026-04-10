import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISale } from '../sale.model';
import { SaleService } from '../service/sale.service';

const saleResolve = (route: ActivatedRouteSnapshot): Observable<null | ISale> => {
  const id = route.params.id;
  if (id) {
    return inject(SaleService)
      .find(id)
      .pipe(
        mergeMap((sale: HttpResponse<ISale>) => {
          if (sale.body) {
            return of(sale.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default saleResolve;
