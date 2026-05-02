import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService, LikerDto } from '../../services/notification.service';

export interface LikersDialogData {
  postId: number;
  postCreationDate: string;
}

@Component({
  selector: 'app-likers-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule],
  templateUrl: './likers-dialog.component.html',
  styleUrl: './likers-dialog.component.scss',
})
export class LikersDialogComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<LikersDialogComponent>);
  readonly data = inject<LikersDialogData>(MAT_DIALOG_DATA);

  likers = signal<LikerDto[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.notificationService.getPostLikers(this.data.postId).subscribe({
      next: (data) => {
        this.likers.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar los likes.');
      },
    });
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
