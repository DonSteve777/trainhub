import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  PostService,
  type BoxWodSummaryDto,
  type NewCheckinRequest,
  type TrainingTag,
} from '../../core/services/post.service';

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
  checkinForm!: FormGroup;
  private readonly fb = inject(FormBuilder);
  private readonly postService = inject(PostService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly boxWods = signal<BoxWodSummaryDto[]>([]);
  readonly boxWodsLoading = signal(false);

  readonly trainingTagOptions: readonly TrainingTagOption[] = [
    { value: 'HYROX', label: 'HYROX' },
    { value: 'FUERZA', label: 'Fuerza' },
    { value: 'CARRERA', label: 'Carrera' },
    { value: 'CLASE', label: 'Clase' },
    { value: 'DESCANSO_ACTIVO', label: 'Descanso activo' },
    { value: 'OTRO', label: 'Otro' },
  ];

  readonly backendMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  ngOnInit(): void {
    this.checkinForm = this.fb.group({
      trainingTag: ['', Validators.required],
      description: [''],
      wodPostId: [''],
    });

    const wodPostIdParam = this.route.snapshot.queryParamMap.get('wodPostId');
    if (wodPostIdParam) {
      this.checkinForm.get('wodPostId')!.setValue(wodPostIdParam);
    }

    this.checkinForm.get('wodPostId')!.valueChanges.subscribe((wodId: string) => {
      this.applyWodDefaults(wodId);
      this.syncTrainingTagLock(wodId);
    });
    this.syncTrainingTagLock(this.checkinForm.get('wodPostId')!.value);

    this.loadBoxWods();
  }

  private loadBoxWods(): void {
    this.boxWodsLoading.set(true);
    this.postService.listRecentBoxWods().subscribe({
      next: wods => {
        this.boxWods.set(wods);
        this.boxWodsLoading.set(false);
        const selected = this.checkinForm.get('wodPostId')?.value;
        if (selected && !wods.some(w => String(w.id) === String(selected))) {
          this.checkinForm.get('wodPostId')!.setValue('');
          return;
        }
        if (selected) {
          this.applyWodDefaults(String(selected));
          this.syncTrainingTagLock(String(selected));
        }
      },
      error: () => {
        this.boxWods.set([]);
        this.boxWodsLoading.set(false);
      },
    });
  }

  private applyWodDefaults(wodId: string): void {
    if (!wodId) return;
    const wod = this.boxWods().find(w => String(w.id) === String(wodId));
    if (!wod) return;
    if (wod.trainingTag) {
      this.checkinForm.get('trainingTag')!.setValue(wod.trainingTag, { emitEvent: false });
    }
    if (wod.description) {
      this.checkinForm.get('description')!.setValue(wod.description);
    }
  }

  /** Con WOD vinculado que trae tipo, el tag queda fijado al del WOD. */
  private syncTrainingTagLock(wodId: string): void {
    const trainingTag = this.checkinForm.get('trainingTag')!;
    const wod = wodId
      ? this.boxWods().find(w => String(w.id) === String(wodId))
      : undefined;
    if (wodId && wod?.trainingTag) {
      trainingTag.disable({ emitEvent: false });
    } else {
      trainingTag.enable({ emitEvent: false });
    }
  }

  onSubmit(): void {
    if (this.submitting()) return;
    const trainingTag = this.checkinForm.getRawValue().trainingTag as string;
    if (this.checkinForm.invalid || !trainingTag) {
      this.checkinForm.markAllAsTouched();
      return;
    }
    this.backendMessage.set(null);
    this.submitting.set(true);
    const payload = this.buildPayload();
    this.postService.createCheckin(payload).subscribe({
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

  private buildPayload(): NewCheckinRequest {
    const formValue = this.checkinForm.getRawValue() as {
      trainingTag: TrainingTag;
      description?: string;
      wodPostId?: string;
    };
    const payload: NewCheckinRequest = {
      trainingTag: formValue.trainingTag,
    };
    const description = formValue.description?.trim();
    if (description) payload.description = description;
    const wodPostId = formValue.wodPostId?.trim();
    if (wodPostId) payload.wodPostId = Number(wodPostId);
    return payload;
  }
}
