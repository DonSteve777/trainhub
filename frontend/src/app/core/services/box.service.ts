import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface BoxDto {
  id: number;
  name: string;
  cityName: string | null;
}

@Injectable({ providedIn: 'root' })
export class BoxService {
  private readonly api = inject(ApiService);

  listBoxes(): Observable<BoxDto[]> {
    return this.api.get<BoxDto[]>('/boxes');
  }
}
