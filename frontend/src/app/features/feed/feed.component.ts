import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  inject,
  signal,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import {
  FeedService,
  FeedPostDto,
  FeedPostType,
  FeedTrainingTag,
  LikeToggleDto,
  JoinToggleDto,
  WodCheckinAuthorDto,
  GoalMarkFeedDto,
  GoalFeedDto,
} from '../../core/services/feed.service';
import { UserService, WeeklyConstancyDto } from '../../core/services/user.service';
import { ConstancyBlockComponent } from '../../core/components/constancy-block/constancy-block.component';
import {
  CommentsDialogComponent,
  CommentsDialogResult,
} from './comments-dialog/comments-dialog.component';
import { PostContentComponent } from './post-content/post-content.component';
import { ObjetivosService } from '../objetivos/objetivos.service';

interface FeedPost {
  id: number;
  userId: number;
  username: string;
  avatarUrl: string;
  description: string;
  postType: FeedPostType;
  boxId: number | null;
  title: string | null;
  trainingTag: FeedTrainingTag | null;
  challengeDeadline: string | null;
  creationDate: string;
  participantsCount: number;
  joinedByCurrentUser: boolean;
  likes: number;
  liked: boolean;
  commentsCount: number;
  streakWeeks: number | null;
  weekDayTags: Array<FeedTrainingTag | null> | null;
  wodPostId: number | null;
  wodTitle: string | null;
  wodCheckinsCount: number | null;
  wodCheckinAuthors: WodCheckinAuthorDto[] | null;
  goalMark: GoalMarkFeedDto | null;
  goal: GoalFeedDto | null;
}

const PAGE_SIZE = 5;

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [
    RouterLink,
    MatIconModule,
    MatDialogModule,
    PostContentComponent,
    ConstancyBlockComponent,
  ],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('feedContainer') feedContainerRef!: ElementRef<HTMLElement>;
  @ViewChild('sentinel') sentinelRef!: ElementRef<HTMLElement>;

  private readonly dialog = inject(MatDialog);
  private readonly feedService = inject(FeedService);
  private readonly userService = inject(UserService);
  private readonly objetivosService = inject(ObjetivosService);
  private readonly router = inject(Router);

  posts = signal<FeedPost[]>([]);
  hasMore = signal(true);
  loading = signal(false);

  myConstancy = signal<WeeklyConstancyDto | null>(null);
  currentUserId = signal<number | null>(null);

  private cursorDate: string | null = null;
  private cursorId: number | null = null;
  private observer: IntersectionObserver | null = null;

  ngOnInit(): void {
    this.loadMyConstancy();
    this.loadCurrentUser();
    this.feedService.getFeed(PAGE_SIZE).subscribe({
      next: feed => {
        const mapped = feed.map(dto => this.mapDto(dto));
        this.posts.set(mapped);
        if (feed.length === 0) {
          this.hasMore.set(false);
          return;
        }
        this.updateCursor(feed);
        if (feed.length < PAGE_SIZE) this.hasMore.set(false);
      },
      error: err => {
        console.error('Feed HTTP error', err);
        this.posts.set([]);
        this.hasMore.set(false);
      },
    });
  }

  private loadMyConstancy(): void {
    this.userService.getWeeklyConstancy().subscribe({
      next: constancy => this.myConstancy.set(constancy),
      error: err => {
        console.error('Constancy HTTP error', err);
        this.myConstancy.set(null);
      },
    });
  }

  private loadCurrentUser(): void {
    this.userService.getProfile().subscribe({
      next: profile => this.currentUserId.set(profile.id),
      error: err => {
        console.error('Profile HTTP error', err);
        this.currentUserId.set(null);
      },
    });
  }

  isOwnPost(post: FeedPost): boolean {
    const uid = this.currentUserId();
    return uid != null && post.userId === uid;
  }

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      entries => {
        if (entries[0].isIntersecting) this.loadNextPage();
      },
      { root: this.feedContainerRef.nativeElement, rootMargin: '200px', threshold: 0 }
    );
    this.observer.observe(this.sentinelRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private loadNextPage(): void {
    if (!this.hasMore() || this.loading() || !this.cursorDate || this.cursorId === null) return;
    this.loading.set(true);
    this.feedService.getNextPage(this.cursorDate, this.cursorId, PAGE_SIZE).subscribe({
      next: newPosts => {
        this.posts.update(posts => [...posts, ...newPosts.map(dto => this.mapDto(dto))]);
        this.updateCursor(newPosts);
        if (newPosts.length < PAGE_SIZE) this.hasMore.set(false);
        this.loading.set(false);
      },
      error: err => {
        console.error('Feed pagination error', err);
        this.loading.set(false);
      },
    });
  }

  private updateCursor(feed: FeedPostDto[]): void {
    if (feed.length === 0) return;
    const last = feed[feed.length - 1];
    this.cursorDate = last.creationDate;
    this.cursorId = last.id;
  }

  toggleLike(post: FeedPost): void {
    // Posts mock (id negativo) solo actualizan UI local.
    if (post.id < 0) {
      this.posts.update(posts =>
        posts.map(p =>
          p.id === post.id
            ? {
                ...p,
                liked: !p.liked,
                likes: p.likes + (p.liked ? -1 : 1),
              }
            : p
        )
      );
      return;
    }

    const optimisticLiked = !post.liked;
    const optimisticCount = post.likes + (post.liked ? -1 : 1);

    this.posts.update(posts =>
      posts.map(p =>
        p.id === post.id ? { ...p, liked: optimisticLiked, likes: optimisticCount } : p
      )
    );

    this.feedService.toggleLike(post.id).subscribe({
      next: (res: LikeToggleDto) => {
        this.posts.update(posts =>
          posts.map(p => (p.id === post.id ? { ...p, liked: res.liked, likes: res.likesCount } : p))
        );
      },
      error: () => {
        this.posts.update(posts =>
          posts.map(p => (p.id === post.id ? { ...p, liked: post.liked, likes: post.likes } : p))
        );
      },
    });
  }

  joinGoal(post: FeedPost): void {
    if (post.postType !== 'GOAL_CREATED' || post.joinedByCurrentUser || this.isOwnPost(post)) {
      return;
    }

    const goalId = post.goal?.goalId;
    if (goalId == null) return;

    const previousCount = post.participantsCount;
    const optimisticCount = previousCount + 1;

    this.posts.update(posts =>
      posts.map(p => {
        if (p.id !== post.id) return p;
        return {
          ...p,
          joinedByCurrentUser: true,
          participantsCount: optimisticCount,
          goal: p.goal
            ? { ...p.goal, joinedByCurrentUser: true, participantsCount: optimisticCount }
            : p.goal,
        };
      })
    );

    this.feedService.joinGoal(goalId).subscribe({
      next: res => {
        this.posts.update(posts =>
          posts.map(p => {
            if (p.id !== post.id) return p;
            return {
              ...p,
              joinedByCurrentUser: res.joined,
              participantsCount: res.participantsCount,
              goal: p.goal
                ? {
                    ...p.goal,
                    joinedByCurrentUser: res.joined,
                    participantsCount: res.participantsCount,
                  }
                : p.goal,
            };
          })
        );
        this.objetivosService.refreshAfterJoin();
      },
      error: () => {
        this.posts.update(posts =>
          posts.map(p => {
            if (p.id !== post.id) return p;
            return {
              ...p,
              joinedByCurrentUser: false,
              participantsCount: previousCount,
              goal: p.goal
                ? {
                    ...p.goal,
                    joinedByCurrentUser: false,
                    participantsCount: previousCount,
                  }
                : p.goal,
            };
          })
        );
      },
    });
  }

  toggleJoin(post: FeedPost): void {
    const optimisticJoined = !post.joinedByCurrentUser;
    const optimisticCount = post.participantsCount + (post.joinedByCurrentUser ? -1 : 1);

    this.posts.update(posts =>
      posts.map(p =>
        p.id === post.id
          ? {
              ...p,
              joinedByCurrentUser: optimisticJoined,
              participantsCount: optimisticCount,
              wodCheckinsCount:
                p.postType === 'BOX_WOD' ? optimisticCount : p.wodCheckinsCount,
            }
          : p
      )
    );

    this.feedService.toggleJoin(post.id).subscribe({
      next: (res: JoinToggleDto) => {
        this.posts.update(posts =>
          posts.map(p =>
            p.id === post.id
              ? {
                  ...p,
                  joinedByCurrentUser: res.joined,
                  participantsCount: res.participantsCount,
                  wodCheckinsCount:
                    p.postType === 'BOX_WOD' ? res.participantsCount : p.wodCheckinsCount,
                }
              : p
          )
        );
      },
      error: () => {
        this.posts.update(posts =>
          posts.map(p =>
            p.id === post.id
              ? {
                  ...p,
                  joinedByCurrentUser: post.joinedByCurrentUser,
                  participantsCount: post.participantsCount,
                  wodCheckinsCount: post.wodCheckinsCount,
                }
              : p
          )
        );
      },
    });
  }

  checkInWod(post: FeedPost): void {
    if (post.postType !== 'BOX_WOD') return;
    void this.router.navigate(['/create-post'], {
      queryParams: { wodPostId: post.id },
    });
  }

  typeLabel(postType: FeedPostType): string {
    switch (postType) {
      case 'CHECKIN':
        return 'Check-in';
      case 'BOX_WOD':
        return 'WOD';
      case 'BOX_ANNOUNCEMENT':
        return 'Anuncio';
      case 'BOX_CHALLENGE':
        return 'Reto';
      case 'GOAL_MARK':
        return 'Objetivo';
      case 'GOAL_CREATED':
      case 'GOAL_JOIN':
        return 'Objetivo';
      default:
        return postType;
    }
  }

  formatPostDate(dateStr: string): string {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return '';
    return d.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  openComments(post: FeedPost): void {
    const ref = this.dialog.open(CommentsDialogComponent, {
      data: { postId: post.id, username: post.username, avatarUrl: post.avatarUrl },
      width: '500px',
      maxWidth: '95vw',
      height: '70vh',
      panelClass: 'th-comments-panel',
    });

    ref.afterClosed().subscribe((result?: CommentsDialogResult) => {
      if (result?.newCommentsCount !== undefined) {
        this.posts.update(posts =>
          posts.map(p => (p.id === post.id ? { ...p, commentsCount: result.newCommentsCount } : p))
        );
      }
    });
  }

  private mapDto(dto: FeedPostDto): FeedPost {
    return {
      id: dto.id,
      userId: dto.userId,
      username: dto.username,
      avatarUrl: dto.photoUrl ?? `https://i.pravatar.cc/48?u=${dto.userId}`,
      description: dto.description ?? '',
      postType: dto.postType,
      boxId: dto.boxId ?? null,
      title: dto.title ?? null,
      trainingTag: dto.trainingTag,
      challengeDeadline: dto.challengeDeadline ?? null,
      creationDate: dto.creationDate,
      participantsCount: dto.participantsCount ?? 0,
      joinedByCurrentUser: dto.joinedByCurrentUser ?? false,
      likes: dto.likesCount ?? 0,
      liked: dto.likedByCurrentUser ?? false,
      commentsCount: dto.commentsCount ?? 0,
      streakWeeks: dto.streakWeeks ?? null,
      weekDayTags: dto.weekDayTags ?? null,
      wodPostId: dto.wodPostId ?? null,
      wodTitle: dto.wodTitle ?? null,
      wodCheckinsCount: dto.wodCheckinsCount ?? null,
      wodCheckinAuthors: dto.wodCheckinAuthors ?? null,
      goalMark: dto.goalMark ?? null,
      goal: dto.goal ?? null,
    };
  }
}
