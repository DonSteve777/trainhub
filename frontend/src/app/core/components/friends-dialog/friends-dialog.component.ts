import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { FriendPreviewDto, UserService } from '../../services/user.service';

@Component({
  selector: 'app-friends-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatIconModule, MatButtonModule, RouterLink],
  templateUrl: './friends-dialog.component.html',
  styleUrl: './friends-dialog.component.scss',
})
export class FriendsDialogComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(MatDialogRef<FriendsDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);

  readonly friends = signal<FriendPreviewDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly processingIds = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.userService.getFriends().subscribe({
      next: (data) => {
        this.friends.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus amigos.');
        this.loading.set(false);
      },
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  avatarUrl(friend: FriendPreviewDto): string {
    return friend.photoUrl ?? `https://i.pravatar.cc/80?u=${encodeURIComponent(friend.username)}`;
  }

  removeFriend(friend: FriendPreviewDto, event: Event): void {
    event.stopPropagation();
    if (this.processingIds().has(friend.id)) return;

    this.processingIds.update((s) => {
      const next = new Set(s);
      next.add(friend.id);
      return next;
    });

    // Optimistic UI: quitamos de la lista antes de llamar al backend.
    const previous = this.friends();
    this.friends.set(previous.filter((f) => f.id !== friend.id));

    this.userService.removeFriend(friend.id).subscribe({
      next: () => {
        this.processingIds.update((s) => {
          const next = new Set(s);
          next.delete(friend.id);
          return next;
        });
      },
      error: () => {
        this.friends.set(previous);
        this.processingIds.update((s) => {
          const next = new Set(s);
          next.delete(friend.id);
          return next;
        });
        this.snackBar.open('No se pudo eliminar la amistad', 'Cerrar', { duration: 3000 });
      },
    });
  }
}

