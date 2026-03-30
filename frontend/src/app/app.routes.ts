import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ConfirmEmailComponent } from './features/confirm-email/confirm-email.component';
import { UserProfileComponent } from './features/user-profile/user-profile.component';
import { VerifyEmailComponent } from './features/verify-email/verify-email.component';
import { RegisterFormComponent } from './features/register-form/register-form.component';
import { FeedComponent } from './features/feed/feed.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'feed', component: FeedComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'user-profile', component: UserProfileComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'register-form', component: RegisterFormComponent },
  { path: '**', redirectTo: '' },
];
