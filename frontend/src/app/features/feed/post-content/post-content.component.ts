import { Component, computed, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { FeedPostType, FeedTrainingTag } from '../../../core/services/feed.service';

export interface PostContentPost {
  postType: FeedPostType;
  description: string;
  trainingTag: FeedTrainingTag | null;
  title: string | null;
  challengeDeadline: string | null;
  participantsCount: number;
  joinedByCurrentUser: boolean;
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
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './post-content.component.html',
  styleUrl: './post-content.component.scss',
})
export class PostContentComponent {
  post = input.required<PostContentPost>();
  joinToggle = output<void>();

  isCheckin = computed(() => this.post().postType === 'CHECKIN');
  isBoxTextContent = computed(
    () => this.post().postType === 'BOX_WOD' || this.post().postType === 'BOX_ANNOUNCEMENT'
  );
  isBoxChallenge = computed(() => this.post().postType === 'BOX_CHALLENGE');

  participantsLabel = computed(() => {
    const count = this.post().participantsCount;
    return count === 1 ? '1 participante' : `${count} participantes`;
  });

  checkinText = computed(() => {
    const tag = this.post().trainingTag;
    return tag ? `Check-in de ${this.formatTrainingTag(tag)}` : 'Check-in de entrenamiento';
  });

  formattedDeadline = computed(() => {
    const deadline = this.post().challengeDeadline;
    return deadline ? this.formatDate(deadline) : null;
  });

  onJoinToggle(): void {
    this.joinToggle.emit();
  }

  private formatTrainingTag(tag: FeedTrainingTag): string {
    return TRAINING_TAG_LABELS[tag];
  }

  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
