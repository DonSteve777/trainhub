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
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import {
  FeedService,
  FeedPostDto,
  FeedPostType,
  FeedTrainingTag,
  LikeToggleDto,
  JoinToggleDto,
} from '../../core/services/feed.service';
import {
  CommentsDialogComponent,
  CommentsDialogResult,
} from './comments-dialog/comments-dialog.component';
import { PostContentComponent } from './post-content/post-content.component';

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
  participantsCount: number;
  joinedByCurrentUser: boolean;
  mateUserId: number | null;
  mateUsername: string | null;
  likes: number;
  liked: boolean;
  commentsCount: number;
}

const PAGE_SIZE = 5;

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatButtonModule, MatDialogModule, PostContentComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('feedContainer') feedContainerRef!: ElementRef<HTMLElement>;
  @ViewChild('sentinel') sentinelRef!: ElementRef<HTMLElement>;

  private readonly dialog = inject(MatDialog);
  private readonly feedService = inject(FeedService);

  posts = signal<FeedPost[]>([]);
  hasMore = signal(true);
  loading = signal(false);

  private cursorDate: string | null = null;
  private cursorId: number | null = null;
  private observer: IntersectionObserver | null = null;

  ngOnInit(): void {
    this.feedService.getFeed(PAGE_SIZE).subscribe({
      next: feed => {
        if (feed.length === 0) {
          this.hasMore.set(false);
          return;
        }
        this.posts.set(feed.map(dto => this.mapDto(dto)));
        this.updateCursor(feed);
        if (feed.length < PAGE_SIZE) this.hasMore.set(false);
      },
      error: err => {
        console.error('Feed HTTP error', err);
      },
    });
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

  toggleJoin(post: FeedPost): void {
    const optimisticJoined = !post.joinedByCurrentUser;
    const optimisticCount = post.participantsCount + (post.joinedByCurrentUser ? -1 : 1);

    this.posts.update(posts =>
      posts.map(p =>
        p.id === post.id
          ? { ...p, joinedByCurrentUser: optimisticJoined, participantsCount: optimisticCount }
          : p
      )
    );

    this.feedService.toggleJoin(post.id).subscribe({
      next: (res: JoinToggleDto) => {
        this.posts.update(posts =>
          posts.map(p =>
            p.id === post.id
              ? { ...p, joinedByCurrentUser: res.joined, participantsCount: res.participantsCount }
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
                }
              : p
          )
        );
      },
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
      participantsCount: dto.participantsCount ?? 0,
      joinedByCurrentUser: dto.joinedByCurrentUser ?? false,
      mateUserId: dto.mateId ?? null,
      mateUsername: dto.mateUsername ?? null,
      likes: dto.likesCount ?? 0,
      liked: dto.likedByCurrentUser ?? false,
      commentsCount: dto.commentsCount ?? 0,
    };
  }
}
