import { Component, computed, input, output } from '@angular/core';

export interface ParticipantPreview {
  userId: number;
  username: string;
  photoUrl: string | null;
}

@Component({
  selector: 'app-participants-strip',
  standalone: true,
  templateUrl: './participants-strip.component.html',
  styleUrl: './participants-strip.component.scss',
})
export class ParticipantsStripComponent {
  participants = input<ParticipantPreview[]>([]);
  label = input.required<string>();
  clickable = input(false);
  ariaLabel = input<string | null>(null);

  participantsClick = output<void>();

  resolvedAriaLabel = computed(() => this.ariaLabel() ?? this.label());

  avatarUrl(participant: ParticipantPreview): string {
    return participant.photoUrl ?? `https://i.pravatar.cc/48?u=${participant.userId}`;
  }

  onClick(): void {
    if (this.clickable()) {
      this.participantsClick.emit();
    }
  }
}
