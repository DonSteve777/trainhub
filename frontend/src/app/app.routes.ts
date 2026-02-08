import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ConfirmEmailComponent } from './features/confirm-email/confirm-email.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: '**', redirectTo: '' },
];
