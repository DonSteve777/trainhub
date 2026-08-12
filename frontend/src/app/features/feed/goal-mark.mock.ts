import { FeedPostDto } from '../../core/services/feed.service';



const BASE_MARK = {

  goalId: 1,

  goalTitle: 'Sub 90 en Hyrox Open',

  goalStatus: 'ACTIVE' as const,

  progressPercent: 72,

  targetValue: 5400,

  unit: 'time' as const,

  direction: 'lower' as const,

  deadline: '2026-09-15T23:59:59Z',

  markValue: 5520,

  previousPrValue: 5640,

  friendsCount: 3,

  friendAvatars: [

    { userId: 3, username: 'marco.hyrox', photoUrl: 'https://i.pravatar.cc/48?u=3' },

    { userId: 4, username: 'sofia.fit', photoUrl: 'https://i.pravatar.cc/48?u=4' },

    { userId: 5, username: 'dani.strength', photoUrl: 'https://i.pravatar.cc/48?u=5' },

  ],

};



/** Amigo ve la marca de otro — CTA «Me uno». */

export const MOCK_GOAL_MARK_VISITOR: FeedPostDto = {

  id: -9001,

  userId: 2,

  username: 'laura.rx',

  photoUrl: 'https://i.pravatar.cc/48?u=laura.rx',

  postType: 'GOAL_MARK',

  boxId: null,

  title: 'Sub 90 en Hyrox Open',

  trainingTag: null,

  challengeDeadline: '2026-09-15T23:59:59Z',

  description: 'Simulación en el box. Mejora clara en wall balls.',

  creationDate: '2026-08-09T18:30:00Z',

  commentsCount: 5,

  likesCount: 18,

  likedByCurrentUser: false,

  participantsCount: 3,

  joinedByCurrentUser: false,

  streakWeeks: null,

  weekDayTags: null,

  wodPostId: null,

  wodTitle: null,

  wodCheckinsCount: null,

  wodCheckinAuthors: null,

  goalMark: { ...BASE_MARK },

  goal: null,

};



/** Amigo que ya participa en el objetivo — «Ya participas». */

export const MOCK_GOAL_MARK_JOINED: FeedPostDto = {

  ...MOCK_GOAL_MARK_VISITOR,

  id: -9002,

  likesCount: 21,

  likedByCurrentUser: true,

  participantsCount: 4,

  joinedByCurrentUser: true,

  goalMark: {

    ...BASE_MARK,

    friendsCount: 4,

    friendAvatars: [

      { userId: 2, username: 'laura.rx', photoUrl: 'https://i.pravatar.cc/48?u=laura' },

      ...BASE_MARK.friendAvatars,

    ],

  },

};



/** Vista del autor de la marca — sin botón de adhesión. */

export const MOCK_GOAL_MARK_AUTHOR: FeedPostDto = {

  ...MOCK_GOAL_MARK_VISITOR,

  id: -9003,

  userId: 99,

  username: 'tu_usuario',

  photoUrl: 'https://i.pravatar.cc/48?u=tu_usuario',

  description: 'PR en simulacro. Aún me falta para la meta.',

  likesCount: 9,

  joinedByCurrentUser: true,

  goalMark: { ...BASE_MARK, progressPercent: 68, markValue: 5580, previousPrValue: 5700 },

};



/** Meta numérica (kg) — deadlift del seed. */

export const MOCK_GOAL_MARK_KG: FeedPostDto = {

  id: -9004,

  userId: 1,

  username: 'pedro.alonso',

  photoUrl: 'https://i.pravatar.cc/48?u=pedro.alonso',

  postType: 'GOAL_MARK',

  boxId: null,

  title: 'Deadlift 180 kg',

  trainingTag: null,

  challengeDeadline: '2026-10-15T23:59:59Z',

  description: 'Sesión de fuerza. Buen día de tirones.',

  creationDate: '2026-08-11T11:00:00Z',

  commentsCount: 1,

  likesCount: 6,

  likedByCurrentUser: false,

  participantsCount: 2,

  joinedByCurrentUser: false,

  streakWeeks: null,

  weekDayTags: null,

  wodPostId: null,

  wodTitle: null,

  wodCheckinsCount: null,

  wodCheckinAuthors: null,

  goalMark: {

    goalId: 2,

    goalTitle: 'Deadlift 180 kg',

    goalStatus: 'ACTIVE',

    progressPercent: 94,

    targetValue: 180,

    unit: 'kg',

    direction: 'higher',

    deadline: '2026-10-15T23:59:59Z',

    markValue: 170,

    previousPrValue: 165,

    friendsCount: 1,

    friendAvatars: [

      { userId: 6, username: 'ana.gym', photoUrl: 'https://i.pravatar.cc/48?u=ana' },

    ],

  },

  goal: null,

};



/** Alias usado en el feed real hasta que exista backend. */

export const MOCK_GOAL_MARK_POST = MOCK_GOAL_MARK_VISITOR;



export const GOAL_MARK_DEMO_VARIANTS = [

  { label: 'Amigo — Me uno', mock: MOCK_GOAL_MARK_VISITOR, isAuthor: false },

  { label: 'Amigo — Ya participas', mock: MOCK_GOAL_MARK_JOINED, isAuthor: false },

  { label: 'Autor — sin botón', mock: MOCK_GOAL_MARK_AUTHOR, isAuthor: true },

  { label: 'Meta en kg (deadlift)', mock: MOCK_GOAL_MARK_KG, isAuthor: false },

] as const;


