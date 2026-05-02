import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService, NotificationDto, LikerDto } from '../../services/notification.service';
import { LikersDialogComponent, LikersDialogData } from '../likers-dialog/likers-dialog.component';

@Component({
  selector: 'app-notifications-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule],
  templateUrl: './notifications-dialog.component.html',
  styleUrl: './notifications-dialog.component.scss',
})
export class NotificationsDialogComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<NotificationsDialogComponent>);
  private readonly dialog = inject(MatDialog);

  notifications = signal<NotificationDto[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications.set(data);
        this.loading.set(false);
        this.notificationService.markSeen().subscribe();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar las notificaciones.');
      },
    });
  }

  openLikers(notification: NotificationDto): void {
    const data: LikersDialogData = {
      postId: notification.postId,
      postCreationDate: notification.postCreationDate,
    };
    this.dialog.open(LikersDialogComponent, {
      data,
      width: '400px',
      maxHeight: '70vh',
      panelClass: 'trainhub-dialog',
    });
  }

  buildText(n: NotificationDto): string {
    const date = this.formatDate(n.postCreationDate);
    if (n.totalLikers === 1) {
      return `ha dado like a tu publicación del ${date}`;
    }
    const others = n.totalLikers - 1;
    return `y ${others} ${others === 1 ? 'persona más han' : 'personas más han'} dado like a tu publicación del ${date}`;
  }

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  avatarUrl(photoUrl: string | null, username: string): string {
    return photoUrl ?? `https://i.pravatar.cc/40?u=${encodeURIComponent(username)}`;
  }

  close(): void {
    this.dialogRef.close();
  }
}
