import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { NotificationsDialogComponent } from '../notifications-dialog/notifications-dialog.component';

@Component({
  selector: 'app-header-toolbar',
  imports: [
    RouterLink,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
  ],
  templateUrl: './header-toolbar.component.html',
  styleUrl: './header-toolbar.component.scss',
})
export class HeaderToolbarComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  private readonly navigationEnd = toSignal(
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)),
  );

  readonly isOnCreatePost = computed(() => {
    this.navigationEnd();
    return this.router.url === '/create-post';
  });

  unreadCount = signal(0);

  ngOnInit(): void {
    this.loadUnreadCount();
  }

  private loadUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => this.unreadCount.set(Number(res.count)),
      error: () => this.unreadCount.set(0),
    });
  }

  openNotifications(): void {
    const ref = this.dialog.open(NotificationsDialogComponent, {
      width: '420px',
      maxHeight: '80vh',
      panelClass: 'trainhub-dialog',
    });

    ref.afterClosed().subscribe(() => {
      this.unreadCount.set(0);
    });
  }

  goToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }

  logout(): void {
    this.authService.logout();
  }
}
