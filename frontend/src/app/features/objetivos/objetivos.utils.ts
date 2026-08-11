import { Goal, GoalDirection, GoalMark, GoalParticipant, GoalUnit } from './objetivos.mock';

export interface ParticipantProgress {
  participant: GoalParticipant;
  bestValue: number | null;
  latestValue: number | null;
  progressPercent: number;
  achieved: boolean;
  marksCount: number;
}

export function bestMark(marks: GoalMark[], direction: GoalDirection): GoalMark | null {
  if (marks.length === 0) return null;
  return marks.reduce((best, m) => {
    if (direction === 'lower') return m.value < best.value ? m : best;
    return m.value > best.value ? m : best;
  });
}

export function latestMark(marks: GoalMark[]): GoalMark | null {
  if (marks.length === 0) return null;
  return [...marks].sort(
    (a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
  )[0];
}

/** Progreso 0–100 hacia el objetivo según dirección. */
export function progressPercent(
  value: number | null,
  target: number,
  direction: GoalDirection,
  startBaseline?: number
): number {
  if (value === null || Number.isNaN(value)) return 0;

  if (direction === 'higher') {
    if (target <= 0) return value > 0 ? 100 : 0;
    return Math.min(100, Math.max(0, (value / target) * 100));
  }

  // lower is better: baseline (peor marca o 120% del target) → 0%, target → 100%
  const baseline = startBaseline ?? target * 1.25;
  if (value <= target) return 100;
  if (value >= baseline) return 0;
  return Math.min(100, Math.max(0, ((baseline - value) / (baseline - target)) * 100));
}

export function isAchieved(value: number | null, target: number, direction: GoalDirection): boolean {
  if (value === null) return false;
  return direction === 'lower' ? value <= target : value >= target;
}

export function participantProgress(goal: Goal, participant: GoalParticipant): ParticipantProgress {
  const best = bestMark(participant.marks, goal.direction);
  const latest = latestMark(participant.marks);
  const bestValue = best?.value ?? null;
  const baseline =
    goal.direction === 'lower' && participant.marks.length > 0
      ? Math.max(...participant.marks.map(m => m.value), goal.targetValue * 1.25)
      : undefined;

  return {
    participant,
    bestValue,
    latestValue: latest?.value ?? null,
    progressPercent: progressPercent(bestValue, goal.targetValue, goal.direction, baseline),
    achieved: isAchieved(bestValue, goal.targetValue, goal.direction),
    marksCount: participant.marks.length,
  };
}

export function meParticipant(goal: Goal): GoalParticipant | undefined {
  return goal.participants.find(p => p.isMe);
}

export function friendsProgress(goal: Goal): ParticipantProgress[] {
  return goal.participants
    .filter(p => !p.isMe)
    .map(p => participantProgress(goal, p))
    .sort((a, b) => b.progressPercent - a.progressPercent);
}

export function formatValue(value: number, unit: GoalUnit): string {
  if (unit === 'time') return formatSeconds(value);
  if (unit === 'kg') return `${trimNum(value)} kg`;
  if (unit === 'meters') return `${trimNum(value)} m`;
  if (unit === 'reps') return `${trimNum(value)} reps`;
  return String(value);
}

export function formatSeconds(totalSeconds: number): string {
  const s = Math.max(0, Math.round(totalSeconds));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  if (h > 0) {
    return `${h}:${pad(m)}:${pad(sec)}`;
  }
  return `${m}:${pad(sec)}`;
}

/** Acepta `h:mm:ss`, `m:ss` o segundos sueltos. */
export function parseSeconds(raw: string): number | null {
  const text = raw.trim();
  if (!text) return null;

  if (/^\d+([.,]\d+)?$/.test(text)) {
    const n = Number(text.replace(',', '.'));
    return Number.isFinite(n) && n >= 0 ? Math.round(n) : null;
  }

  const parts = text.split(':').map(p => p.trim());
  if (parts.length < 2 || parts.length > 3) return null;
  if (parts.some(p => !/^\d+$/.test(p))) return null;

  const nums = parts.map(Number);
  let h = 0;
  let m = 0;
  let s = 0;
  if (nums.length === 3) {
    [h, m, s] = nums;
  } else {
    [m, s] = nums;
  }

  if (m > 59 || s > 59) return null;
  return h * 3600 + m * 60 + s;
}

function pad(n: number): string {
  return n.toString().padStart(2, '0');
}

function trimNum(n: number): string {
  return Number.isInteger(n) ? String(n) : n.toFixed(1);
}

export function formatDate(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function daysUntil(deadlineIso: string): number {
  const now = new Date();
  const end = new Date(deadlineIso);
  const diff = end.getTime() - now.getTime();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

export function deadlineLabel(deadlineIso: string, status: string): string {
  if (status === 'ACHIEVED') return 'Logrado';
  const days = daysUntil(deadlineIso);
  if (days < 0) return `Venció hace ${Math.abs(days)} d`;
  if (days === 0) return 'Vence hoy';
  if (days === 1) return 'Vence mañana';
  return `${days} días restantes`;
}

/** Puntos normalizados para un mini chart SVG (x,y en 0–100). */
export function chartPoints(
  marks: GoalMark[],
  target: number,
  direction: GoalDirection
): { x: number; y: number; value: number; date: string }[] {
  if (marks.length === 0) return [];
  const sorted = [...marks].sort(
    (a, b) => new Date(a.recordedAt).getTime() - new Date(b.recordedAt).getTime()
  );
  const values = sorted.map(m => m.value);
  const minV = Math.min(...values, target);
  const maxV = Math.max(...values, target);
  const range = maxV - minV || 1;

  return sorted.map((m, i) => {
    const x = sorted.length === 1 ? 50 : (i / (sorted.length - 1)) * 100;
    // Para tiempos (lower better), invertir eje Y visualmente sigue siendo "mejor = más arriba"
    const norm = (m.value - minV) / range;
    const y = direction === 'lower' ? norm * 80 + 10 : (1 - norm) * 80 + 10;
    return { x, y, value: m.value, date: m.recordedAt };
  });
}

export function targetChartY(
  marks: GoalMark[],
  target: number,
  direction: GoalDirection
): number | null {
  if (marks.length === 0) return null;
  const values = marks.map(m => m.value);
  const minV = Math.min(...values, target);
  const maxV = Math.max(...values, target);
  const range = maxV - minV || 1;
  const norm = (target - minV) / range;
  return direction === 'lower' ? norm * 80 + 10 : (1 - norm) * 80 + 10;
}
