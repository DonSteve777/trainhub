import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

const WEEK_DAY_LABELS = ['L', 'M', 'X', 'J', 'V', 'S', 'D'] as const;

export type ConstancyTrainingTag = 'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'DESCANSO_ACTIVO' | 'OTRO';

const TAG_LABELS: Record<ConstancyTrainingTag, string> = {
  HYROX: 'HYROX',
  FUERZA: 'Fuerza',
  CARRERA: 'Carrera',
  CLASE: 'Clase',
  DESCANSO_ACTIVO: 'Descanso activo',
  OTRO: 'Otro',
};

@Component({
  selector: 'app-constancy-block',
  standalone: true,
  imports: [MatIconModule, MatTooltipModule],
  templateUrl: './constancy-block.component.html',
  styleUrl: './constancy-block.component.scss',
})
export class ConstancyBlockComponent {
  readonly weekDayLabels = WEEK_DAY_LABELS;

  streakWeeks = input(0);
  weekDayTags = input<(ConstancyTrainingTag | null)[] | null>(null);

  resolvedStreakWeeks = computed(() => this.streakWeeks() ?? 0);

  resolvedWeekDayTags = computed((): (ConstancyTrainingTag | null)[] => {
    const tags = this.weekDayTags();
    if (!tags || tags.length !== 7) {
      return [null, null, null, null, null, null, null];
    }
    return tags;
  });

  weekActiveCount = computed(() => this.resolvedWeekDayTags().filter(tag => tag != null).length);

  streakLabel = computed(() => {
    const weeks = this.resolvedStreakWeeks();
    if (weeks <= 0) {
      return 'Sin racha aún';
    }
    return weeks === 1 ? '1 semana' : `${weeks} semanas`;
  });

  ariaLabel = computed(() => {
    const weeks = this.resolvedStreakWeeks();
    const count = this.weekActiveCount();
    const tags = this.resolvedWeekDayTags()
      .map((tag, i) => (tag ? `${WEEK_DAY_LABELS[i]} ${TAG_LABELS[tag]}` : null))
      .filter((v): v is string => v != null)
      .join(', ');
    return (
      this.streakLabel() +
      (weeks > 0 ? ' de constancia' : '') +
      '. Esta semana, ' +
      count +
      (count === 1 ? ' día' : ' días') +
      (tags ? ': ' + tags : '')
    );
  });

  dayTooltip(index: number): string {
    const tag = this.resolvedWeekDayTags()[index];
    if (!tag) {
      return 'Sin entrenamiento';
    }
    return TAG_LABELS[tag];
  }
}
