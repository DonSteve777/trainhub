import {
  Component,
  OnInit,
  OnDestroy,
  Input,
  computed,
  inject,
  signal,
  DestroyRef,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, debounceTime, switchMap, distinctUntilChanged, catchError } from 'rxjs';
import { Subject, EMPTY } from 'rxjs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { UserService, UserSearchResultDto } from '../../services/user.service';
import { NotificationsDialogComponent } from '../notifications-dialog/notifications-dialog.component';
import { ProfileMenuDialogComponent } from '../profile-menu-dialog/profile-menu-dialog.component';
import { QuickMarkDialogComponent } from '../../../features/objetivos/quick-mark-dialog/quick-mark-dialog.component';

@Component({
  selector: 'app-header-toolbar',
  imports: [
    RouterLink,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
  ],
  templateUrl: './header-toolbar.component.html',
  styleUrl: './header-toolbar.component.scss',
})
export class HeaderToolbarComponent implements OnInit, OnDestroy {
  @Input() panel: 'left' | 'right' = 'left';
  @ViewChild('searchInput') searchInputRef?: ElementRef<HTMLInputElement>;

  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  private readonly navigationEnd = toSignal(
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)),
  );

  readonly isOnCreatePost = computed(() => {
    this.navigationEnd();
    return this.router.url === '/create-post';
  });

  readonly isOnCreateBoxPost = computed(() => {
    this.navigationEnd();
    return this.router.url === '/create-box-post';
  });

  readonly isOnObjetivos = computed(() => {
    this.navigationEnd();
    return this.router.url === '/objetivos' || this.router.url.startsWith('/objetivos?');
  });

  readonly isBoxAdmin = signal(false);

  unreadCount = signal(0);

  readonly searchOpen = signal(false);
  readonly searchQuery = signal('');
  readonly searchResults = signal<UserSearchResultDto[]>([]);
  readonly isSearching = signal(false);

  private readonly searchSubject = new Subject<string>();

  ngOnInit(): void {
    if (this.panel === 'right') {
      this.loadUnreadCount();
      this.initSearch();
      this.loadBoxAdminStatus();
    }
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  private loadUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => this.unreadCount.set(Number(res.count)),
      error: () => this.unreadCount.set(0),
    });
  }

  private loadBoxAdminStatus(): void {
    this.userService.getProfile().subscribe({
      next: (profile) => this.isBoxAdmin.set(profile.role === 'BOX_ADMIN'),
      error: () => this.isBoxAdmin.set(false),
    });
  }

  private initSearch(): void {
    this.searchSubject
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((q) => {
          if (!q || q.trim().length === 0) {
            this.searchResults.set([]);
            this.isSearching.set(false);
            return EMPTY;
          }
          this.isSearching.set(true);
          return this.userService.searchUsers(q.trim(), 3).pipe(
            catchError(() => {
              this.isSearching.set(false);
              this.searchResults.set([]);
              return EMPTY;
            }),
          );
        }),
      )
      .subscribe({
        next: (results) => {
          this.searchResults.set(results);
          this.isSearching.set(false);
        },
        error: () => {
          this.searchResults.set([]);
          this.isSearching.set(false);
        },
      });
  }

  toggleSearch(): void {
    const isOpen = this.searchOpen();
    if (isOpen) {
      this.closeSearch();
    } else {
      this.searchOpen.set(true);
      setTimeout(() => this.searchInputRef?.nativeElement.focus(), 50);
    }
  }

  closeSearch(): void {
    this.searchOpen.set(false);
    this.searchQuery.set('');
    this.searchResults.set([]);
    this.isSearching.set(false);
  }

  onSearchInput(value: string): void {
    this.searchQuery.set(value);
    this.searchSubject.next(value);
  }

  openNotifications(): void {
    const ref = this.dialog.open(NotificationsDialogComponent, {
      width: '420px',
      maxHeight: '80vh',
      panelClass: 'trainhub-dialog',
    });

    ref.afterClosed().subscribe(() => {
      this.unreadCount.set(0);
    });
  }

  openProfileMenu(): void {
    this.dialog.open(ProfileMenuDialogComponent, {
      width: '380px',
      panelClass: 'trainhub-dialog',
    });
  }

  navigateToUser(user: UserSearchResultDto): void {
    if (user.friendshipStatus !== 'FRIEND') return;
    this.closeSearch();
    this.router.navigate(['/user', user.id]);
  }

  requestFriendship(user: UserSearchResultDto, event: Event): void {
    event.stopPropagation();
    // Actualización optimista
    this.searchResults.update((list) =>
      list.map((u) => (u.id === user.id ? { ...u, friendshipStatus: 'PENDING' as const } : u))
    );
    this.userService.sendFriendRequest(user.id).subscribe({
      error: () => {
        // Revertir si falla
        this.searchResults.update((list) =>
          list.map((u) => (u.id === user.id ? { ...u, friendshipStatus: 'NONE' as const } : u))
        );
        this.snackBar.open('No se pudo enviar la solicitud', 'Cerrar', { duration: 3000 });
      },
    });
  }

  goToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }

  goToCreateBoxPost(): void {
    this.router.navigate(['/create-box-post']);
  }

  goToObjetivos(): void {
    this.router.navigate(['/objetivos']);
  }

  openQuickMark(): void {
    this.dialog.open(QuickMarkDialogComponent, {
      width: '440px',
      maxHeight: '90vh',
      panelClass: 'trainhub-dialog',
      autoFocus: 'first-tabbable',
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
