import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { finalize, switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';

interface UserProfileResponse {
  id: number;
  email: string;
  photoUrl: string | null;
  name: string;
  accountStatus: string;
  emailVerified: boolean;
}

interface AvatarUploadResponse {
  url: string;
}

const AVATAR_MAX_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss',
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly apiService = inject(ApiService);
  private readonly fb = inject(FormBuilder);

  profileForm!: FormGroup;

  readonly isLoading = signal(false);
  readonly isSubmitting = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  /** URL de la foto guardada en servidor (tras cargar o guardar perfil). */
  readonly savedPhotoUrl = signal<string | null>(null);

  /** Fichero de imagen pendiente de subir al guardar. */
  readonly pendingAvatarFile = signal<File | null>(null);

  /** Object URL para previsualizar el fichero seleccionado. */
  readonly avatarPreviewUrl = signal<string>('');

  readonly avatarDisplayUrl = computed(() => this.avatarPreviewUrl() || this.savedPhotoUrl());

  ngOnInit(): void {
    this.initializeForm();
    this.loadCurrentUser();
  }

  ngOnDestroy(): void {
    this.revokePreviewUrl();
  }

  private initializeForm(): void {
    this.profileForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
    });
  }

  private loadCurrentUser(): void {
    this.isLoading.set(true);
    this.apiService
      .get<UserProfileResponse>('/user/me')
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (user) => {
          this.savedPhotoUrl.set(user.photoUrl);
          this.profileForm.patchValue({
            email: user.email,
            name: user.name,
          });
        },
        error: (error) => {
          if (error.status === 401) {
            this.router.navigate(['/']);
          } else {
            this.errorMessage.set('Error al cargar el perfil. Por favor, recarga la página.');
          }
        },
      });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.errorMessage.set('El fichero debe ser una imagen');
      return;
    }

    if (file.size > AVATAR_MAX_BYTES) {
      this.errorMessage.set('La imagen no puede superar 5 MB');
      return;
    }

    this.errorMessage.set('');
    this.revokePreviewUrl();
    this.pendingAvatarFile.set(file);
    this.avatarPreviewUrl.set(URL.createObjectURL(file));
  }

  clearPendingAvatar(): void {
    this.revokePreviewUrl();
    this.pendingAvatarFile.set(null);
    this.avatarPreviewUrl.set('');
  }

  private revokePreviewUrl(): void {
    const url = this.avatarPreviewUrl();
    if (url) {
      URL.revokeObjectURL(url);
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const { email, name } = this.profileForm.value;
    const file = this.pendingAvatarFile();

    let upload$: Observable<AvatarUploadResponse | null>;
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      upload$ = this.apiService.post<AvatarUploadResponse>('/user/avatar', formData);
    } else {
      upload$ = of(null);
    }

    upload$
      .pipe(
        switchMap((avatarRes: AvatarUploadResponse | null) => {
          const photoUrl = avatarRes?.url ?? this.savedPhotoUrl() ?? '';
          return this.apiService.put<UserProfileResponse>('/user/profile', {
            email,
            name,
            photoUrl,
          });
        }),
        finalize(() => this.isSubmitting.set(false)),
      )
      .subscribe({
        next: (updated: UserProfileResponse) => {
          this.savedPhotoUrl.set(updated.photoUrl);
          this.clearPendingAvatar();
          this.successMessage.set('Perfil actualizado correctamente');
        },
        error: (error: { status?: number; error?: { message?: string } }) => {
          if (error.status === 409) {
            this.errorMessage.set('El email ya está en uso por otro usuario');
          } else if (error.error?.message) {
            this.errorMessage.set(error.error.message);
          } else {
            this.errorMessage.set('Error al actualizar el perfil. Por favor, intenta de nuevo.');
          }
        },
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
