import { Component, OnInit, AfterViewInit, OnDestroy, QueryList, ViewChildren, ViewChild, ElementRef, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RaceDistributionChartComponent } from '../../shared/components/race-distribution-chart/race-distribution-chart.component';
import { PerformanceRadarChartComponent, RadarSegment } from '../../shared/components/performance-radar-chart/performance-radar-chart.component';
import { forkJoin } from 'rxjs';
import { FeedService, FeedPostDto, FeedHistoryDto } from '../../core/services/feed.service';

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
  username: string;
  avatarUrl: string;
  description: string;
  stats: RaceStats;
  currentSlide: number;
  likes: number;
  liked: boolean;
  commentsCount: number;
  creationDate: string;
}

const PAGE_SIZE = 5;

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatDialogModule, RaceDistributionChartComponent, PerformanceRadarChartComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChildren('carousel') carouselRefs!: QueryList<ElementRef<HTMLElement>>;
  @ViewChild('feedContainer') feedContainerRef!: ElementRef<HTMLElement>;
  @ViewChild('sentinel') sentinelRef!: ElementRef<HTMLElement>;

  readonly slides = ['total', 'runSegments', 'workoutSegments', 'radar'] as const;

  posts = signal<FeedPost[]>([]);
  hasMore = signal(true);
  loading = signal(false);

  private cursorDate: string | null = null;
  private cursorId: number | null = null;
  private cachedHistory: FeedHistoryDto | null = null;
  private observer: IntersectionObserver | null = null;

  constructor(
    private readonly dialog: MatDialog,
    private readonly feedService: FeedService,
  ) {}

  ngOnInit(): void {
    forkJoin({
      feed: this.feedService.getFeed(PAGE_SIZE),
      history: this.feedService.getHistory(),
    }).subscribe({
      next: ({ feed, history }) => {
        this.cachedHistory = history;
        this.posts.set(feed.map((dto) => this.mapDto(dto, history)));
        this.updateCursor(feed);
        if (feed.length < PAGE_SIZE) this.hasMore.set(false);
      },
      error: (err) => {
        console.error('Feed HTTP error', err);
      },
    });
  }

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) this.loadNextPage();
      },
      { root: this.feedContainerRef.nativeElement, rootMargin: '200px', threshold: 0 },
    );
    this.observer.observe(this.sentinelRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private loadNextPage(): void {
    if (!this.hasMore() || this.loading() || !this.cursorDate || this.cursorId === null || !this.cachedHistory) return;
    this.loading.set(true);
    this.feedService.getNextPage(this.cursorDate, this.cursorId, PAGE_SIZE).subscribe({
      next: (newPosts) => {
        this.posts.update(posts => [
          ...posts,
          ...newPosts.map(dto => this.mapDto(dto, this.cachedHistory!)),
        ]);
        this.updateCursor(newPosts);
        if (newPosts.length < PAGE_SIZE) this.hasMore.set(false);
        this.loading.set(false);
      },
      error: (err) => {
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
    this.posts.update(posts =>
      posts.map(p =>
        p.id === post.id
          ? { ...p, liked: !p.liked, likes: p.likes + (p.liked ? -1 : 1) }
          : p,
      ),
    );
  }

  openComments(post: FeedPost): void {
    console.log("Implementar más tarde...");
  }

  private mapDto(dto: FeedPostDto, history: FeedHistoryDto): FeedPost {
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
      username: dto.username,
      avatarUrl: dto.photoUrl ?? `https://i.pravatar.cc/48?u=${dto.userId}`,
      description: dto.description ?? '',
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
      liked: false,
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
