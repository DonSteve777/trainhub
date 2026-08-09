import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface TimeEntryDto {
  time: number;
  date: string;
}

export interface FriendTimeEntryDto {
  time: number;
  date: string;
}

export interface FriendTimeHistoryDto {
  username: string;
  totalHistory: FriendTimeEntryDto[];
  workoutsHistory: FriendTimeEntryDto[];
  runsHistory: FriendTimeEntryDto[];
}

export interface UserTimeHistoryDto {
  totalHistory:    TimeEntryDto[];
  workoutsHistory: TimeEntryDto[];
  runsHistory:     TimeEntryDto[];
}

export type UserRole = 'USER' | 'BOX_ADMIN';

export interface UserProfileDto {
  id: number;
  email: string;
  photoUrl: string | null;
  name: string;
  accountStatus: string;
  emailVerified: boolean;
  gender: 'MALE' | 'FEMALE' | null;
  role: UserRole | null;
  boxId: number | null;
}

export interface UserSearchResultDto {
  id: number;
  username: string;
  photoUrl: string | null;
  friendshipStatus: 'NONE' | 'PENDING' | 'FRIEND';
}

export interface PersonalRecordEntryDto {
  time: number;
  date: string;
}

export interface PersonalRecordsDto {
  bestTotal:          PersonalRecordEntryDto | null;
  bestRunning:        PersonalRecordEntryDto | null;
  bestSkiErg:         PersonalRecordEntryDto | null;
  bestSledPush:       PersonalRecordEntryDto | null;
  bestSledPull:       PersonalRecordEntryDto | null;
  bestBurpeeBj:       PersonalRecordEntryDto | null;
  bestRow:            PersonalRecordEntryDto | null;
  bestFarmersCarry:   PersonalRecordEntryDto | null;
  bestSandbagLunges:  PersonalRecordEntryDto | null;
  bestWallBalls:      PersonalRecordEntryDto | null;
}

export interface WeeklyConstancyDto {
  streakWeeks: number;
  weekDayTags: Array<'HYROX' | 'FUERZA' | 'CARRERA' | 'CLASE' | 'DESCANSO_ACTIVO' | 'OTRO' | null>;
  weekActiveCount: number;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);

  getTimeHistory(userId?: number): Observable<UserTimeHistoryDto> {
    const url = userId != null ? `/user/${userId}/time-history` : '/user/time-history';
    return this.api.get<UserTimeHistoryDto>(url);
  }

  getFriendsTimeHistory(): Observable<FriendTimeHistoryDto[]> {
    return this.api.get<FriendTimeHistoryDto[]>('/user/friends/time-history');
  }

  getPersonalRecords(userId?: number): Observable<PersonalRecordsDto> {
    const url = userId != null ? `/user/${userId}/personal-records` : '/user/personal-records';
    return this.api.get<PersonalRecordsDto>(url);
  }

  getWeeklyConstancy(): Observable<WeeklyConstancyDto> {
    return this.api.get<WeeklyConstancyDto>('/user/constancy');
  }

  getProfile(userId?: number): Observable<UserProfileDto> {
    const url = userId != null ? `/user/${userId}/profile` : '/user/me';
    return this.api.get<UserProfileDto>(url);
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

  acceptFriendRequest(requesterId: number): Observable<void> {
    return this.api.post<void>(`/user/${requesterId}/friend-request/accept`, {});
  }

  rejectFriendRequest(requesterId: number): Observable<void> {
    return this.api.delete<void>(`/user/${requesterId}/friend-request`);
  }
}
