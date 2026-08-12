/** Modelo de dominio de objetivos (contrato API / vista). */

export type GoalUnit = 'time' | 'reps' | 'kg' | 'meters';
export type GoalDirection = 'lower' | 'higher';
export type GoalStatus = 'ACTIVE' | 'ACHIEVED' | 'EXPIRED';

export interface GoalMark {
  id: number;
  userId: number;
  value: number;
  recordedAt: string;
  note?: string | null;
}

export interface GoalParticipant {
  userId: number;
  username: string;
  avatarUrl: string | null;
  isOwner: boolean;
  isMe: boolean;
  /** Fecha en que el usuario se apuntó / empezó el objetivo. */
  startedAt: string;
  /** Fecha de finalización del plazo para ese usuario. */
  endsAt: string;
  marks: GoalMark[];
}

export interface Goal {
  id: number;
  title: string;
  description: string;
  metricLabel: string;
  targetValue: number;
  unit: GoalUnit;
  direction: GoalDirection;
  weeks?: number;
  deadline: string;
  status: GoalStatus;
  createdAt: string;
  participants: GoalParticipant[];
}
