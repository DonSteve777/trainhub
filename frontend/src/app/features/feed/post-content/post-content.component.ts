import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FeedPostType, FeedTrainingTag } from '../../../core/services/feed.service';

export interface PostContentPost {
  postType: FeedPostType;
  description: string;
  trainingTag: FeedTrainingTag | null;
  title: string | null;
  challengeDeadline: string | null;
}

const TRAINING_TAG_LABELS: Record<FeedTrainingTag, string> = {
  HYROX: 'HYROX',
  FUERZA: 'fuerza',
  CARRERA: 'carrera',
  CLASE: 'clase',
  OTRO: 'entrenamiento',
};

@Component({
  selector: 'app-post-content',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './post-content.component.html',
  styleUrl: './post-content.component.scss',
})
export class PostContentComponent {
  post = input.required<PostContentPost>();

  isCheckin = computed(() => this.post().postType === 'CHECKIN');
  isBoxTextContent = computed(
    () => this.post().postType === 'BOX_WOD' || this.post().postType === 'BOX_ANNOUNCEMENT'
  );
  isBoxChallenge = computed(() => this.post().postType === 'BOX_CHALLENGE');

  checkinText = computed(() => {
    const tag = this.post().trainingTag;
    return tag ? `Check-in de ${this.formatTrainingTag(tag)}` : 'Check-in de entrenamiento';
  });

  formattedDeadline = computed(() => {
    const deadline = this.post().challengeDeadline;
    return deadline ? this.formatDate(deadline) : null;
  });

  private formatTrainingTag(tag: FeedTrainingTag): string {
    return TRAINING_TAG_LABELS[tag];
  }

  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
