import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export type FeedPostType = 'CHECKIN' | 'RESULT' | 'BOX_WOD' | 'BOX_CHALLENGE' | 'BOX_ANNOUNCEMENT';

export type FeedTrainingTag = 'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'OTRO';

export interface FeedPostDto {
  id: number;
  userId: number;
  username: string;
  photoUrl: string | null;
  postType: FeedPostType;
  boxId: number | null;
  title: string | null;
  r1Time: string | null;
  r2Time: string | null;
  r3Time: string | null;
  r4Time: string | null;
  r5Time: string | null;
  r6Time: string | null;
  r7Time: string | null;
  r8Time: string | null;
  skiErgTime: string | null;
  sledPushTime: string | null;
  sledPullTime: string | null;
  burpeeBjTime: string | null;
  rowTime: string | null;
  farmersCarryTime: string | null;
  sandbagLungesTime: string | null;
  wallBallsTime: string | null;
  totalTime: string | null;
  trainingTag: FeedTrainingTag | null;
  challengeDeadline: string | null;
  description: string | null;
  category: string | null;
  mateId: number | null;
  mateUsername: string | null;
  creationDate: string;
  commentsCount: number;
  likesCount: number;
  likedByCurrentUser: boolean;
  participantsCount: number;
  joinedByCurrentUser: boolean;
}

export interface LikeToggleDto {
  liked: boolean;
  likesCount: number;
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

  toggleLike(postId: number): Observable<LikeToggleDto> {
    return this.api.post<LikeToggleDto>(`/feed/posts/${postId}/like`, {});
  }
}
