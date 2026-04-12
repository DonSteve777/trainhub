import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Impide activar la ruta sin JWT. La pantalla de login es la raíz (`/`).
 * Coexiste con `unauthorizedInterceptor` (401 en API → limpiar token y redirigir).
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) {
    return true;
  }
  return router.parseUrl('/');
};
