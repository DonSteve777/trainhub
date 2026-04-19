import { Component, OnInit, QueryList, ViewChildren, ElementRef } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RaceDistributionChartComponent } from '../../shared/components/race-distribution-chart/race-distribution-chart.component';
import { FeedService, FeedPostDto } from '../../core/services/feed.service';

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

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatDialogModule, RaceDistributionChartComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnInit {
  @ViewChildren('carousel') carouselRefs!: QueryList<ElementRef<HTMLElement>>;

  readonly slides = ['total', 'workouts', 'runs'] as const;

  posts: FeedPost[] = [];

  constructor(
    private readonly dialog: MatDialog,
    private readonly feedService: FeedService,
  ) {}

  ngOnInit(): void {
    this.feedService.getFeed().subscribe((dtos) => {
      this.posts = dtos.map((dto) => this.mapDto(dto));
    });
  }

  prevSlide(index: number, post: FeedPost): void {
    if (post.currentSlide <= 0) return;
    post.currentSlide--;
    this.moveCarousel(index, post.currentSlide);
  }

  nextSlide(index: number, post: FeedPost): void {
    if (post.currentSlide >= this.slides.length - 1) return;
    post.currentSlide++;
    this.moveCarousel(index, post.currentSlide);
  }

  toggleLike(post: FeedPost): void {
    post.liked = !post.liked;
    post.likes += post.liked ? 1 : -1;
  }

  openComments(post: FeedPost): void {
    console.log("Implementar más tarde...");
  }

  private mapDto(dto: FeedPostDto): FeedPost {
    return {
      id: dto.id,
      username: dto.username,
      avatarUrl: dto.photoUrl ?? `https://i.pravatar.cc/48?u=${dto.userId}`,
      description: dto.description ?? '',
      stats: {
        allTotalTimes: dto.allTotalTimes,
        allRunTimes: dto.allRunTimes,
        allWorkoutTimes: dto.allWorkoutTimes,
        athleteTotal: dto.totalTime,
        athleteRun: dto.athleteRunTime,
        athleteWorkout: dto.athleteWorkoutTime,
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
