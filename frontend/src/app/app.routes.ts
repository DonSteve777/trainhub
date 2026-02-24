import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ConfirmEmailComponent } from './features/confirm-email/confirm-email.component';
import { UserProfileComponent } from './features/user-profile/user-profile.component';
import { RegisterComponent } from './features/register/register.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'user-profile', component: UserProfileComponent },
   { path: 'register', component: RegisterComponent },
  { path: '**', redirectTo: '' },
];
