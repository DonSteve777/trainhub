import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

const TOKEN_STORAGE_KEY = 'trainhub_jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);

  setToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    this.clearToken();
    void this.router.navigate(['/'], { replaceUrl: true });
  }
}
