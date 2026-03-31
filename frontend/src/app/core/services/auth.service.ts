import { Injectable } from '@angular/core';

const TOKEN_STORAGE_KEY = 'trainhub_jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
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
}
