import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/** No redirigir: el login devuelve 401 con credenciales incorrectas. */
const LOGIN_URL_PART = '/auth/login';

export const unauthorizedInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: unknown) => {
      if (
        err instanceof HttpErrorResponse &&
        err.status === 401 &&
        !req.url.includes(LOGIN_URL_PART)
      ) {
        authService.clearToken();
        void router.navigate(['/'], { replaceUrl: true });
      }
      return throwError(() => err);
    }),
  );
};
