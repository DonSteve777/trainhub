import { FeedPostDto } from '../../core/services/feed.service';

/**
 * Post de demo para la UI de GOAL_MARK hasta que exista backend.
 * IDs negativos para no colisionar con posts reales.
 */
export const MOCK_GOAL_MARK_POST: FeedPostDto = {
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
  joinedByCurrentUser: true,
  streakWeeks: null,
  weekDayTags: null,
  wodPostId: null,
  wodTitle: null,
  wodCheckinsCount: null,
  wodCheckinAuthors: null,
  goalMark: {
    goalId: 1,
    goalTitle: 'Sub 90 en Hyrox Open',
    goalStatus: 'ACTIVE',
    progressPercent: 72,
    targetValue: 5400,
    unit: 'time',
    direction: 'lower',
    deadline: '2026-09-15T23:59:59Z',
    markValue: 5520,
    previousPrValue: 5640,
    friendsCount: 3,
    friendAvatars: [
      { userId: 3, username: 'marco.hyrox', photoUrl: 'https://i.pravatar.cc/48?u=3' },
      { userId: 4, username: 'sofia.fit', photoUrl: 'https://i.pravatar.cc/48?u=4' },
      { userId: 5, username: 'dani.strength', photoUrl: 'https://i.pravatar.cc/48?u=5' },
    ],
  },
};
