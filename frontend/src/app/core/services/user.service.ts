import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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
}
