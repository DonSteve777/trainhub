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
  w1Time: string;
  w2Time: string;
  w3Time: string;
  w4Time: string;
  w5Time: string;
  w6Time: string;
  w7Time: string;
  w8Time: string;
  totalTime: string;
  description: string | null;
  creationDate: string;
  commentsCount: number;
  likesCount: number;
}

export interface FeedHistoryDto {
  totalsHistory: number[];
  runHistory: number[];
  workoutHistory: number[];
}

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly api = inject(ApiService);

  getFeed(size = 5): Observable<FeedPostDto[]> {
    return this.api.get<FeedPostDto[]>(`/feed?size=${size}`);
  }

  getNextPage(cursorDate: string, cursorId: number, size = 5): Observable<FeedPostDto[]> {
    return this.api.get<FeedPostDto[]>(
      `/feed?cursorDate=${encodeURIComponent(cursorDate)}&cursorId=${cursorId}&size=${size}`,
    );
  }

  getHistory(): Observable<FeedHistoryDto> {
    return this.api.get<FeedHistoryDto>('/feed/history');
  }
}
