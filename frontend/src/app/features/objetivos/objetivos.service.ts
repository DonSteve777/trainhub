import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import {
  Goal,
  GoalDirection,
  GoalMark,
  GoalUnit,
} from './objetivos.models';

export interface AddGoalMarkInput {
  goalId: number;
  value: number;
  note?: string;
  recordedAt?: string;
}

export interface CreateGoalInput {
  title: string;
  description: string;
  metricLabel: string;
  targetValue: number;
  unit: GoalUnit;
  direction: GoalDirection;
  /** Plazo en semanas desde hoy; se deriva deadline / endsAt. */
  weeks: number;
}

/**
 * Objetivos del usuario autenticado.
 * La lista viene de GET /api/goals; create/addMark siguen en cliente
 * hasta las specs 18/19 (POST).
 */
@Injectable({ providedIn: 'root' })
export class ObjetivosService {
  private readonly api = inject(ApiService);

  private readonly _goals = signal<Goal[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private nextMarkId = 10_000;
  private nextGoalId = 100;

  readonly goals = this._goals.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  /** Carga objetivos del usuario desde el backend. */
  loadGoals(): Observable<Goal[]> {
    this._loading.set(true);
    this._error.set(null);
    return this.api.get<Goal[]>('/goals').pipe(
      tap({
        next: goals => {
          this._goals.set(goals);
          this._loading.set(false);
        },
        error: () => {
          this._error.set('No se pudieron cargar los objetivos.');
          this._loading.set(false);
        },
      }),
    );
  }

  activeGoalsForMe(): Goal[] {
    return this._goals().filter(
      g => g.status === 'ACTIVE' && g.participants.some(p => p.isMe),
    );
  }

  /**
   * Alta local (mock) hasta POST de la spec 18.
   * Tras loadGoals, muta el signal en memoria.
   */
  createGoal(input: CreateGoalInput): Goal {
    const now = new Date();
    const startedAt = now.toISOString();
    const ends = new Date(now);
    ends.setDate(ends.getDate() + Math.max(1, Math.round(input.weeks)) * 7);
    ends.setHours(23, 59, 59, 999);
    const deadline = ends.toISOString();

    const me = this._goals()
      .flatMap(g => g.participants)
      .find(p => p.isMe);

    const goal: Goal = {
      id: this.nextGoalId++,
      title: input.title.trim(),
      description: input.description.trim(),
      metricLabel: input.metricLabel.trim(),
      targetValue: input.targetValue,
      unit: input.unit,
      direction: input.direction,
      weeks: input.weeks,
      deadline,
      status: 'ACTIVE',
      createdAt: startedAt,
      participants: [
        {
          userId: me?.userId ?? 0,
          username: me?.username ?? 'tú',
          avatarUrl: me?.avatarUrl ?? null,
          isOwner: true,
          isMe: true,
          startedAt,
          endsAt: deadline,
          marks: [],
        },
      ],
    };

    this._goals.update(list => [goal, ...list]);
    return goal;
  }

  /**
   * Alta local (mock) hasta POST de la spec 19.
   */
  addMark(input: AddGoalMarkInput): GoalMark | null {
    const goals = this._goals();
    const goalIdx = goals.findIndex(g => g.id === input.goalId);
    if (goalIdx < 0) return null;

    const goal = goals[goalIdx];
    const meIdx = goal.participants.findIndex(p => p.isMe);
    if (meIdx < 0) return null;

    const me = goal.participants[meIdx];
    const mark: GoalMark = {
      id: this.nextMarkId++,
      userId: me.userId,
      value: input.value,
      recordedAt: input.recordedAt ?? new Date().toISOString(),
      note: input.note?.trim() || undefined,
    };

    const participants = goal.participants.map((p, i) =>
      i === meIdx ? { ...p, marks: [...p.marks, mark] } : p,
    );

    const next = [...goals];
    next[goalIdx] = { ...goal, participants };
    this._goals.set(next);
    return mark;
  }
}
