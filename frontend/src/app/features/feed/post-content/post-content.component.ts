import { Component, computed, inject, input } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  FeedPostType,
  FeedTrainingTag,
  GoalFeedDto,
  GoalMarkFeedDto,
  WodCheckinAuthorDto,
} from '../../../core/services/feed.service';
import { ConstancyBlockComponent } from '../../../core/components/constancy-block/constancy-block.component';
import {
  ParticipantPreview,
  ParticipantsStripComponent,
} from '../../../core/components/participants-strip/participants-strip.component';
import {
  WodCheckinsDialogComponent,
  WodCheckinsDialogData,
} from '../../../core/components/wod-checkins-dialog/wod-checkins-dialog.component';
import {
  formatGoalDeadline,
  formatGoalValue,
  goalParticipantsLabel,
  goalWeeksLabel,
  isPrImproved,
  previousPrDeltaLabel,
} from '../goal-mark.format';

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
  participantsCount: number;
  goalMark: GoalMarkFeedDto | null;
  goal: GoalFeedDto | null;
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
  imports: [MatIconModule, MatDialogModule, ConstancyBlockComponent, ParticipantsStripComponent],
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
  isGoalMark = computed(() => this.post().postType === 'GOAL_MARK');
  isGoalCreated = computed(() => this.post().postType === 'GOAL_CREATED');
  isGoalJoin = computed(() => this.post().postType === 'GOAL_JOIN');

  checkinText = computed(() => {
    const tag = this.post().trainingTag;
    return tag ? `Check-in de ${this.formatTrainingTag(tag)}` : 'Check-in de entrenamiento';
  });

  wodTitle = computed(() => this.post().wodTitle?.trim() || null);

  wodCheckinsCount = computed(() => this.post().wodCheckinsCount ?? 0);

  wodCheckinAuthors = computed(() => this.post().wodCheckinAuthors ?? []);

  wodParticipants = computed((): ParticipantPreview[] =>
    this.wodCheckinAuthors().map(author => ({
      userId: author.userId,
      username: author.username,
      photoUrl: author.photoUrl,
    }))
  );

  canOpenWodMuro = computed(() => this.isBoxWod() && this.wodCheckinsCount() > 0);

  wodMuroLabel = computed(() => {
    const count = this.wodCheckinsCount();
    if (count <= 0) {
      return 'Sé el primero en apuntarte';
    }
    return count === 1 ? '1 del box va a participar' : `${count} del box van a participar`;
  });

  challengeParticipantsCount = computed(() => this.post().participantsCount ?? 0);

  challengeParticipantAuthors = computed(() => this.post().wodCheckinAuthors ?? []);

  challengeParticipants = computed((): ParticipantPreview[] =>
    this.challengeParticipantAuthors().map(author => ({
      userId: author.userId,
      username: author.username,
      photoUrl: author.photoUrl,
    }))
  );

  canOpenChallengeParticipants = computed(
    () => this.isBoxChallenge() && this.challengeParticipantsCount() > 0
  );

  challengeParticipantsLabel = computed(() => {
    const count = this.challengeParticipantsCount();
    if (count <= 0) {
      return 'Sé el primero en apuntarte';
    }
    return goalParticipantsLabel(count);
  });

  formattedDeadline = computed(() => {
    const deadline = this.post().challengeDeadline;
    return deadline ? this.formatDate(deadline) : null;
  });

  hasDescription = computed(() => Boolean(this.post().description?.trim()));

  streakWeeks = computed(() => this.post().streakWeeks ?? 0);

  weekDayTags = computed(() => this.post().weekDayTags);

  goalMark = computed(() => this.post().goalMark);

  goalMarkLead = computed(() => {
    const mark = this.goalMark();
    const title = mark?.goalTitle?.trim() || this.post().title?.trim() || 'Objetivo';
    return `Nueva marca · ${title}`;
  });

  goalTargetText = computed(() => {
    const mark = this.goalMark();
    return mark ? formatGoalValue(mark.targetValue, mark.unit) : '';
  });

  goalDeadlineText = computed(() => {
    const mark = this.goalMark();
    return mark ? formatGoalDeadline(mark.deadline) : '';
  });

  goalMarkValueText = computed(() => {
    const mark = this.goalMark();
    return mark ? formatGoalValue(mark.markValue, mark.unit) : '';
  });

  goalDeltaText = computed(() => {
    const mark = this.goalMark();
    return mark ? previousPrDeltaLabel(mark) : null;
  });

  goalDeltaImproved = computed(() => {
    const mark = this.goalMark();
    return mark ? isPrImproved(mark) : false;
  });

  goalMarkParticipantsLabel = computed(() =>
    goalParticipantsLabel(this.goalMark()?.friendsCount ?? 0)
  );

  goalFriendAvatars = computed(() => this.goalMark()?.friendAvatars ?? []);

  goalMarkParticipants = computed((): ParticipantPreview[] =>
    this.goalFriendAvatars().map(friend => ({
      userId: friend.userId,
      username: friend.username,
      photoUrl: friend.photoUrl,
    }))
  );

  goalFeed = computed(() => this.post().goal);

  goalCreatedLead = computed(() => {
    const title =
      this.goalFeed()?.goalTitle?.trim() || this.post().title?.trim() || 'Objetivo';
    return `Nuevo objetivo · ${title}`;
  });

  goalJoinLead = computed(() => {
    const creator = this.goalFeed()?.creatorUsername?.trim() || 'usuario';
    return `Se ha apuntado al objetivo de @${creator}`;
  });

  goalJoinBody = computed(() => {
    const goal = this.goalFeed();
    if (!goal) return '';
    const creator = goal.creatorUsername;
    const others = goal.otherParticipants ?? [];
    if (others.length === 0) {
      return `Se ha apuntado al objetivo «${goal.goalTitle}» de @${creator}.`;
    }
    const mentions = others.map(p => `@${p.username}`).join(', ');
    return `Se ha apuntado al objetivo «${goal.goalTitle}» de @${creator}. Participa también con ${mentions}.`;
  });

  goalTargetTextFromFeed = computed(() => {
    const goal = this.goalFeed();
    return goal ? formatGoalValue(goal.targetValue, goal.unit) : '';
  });

  goalDeadlineTextFromFeed = computed(() => {
    const goal = this.goalFeed();
    return goal ? formatGoalDeadline(goal.deadline) : '';
  });

  goalWeeksText = computed(() => {
    const goal = this.goalFeed();
    return goal ? goalWeeksLabel(goal.weeks) : '';
  });

  goalFeedParticipantsLabel = computed(() =>
    goalParticipantsLabel(this.goalFeed()?.participantsCount ?? 0)
  );

  goalFeedFriendAvatars = computed(() => this.goalFeed()?.friendAvatars ?? []);

  goalFeedParticipants = computed((): ParticipantPreview[] =>
    this.goalFeedFriendAvatars().map(friend => ({
      userId: friend.userId,
      username: friend.username,
      photoUrl: friend.photoUrl,
    }))
  );

  canOpenGoalParticipants = computed(() => {
    const goal = this.goalFeed();
    return (
      (this.isGoalCreated() || this.isGoalJoin()) &&
      (goal?.participantsCount ?? 0) > 0 &&
      this.goalFeedFriendAvatars().length > 0
    );
  });

  openWodCheckins(): void {
    if (!this.canOpenWodMuro()) {
      return;
    }
    this.openParticipantsDialog({
      wodPostId: this.post().id,
      wodTitle: this.post().title,
    });
  }

  openChallengeParticipants(): void {
    if (!this.canOpenChallengeParticipants()) {
      return;
    }
    this.openParticipantsDialog({
      wodPostId: this.post().id,
      wodTitle: this.post().title,
      dialogTitle: 'Participantes',
      headerIcon: 'emoji_events',
      emptyMessage: 'Nadie se ha apuntado todavía.',
    });
  }

  openGoalParticipants(): void {
    const goal = this.goalFeed();
    if (!goal || !this.canOpenGoalParticipants()) {
      return;
    }

    const participants: WodCheckinAuthorDto[] = goal.friendAvatars.map(friend => ({
      userId: friend.userId,
      username: friend.username,
      photoUrl: friend.photoUrl,
    }));

    this.openParticipantsDialog({
      wodTitle: goal.goalTitle,
      dialogTitle: 'Participantes',
      headerIcon: 'flag',
      participants,
      emptyMessage: 'Nadie participa todavía.',
    });
  }

  private openParticipantsDialog(data: WodCheckinsDialogData): void {
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
