import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService, NotificationDto } from '../../services/notification.service';
import { UserService } from '../../services/user.service';
import { LikersDialogComponent, LikersDialogData } from '../likers-dialog/likers-dialog.component';
import { CommentsDialogComponent } from '../../../features/feed/comments-dialog/comments-dialog.component';

@Component({
  selector: 'app-notifications-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule],
  templateUrl: './notifications-dialog.component.html',
  styleUrl: './notifications-dialog.component.scss',
})
export class NotificationsDialogComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(MatDialogRef<NotificationsDialogComponent>);
  private readonly dialog = inject(MatDialog);

  notifications = signal<NotificationDto[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  processingIds = signal<Set<number>>(new Set());

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

  onItemClick(n: NotificationDto): void {
    if (n.type === 'LIKE') {
      this.openLikers(n);
    } else if (n.type === 'COMMENT') {
      this.openComments(n);
    }
  }

  acceptRequest(n: NotificationDto, event: Event): void {
    event.stopPropagation();
    if (!n.actorId) return;
    this.processingIds.update(s => new Set(s).add(n.actorId!));
    this.userService.acceptFriendRequest(n.actorId).subscribe({
      next: () => this.removeFriendRequestNotification(n.actorId!),
      error: () => this.processingIds.update(s => { const next = new Set(s); next.delete(n.actorId!); return next; }),
    });
  }

  rejectRequest(n: NotificationDto, event: Event): void {
    event.stopPropagation();
    if (!n.actorId) return;
    this.processingIds.update(s => new Set(s).add(n.actorId!));
    this.userService.rejectFriendRequest(n.actorId).subscribe({
      next: () => this.removeFriendRequestNotification(n.actorId!),
      error: () => this.processingIds.update(s => { const next = new Set(s); next.delete(n.actorId!); return next; }),
    });
  }

  private removeFriendRequestNotification(actorId: number): void {
    this.notifications.update(list => list.filter(n => n.actorId !== actorId));
    this.processingIds.update(s => { const next = new Set(s); next.delete(actorId); return next; });
  }

  private openLikers(n: NotificationDto): void {
    const data: LikersDialogData = {
      postId: n.postId!,
      postCreationDate: n.postCreationDate!,
    };
    this.dialog.open(LikersDialogComponent, {
      data,
      width: '400px',
      maxHeight: '70vh',
      panelClass: 'trainhub-dialog',
    });
  }

  private openComments(n: NotificationDto): void {
    this.dialog.open(CommentsDialogComponent, {
      data: {
        postId: n.postId,
        username: n.lastActorUsername,
        avatarUrl: this.avatarUrl(n.lastActorPhotoUrl, n.lastActorUsername),
      },
      width: '500px',
      maxWidth: '95vw',
      height: '70vh',
      panelClass: 'th-comments-panel',
    });
  }

  buildText(n: NotificationDto): string {
    if (n.type === 'FRIEND_REQUEST') {
      return 'te ha enviado una solicitud de amistad';
    }
    const date = this.formatDate(n.postCreationDate!);
    if (n.type === 'LIKE') {
      if (n.totalCount === 1) {
        return `ha dado like a tu publicación del ${date}`;
      }
      const others = n.totalCount - 1;
      return `y ${others} ${others === 1 ? 'persona más han' : 'personas más han'} dado like a tu publicación del ${date}`;
    } else {
      if (n.totalCount === 1) {
        return `ha comentado en tu publicación del ${date}`;
      }
      const others = n.totalCount - 1;
      return `y ${others} ${others === 1 ? 'persona más han' : 'personas más han'} comentado en tu publicación del ${date}`;
    }
  }

  iconForType(type: 'LIKE' | 'COMMENT' | 'FRIEND_REQUEST'): string {
    if (type === 'LIKE') return 'favorite';
    if (type === 'FRIEND_REQUEST') return 'person_add';
    return 'chat_bubble';
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
