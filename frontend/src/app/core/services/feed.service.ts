import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export type FeedPostType =
  | 'CHECKIN'
  | 'BOX_WOD'
  | 'BOX_CHALLENGE'
  | 'BOX_ANNOUNCEMENT'
  | 'GOAL_MARK';

export type FeedTrainingTag = 'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'DESCANSO_ACTIVO' | 'OTRO';

export type GoalFeedUnit = 'time' | 'reps' | 'kg' | 'meters';
export type GoalFeedDirection = 'lower' | 'higher';
export type GoalFeedStatus = 'ACTIVE' | 'ACHIEVED' | 'EXPIRED';

export interface GoalFriendPreviewDto {
  userId: number;
  username: string;
  photoUrl: string | null;
}

/** Payload de publicación GOAL_MARK (nueva marca en un objetivo). */
export interface GoalMarkFeedDto {
  goalId: number;
  goalTitle: string;
  goalStatus: GoalFeedStatus;
  progressPercent: number;
  targetValue: number;
  unit: GoalFeedUnit;
  direction: GoalFeedDirection;
  deadline: string;
  markValue: number;
  previousPrValue: number | null;
  friendsCount: number;
  friendAvatars: GoalFriendPreviewDto[];
}

export interface FeedPostDto {
  id: number;
  userId: number;
  username: string;
  photoUrl: string | null;
  postType: FeedPostType;
  boxId: number | null;
  title: string | null;
  trainingTag: FeedTrainingTag | null;
  challengeDeadline: string | null;
  description: string | null;
  creationDate: string;
  commentsCount: number;
  likesCount: number;
  likedByCurrentUser: boolean;
  participantsCount: number;
  joinedByCurrentUser: boolean;
  /** Semanas ISO consecutivas de constancia; solo en CHECKIN, null en el resto. */
  streakWeeks: number | null;
  /** Tags L→D de la semana ISO actual (null = inactivo); solo en CHECKIN. */
  weekDayTags: Array<FeedTrainingTag | null> | null;
  /** CHECKIN vinculado a WOD. */
  wodPostId: number | null;
  wodTitle: string | null;
  /** BOX_WOD: muro de participantes. */
  wodCheckinsCount: number | null;
  wodCheckinAuthors: WodCheckinAuthorDto[] | null;
  /** GOAL_MARK: detalle de marca y estado del objetivo. */
  goalMark: GoalMarkFeedDto | null;
}

export interface WodCheckinAuthorDto {
  userId: number;
  username: string;
  photoUrl: string | null;
  /** Fecha de apuntarse (joined_at). */
  checkedInAt?: string | null;
}

export interface LikeToggleDto {
  liked: boolean;
  likesCount: number;
}

export interface JoinToggleDto {
  joined: boolean;
  participantsCount: number;
}

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly api = inject(ApiService);

  getFeed(size = 5): Observable<FeedPostDto[]> {
    return this.api.get<FeedPostDto[]>(`/feed?size=${size}`);
  }

  getNextPage(cursorDate: string, cursorId: number, size = 5): Observable<FeedPostDto[]> {
    return this.api.get<FeedPostDto[]>(
      `/feed?cursorDate=${encodeURIComponent(cursorDate)}&cursorId=${cursorId}&size=${size}`
    );
  }

  getUserPosts(userId: number): Observable<FeedPostDto[]> {
    return this.api.get<FeedPostDto[]>(`/user/${userId}/posts`);
  }

  getWodParticipants(wodPostId: number): Observable<WodCheckinAuthorDto[]> {
    return this.api.get<WodCheckinAuthorDto[]>(`/feed/posts/${wodPostId}/wod-participants`);
  }

  toggleLike(postId: number): Observable<LikeToggleDto> {
    return this.api.post<LikeToggleDto>(`/feed/posts/${postId}/like`, {});
  }

  toggleJoin(postId: number): Observable<JoinToggleDto> {
    return this.api.post<JoinToggleDto>(`/feed/posts/${postId}/join`, {});
  }
}
