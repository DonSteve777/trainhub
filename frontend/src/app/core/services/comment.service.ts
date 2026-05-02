import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface CommentDto {
  id: number;
  username: string;
  avatarUrl: string | null;
  content: string;
  creationDate: string;
  likesCount: number;
  likedByCurrentUser: boolean;
}

export interface CommentLikeToggleDto {
  liked: boolean;
  likesCount: number;
}

@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly api = inject(ApiService);

  getComments(postId: number): Observable<CommentDto[]> {
    return this.api.get<CommentDto[]>(`/posts/${postId}/comments`);
  }

  addComment(postId: number, content: string): Observable<CommentDto> {
    return this.api.post<CommentDto>(`/posts/${postId}/comments`, { content });
  }

  toggleCommentLike(postId: number, commentId: number): Observable<CommentLikeToggleDto> {
    return this.api.post<CommentLikeToggleDto>(`/posts/${postId}/comments/${commentId}/likes`, {});
  }
}
