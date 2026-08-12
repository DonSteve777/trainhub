import { GoalFeedDirection, GoalFeedUnit, GoalMarkFeedDto } from '../../core/services/feed.service';

export function formatGoalValue(value: number, unit: GoalFeedUnit): string {
  if (unit === 'time') return formatSeconds(value);
  if (unit === 'kg') return `${trimNum(value)} kg`;
  if (unit === 'meters') return `${trimNum(value)} m`;
  if (unit === 'reps') return `${trimNum(value)} reps`;
  return String(value);
}

export function formatGoalDeadline(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return d.toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export function goalStatusLabel(status: GoalMarkFeedDto['goalStatus']): string {
  switch (status) {
    case 'ACTIVE':
      return 'En curso';
    case 'ACHIEVED':
      return 'Logrado';
    case 'EXPIRED':
      return 'Vencido';
    default:
      return status;
  }
}

/**
 * Línea compacta de mejora vs PR anterior.
 * Ej: "−2:00 vs PR anterior (1:34:00)"
 */
export function previousPrDeltaLabel(mark: GoalMarkFeedDto): string | null {
  if (mark.previousPrValue === null || mark.previousPrValue === undefined) return null;

  const improved =
    mark.direction === 'lower'
      ? mark.markValue < mark.previousPrValue
      : mark.markValue > mark.previousPrValue;

  if (!improved && mark.markValue === mark.previousPrValue) {
    return `Igual que PR anterior (${formatGoalValue(mark.previousPrValue, mark.unit)})`;
  }

  const delta = Math.abs(mark.markValue - mark.previousPrValue);
  const sign = improved ? '−' : '+';
  const deltaText =
    mark.unit === 'time' ? formatSeconds(delta) : formatGoalValue(delta, mark.unit);

  return `${sign}${deltaText} vs PR anterior (${formatGoalValue(mark.previousPrValue, mark.unit)})`;
}

export function isPrImproved(mark: GoalMarkFeedDto): boolean {
  if (mark.previousPrValue === null || mark.previousPrValue === undefined) return false;
  return mark.direction === 'lower'
    ? mark.markValue < mark.previousPrValue
    : mark.markValue > mark.previousPrValue;
}

function formatSeconds(totalSeconds: number): string {
  const s = Math.max(0, Math.round(totalSeconds));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  if (h > 0) return `${h}:${pad(m)}:${pad(sec)}`;
  return `${m}:${pad(sec)}`;
}

function pad(n: number): string {
  return n.toString().padStart(2, '0');
}

function trimNum(n: number): string {
  return Number.isInteger(n) ? String(n) : n.toFixed(1);
}

export function progressBarWidth(pct: number): string {
  return `${Math.round(Math.min(100, Math.max(0, pct)))}%`;
}

export function friendsInGoalLabel(count: number): string {
  if (count <= 0) return 'Sin amigos en este objetivo';
  return count === 1 ? '1 amigo en este objetivo' : `${count} amigos en este objetivo`;
}

export function goalWeeksLabel(weeks: number): string {
  return weeks === 1 ? '1 semana' : `${weeks} semanas`;
}

export function goalParticipantsLabel(count: number): string {
  return count === 1 ? '1 participante' : `${count} participantes`;
}

export function goalDirectionHint(direction: GoalFeedDirection, unit: GoalFeedUnit): string {
  if (unit === 'time') return direction === 'lower' ? 'Menor tiempo' : 'Mayor tiempo';
  if (unit === 'kg') return direction === 'higher' ? 'Más peso' : 'Menos peso';
  if (unit === 'reps') return direction === 'higher' ? 'Más repeticiones' : 'Menos repeticiones';
  if (unit === 'meters') return direction === 'higher' ? 'Más distancia' : 'Menos distancia';
  return direction === 'higher' ? 'Más es mejor' : 'Menos es mejor';
}

/** Reexport tipado por claridad en templates. */
export type { GoalFeedDirection, GoalFeedUnit };
