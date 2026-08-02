import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface FeedPostDto {
  id: number;
  userId: number;
  username: string;
  photoUrl: string | null;
  r1Time: string;
  r2Time: string;
  r3Time: string;
  r4Time: string;
  r5Time: string;
  r6Time: string;
  r7Time: string;
  r8Time: string;
  skiErgTime: string;
  sledPushTime: string;
  sledPullTime: string;
  burpeeBjTime: string;
  rowTime: string;
  farmersCarryTime: string;
  sandbagLungesTime: string;
  wallBallsTime: string;
  totalTime: string;
  description: string | null;
  category: string | null;
  mateId: number | null;
  mateUsername: string | null;
  creationDate: string;
  commentsCount: number;
  likesCount: number;
  likedByCurrentUser: boolean;
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
