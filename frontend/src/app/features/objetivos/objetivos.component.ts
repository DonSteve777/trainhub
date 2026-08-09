import { Component, computed, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { Goal, MOCK_GOALS } from './objetivos.mock';
import {
  ParticipantProgress,
  chartPoints,
  deadlineLabel,
  formatDate,
  formatValue,
  friendsProgress,
  latestMark,
  meParticipant,
  participantProgress,
  targetChartY,
} from './objetivos.utils';

type GoalFilter = 'ACTIVE' | 'ACHIEVED' | 'ALL';

@Component({
  selector: 'app-objetivos',
  standalone: true,
  imports: [MatIconModule, RouterLink],
  templateUrl: './objetivos.component.html',
  styleUrl: './objetivos.component.scss',
})
export class ObjetivosComponent {
  /** Expuesto para redondeos en la plantilla. */
  readonly Math = Math;

  readonly goals = signal<Goal[]>(MOCK_GOALS);
  readonly filter = signal<GoalFilter>('ACTIVE');
  readonly selectedId = signal<number | null>(MOCK_GOALS.find(g => g.status === 'ACTIVE')?.id ?? MOCK_GOALS[0]?.id ?? null);

  readonly filteredGoals = computed(() => {
    const f = this.filter();
    const list = this.goals();
    if (f === 'ALL') return list;
    return list.filter(g => g.status === f);
  });

  readonly selectedGoal = computed(() => {
    const id = this.selectedId();
    return this.goals().find(g => g.id === id) ?? null;
  });

  readonly myProgress = computed((): ParticipantProgress | null => {
    const goal = this.selectedGoal();
    if (!goal) return null;
    const me = meParticipant(goal);
    if (!me) return null;
    return participantProgress(goal, me);
  });

  readonly friends = computed((): ParticipantProgress[] => {
    const goal = this.selectedGoal();
    if (!goal) return [];
    return friendsProgress(goal);
  });

  readonly myMarksSorted = computed(() => {
    const goal = this.selectedGoal();
    const me = goal ? meParticipant(goal) : undefined;
    if (!me) return [];
    return [...me.marks].sort(
      (a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    );
  });

  readonly chartPts = computed(() => {
    const goal = this.selectedGoal();
    const me = goal ? meParticipant(goal) : undefined;
    if (!goal || !me) return [];
    return chartPoints(me.marks, goal.targetValue, goal.direction);
  });

  readonly chartPolyline = computed(() =>
    this.chartPts()
      .map(p => `${p.x},${p.y}`)
      .join(' ')
  );

  readonly targetY = computed(() => {
    const goal = this.selectedGoal();
    const me = goal ? meParticipant(goal) : undefined;
    if (!goal || !me) return null;
    return targetChartY(me.marks, goal.targetValue, goal.direction);
  });

  selectGoal(id: number): void {
    this.selectedId.set(id);
  }

  setFilter(f: GoalFilter): void {
    this.filter.set(f);
    const list = this.filteredGoals();
    const current = this.selectedId();
    if (!list.some(g => g.id === current)) {
      this.selectedId.set(list[0]?.id ?? null);
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Activo';
      case 'ACHIEVED':
        return 'Logrado';
      case 'EXPIRED':
        return 'Vencido';
      default:
        return status;
    }
  }

  deadlineText(goal: Goal): string {
    return deadlineLabel(goal.deadline, goal.status);
  }

  formatVal(value: number | null, goal: Goal): string {
    if (value === null) return '—';
    return formatValue(value, goal.unit);
  }

  formatMarkDate(iso: string): string {
    return formatDate(iso);
  }

  myLatestLabel(goal: Goal): string {
    const me = meParticipant(goal);
    if (!me) return 'Sin marcas';
    const latest = latestMark(me.marks);
    if (!latest) return 'Sin marcas';
    return formatValue(latest.value, goal.unit);
  }

  progressBarWidth(pct: number): string {
    return `${Math.round(Math.min(100, Math.max(0, pct)))}%`;
  }
}
