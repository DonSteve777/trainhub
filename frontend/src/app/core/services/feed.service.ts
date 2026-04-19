import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface FeedPostDto {
  id: number;
  userId: number;
  username: string;
  photoUrl: string;
  description: string;
  totalTime: number;
  athleteRunTime: number;
  athleteWorkoutTime: number;
  allTotalTimes: number[];
  allRunTimes: number[];
  allWorkoutTimes: number[];
  commentsCount: number;
  likesCount: number;
  creationDate: string;
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
}
