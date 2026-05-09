import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-profile-menu-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule],
  templateUrl: './profile-menu-dialog.component.html',
  styleUrl: './profile-menu-dialog.component.scss',
})
export class ProfileMenuDialogComponent {
  private readonly router = inject(Router);
  private readonly dialogRef = inject(MatDialogRef<ProfileMenuDialogComponent>);
  private readonly userService = inject(UserService);

  goToEditProfile(): void {
    this.dialogRef.close();
    this.router.navigate(['/user-profile']);
  }

  goToStats(): void {
    this.dialogRef.close();
    this.router.navigate(['/time-history']);
  }

  goToPosts(): void {
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.dialogRef.close();
        this.router.navigate(['/user', profile.id]);
      },
      error: (err) => console.error('Error obteniendo perfil del usuario', err),
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
