import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [TrainhubLogoComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  loginForm!: FormGroup;
  private readonly fb = inject(FormBuilder);
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly backendMessage = signal<string | null>(null);

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/feed'], { replaceUrl: true });
    }
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onLogin(): void {
    if (this.loginForm.invalid) return;
    this.backendMessage.set(null);
    this.apiService.post<{ token: string; message: string }>('/auth/login', this.loginForm.value).subscribe({
      next: (res) => {
        this.authService.setToken(res.token);
        this.router.navigate(['/feed'], { replaceUrl: true });
      },
      error: (err) => {
        const body = err.error;
        this.backendMessage.set(
          body?.message ?? body?.error ?? 'Error al iniciar sesión. Inténtalo de nuevo.'
        );
      },
    });
  }
}
