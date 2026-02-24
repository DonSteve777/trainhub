import { Component, OnInit, inject, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';

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
  private readonly ngZone = inject(NgZone);
  private readonly cdr = inject(ChangeDetectorRef);

  profileForm!: FormGroup;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  ngOnInit() {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(60)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      bio: ['', [Validators.maxLength(1000)]],
      photoUrl: ['', [Validators.maxLength(255)]]
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const profileData = this.profileForm.value;

    this.apiService.put('/api/user/profile', profileData)
      .pipe(
        finalize(() => {
          this.isSubmitting = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (response) => {
          this.successMessage = 'Perfil actualizado correctamente';
          this.ngZone.run(() => {
            this.cdr.detectChanges();
          });
        },
        error: (error) => {
          if (error.status === 409) {
            this.errorMessage = 'El email ya está en uso por otro usuario';
          } else if (error.error?.message) {
            this.errorMessage = error.error.message;
          } else {
            this.errorMessage = 'Error al actualizar el perfil. Por favor, intenta de nuevo.';
          }
          this.ngZone.run(() => {
            this.cdr.detectChanges();
          });
        }
      });
  }

  getFieldError(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) {
        return 'Este campo es obligatorio';
      }
      if (field.errors['email']) {
        return 'Debe ser un email válido';
      }
      if (field.errors['maxlength']) {
        const maxLength = field.errors['maxlength'].requiredLength;
        return `Máximo ${maxLength} caracteres`;
      }
    }
    return '';
  }
}
