import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface NotificationDto {
  type:
    | 'LIKE'
    | 'COMMENT'
    | 'COMMENT_LIKE'
    | 'FRIEND_REQUEST'
    | 'FRIEND_REQUEST_ACCEPTED'
    | 'GOAL_JOIN';
  postId: number | null;
  postCreationDate: string | null;
  lastActorUsername: string;
  lastActorPhotoUrl: string | null;
  lastActionAt: string;
  totalCount: number;
  unread: boolean;
  actorId: number | null;
  commentId: number | null;
  goalTitle?: string | null;
}

export interface LikerDto {
  userId: number;
  username: string;
  photoUrl: string | null;
  likedAt: string;
}

export interface UnreadCountDto {
  count: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly api = inject(ApiService);

  getNotifications(): Observable<NotificationDto[]> {
    return this.api.get<NotificationDto[]>('/notifications');
  }

  getUnreadCount(): Observable<UnreadCountDto> {
    return this.api.get<UnreadCountDto>('/notifications/count');
  }

  markSeen(): Observable<void> {
    return this.api.post<void>('/notifications/mark-seen', {});
  }

  getPostLikers(postId: number): Observable<LikerDto[]> {
    return this.api.get<LikerDto[]>(`/notifications/posts/${postId}/likers`);
  }
}
