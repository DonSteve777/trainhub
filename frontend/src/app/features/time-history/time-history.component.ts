import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { UserService, UserTimeHistoryDto, UserProfileDto, TimeEntryDto } from '../../core/services/user.service';
import { ProgressionChartComponent } from '../../shared/components/progression-chart/progression-chart.component';

interface PersonalRecord {
  time: number;
  date: string;
}

@Component({
  selector: 'app-time-history',
  standalone: true,
  imports: [ProgressionChartComponent],
  templateUrl: './time-history.component.html',
  styleUrl: './time-history.component.scss',
})
export class TimeHistoryComponent implements OnInit {
  private readonly userService = inject(UserService);

  history = signal<UserTimeHistoryDto | null>(null);
  profile = signal<UserProfileDto | null>(null);
  error = signal<string | null>(null);

  avatarUrl = computed(() => {
    const p = this.profile();
    return p?.photoUrl ?? `https://i.pravatar.cc/80?u=${p?.id ?? 0}`;
  });

  lastFourWeeksCount = computed(() => {
    const h = this.history();
    if (!h) return 0;
    const cutoff = Date.now() - 28 * 24 * 60 * 60 * 1000;
    return h.totalHistory.filter((e) => new Date(e.date).getTime() >= cutoff).length;
  });

  totalRecord = computed(() => this.bestEntry(this.history()?.totalHistory));
  workoutsRecord = computed(() => this.bestEntry(this.history()?.workoutsHistory));
  runsRecord = computed(() => this.bestEntry(this.history()?.runsHistory));

  ngOnInit(): void {
    forkJoin({
      history: this.userService.getTimeHistory(),
      profile: this.userService.getProfile(),
    }).subscribe({
      next: ({ history, profile }) => {
        this.history.set(history);
        this.profile.set(profile);
      },
      error: (err) => {
        console.error('Error cargando datos del histórico', err);
        this.error.set('No se pudo cargar el histórico de tiempos.');
      },
    });
  }

  formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    if (h > 0) {
      return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    }
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  formatDate(isoString: string): string {
    const d = new Date(isoString);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
  }

  private bestEntry(entries: TimeEntryDto[] | undefined): PersonalRecord | null {
    if (!entries || entries.length === 0) return null;
    return entries.reduce((best, e) => (e.time < best.time ? e : best));
  }
}
