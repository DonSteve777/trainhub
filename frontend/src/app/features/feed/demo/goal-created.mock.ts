import { FeedPostDto } from '../../../core/services/feed.service';

const BASE_GOAL = {
  goalId: 12,
  goalTitle: 'Sub 90 en Hyrox Open',
  goalStatus: 'ACTIVE' as const,
  targetValue: 5400,
  unit: 'time' as const,
  direction: 'lower' as const,
  weeks: 8,
  deadline: '2026-09-15T23:59:59Z',
  creatorUserId: 1,
  creatorUsername: 'pedro.alonso',
  participantsCount: 1,
  joinedByCurrentUser: false,
  friendAvatars: [{ userId: 1, username: 'pedro.alonso', photoUrl: 'https://i.pravatar.cc/48?u=pedro' }],
};

/** Post GOAL_CREATED — vista amigo sin unirse (CTA «Me acojo»). */
export const MOCK_GOAL_CREATED_VISITOR: FeedPostDto = {
  id: -9101,
  userId: 1,
  username: 'pedro.alonso',
  photoUrl: 'https://i.pravatar.cc/48?u=pedro.alonso',
  postType: 'GOAL_CREATED',
  boxId: null,
  title: 'Sub 90 en Hyrox Open',
  trainingTag: null,
  challengeDeadline: '2026-09-15T23:59:59Z',
  description: 'Quiero bajar de 1h30 antes del regional. Wall balls y sled son mis puntos débiles.',
  creationDate: '2026-08-10T09:15:00Z',
  commentsCount: 2,
  likesCount: 7,
  likedByCurrentUser: false,
  participantsCount: 1,
  joinedByCurrentUser: false,
  streakWeeks: null,
  weekDayTags: null,
  wodPostId: null,
  wodTitle: null,
  wodCheckinsCount: null,
  wodCheckinAuthors: null,
  goalMark: null,
  goal: { ...BASE_GOAL },
};

/** Post GOAL_CREATED — amigo que ya se acogió («Ya participas»). */
export const MOCK_GOAL_CREATED_JOINED: FeedPostDto = {
  ...MOCK_GOAL_CREATED_VISITOR,
  id: -9102,
  commentsCount: 5,
  likesCount: 11,
  likedByCurrentUser: true,
  participantsCount: 4,
  joinedByCurrentUser: true,
  goal: {
    ...BASE_GOAL,
    participantsCount: 4,
    joinedByCurrentUser: true,
    friendAvatars: [
      { userId: 1, username: 'pedro.alonso', photoUrl: 'https://i.pravatar.cc/48?u=pedro' },
      { userId: 2, username: 'laura.rx', photoUrl: 'https://i.pravatar.cc/48?u=laura' },
      { userId: 3, username: 'marco.hyrox', photoUrl: 'https://i.pravatar.cc/48?u=marco' },
      { userId: 4, username: 'sofia.fit', photoUrl: 'https://i.pravatar.cc/48?u=sofia' },
    ],
  },
};

/** Post GOAL_CREATED — vista del creador (sin botón de adhesión). */
export const MOCK_GOAL_CREATED_OWNER: FeedPostDto = {
  ...MOCK_GOAL_CREATED_VISITOR,
  id: -9103,
  userId: 99,
  username: 'tu_usuario',
  photoUrl: 'https://i.pravatar.cc/48?u=tu_usuario',
  participantsCount: 3,
  goal: {
    ...BASE_GOAL,
    creatorUserId: 99,
    creatorUsername: 'tu_usuario',
    participantsCount: 3,
    friendAvatars: [
      { userId: 99, username: 'tu_usuario', photoUrl: 'https://i.pravatar.cc/48?u=tu_usuario' },
      { userId: 2, username: 'laura.rx', photoUrl: 'https://i.pravatar.cc/48?u=laura' },
      { userId: 3, username: 'marco.hyrox', photoUrl: 'https://i.pravatar.cc/48?u=marco' },
    ],
  },
};

/** Variante con meta numérica (reps) para probar distintos formatos. */
export const MOCK_GOAL_CREATED_REPS: FeedPostDto = {
  id: -9104,
  userId: 5,
  username: 'dani.strength',
  photoUrl: 'https://i.pravatar.cc/48?u=dani.strength',
  postType: 'GOAL_CREATED',
  boxId: null,
  title: '50 pull-ups strict',
  trainingTag: null,
  challengeDeadline: '2026-10-01T23:59:59Z',
  description: null,
  creationDate: '2026-08-11T14:00:00Z',
  commentsCount: 0,
  likesCount: 3,
  likedByCurrentUser: false,
  participantsCount: 2,
  joinedByCurrentUser: false,
  streakWeeks: null,
  weekDayTags: null,
  wodPostId: null,
  wodTitle: null,
  wodCheckinsCount: null,
  wodCheckinAuthors: null,
  goalMark: null,
  goal: {
    goalId: 20,
    goalTitle: '50 pull-ups strict',
    goalStatus: 'ACTIVE',
    targetValue: 50,
    unit: 'reps',
    direction: 'higher',
    weeks: 6,
    deadline: '2026-10-01T23:59:59Z',
    creatorUserId: 5,
    creatorUsername: 'dani.strength',
    participantsCount: 2,
    joinedByCurrentUser: false,
    friendAvatars: [
      { userId: 5, username: 'dani.strength', photoUrl: 'https://i.pravatar.cc/48?u=dani' },
      { userId: 6, username: 'ana.gym', photoUrl: 'https://i.pravatar.cc/48?u=ana' },
    ],
  },
};

export const GOAL_CREATED_DEMO_VARIANTS = [
  { label: 'Amigo — Me uno', mock: MOCK_GOAL_CREATED_VISITOR, isOwner: false },
  { label: 'Amigo — Ya participas', mock: MOCK_GOAL_CREATED_JOINED, isOwner: false },
  { label: 'Creador — sin botón', mock: MOCK_GOAL_CREATED_OWNER, isOwner: true },
  { label: 'Meta en reps (sin descripción)', mock: MOCK_GOAL_CREATED_REPS, isOwner: false },
] as const;
