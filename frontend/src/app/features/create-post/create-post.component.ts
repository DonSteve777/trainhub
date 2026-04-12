import { Component, OnInit, inject, signal } from '@angular/core';
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
  private readonly router = inject(Router);

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

  readonly backendMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  ngOnInit(): void {
    this.postForm = this.fb.group({
      segments: this.fb.array(this.hyroxRows.map(() => this.createSegmentGroup())),
    });
  }

  get segments(): FormArray {
    return this.postForm.get('segments') as FormArray;
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
    queueMicrotask(() => {
      const len = input.value.length;
      input.setSelectionRange(len, len);
    });
  }

  private buildPayload(): Record<string, number> {
    const rows = this.postForm.getRawValue().segments as Array<{
      running: string;
      station: string;
    }>;
    const payload: Record<string, number> = {};
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

  onSubmit(): void {
    if (this.postForm.invalid || this.submitting()) return;
    this.backendMessage.set(null);
    this.submitting.set(true);
    let body: Record<string, number>;
    try {
      body = this.buildPayload();
    } catch {
      this.submitting.set(false);
      this.backendMessage.set('Revisa el formato de los tiempos (mm:ss).');
      return;
    }
    this.apiService.post<void>('/posts', body).subscribe({
      next: () => void this.router.navigate(['/feed'], { replaceUrl: true }),
      error: (err) => {
        this.submitting.set(false);
        const errBody = err.error;
        this.backendMessage.set(
          errBody?.message ?? errBody?.error ?? 'No se pudo publicar el entrenamiento. Inténtalo de nuevo.',
        );
      },
    });
  }
}
