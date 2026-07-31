import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { FeedService, FeedPostDto, LikeToggleDto } from '../../core/services/feed.service';
import {
  CommentsDialogComponent,
  CommentsDialogResult,
} from '../feed/comments-dialog/comments-dialog.component';

interface FeedPost {
  id: number;
  userId: number;
  username: string;
  avatarUrl: string;
  description: string;
  mateUserId: number | null;
  mateUsername: string | null;
  likes: number;
  liked: boolean;
  commentsCount: number;
}

@Component({
  selector: 'app-user-posts-feed',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatDialogModule, RouterLink],
  templateUrl: './user-posts-feed.component.html',
  styleUrl: './user-posts-feed.component.scss',
})
export class UserPostsFeedComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly feedService = inject(FeedService);

  posts = signal<FeedPost[]>([]);
  loading = signal(true);
  targetUserId = signal<number | null>(null);

  userInfo = computed(() => {
    const p = this.posts();
    return p.length > 0 ? { username: p[0].username, avatarUrl: p[0].avatarUrl } : null;
  });

  ngOnInit(): void {
    const userId = Number(this.route.snapshot.paramMap.get('userId'));
    this.targetUserId.set(userId);

    this.feedService.getUserPosts(userId).subscribe({
      next: feed => {
        this.posts.set(feed.map(dto => this.mapDto(dto)));
        this.loading.set(false);
      },
      error: err => {
        console.error('UserPostsFeed error', err);
        this.loading.set(false);
      },
    });
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
      mateUserId: dto.mateId ?? null,
      mateUsername: dto.mateUsername ?? null,
      likes: dto.likesCount ?? 0,
      liked: dto.likedByCurrentUser ?? false,
      commentsCount: dto.commentsCount ?? 0,
    };
  }
}
