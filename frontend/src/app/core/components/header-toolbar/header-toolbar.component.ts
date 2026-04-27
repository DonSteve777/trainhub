import { Component, computed, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-header-toolbar',
  imports: [
    RouterLink,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './header-toolbar.component.html',
  styleUrl: './header-toolbar.component.scss',
})
export class HeaderToolbarComponent {
  private readonly router = inject(Router);

  private readonly navigationEnd = toSignal(
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)),
  );

  readonly isOnCreatePost = computed(() => {
    this.navigationEnd();
    return this.router.url === '/create-post';
  });

  goToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }
}
