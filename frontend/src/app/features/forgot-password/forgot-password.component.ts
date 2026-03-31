import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TrainhubLogoComponent],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent implements OnInit {
  form!: FormGroup;
  isSubmitting = false;
  message: string | null = null;
  errorMessage: string | null = null;

  private readonly fb = inject(FormBuilder);
  private readonly apiService = inject(ApiService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  onSubmit(): void {
    this.message = null;
    this.errorMessage = null;

    if (this.form.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    this.apiService.post<{ message: string }>('/auth/forgot-password', this.form.value).subscribe({
      next: (res) => {
        this.message =
          res?.message ??
          'Si existe una cuenta con ese email, recibirás instrucciones para restablecer tu contraseña.';
      },
      error: (err) => {
        const body = err?.error;
        this.errorMessage =
          body?.message ??
          body?.error ??
          'No se pudo procesar la solicitud. Inténtalo de nuevo.';
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  goToLogin(): void {
    this.router.navigate(['/']);
  }
}

