import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export type TrainingTag = 'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'DESCANSO_ACTIVO' | 'OTRO';

export type BoxPostType = 'BOX_WOD' | 'BOX_CHALLENGE' | 'BOX_ANNOUNCEMENT';

export interface NewCheckinRequest {
  trainingTag: TrainingTag;
  description?: string;
  wodPostId?: number;
}

export interface BoxWodSummaryDto {
  id: number;
  title: string;
  description: string | null;
  trainingTag: TrainingTag | null;
  creationDate: string;
}

export interface NewBoxPostRequest {
  postType: BoxPostType;
  title: string;
  description: string;
  trainingTag?: TrainingTag;
  challengeDeadline?: string;
}

@Injectable({ providedIn: 'root' })
export class PostService {
  private readonly api = inject(ApiService);

  createCheckin(payload: NewCheckinRequest): Observable<void> {
    return this.api.post<void>('/posts/checkin', payload);
  }

  listRecentBoxWods(): Observable<BoxWodSummaryDto[]> {
    return this.api.get<BoxWodSummaryDto[]>('/posts/box/wods');
  }

  createBoxPost(payload: NewBoxPostRequest): Observable<void> {
    return this.api.post<void>('/posts/box', payload);
  }
}
