import { Component, OnInit, QueryList, ViewChildren, ElementRef, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { RaceDistributionChartComponent } from '../../shared/components/race-distribution-chart/race-distribution-chart.component';
import { PerformanceRadarChartComponent, RadarSegment } from '../../shared/components/performance-radar-chart/performance-radar-chart.component';
import { forkJoin, of, switchMap } from 'rxjs';
import { FeedService, FeedPostDto, FeedHistoryDto, LikeToggleDto } from '../../core/services/feed.service';
import { CommentsDialogComponent, CommentsDialogResult } from '../feed/comments-dialog/comments-dialog.component';

interface SegmentStat {
  label: string;
  allTimes: number[];
  athleteTime: number;
}

interface RaceStats {
  allTotalTimes: number[];
  allRunTimes: number[];
  allWorkoutTimes: number[];
  athleteTotal: number;
  athleteRun: number;
  athleteWorkout: number;
  runSegments: SegmentStat[];
  workoutSegments: SegmentStat[];
  radarSegments: RadarSegment[];
}

interface FeedPost {
  id: number;
  userId: number;
  username: string;
  avatarUrl: string;
  description: string;
  category: string | null;
  mateUserId: number | null;
  mateUsername: string | null;
  stats: RaceStats;
  currentSlide: number;
  likes: number;
  liked: boolean;
  commentsCount: number;
  creationDate: string;
}

@Component({
  selector: 'app-user-posts-feed',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    RouterLink,
    RaceDistributionChartComponent,
    PerformanceRadarChartComponent,
  ],
  templateUrl: './user-posts-feed.component.html',
  styleUrl: './user-posts-feed.component.scss',
})
export class UserPostsFeedComponent implements OnInit {
  @ViewChildren('carousel') carouselRefs!: QueryList<ElementRef<HTMLElement>>;

  readonly slides = ['total', 'runSegments', 'workoutSegments', 'radar'] as const;

  posts = signal<FeedPost[]>([]);
  loading = signal(true);

  userInfo = computed(() => {
    const p = this.posts();
    return p.length > 0 ? { username: p[0].username, avatarUrl: p[0].avatarUrl } : null;
  });

  private historyByCategory = new Map<string, FeedHistoryDto>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
    private readonly feedService: FeedService,
  ) {}

  ngOnInit(): void {
    const userId = Number(this.route.snapshot.paramMap.get('userId'));

    this.feedService.getUserPosts(userId).pipe(
      switchMap((feed) => {
        if (feed.length === 0) {
          return of({ feed, entries: [] as [string, FeedHistoryDto][] });
        }
        const uniqueCats = [...new Set(feed.map(d => d.category ?? 'INDIVIDUAL_MALE'))];
        const requests = Object.fromEntries(
          uniqueCats.map(cat => [cat, this.feedService.getHistory(cat)]),
        );
        return forkJoin(requests).pipe(
          switchMap((results) => of({ feed, entries: Object.entries(results) as [string, FeedHistoryDto][] })),
        );
      }),
    ).subscribe({
      next: ({ feed, entries }) => {
        entries.forEach(([cat, hist]) => this.historyByCategory.set(cat, hist));
        this.posts.set(feed.map((dto) => this.mapDto(dto)));
        this.loading.set(false);
      },
      error: (err) => {
        console.error('UserPostsFeed error', err);
        this.loading.set(false);
      },
    });
  }

  prevSlide(index: number, post: FeedPost): void {
    if (post.currentSlide <= 0) return;
    const newSlide = post.currentSlide - 1;
    this.posts.update(posts =>
      posts.map(p => p.id === post.id ? { ...p, currentSlide: newSlide } : p),
    );
    this.moveCarousel(index, newSlide);
  }

  nextSlide(index: number, post: FeedPost): void {
    if (post.currentSlide >= this.slides.length - 1) return;
    const newSlide = post.currentSlide + 1;
    this.posts.update(posts =>
      posts.map(p => p.id === post.id ? { ...p, currentSlide: newSlide } : p),
    );
    this.moveCarousel(index, newSlide);
  }

  toggleLike(post: FeedPost): void {
    const optimisticLiked = !post.liked;
    const optimisticCount = post.likes + (post.liked ? -1 : 1);

    this.posts.update(posts =>
      posts.map(p =>
        p.id === post.id
          ? { ...p, liked: optimisticLiked, likes: optimisticCount }
          : p,
      ),
    );

    this.feedService.toggleLike(post.id).subscribe({
      next: (res: LikeToggleDto) => {
        this.posts.update(posts =>
          posts.map(p =>
            p.id === post.id
              ? { ...p, liked: res.liked, likes: res.likesCount }
              : p,
          ),
        );
      },
      error: () => {
        this.posts.update(posts =>
          posts.map(p =>
            p.id === post.id
              ? { ...p, liked: post.liked, likes: post.likes }
              : p,
          ),
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
          posts.map(p => p.id === post.id ? { ...p, commentsCount: result.newCommentsCount } : p),
        );
      }
    });
  }

  private mapDto(dto: FeedPostDto): FeedPost {
    const history = this.historyByCategory.get(dto.category ?? 'INDIVIDUAL_MALE')!;
    const runTimes = [
      dto.r1Time, dto.r2Time, dto.r3Time, dto.r4Time,
      dto.r5Time, dto.r6Time, dto.r7Time, dto.r8Time,
    ].map(Number);

    const workoutTimes = [
      dto.skiErgTime, dto.sledPushTime, dto.sledPullTime, dto.burpeeBjTime,
      dto.rowTime, dto.farmersCarryTime, dto.sandbagLungesTime, dto.wallBallsTime,
    ].map(Number);

    const athleteRun = runTimes.reduce((a, b) => a + b, 0);
    const athleteWorkout = workoutTimes.reduce((a, b) => a + b, 0);
    const athleteTotal = Number(dto.totalTime);

    return {
      id: dto.id,
      userId: dto.userId,
      username: dto.username,
      avatarUrl: dto.photoUrl ?? `https://i.pravatar.cc/48?u=${dto.userId}`,
      description: dto.description ?? '',
      category: dto.category ?? null,
      mateUserId: dto.mateId ?? null,
      mateUsername: dto.mateUsername ?? null,
      stats: {
        allTotalTimes: history.totalsHistory,
        allRunTimes: history.r1History.map((_, i) =>
          history.r1History[i] + history.r2History[i] + history.r3History[i] + history.r4History[i] +
          history.r5History[i] + history.r6History[i] + history.r7History[i] + history.r8History[i],
        ),
        allWorkoutTimes: history.w1History.map((_, i) =>
          history.w1History[i] + history.w2History[i] + history.w3History[i] + history.w4History[i] +
          history.w5History[i] + history.w6History[i] + history.w7History[i] + history.w8History[i],
        ),
        athleteTotal,
        athleteRun,
        athleteWorkout,
        runSegments: [
          { label: 'Running 1', allTimes: history.r1History, athleteTime: Number(dto.r1Time) },
          { label: 'Running 2', allTimes: history.r2History, athleteTime: Number(dto.r2Time) },
          { label: 'Running 3', allTimes: history.r3History, athleteTime: Number(dto.r3Time) },
          { label: 'Running 4', allTimes: history.r4History, athleteTime: Number(dto.r4Time) },
          { label: 'Running 5', allTimes: history.r5History, athleteTime: Number(dto.r5Time) },
          { label: 'Running 6', allTimes: history.r6History, athleteTime: Number(dto.r6Time) },
          { label: 'Running 7', allTimes: history.r7History, athleteTime: Number(dto.r7Time) },
          { label: 'Running 8', allTimes: history.r8History, athleteTime: Number(dto.r8Time) },
        ],
        workoutSegments: [
          { label: 'SkiErg',      allTimes: history.w1History, athleteTime: Number(dto.skiErgTime) },
          { label: 'Sled Push',   allTimes: history.w2History, athleteTime: Number(dto.sledPushTime) },
          { label: 'Sled Pull',   allTimes: history.w3History, athleteTime: Number(dto.sledPullTime) },
          { label: 'Burpee BJ',   allTimes: history.w4History, athleteTime: Number(dto.burpeeBjTime) },
          { label: 'Row',         allTimes: history.w5History, athleteTime: Number(dto.rowTime) },
          { label: 'Farmers C.',  allTimes: history.w6History, athleteTime: Number(dto.farmersCarryTime) },
          { label: 'S. Lunges',   allTimes: history.w7History, athleteTime: Number(dto.sandbagLungesTime) },
          { label: 'Wall Balls',  allTimes: history.w8History, athleteTime: Number(dto.wallBallsTime) },
        ],
        radarSegments: this.buildRadarSegments(dto, history),
      },
      currentSlide: 0,
      likes: dto.likesCount ?? 0,
      liked: dto.likedByCurrentUser ?? false,
      commentsCount: dto.commentsCount ?? 0,
      creationDate: dto.creationDate,
    };
  }

  private buildRadarSegments(dto: FeedPostDto, history: FeedHistoryDto): RadarSegment[] {
    const allRunTimes = history.r1History.map((_, i) =>
      history.r1History[i] + history.r2History[i] + history.r3History[i] + history.r4History[i] +
      history.r5History[i] + history.r6History[i] + history.r7History[i] + history.r8History[i],
    );
    const athleteRun =
      Number(dto.r1Time) + Number(dto.r2Time) + Number(dto.r3Time) + Number(dto.r4Time) +
      Number(dto.r5Time) + Number(dto.r6Time) + Number(dto.r7Time) + Number(dto.r8Time);

    return [
      { label: 'Running',    allTimes: allRunTimes,       athleteTime: athleteRun },
      { label: 'SkiErg',     allTimes: history.w1History, athleteTime: Number(dto.skiErgTime) },
      { label: 'Sled Push',  allTimes: history.w2History, athleteTime: Number(dto.sledPushTime) },
      { label: 'Sled Pull',  allTimes: history.w3History, athleteTime: Number(dto.sledPullTime) },
      { label: 'Burpee BJ',  allTimes: history.w4History, athleteTime: Number(dto.burpeeBjTime) },
      { label: 'Row',        allTimes: history.w5History, athleteTime: Number(dto.rowTime) },
      { label: 'Farmers C.', allTimes: history.w6History, athleteTime: Number(dto.farmersCarryTime) },
      { label: 'S. Lunges',  allTimes: history.w7History, athleteTime: Number(dto.sandbagLungesTime) },
      { label: 'Wall Balls', allTimes: history.w8History, athleteTime: Number(dto.wallBallsTime) },
    ];
  }

  private moveCarousel(carouselIndex: number, slideIndex: number): void {
    const carousel = this.carouselRefs.get(carouselIndex)?.nativeElement;
    const track = carousel?.querySelector<HTMLElement>('.carousel-track');
    if (!track || !carousel) return;
    track.style.transform = `translateX(-${slideIndex * carousel.offsetWidth}px)`;
  }
}
