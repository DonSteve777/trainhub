import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import {
  FeedService,
  FeedPostDto,
  FeedPostType,
  FeedTrainingTag,
  LikeToggleDto,
  JoinToggleDto,
  WodCheckinAuthorDto,
} from '../../core/services/feed.service';
import {
  CommentsDialogComponent,
  CommentsDialogResult,
} from '../feed/comments-dialog/comments-dialog.component';
import { PostContentComponent } from '../feed/post-content/post-content.component';

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
}

@Component({
  selector: 'app-user-posts-feed',
  standalone: true,
  imports: [MatIconModule, MatDialogModule, RouterLink, PostContentComponent],
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
        // #region agent log
        fetch('http://127.0.0.1:7276/ingest/5d8934ba-b284-461b-918f-bda8d35250fc',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'9971ba'},body:JSON.stringify({sessionId:'9971ba',runId:'post-fix',hypothesisId:'A',location:'user-posts-feed.component.ts:next',message:'getUserPosts ok',data:{userId,count:feed?.length??0,types:(feed??[]).slice(0,5).map(p=>p.postType)},timestamp:Date.now()})}).catch(()=>{});
        // #endregion
        this.posts.set(feed.map(dto => this.mapDto(dto)));
        this.loading.set(false);
      },
      error: err => {
        // #region agent log
        fetch('http://127.0.0.1:7276/ingest/5d8934ba-b284-461b-918f-bda8d35250fc',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'9971ba'},body:JSON.stringify({sessionId:'9971ba',runId:'post-fix',hypothesisId:'A',location:'user-posts-feed.component.ts:error',message:'getUserPosts error',data:{userId,status:err?.status,body:err?.error?.message??err?.message},timestamp:Date.now()})}).catch(()=>{});
        // #endregion
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
      default:
        return postType;
    }
  }

  participantsLabel(count: number): string {
    return count === 1 ? '1 participante' : `${count} participantes`;
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
    };
  }
}
