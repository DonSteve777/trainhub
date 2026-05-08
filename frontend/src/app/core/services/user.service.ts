import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface TimeEntryDto {
  time: number;
  date: string;
}

export interface UserTimeHistoryDto {
  totalHistory:    TimeEntryDto[];
  workoutsHistory: TimeEntryDto[];
  runsHistory:     TimeEntryDto[];
}

export interface UserProfileDto {
  id: number;
  email: string;
  photoUrl: string | null;
  name: string;
  accountStatus: string;
  emailVerified: boolean;
  gender: 'MALE' | 'FEMALE' | null;
}

export interface UserSearchResultDto {
  id: number;
  username: string;
  photoUrl: string | null;
  friendshipStatus: 'NONE' | 'PENDING' | 'FRIEND';
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  getTimeHistory(): Observable<UserTimeHistoryDto> {
    return this.api.get<UserTimeHistoryDto>('/user/time-history');
  }

  getProfile(): Observable<UserProfileDto> {
    return this.api.get<UserProfileDto>('/user/me');
  }

  searchUsers(q: string, limit = 5): Observable<UserSearchResultDto[]> {
    return this.api.get<UserSearchResultDto[]>(
      `/user/search?q=${encodeURIComponent(q)}&limit=${limit}`
    );
  }

  friendshipStatus(targetUserId: number): Observable<'NONE' | 'PENDING' | 'FRIEND'> {
    return this.api.get<{ status: 'NONE' | 'PENDING' | 'FRIEND' }>(
      `/user/${targetUserId}/friendship-status`
    ).pipe(map((res) => res.status));
  }

  sendFriendRequest(targetUserId: number): Observable<void> {
    return this.api.post<void>(`/user/${targetUserId}/friend-request`, {});
  }
}
