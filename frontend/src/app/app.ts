import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { HeaderToolbarComponent } from './core/components/header-toolbar/header-toolbar.component';
import { filter, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderToolbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  protected readonly title = signal('frontend');

  protected readonly showHeader = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => {
        const url = (event as NavigationEnd).url;
        // Ocultar header en confirmación de email, verify-email y home
        return !url.includes('/confirm-email') && url !== '/' && !url.includes('/home') && !url.includes('/verify-email') && !url.includes('/register-form')
        && !url.includes('/forgot-password') && !url.includes('/reset-password');
      })
    ),
    { initialValue: false }
  );
}
