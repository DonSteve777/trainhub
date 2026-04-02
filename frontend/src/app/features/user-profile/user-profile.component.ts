import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';

interface UserProfileResponse {
  id: number;
  email: string;
  photoUrl: string | null;
  name: string;
  accountStatus: string;
  emailVerified: boolean;
}

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss',
})
export class UserProfileComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly apiService = inject(ApiService);
  private readonly fb = inject(FormBuilder);

  profileForm!: FormGroup;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.initializeForm();
    this.loadCurrentUser();
  }

  private initializeForm(): void {
    this.profileForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      photoUrl: ['', [Validators.maxLength(255)]],
      name: ['', [Validators.required, Validators.maxLength(255)]]
    });
  }

  private loadCurrentUser(): void {
    this.isLoading.set(true);
    this.apiService.get<UserProfileResponse>('/user/me')
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (user) => {
          this.profileForm.patchValue({
            email: user.email,
            photoUrl: user.photoUrl ?? '',
            name: user.name
          });
        },
        error: (error) => {
          if (error.status === 401) {
            this.router.navigate(['/']);
          } else {
            this.errorMessage.set('Error al cargar el perfil. Por favor, recarga la página.');
          }
        }
      });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.apiService.put('/user/profile', this.profileForm.value)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => this.successMessage.set('Perfil actualizado correctamente'),
        error: (error) => {
          if (error.status === 409) {
            this.errorMessage.set('El email ya está en uso por otro usuario');
          } else if (error.error?.message) {
            this.errorMessage.set(error.error.message);
          } else {
            this.errorMessage.set('Error al actualizar el perfil. Por favor, intenta de nuevo.');
          }
        }
      });
  }

  getFieldError(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) return 'Este campo es obligatorio';
      if (field.errors['email']) return 'Debe ser un email válido';
      if (field.errors['maxlength']) {
        return `Máximo ${field.errors['maxlength'].requiredLength} caracteres`;
      }
    }
    return '';
  }
}
