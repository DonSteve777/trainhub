import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomeComponent } from './features/home/home.component';
import { ConfirmEmailComponent } from './features/confirm-email/confirm-email.component';
import { UserProfileComponent } from './features/user-profile/user-profile.component';
import { VerifyEmailComponent } from './features/verify-email/verify-email.component';
import { RegisterFormComponent } from './features/register-form/register-form.component';
import { FeedComponent } from './features/feed/feed.component';
import { CreatePostComponent } from './features/create-post/create-post.component';
import { CreateBoxPostComponent } from './features/create-box-post/create-box-post.component';
import { ForgotPasswordComponent } from './features/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './features/reset-password/reset-password.component';
import { UserPostsFeedComponent } from './features/user-posts-feed/user-posts-feed.component';
import { ObjetivosComponent } from './features/objetivos/objetivos.component';
import { NuevaMarcaComponent } from './features/objetivos/nueva-marca/nueva-marca.component';
import { NuevoObjetivoComponent } from './features/objetivos/nuevo-objetivo/nuevo-objetivo.component';
import { GoalCreatedDemoComponent } from './features/feed/demo/goal-created-demo.component';
import { GoalMarkDemoComponent } from './features/feed/demo/goal-mark-demo.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'feed', component: FeedComponent, canActivate: [authGuard] },
  { path: 'create-post', component: CreatePostComponent, canActivate: [authGuard] },
  { path: 'create-box-post', component: CreateBoxPostComponent, canActivate: [authGuard] },
  { path: 'objetivos', component: ObjetivosComponent, canActivate: [authGuard] },
  { path: 'nuevo-objetivo', component: NuevoObjetivoComponent, canActivate: [authGuard] },
  { path: 'nueva-marca', component: NuevaMarcaComponent, canActivate: [authGuard] },
  { path: 'demo/post-objetivo-creado', component: GoalCreatedDemoComponent },
  { path: 'demo/post-marca-objetivo', component: GoalMarkDemoComponent },
  { path: 'confirm-email/:token', component: ConfirmEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password/:token', component: ResetPasswordComponent },
  { path: 'user-profile', component: UserProfileComponent, canActivate: [authGuard] },
  { path: 'user/:userId', component: UserPostsFeedComponent, canActivate: [authGuard] },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'register-form', component: RegisterFormComponent },
  { path: '**', redirectTo: '' },
];
