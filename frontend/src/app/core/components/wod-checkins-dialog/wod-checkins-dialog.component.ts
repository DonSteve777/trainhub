import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { FeedService, WodCheckinAuthorDto } from '../../services/feed.service';

export interface WodCheckinsDialogData {
  wodPostId?: number;
  wodTitle?: string | null;
  /** Título principal del diálogo. */
  dialogTitle?: string;
  /** Icono Material del encabezado. */
  headerIcon?: string;
  /** Lista precargada (p. ej. objetivos en mock). Si no hay wodPostId, no se llama a la API. */
  participants?: WodCheckinAuthorDto[];
  emptyMessage?: string;
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
    if (this.data.participants) {
      this.authors.set(this.data.participants);
      this.loading.set(false);
      return;
    }

    if (!this.data.wodPostId) {
      this.loading.set(false);
      return;
    }

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

  dialogTitle = (): string => this.data.dialogTitle ?? 'Quién va a participar';

  headerIcon = (): string => this.data.headerIcon ?? 'fitness_center';

  emptyMessage = (): string => this.data.emptyMessage ?? 'Nadie se ha apuntado todavía.';

  close(): void {
    this.dialogRef.close();
  }
}
