import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-confirm-email',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-email.component.html',
  styleUrl: './confirm-email.component.scss',
})
export class ConfirmEmailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly apiService = inject(ApiService);

  token: string | null = null;
  apiResponse: any = null;
  loading: boolean = false;
  error: string | null = null;

  ngOnInit() {
    // Obtener el token de la URL
    this.token = this.route.snapshot.paramMap.get('token');

    if (this.token) {
      this.confirmEmail();
    } else {
      this.error = 'No se encontró el token en la URL';
    }
  }

  private confirmEmail() {
    this.loading = true;
    this.error = null;

    console.log('=== FRONTEND: Enviando petición de confirmación de email ===');
    console.log('Token:', this.token);
    console.log('Endpoint:', `/auth/confirm-email/${this.token}`);
    console.log('Método: GET');

    this.apiService.get(`/auth/confirm-email/${this.token}`)
      .subscribe({
        next: (response) => {
          console.log('=== FRONTEND: Respuesta exitosa recibida ===');
          console.log('Respuesta:', response);
          this.apiResponse = response;
          this.loading = false;
        },
        error: (err) => {
          console.error('=== FRONTEND: Error recibido ===');
          console.error('Error:', err);
          this.error = err.message || 'Error al confirmar el email';
          this.apiResponse = err;
          this.loading = false;
        }
      });
  }
}
