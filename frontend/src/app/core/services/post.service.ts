import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export type TrainingTag = 'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'OTRO';

export interface NewCheckinRequest {
  trainingTag: TrainingTag;
  description?: string;
  mateUsername?: string;
}

@Injectable({ providedIn: 'root' })
export class PostService {
  private readonly api = inject(ApiService);

  createCheckin(payload: NewCheckinRequest): Observable<void> {
    return this.api.post<void>('/posts/checkin', payload);
  }
}
