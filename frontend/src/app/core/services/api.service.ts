import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  /**
   * Realiza una petición GET a la API
   * @param endpoint Endpoint relativo a la URL base de la API
   */
  get<T>(endpoint: string) {
    return this.http.get<T>(`${this.baseUrl}${endpoint}`);
  }

  /**
   * Realiza una petición POST a la API
   * @param endpoint Endpoint relativo a la URL base de la API
   * @param body Cuerpo de la petición
   */
  post<T>(endpoint: string, body: any) {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body);
  }

  /**
   * Realiza una petición PUT a la API
   * @param endpoint Endpoint relativo a la URL base de la API
   * @param body Cuerpo de la petición
   */
  put<T>(endpoint: string, body: any) {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body);
  }

  /**
   * Realiza una petición DELETE a la API
   * @param endpoint Endpoint relativo a la URL base de la API
   */
  delete<T>(endpoint: string) {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`);
  }
}
