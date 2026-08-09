import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { FeedService, WodCheckinAuthorDto } from '../../services/feed.service';

export interface WodCheckinsDialogData {
  wodPostId: number;
  wodTitle: string | null;
}

@Component({
  selector: 'app-wod-checkins-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule, DatePipe, RouterLink],
  templateUrl: './wod-checkins-dialog.component.html',
  styleUrl: './wod-checkins-dialog.component.scss',
})
export class WodCheckinsDialogComponent implements OnInit {
  private readonly feedService = inject(FeedService);
  private readonly dialogRef = inject(MatDialogRef<WodCheckinsDialogComponent>);
  readonly data = inject<WodCheckinsDialogData>(MAT_DIALOG_DATA);

  authors = signal<WodCheckinAuthorDto[]>([]);
  loading = signal(true);
  error = signal(false);

  ngOnInit(): void {
    this.feedService.getWodParticipants(this.data.wodPostId).subscribe({
      next: authors => {
        this.authors.set(authors);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  avatarUrl(author: WodCheckinAuthorDto): string {
    return author.photoUrl ?? `https://i.pravatar.cc/40?u=${author.userId}`;
  }

  close(): void {
    this.dialogRef.close();
  }
}
