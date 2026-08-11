import { Injectable, signal } from '@angular/core';
import {
  Goal,
  GoalMark,
  MOCK_CURRENT_USER_ID,
  MOCK_GOALS,
} from './objetivos.mock';

export interface AddGoalMarkInput {
  goalId: number;
  value: number;
  note?: string;
  recordedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class ObjetivosService {
  private readonly _goals = signal<Goal[]>(structuredClone(MOCK_GOALS));
  private nextMarkId = 10_000;

  readonly goals = this._goals.asReadonly();

  activeGoalsForMe(): Goal[] {
    return this._goals().filter(
      g => g.status === 'ACTIVE' && g.participants.some(p => p.isMe),
    );
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
