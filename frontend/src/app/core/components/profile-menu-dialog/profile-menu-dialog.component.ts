import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

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

  goToEditProfile(): void {
    this.dialogRef.close();
    this.router.navigate(['/user-profile']);
  }

  goToStats(): void {
    this.dialogRef.close();
    this.router.navigate(['/time-history']);
  }

  goToPosts(): void {
    this.dialogRef.close();
    this.router.navigate(['/my-posts']);
  }

  close(): void {
    this.dialogRef.close();
  }
}
