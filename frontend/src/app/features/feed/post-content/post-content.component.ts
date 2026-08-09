import { Component, computed, inject, input } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  FeedPostType,
  FeedTrainingTag,
  WodCheckinAuthorDto,
} from '../../../core/services/feed.service';
import { ConstancyBlockComponent } from '../../../core/components/constancy-block/constancy-block.component';
import {
  WodCheckinsDialogComponent,
  WodCheckinsDialogData,
} from '../../../core/components/wod-checkins-dialog/wod-checkins-dialog.component';

export interface PostContentPost {
  id: number;
  postType: FeedPostType;
  description: string;
  trainingTag: FeedTrainingTag | null;
  title: string | null;
  challengeDeadline: string | null;
  streakWeeks: number | null;
  weekDayTags: Array<FeedTrainingTag | null> | null;
  wodPostId: number | null;
  wodTitle: string | null;
  wodCheckinsCount: number | null;
  wodCheckinAuthors: WodCheckinAuthorDto[] | null;
}

const TRAINING_TAG_LABELS: Record<FeedTrainingTag, string> = {
  HYROX: 'HYROX',
  FUERZA: 'fuerza',
  CARRERA: 'carrera',
  CLASE: 'clase',
  DESCANSO_ACTIVO: 'descanso activo',
  OTRO: 'entrenamiento',
};

@Component({
  selector: 'app-post-content',
  standalone: true,
  imports: [MatIconModule, MatDialogModule, ConstancyBlockComponent],
  templateUrl: './post-content.component.html',
  styleUrl: './post-content.component.scss',
})
export class PostContentComponent {
  private readonly dialog = inject(MatDialog);

  post = input.required<PostContentPost>();

  isCheckin = computed(() => this.post().postType === 'CHECKIN');
  isBoxWod = computed(() => this.post().postType === 'BOX_WOD');
  isBoxAnnouncement = computed(() => this.post().postType === 'BOX_ANNOUNCEMENT');
  isBoxChallenge = computed(() => this.post().postType === 'BOX_CHALLENGE');
  isResult = computed(() => this.post().postType === 'RESULT');

  checkinText = computed(() => {
    const tag = this.post().trainingTag;
    return tag ? `Check-in de ${this.formatTrainingTag(tag)}` : 'Check-in de entrenamiento';
  });

  wodTitle = computed(() => this.post().wodTitle?.trim() || null);

  wodCheckinsCount = computed(() => this.post().wodCheckinsCount ?? 0);

  wodCheckinAuthors = computed(() => this.post().wodCheckinAuthors ?? []);

  canOpenWodMuro = computed(() => this.isBoxWod() && this.wodCheckinsCount() > 0);

  wodMuroLabel = computed(() => {
    const count = this.wodCheckinsCount();
    if (count <= 0) {
      return 'Sé el primero en hacerlo';
    }
    return count === 1 ? '1 del box ya lo ha hecho' : `${count} del box ya lo han hecho`;
  });

  formattedDeadline = computed(() => {
    const deadline = this.post().challengeDeadline;
    return deadline ? this.formatDate(deadline) : null;
  });

  hasDescription = computed(() => Boolean(this.post().description?.trim()));

  streakWeeks = computed(() => this.post().streakWeeks ?? 0);

  weekDayTags = computed(() => this.post().weekDayTags);

  authorAvatarUrl(author: WodCheckinAuthorDto): string {
    return author.photoUrl ?? `https://i.pravatar.cc/48?u=${author.userId}`;
  }

  openWodCheckins(): void {
    if (!this.canOpenWodMuro()) {
      return;
    }
    const data: WodCheckinsDialogData = {
      wodPostId: this.post().id,
      wodTitle: this.post().title,
    };
    this.dialog.open(WodCheckinsDialogComponent, {
      data,
      width: '420px',
      maxWidth: '95vw',
      maxHeight: '85vh',
      panelClass: 'th-wod-checkins-panel',
    });
  }

  private formatTrainingTag(tag: FeedTrainingTag): string {
    return TRAINING_TAG_LABELS[tag];
  }

  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
