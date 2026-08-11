/** Datos mock para la vista de objetivos (sin backend). */

export type GoalUnit = 'time' | 'reps' | 'kg' | 'meters';
export type GoalDirection = 'lower' | 'higher';
export type GoalStatus = 'ACTIVE' | 'ACHIEVED' | 'EXPIRED';

export interface GoalMark {
  id: number;
  userId: number;
  value: number;
  recordedAt: string;
  note?: string;
}

export interface GoalParticipant {
  userId: number;
  username: string;
  avatarUrl: string;
  isOwner: boolean;
  isMe: boolean;
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
  deadline: string;
  status: GoalStatus;
  createdAt: string;
  participants: GoalParticipant[];
}

export const MOCK_CURRENT_USER_ID = 1;

export const MOCK_GOALS: Goal[] = [
  {
    id: 1,
    title: 'Sub 90 en Hyrox Open',
    description: 'Bajar de 1h 30m en la próxima carrera Open.',
    metricLabel: 'Tiempo total',
    targetValue: 5400,
    unit: 'time',
    direction: 'lower',
    deadline: '2026-09-15T23:59:59Z',
    status: 'ACTIVE',
    createdAt: '2026-06-01T10:00:00Z',
    participants: [
      {
        userId: 1,
        username: 'tú',
        avatarUrl: 'https://i.pravatar.cc/48?u=1',
        isOwner: true,
        isMe: true,
        marks: [
          { id: 101, userId: 1, value: 6120, recordedAt: '2026-06-05T18:00:00Z', note: 'Simulación box' },
          { id: 102, userId: 1, value: 5880, recordedAt: '2026-06-20T18:00:00Z', note: 'Mejora en wall balls' },
          { id: 103, userId: 1, value: 5640, recordedAt: '2026-07-12T18:00:00Z', note: 'PR parcial' },
          { id: 104, userId: 1, value: 5520, recordedAt: '2026-08-01T18:00:00Z', note: 'Buena simulación' },
        ],
      },
      {
        userId: 2,
        username: 'laura.rx',
        avatarUrl: 'https://i.pravatar.cc/48?u=2',
        isOwner: false,
        isMe: false,
        marks: [
          { id: 201, userId: 2, value: 6000, recordedAt: '2026-06-08T17:00:00Z' },
          { id: 202, userId: 2, value: 5700, recordedAt: '2026-07-01T17:00:00Z' },
          { id: 203, userId: 2, value: 5460, recordedAt: '2026-07-28T17:00:00Z', note: 'Casi en el objetivo' },
        ],
      },
      {
        userId: 3,
        username: 'marco.hyrox',
        avatarUrl: 'https://i.pravatar.cc/48?u=3',
        isOwner: false,
        isMe: false,
        marks: [
          { id: 301, userId: 3, value: 6300, recordedAt: '2026-06-10T19:00:00Z' },
          { id: 302, userId: 3, value: 5950, recordedAt: '2026-07-15T19:00:00Z' },
        ],
      },
      {
        userId: 4,
        username: 'sofia.fit',
        avatarUrl: 'https://i.pravatar.cc/48?u=4',
        isOwner: false,
        isMe: false,
        marks: [
          { id: 401, userId: 4, value: 5550, recordedAt: '2026-06-12T16:00:00Z' },
          { id: 402, userId: 4, value: 5380, recordedAt: '2026-07-20T16:00:00Z', note: '¡Objetivo logrado!' },
        ],
      },
    ],
  },
  {
    id: 2,
    title: '100 kg en deadlift',
    description: 'Alcanzar 100 kg en peso muerto convencional.',
    metricLabel: 'Carga máxima',
    targetValue: 100,
    unit: 'kg',
    direction: 'higher',
    deadline: '2026-10-01T23:59:59Z',
    status: 'ACTIVE',
    createdAt: '2026-07-01T09:00:00Z',
    participants: [
      {
        userId: 1,
        username: 'tú',
        avatarUrl: 'https://i.pravatar.cc/48?u=1',
        isOwner: true,
        isMe: true,
        marks: [
          { id: 501, userId: 1, value: 80, recordedAt: '2026-07-02T10:00:00Z' },
          { id: 502, userId: 1, value: 87.5, recordedAt: '2026-07-18T10:00:00Z' },
          { id: 503, userId: 1, value: 92.5, recordedAt: '2026-08-05T10:00:00Z', note: 'Muy cerca' },
        ],
      },
      {
        userId: 5,
        username: 'dani.strength',
        avatarUrl: 'https://i.pravatar.cc/48?u=5',
        isOwner: false,
        isMe: false,
        marks: [
          { id: 601, userId: 5, value: 90, recordedAt: '2026-07-05T11:00:00Z' },
          { id: 602, userId: 5, value: 100, recordedAt: '2026-07-30T11:00:00Z', note: 'Hecho' },
        ],
      },
    ],
  },
  {
    id: 3,
    title: 'SkiErg 1000 m en 3:30',
    description: 'Marca de referencia en SkiErg para la estación.',
    metricLabel: 'SkiErg 1000 m',
    targetValue: 210,
    unit: 'time',
    direction: 'lower',
    deadline: '2026-05-01T23:59:59Z',
    status: 'ACHIEVED',
    createdAt: '2026-03-01T10:00:00Z',
    participants: [
      {
        userId: 1,
        username: 'tú',
        avatarUrl: 'https://i.pravatar.cc/48?u=1',
        isOwner: true,
        isMe: true,
        marks: [
          { id: 701, userId: 1, value: 245, recordedAt: '2026-03-05T18:00:00Z' },
          { id: 702, userId: 1, value: 228, recordedAt: '2026-03-20T18:00:00Z' },
          { id: 703, userId: 1, value: 208, recordedAt: '2026-04-15T18:00:00Z', note: 'Objetivo conseguido' },
        ],
      },
      {
        userId: 2,
        username: 'laura.rx',
        avatarUrl: 'https://i.pravatar.cc/48?u=2',
        isOwner: false,
        isMe: false,
        marks: [
          { id: 801, userId: 2, value: 235, recordedAt: '2026-03-10T17:00:00Z' },
          { id: 802, userId: 2, value: 215, recordedAt: '2026-04-02T17:00:00Z' },
        ],
      },
    ],
  },
];
