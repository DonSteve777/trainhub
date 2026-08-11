import { Injectable, signal } from '@angular/core';
import {
  Goal,
  GoalDirection,
  GoalMark,
  GoalUnit,
  MOCK_CURRENT_USER_ID,
  MOCK_GOALS,
} from './objetivos.mock';

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

@Injectable({ providedIn: 'root' })
export class ObjetivosService {
  private readonly _goals = signal<Goal[]>(structuredClone(MOCK_GOALS));
  private nextMarkId = 10_000;
  private nextGoalId = 100;

  readonly goals = this._goals.asReadonly();

  activeGoalsForMe(): Goal[] {
    return this._goals().filter(
      g => g.status === 'ACTIVE' && g.participants.some(p => p.isMe),
    );
  }

  createGoal(input: CreateGoalInput): Goal {
    const now = new Date();
    const startedAt = now.toISOString();
    const ends = new Date(now);
    ends.setDate(ends.getDate() + Math.max(1, Math.round(input.weeks)) * 7);
    ends.setHours(23, 59, 59, 999);
    const deadline = ends.toISOString();

    const goal: Goal = {
      id: this.nextGoalId++,
      title: input.title.trim(),
      description: input.description.trim(),
      metricLabel: input.metricLabel.trim(),
      targetValue: input.targetValue,
      unit: input.unit,
      direction: input.direction,
      deadline,
      status: 'ACTIVE',
      createdAt: startedAt,
      participants: [
        {
          userId: MOCK_CURRENT_USER_ID,
          username: 'tú',
          avatarUrl: 'https://i.pravatar.cc/48?u=1',
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

  addMark(input: AddGoalMarkInput): GoalMark | null {
    const goals = this._goals();
    const goalIdx = goals.findIndex(g => g.id === input.goalId);
    if (goalIdx < 0) return null;

    const goal = goals[goalIdx];
    const meIdx = goal.participants.findIndex(p => p.isMe);
    if (meIdx < 0) return null;

    const mark: GoalMark = {
      id: this.nextMarkId++,
      userId: MOCK_CURRENT_USER_ID,
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
