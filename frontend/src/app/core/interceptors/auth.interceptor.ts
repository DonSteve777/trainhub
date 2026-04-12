import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/** Coincide con rutas `permitAll` del backend: no enviar JWT envejecido en el login u otros flujos públicos. */
const PUBLIC_AUTH_URL_FRAGMENTS = [
  '/auth/login',
  '/auth/register',
  '/auth/forgot-password',
  '/auth/reset-password',
  '/auth/confirm-email',
] as const;

function isPublicAuthRequest(url: string): boolean {
  return PUBLIC_AUTH_URL_FRAGMENTS.some((fragment) => url.includes(fragment));
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  const skipBearer = isPublicAuthRequest(req.url);
  if (!token || skipBearer) {
    return next(req);
  }
  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};
