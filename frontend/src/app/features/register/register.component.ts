import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TrainhubLogoComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  registerForm: FormGroup;
  selectedPhotoFile: File | null = null;

  constructor(private fb: FormBuilder, private router: Router) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      photo: [null],
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedPhotoFile = input.files[0];
      this.registerForm.patchValue({ photo: this.selectedPhotoFile });
    }
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    console.log('Register form data:', this.registerForm.value);
    // TODO: Llamar al backend para registrar usuario (correo, password, foto)
  }

  getFieldError(fieldName: string): string {
    const field = this.registerForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) {
        return 'Este campo es obligatorio';
      }
      if (field.errors['email']) {
        return 'Debe ser un email válido';
      }
      if (field.errors['minlength']) {
        const minLength = field.errors['minlength'].requiredLength;
        return `Mínimo ${minLength} caracteres`;
      }
    }
    return '';
  }
}

