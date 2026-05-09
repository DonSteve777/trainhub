import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  UserService,
  UserTimeHistoryDto,
  UserProfileDto,
  TimeEntryDto,
  PersonalRecordsDto,
  PersonalRecordEntryDto,
  FriendTimeHistoryDto,
} from '../../core/services/user.service';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import {
  ProgressionChartComponent,
  FriendSeries,
} from '../../shared/components/progression-chart/progression-chart.component';

interface PersonalRecord {
  time: number;
  date: string;
}

interface CalendarCell {
  dayNum: number;
  active: boolean;
  isToday: boolean;
  isFuture: boolean;
}

interface StationRecord {
  label: string;
  icon: string;
  record: PersonalRecordEntryDto | null;
  color: string;
}

@Component({
  selector: 'app-time-history',
  standalone: true,
  imports: [RouterLink, MatIconModule, ProgressionChartComponent],
  templateUrl: './time-history.component.html',
  styleUrl: './time-history.component.scss',
})
export class TimeHistoryComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);

  history = signal<UserTimeHistoryDto | null>(null);
  profile = signal<UserProfileDto | null>(null);
  records = signal<PersonalRecordsDto | null>(null);
  friendsHistory = signal<FriendTimeHistoryDto[]>([]);
  error = signal<string | null>(null);
  targetUserId = signal<number | null>(null);

  readonly weekDayLabels = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];

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

  /** Cuadrícula 4×7 (lunes→domingo) con las actividades de las últimas 4 semanas */
  calendarGrid = computed((): CalendarCell[][] => {
    const h = this.history();
    if (!h) return [];

    const activeDays = new Set(
      h.totalHistory.map((e) => e.date.substring(0, 10)),
    );

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Retroceder hasta el lunes de hace 4 semanas
    const mondayOffset = (today.getDay() + 6) % 7; // días desde el lunes actual
    const gridStart = new Date(today);
    gridStart.setDate(today.getDate() - mondayOffset - 21);

    return Array.from({ length: 4 }, (_, w) =>
      Array.from({ length: 7 }, (_, d) => {
        const date = new Date(gridStart);
        date.setDate(gridStart.getDate() + w * 7 + d);
        const iso = date.toISOString().substring(0, 10);
        return {
          dayNum: date.getDate(),
          active: activeDays.has(iso),
          isToday: date.getTime() === today.getTime(),
          isFuture: date.getTime() > today.getTime(),
        };
      }),
    );
  });

  /** Records globales (total y running) para la sección destacada */
  topRecords = computed(() => {
    const r = this.records();
    if (!r) return null;
    return {
      total: r.bestTotal,
      running: r.bestRunning,
    };
  });

  /** Records individuales por estación */
  stationRecords = computed((): StationRecord[] => {
    const r = this.records();
    if (!r) return [];
    return [
      { label: 'SkiErg',           icon: 'downhill_skiing', record: r.bestSkiErg,        color: '#60C8FF' },
      { label: 'Sled Push',        icon: 'fitness_center',  record: r.bestSledPush,       color: '#60C8FF' },
      { label: 'Sled Pull',        icon: 'fitness_center',  record: r.bestSledPull,       color: '#60C8FF' },
      { label: 'Burpee Broad Jump',icon: 'directions_run',  record: r.bestBurpeeBj,       color: '#60C8FF' },
      { label: 'Row',              icon: 'rowing',          record: r.bestRow,            color: '#60C8FF' },
      { label: 'Farmers Carry',    icon: 'work',            record: r.bestFarmersCarry,   color: '#60C8FF' },
      { label: 'Sandbag Lunges',   icon: 'accessibility_new', record: r.bestSandbagLunges, color: '#60C8FF' },
      { label: 'Wall Balls',       icon: 'sports_basketball',record: r.bestWallBalls,     color: '#60C8FF' },
    ];
  });

  totalRecord    = computed(() => this.bestEntry(this.history()?.totalHistory));
  workoutsRecord = computed(() => this.bestEntry(this.history()?.workoutsHistory));
  runsRecord     = computed(() => this.bestEntry(this.history()?.runsHistory));

  ngOnInit(): void {
    const userIdParam = this.route.snapshot.paramMap.get('userId');
    const userId = userIdParam != null ? +userIdParam : undefined;
    this.targetUserId.set(userId ?? null);

    forkJoin({
      history: this.userService.getTimeHistory(userId),
      profile: this.userService.getProfile(userId),
      records: this.userService.getPersonalRecords(userId),
    }).subscribe({
      next: ({ history, profile, records }) => {
        this.history.set(history);
        this.profile.set(profile);
        this.records.set(records);
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
