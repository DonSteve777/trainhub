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
 * Objetivos del usuario autenticado (GET/POST /api/goals, POST marcas).
 */
@Injectable({ providedIn: 'root' })
export class ObjetivosService {
  private readonly api = inject(ApiService);

  private readonly _goals = signal<Goal[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

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

  /** Crea un objetivo vía POST /api/goals. */
  createGoal(input: CreateGoalInput): Observable<Goal> {
    const description = input.description.trim();
    const body = {
      title: input.title.trim(),
      metricLabel: input.metricLabel.trim(),
      targetValue: input.targetValue,
      unit: input.unit.toUpperCase(),
      direction: input.direction.toUpperCase(),
      weeks: input.weeks,
      ...(description ? { description } : {}),
    };

    return this.api.post<Goal>('/goals', body).pipe(
      tap(goal => this._goals.update(list => [goal, ...list])),
    );
  }

  /** Registra una marca vía POST /api/goals/{goalId}/marks. */
  addMark(input: AddGoalMarkInput): Observable<GoalMark> {
    const note = input.note?.trim();
    const body: { value: number; note?: string } = { value: input.value };
    if (note) body.note = note;

    return this.api.post<GoalMark>(`/goals/${input.goalId}/marks`, body).pipe(
      tap(mark => this.mergeMark(input.goalId, mark)),
    );
  }

  /**
   * Tras unirse a un objetivo desde el feed, recarga la lista para que /objetivos
   * refleje la nueva participación.
   */
  refreshAfterJoin(): void {
    this.loadGoals().subscribe({ error: () => undefined });
  }

  private mergeMark(goalId: number, mark: GoalMark): void {
    this._goals.update(goals =>
      goals.map(g => {
        if (g.id !== goalId) return g;
        return {
          ...g,
          participants: g.participants.map(p =>
            p.isMe ? { ...p, marks: [...p.marks, mark] } : p,
          ),
        };
      }),
    );
  }
}
