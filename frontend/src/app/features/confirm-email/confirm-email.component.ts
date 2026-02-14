import { Component, OnInit, inject, NgZone, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';
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
  private readonly router = inject(Router);
  private readonly apiService = inject(ApiService);
  private readonly ngZone = inject(NgZone);
  private readonly cdr = inject(ChangeDetectorRef);

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
      .pipe(
        finalize(() => this.ngZone.run(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }))
      )
      .subscribe({
        next: (response) => this.ngZone.run(() => {
          console.log('=== FRONTEND: Respuesta exitosa recibida ===');
          console.log('Respuesta:', response);
          this.apiResponse = response;
          this.cdr.detectChanges();
          
        }),
        error: (err) => this.ngZone.run(() => {
          console.error('=== FRONTEND: Error recibido ===');
          console.error('Error:', err);
          this.error = 'Error al confirmar el email';
          this.apiResponse = err;
          this.cdr.detectChanges();
        }),
        complete: () => {
        }
      });
  }

  goToProfile() {
    this.router.navigate(['/user-profile']);
  }
}
