import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TrainhubLogoComponent } from '../../shared/components/trainhub-logo/trainhub-logo.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [TrainhubLogoComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  loginForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onLogin(): void {
    if (this.loginForm.valid) {
      console.log('Login attempt:', this.loginForm.value);
      // TODO: Implementar lógica de inicio de sesión
    }
  }
}
