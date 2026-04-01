import { Component, OnInit, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';
import { ApiService } from '../../core/services/api.service';

function passwordMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const parent = control.parent;
    if (!parent) return null;
    const password = parent.get('password')?.value;
    const confirm = control.value;
    return password === confirm ? null : { passwordMismatch: true };
  };
}

@Component({
  selector: 'app-register-form',
  standalone: true,
  imports: [TrainhubLogoComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './register-form.component.html',
  styleUrl: './register-form.component.scss',
})
export class RegisterFormComponent implements OnInit {
  registerForm!: FormGroup;
  private readonly apiService = inject(ApiService);
  private readonly router = inject(Router);
  backendMessage: string | null = null;
  constructor(private fb: FormBuilder) {}

  // qué hace este código?
  // este código es para el formulario de registro de un nuevo usuario  
  // se crea el formulario con los campos email, password y confirmPassword
  // se valida que el password y el confirmPassword sean iguales
  // se valida que el email sea un email válido
  // se valida que el password tenga al menos 8 caracteres
  // se valida que el confirmPassword tenga al menos 8 caracteres
  ngOnInit(): void {
    this.registerForm = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        username: ['', [Validators.required, Validators.maxLength(50)]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required, passwordMatchValidator()]],
      },
      { updateOn: 'blur' }
    );
    this.registerForm.get('password')?.valueChanges.subscribe(() => {
      this.registerForm.get('confirmPassword')?.updateValueAndValidity();
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;
    const { email, username, password } = this.registerForm.value;
    this.apiService
      .post<{ message: string; email: string }>('/auth/register', {
        email,
        username,
        password,
      })
      .subscribe({
        next: () => this.router.navigate(['/verify-email']),
        error: (err) => {
          const body = err.error;
          this.backendMessage =
            body?.message ?? body?.error ?? 'Error al registrarse. Inténtalo de nuevo.';
            console.log(this.backendMessage);
        },
      });
  }
}
