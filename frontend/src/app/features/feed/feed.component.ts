import { Component, OnInit, AfterViewInit, OnDestroy, QueryList, ViewChildren, ViewChild, ElementRef, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RaceDistributionChartComponent } from '../../shared/components/race-distribution-chart/race-distribution-chart.component';
import { forkJoin } from 'rxjs';
import { FeedService, FeedPostDto, FeedHistoryDto } from '../../core/services/feed.service';

interface RaceStats {
  allTotalTimes: number[];
  allRunTimes: number[];
  allWorkoutTimes: number[];
  athleteTotal: number;
  athleteRun: number;
  athleteWorkout: number;
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
  imports: [MatIconModule, MatButtonModule, MatDialogModule, RaceDistributionChartComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChildren('carousel') carouselRefs!: QueryList<ElementRef<HTMLElement>>;
  @ViewChild('feedContainer') feedContainerRef!: ElementRef<HTMLElement>;
  @ViewChild('sentinel') sentinelRef!: ElementRef<HTMLElement>;

  readonly slides = ['total', 'workouts', 'runs'] as const;

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
      dto.w1Time, dto.w2Time, dto.w3Time, dto.w4Time,
      dto.w5Time, dto.w6Time, dto.w7Time, dto.w8Time,
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
        allRunTimes: history.runHistory,
        allWorkoutTimes: history.workoutHistory,
        athleteTotal,
        athleteRun,
        athleteWorkout,
      },
      currentSlide: 0,
      likes: dto.likesCount ?? 0,
      liked: false,
      commentsCount: dto.commentsCount ?? 0,
      creationDate: dto.creationDate,
    };
  }

  private moveCarousel(carouselIndex: number, slideIndex: number): void {
    const carousel = this.carouselRefs.get(carouselIndex)?.nativeElement;
    const track = carousel?.querySelector<HTMLElement>('.carousel-track');
    if (!track || !carousel) return;
    track.style.transform = `translateX(-${slideIndex * carousel.offsetWidth}px)`;
  }
}
