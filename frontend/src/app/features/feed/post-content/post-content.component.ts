import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FeedPostType, FeedTrainingTag } from '../../../core/services/feed.service';

export interface PostContentPost {
  postType: FeedPostType;
  description: string;
  trainingTag: FeedTrainingTag | null;
  title: string | null;
  challengeDeadline: string | null;
  streakWeeks: number | null;
  weekActiveDays: boolean[] | null;
}

const TRAINING_TAG_LABELS: Record<FeedTrainingTag, string> = {
  HYROX: 'HYROX',
  FUERZA: 'fuerza',
  CARRERA: 'carrera',
  CLASE: 'clase',
  OTRO: 'entrenamiento',
};

const WEEK_DAY_LABELS = ['L', 'M', 'X', 'J', 'V', 'S', 'D'] as const;

@Component({
  selector: 'app-post-content',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './post-content.component.html',
  styleUrl: './post-content.component.scss',
})
export class PostContentComponent {
  readonly weekDayLabels = WEEK_DAY_LABELS;

  post = input.required<PostContentPost>();

  isCheckin = computed(() => this.post().postType === 'CHECKIN');
  isBoxTextContent = computed(
    () => this.post().postType === 'BOX_WOD' || this.post().postType === 'BOX_ANNOUNCEMENT'
  );
  isBoxChallenge = computed(() => this.post().postType === 'BOX_CHALLENGE');
  isResult = computed(() => this.post().postType === 'RESULT');

  checkinText = computed(() => {
    const tag = this.post().trainingTag;
    return tag ? `Check-in de ${this.formatTrainingTag(tag)}` : 'Check-in de entrenamiento';
  });

  formattedDeadline = computed(() => {
    const deadline = this.post().challengeDeadline;
    return deadline ? this.formatDate(deadline) : null;
  });

  hasDescription = computed(() => Boolean(this.post().description?.trim()));

  streakWeeks = computed(() => this.post().streakWeeks ?? 0);

  weekActiveDays = computed(() => {
    const days = this.post().weekActiveDays;
    if (!days || days.length !== 7) {
      return [false, false, false, false, false, false, false];
    }
    return days;
  });

  weekActiveCount = computed(() => this.weekActiveDays().filter(Boolean).length);

  streakLabel = computed(() => {
    const weeks = this.streakWeeks();
    if (weeks <= 0) {
      return 'Sin racha aún';
    }
    return weeks === 1 ? '1 semana' : `${weeks} semanas`;
  });

  private formatTrainingTag(tag: FeedTrainingTag): string {
    return TRAINING_TAG_LABELS[tag];
  }

  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
