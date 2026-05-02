import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';
import { NotificationService, LikerDto } from '../../services/notification.service';

export interface PostLikersDialogData {
  postId: number;
  postCreationDate: string;
}

@Component({
  selector: 'app-post-likers-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule, DatePipe],
  templateUrl: './post-likers-dialog.component.html',
  styleUrl: './post-likers-dialog.component.scss',
})
export class PostLikersDialogComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly dialogRef = inject(MatDialogRef<PostLikersDialogComponent>);
  readonly data = inject<PostLikersDialogData>(MAT_DIALOG_DATA);

  likers = signal<LikerDto[]>([]);
  loading = signal(true);
  error = signal(false);

  ngOnInit(): void {
    this.notificationService.getPostLikers(this.data.postId).subscribe({
      next: (likers) => {
        this.likers.set(likers);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
