import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ConfirmEmailComponent } from './features/confirm-email/confirm-email.component';
import { UserProfileComponent } from './features/user-profile/user-profile.component';
import { VerifyEmailComponent } from './features/verify-email/verify-email.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'user-profile', component: UserProfileComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: '**', redirectTo: '' },
];
