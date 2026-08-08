import { Component, OnInit, inject, signal, computed } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import {
  PostService,
  type NewCheckinRequest,
  type TrainingTag,
} from '../../core/services/post.service';
import { UserService, UserSearchResultDto } from '../../core/services/user.service';

/** Parsea "m:ss" o "mm:ss" (segundos 0–59). Devuelve null si no es válido. */
export function parseMmSsToSeconds(value: string): number | null {
  const v = value.trim();
  const match = v.match(/^(\d+):(\d{1,2})$/);
  if (!match) return null;
  const minutes = parseInt(match[1], 10);
  const seconds = parseInt(match[2], 10);
  if (seconds < 0 || seconds > 59) return null;
  return minutes * 60 + seconds;
}

export function mmSsTimeValidator(control: AbstractControl): ValidationErrors | null {
  const raw = control.value;
  if (raw === null || raw === undefined) return { timeFormat: true };
  const str = String(raw).trim();
  if (str === '') return { required: true };
  return parseMmSsToSeconds(str) === null ? { timeFormat: true } : null;
}

/**
 * Toma solo cifras (máx. 4) y devuelve el texto mostrado: tras la 2.ª cifra se inserta ":".
 * Ej. "1" → "1", "12" → "12:", "123" → "12:3", "1234" → "12:34"
 */
export function formatMmSsDigitsFromInput(digits: string): string {
  const d = digits.replace(/\D/g, '').slice(0, 4);
  if (d.length === 0) return '';
  if (d.length === 1) return d;
  if (d.length === 2) return `${d}:`;
  const mm = d.slice(0, 2);
  const ss = d.slice(2);
  return `${mm}:${ss}`;
}

/** Una fila del formulario: tramo de 1 km Running + prueba de la secuencia HYROX. */
export interface HyroxFormRow {
  runningKey: 'r1Time' | 'r2Time' | 'r3Time' | 'r4Time' | 'r5Time' | 'r6Time' | 'r7Time' | 'r8Time';
  runningLabel: string;
  stationKey:
    | 'skiErg'
    | 'sledPush'
    | 'sledPull'
    | 'burpeeBroadJump'
    | 'row'
    | 'farmersCarry'
    | 'sandbagLunges'
    | 'wallBalls';
  stationLabel: string;
}

type PostMode = 'CHECKIN' | 'HYROX';

interface TrainingTagOption {
  value: TrainingTag;
  label: string;
}

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.scss',
})
export class CreatePostComponent implements OnInit {
  postForm!: FormGroup;
  private readonly fb = inject(FormBuilder);
  private readonly apiService = inject(ApiService);
  private readonly postService = inject(PostService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  private readonly userGender = signal<'MALE' | 'FEMALE' | null>(null);

  readonly postMode = signal<PostMode>('HYROX');
  readonly isCheckinPost = computed(() => this.postMode() === 'CHECKIN');
  readonly isHyroxPost = computed(() => this.postMode() === 'HYROX');

  readonly trainingTagOptions: readonly TrainingTagOption[] = [
    { value: 'HYROX', label: 'HYROX' },
    { value: 'FUERZA', label: 'Fuerza' },
    { value: 'CARRERA', label: 'Carrera' },
    { value: 'CLASE', label: 'Clase' },
    { value: 'OTRO', label: 'Otro' },
  ];

  // --- Mate search ---
  private readonly DOUBLES = new Set(['DOUBLES_MIXED', 'DOUBLES_MALE', 'DOUBLES_FEMALE']);
  readonly selectedCategory = signal<string>('');
  readonly isDoubles = computed(() => this.DOUBLES.has(this.selectedCategory()));

  readonly mateQuery = signal('');
  readonly mateResults = signal<UserSearchResultDto[]>([]);
  readonly selectedMate = signal<UserSearchResultDto | null>(null);
  readonly mateSearching = signal(false);

  private mateSearchTimer: number | null = null;

  readonly checkinMateQuery = signal('');
  readonly checkinMateResults = signal<UserSearchResultDto[]>([]);
  readonly selectedCheckinMate = signal<UserSearchResultDto | null>(null);
  readonly checkinMateSearching = signal(false);

  private checkinMateSearchTimer: number | null = null;

  /** Orden oficial HYROX: Running + estación por fila. */
  readonly hyroxRows: readonly HyroxFormRow[] = [
    {
      runningKey: 'r1Time',
      runningLabel: '1 km Running 1',
      stationKey: 'skiErg',
      stationLabel: 'SkiErg (máquina de esquí)',
    },
    {
      runningKey: 'r2Time',
      runningLabel: '1 km Running 2',
      stationKey: 'sledPush',
      stationLabel: '50 m Sled Push (empuje de trineo)',
    },
    {
      runningKey: 'r3Time',
      runningLabel: '1 km Running 3',
      stationKey: 'sledPull',
      stationLabel: '50 m Sled Pull',
    },
    {
      runningKey: 'r4Time',
      runningLabel: '1 km Running 4',
      stationKey: 'burpeeBroadJump',
      stationLabel: '80 m Burpee Broad Jump',
    },
    {
      runningKey: 'r5Time',
      runningLabel: '1 km Running 5',
      stationKey: 'row',
      stationLabel: '1000 m Rowing (remo)',
    },
    {
      runningKey: 'r6Time',
      runningLabel: '1 km Running 6',
      stationKey: 'farmersCarry',
      stationLabel: '200 m Farmers Carry',
    },
    {
      runningKey: 'r7Time',
      runningLabel: '1 km Running 7',
      stationKey: 'sandbagLunges',
      stationLabel: '100 m Sandbag Lunges',
    },
    {
      runningKey: 'r8Time',
      runningLabel: '1 km Running 8',
      stationKey: 'wallBalls',
      stationLabel: '100 Wall Balls',
    },
  ];

  private readonly allCategories: {
    value: string;
    label: string;
    allowedGenders: ('MALE' | 'FEMALE')[];
  }[] = [
    { value: 'INDIVIDUAL_MALE', label: 'Masculina', allowedGenders: ['MALE'] },
    { value: 'INDIVIDUAL_FEMALE', label: 'Femenina', allowedGenders: ['FEMALE'] },
    { value: 'DOUBLES_MIXED', label: 'Pareja mixta', allowedGenders: ['MALE', 'FEMALE'] },
    { value: 'DOUBLES_MALE', label: 'Pareja masculina', allowedGenders: ['MALE'] },
    { value: 'DOUBLES_FEMALE', label: 'Pareja femenina', allowedGenders: ['FEMALE'] },
  ];

  readonly categories = computed(() => {
    const gender = this.userGender();
    if (!gender) return this.allCategories;
    return this.allCategories.filter(c => c.allowedGenders.includes(gender));
  });

  readonly backendMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  ngOnInit(): void {
    this.postForm = this.fb.group({
      checkin: this.fb.group({
        trainingTag: ['', Validators.required],
        description: [''],
      }),
      category: ['', Validators.required],
      segments: this.fb.array(this.hyroxRows.map(() => this.createSegmentGroup())),
    });

    this.userService.getProfile().subscribe({
      next: profile => this.userGender.set(profile.gender),
    });

    this.postForm.get('category')!.valueChanges.subscribe((val: string) => {
      this.selectedCategory.set(val ?? '');
      if (!this.DOUBLES.has(val)) {
        this.clearMate();
      }
    });
  }

  selectPostMode(mode: PostMode): void {
    if (this.submitting()) return;
    this.backendMessage.set(null);
    this.postMode.set(mode);
  }

  get segments(): FormArray {
    return this.postForm.get('segments') as FormArray;
  }

  get checkinForm(): FormGroup {
    return this.postForm.get('checkin') as FormGroup;
  }

  hyroxFormInvalid(): boolean {
    return Boolean(this.postForm.get('category')?.invalid || this.segments.invalid);
  }

  private createSegmentGroup(): FormGroup {
    const timeControl = (initial: string) =>
      this.fb.control(initial, {
        validators: [Validators.required, mmSsTimeValidator],
        updateOn: 'blur',
      });
    return this.fb.group({
      running: timeControl(''),
      station: timeControl(''),
    });
  }

  /** Formatea el input mientras se escribe (solo cifras → mm:ss con ":" automático). */
  onMmSsInput(event: Event, index: number, field: 'running' | 'station'): void {
    const input = event.target as HTMLInputElement;
    const formatted = formatMmSsDigitsFromInput(input.value);
    const control = this.segments.at(index).get(field)!;
    if (control.value !== formatted) {
      control.setValue(formatted, { emitEvent: true });
    }
    window.queueMicrotask(() => {
      const len = input.value.length;
      input.setSelectionRange(len, len);
    });
  }

  onMateInput(query: string): void {
    this.mateQuery.set(query);
    this.selectedMate.set(null);
    if (this.mateSearchTimer) window.clearTimeout(this.mateSearchTimer);
    if (!query.trim()) {
      this.mateResults.set([]);
      this.mateSearching.set(false);
      return;
    }
    this.mateSearching.set(true);
    this.mateSearchTimer = window.setTimeout(() => {
      this.userService.searchUsers(query.trim(), 5).subscribe({
        next: results => {
          this.mateResults.set(results);
          this.mateSearching.set(false);
        },
        error: () => this.mateSearching.set(false),
      });
    }, 300);
  }

  selectMate(user: UserSearchResultDto): void {
    this.selectedMate.set(user);
    this.mateResults.set([]);
    this.mateQuery.set('');
  }

  clearMate(): void {
    this.selectedMate.set(null);
    this.mateResults.set([]);
    this.mateQuery.set('');
    this.mateSearching.set(false);
  }

  onCheckinMateInput(query: string): void {
    this.checkinMateQuery.set(query);
    this.selectedCheckinMate.set(null);
    if (this.checkinMateSearchTimer) window.clearTimeout(this.checkinMateSearchTimer);
    if (!query.trim()) {
      this.checkinMateResults.set([]);
      this.checkinMateSearching.set(false);
      return;
    }
    this.checkinMateSearching.set(true);
    this.checkinMateSearchTimer = window.setTimeout(() => {
      this.userService.searchUsers(query.trim(), 5).subscribe({
        next: results => {
          this.checkinMateResults.set(results);
          this.checkinMateSearching.set(false);
        },
        error: () => this.checkinMateSearching.set(false),
      });
    }, 300);
  }

  selectCheckinMate(user: UserSearchResultDto): void {
    this.selectedCheckinMate.set(user);
    this.checkinMateResults.set([]);
    this.checkinMateQuery.set('');
  }

  clearCheckinMate(): void {
    this.selectedCheckinMate.set(null);
    this.checkinMateResults.set([]);
    this.checkinMateQuery.set('');
    this.checkinMateSearching.set(false);
  }

  private buildPayload(): Record<string, number | string> {
    const formValue = this.postForm.getRawValue();
    const rows = formValue.segments as Array<{ running: string; station: string }>;
    const payload: Record<string, number | string> = {
      category: formValue.category as string,
    };
    const mate = this.selectedMate();
    if (mate && this.isDoubles()) payload['mateUsername'] = mate.username;
    this.hyroxRows.forEach((row, i) => {
      const seg = rows[i];
      const r = parseMmSsToSeconds(seg.running);
      const s = parseMmSsToSeconds(seg.station);
      if (r === null || s === null) {
        throw new Error('Tiempos inválidos');
      }
      payload[row.runningKey] = r;
      payload[row.stationKey] = s;
    });
    return payload;
  }

  private buildCheckinPayload(): NewCheckinRequest {
    const formValue = this.checkinForm.getRawValue() as {
      trainingTag: TrainingTag;
      description?: string;
    };
    const payload: NewCheckinRequest = {
      trainingTag: formValue.trainingTag,
    };
    const description = formValue.description?.trim();
    if (description) payload.description = description;
    const mate = this.selectedCheckinMate();
    if (mate) payload.mateUsername = mate.username;
    return payload;
  }

  onSubmit(): void {
    if (this.isCheckinPost()) {
      this.submitCheckin();
      return;
    }
    if (this.hyroxFormInvalid() || this.submitting()) return;
    this.backendMessage.set(null);
    this.submitting.set(true);
    let body: Record<string, number | string>;
    try {
      body = this.buildPayload();
    } catch {
      this.submitting.set(false);
      this.backendMessage.set('Revisa el formato de los tiempos (mm:ss).');
      return;
    }
    this.apiService.post<void>('/posts', body).subscribe({
      next: () => void this.router.navigate(['/feed'], { replaceUrl: true }),
      error: err => {
        this.submitting.set(false);
        const errBody = err.error;
        this.backendMessage.set(
          errBody?.message ??
            errBody?.error ??
            'No se pudo publicar el entrenamiento. Inténtalo de nuevo.'
        );
      },
    });
  }

  private submitCheckin(): void {
    if (this.submitting()) return;
    if (this.checkinForm.invalid) {
      this.checkinForm.markAllAsTouched();
      return;
    }
    this.backendMessage.set(null);
    this.submitting.set(true);
    this.postService.createCheckin(this.buildCheckinPayload()).subscribe({
      next: () => void this.router.navigate(['/feed'], { replaceUrl: true }),
      error: err => {
        this.submitting.set(false);
        const errBody = err.error;
        this.backendMessage.set(
          errBody?.message ??
            errBody?.error ??
            'No se pudo publicar el check-in. Inténtalo de nuevo.'
        );
      },
    });
  }
}
