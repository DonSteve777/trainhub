import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface CommentDto {
  id: number;
  username: string;
  avatarUrl: string | null;
  content: string;
  creationDate: string;
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
}
