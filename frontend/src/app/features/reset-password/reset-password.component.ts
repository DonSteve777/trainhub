import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';

function passwordMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const parent = control.parent;
    if (!parent) return null;
    const password = parent.get('newPassword')?.value;
    const confirm = control.value;
    return password === confirm ? null : { passwordMismatch: true };
  };
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TrainhubLogoComponent],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss',
})
export class ResetPasswordComponent implements OnInit {
  form!: FormGroup;
  token!: string;
  isSubmitting = false;
  message: string | null = null;
  errorMessage: string | null = null;

  private readonly fb = inject(FormBuilder);
  private readonly apiService = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') ?? '';
    this.form = this.fb.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required, passwordMatchValidator()]],
      },
      { updateOn: 'blur' }
    );

    this.form.get('newPassword')?.valueChanges.subscribe(() => {
      this.form.get('confirmPassword')?.updateValueAndValidity({ onlySelf: true });
    });
  }

  onSubmit(): void {
    this.message = null;
    this.errorMessage = null;

    if (!this.token) {
      this.errorMessage = 'El enlace no es válido o está incompleto.';
      return;
    }

    if (this.form.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    this.apiService
      .post<{ message: string }>('/auth/reset-password', {
        token: this.token,
        newPassword: this.form.value.newPassword,
      })
      .subscribe({
        next: (res) => {
          this.message = res?.message ?? 'Contraseña restablecida correctamente.';
          setTimeout(() => this.router.navigate(['/']), 800);
        },
        error: (err) => {
          const body = err?.error;
          this.errorMessage =
            body?.message ??
            body?.error ??
            'No se pudo restablecer la contraseña. Verifica el enlace e inténtalo de nuevo.';
        },
        complete: () => {
          this.isSubmitting = false;
        },
      });
  }
}

