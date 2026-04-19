import { Component, QueryList, ViewChildren, ElementRef } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PostCommentsComponent } from '../post-comments/post-comments.component';

interface FeedPost {
  id: number;
  username: string;
  avatarUrl: string;
  description: string;
  slides: { url: string; alt: string }[];
  currentSlide: number;
  likes: number;
  liked: boolean;
  commentsCount: number;
}

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatDialogModule],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent {
  @ViewChildren('carousel') carouselRefs!: QueryList<ElementRef<HTMLElement>>;

  posts: FeedPost[] = [
    {
      id: 1,
      username: 'carlos_fit',
      avatarUrl: 'https://i.pravatar.cc/48?img=1',
      description: 'Sesión de fuerza. 100 kg en press de banca. Nueva marca personal 💪🔥',
      slides: [
        { url: 'https://picsum.photos/seed/gym1/800/600', alt: 'Press de banca' },
        { url: 'https://picsum.photos/seed/gym2/800/600', alt: 'Sentadilla' },
        { url: 'https://picsum.photos/seed/gym3/800/600', alt: 'Peso muerto' },
      ],
      currentSlide: 0,
      likes: 47,
      liked: false,
      commentsCount: 12,
    },
    {
      id: 2,
      username: 'ana_runner',
      avatarUrl: 'https://i.pravatar.cc/48?img=5',
      description: 'Maratón de Madrid completada ✅ 42 km en 3h 45 min. El mejor día de mi vida deportiva 🏃‍♀️',
      slides: [
        { url: 'https://picsum.photos/seed/run1/800/600', alt: 'Salida maratón' },
        { url: 'https://picsum.photos/seed/run2/800/600', alt: 'Durante la carrera' },
        { url: 'https://picsum.photos/seed/run3/800/600', alt: 'Cruzando la meta' },
      ],
      currentSlide: 0,
      likes: 134,
      liked: true,
      commentsCount: 28,
    },
    {
      id: 3,
      username: 'miguel_cycling',
      avatarUrl: 'https://i.pravatar.cc/48?img=8',
      description: 'Ruta por la sierra 🚴 80 km y 2000 m de desnivel acumulado. Las piernas ya no me responden.',
      slides: [
        { url: 'https://picsum.photos/seed/bike1/800/600', alt: 'Inicio de ruta' },
        { url: 'https://picsum.photos/seed/bike2/800/600', alt: 'Puerto de montaña' },
        { url: 'https://picsum.photos/seed/bike3/800/600', alt: 'Vista panorámica' },
      ],
      currentSlide: 0,
      likes: 89,
      liked: false,
      commentsCount: 7,
    },
  ];

  constructor(private readonly dialog: MatDialog) {}

  prevSlide(index: number, post: FeedPost): void {
    if (post.currentSlide <= 0) return;
    post.currentSlide--;
    this.moveCarousel(index, post.currentSlide);
  }

  nextSlide(index: number, post: FeedPost): void {
    if (post.currentSlide >= post.slides.length - 1) return;
    post.currentSlide++;
    this.moveCarousel(index, post.currentSlide);
  }

  toggleLike(post: FeedPost): void {
    post.liked = !post.liked;
    post.likes += post.liked ? 1 : -1;
  }

  openComments(post: FeedPost): void {
    this.dialog.open(PostCommentsComponent, {
      data: { postId: post.id, username: post.username },
      panelClass: 'post-comments-dialog',
      maxWidth: '100vw',
      maxHeight: '100dvh',
      width: '100vw',
      height: '100dvh',
    });
  }

  private moveCarousel(carouselIndex: number, slideIndex: number): void {
    const carousel = this.carouselRefs.get(carouselIndex)?.nativeElement;
    const track = carousel?.querySelector<HTMLElement>('.carousel-track');
    if (!track || !carousel) return;
    track.style.transform = `translateX(-${slideIndex * carousel.offsetWidth}px)`;
  }
}
