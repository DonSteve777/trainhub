import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  PostService,
  type BoxPostType,
  type NewBoxPostRequest,
  type TrainingTag,
} from '../../core/services/post.service';
import { UserService } from '../../core/services/user.service';

interface BoxPostTypeOption {
  value: BoxPostType;
  label: string;
  copy: string;
}

interface TrainingTagOption {
  value: TrainingTag;
  label: string;
}

@Component({
  selector: 'app-create-box-post',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './create-box-post.component.html',
  styleUrl: './create-box-post.component.scss',
})
export class CreateBoxPostComponent implements OnInit {
  postForm!: FormGroup;

  private readonly fb = inject(FormBuilder);
  private readonly postService = inject(PostService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  readonly backendMessage = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly checkingAccess = signal(true);

  readonly boxPostTypeOptions: readonly BoxPostTypeOption[] = [
    { value: 'BOX_WOD', label: 'WOD del día', copy: 'Publica el entrenamiento de hoy para el box.' },
    {
      value: 'BOX_CHALLENGE',
      label: 'Reto',
      copy: 'Los miembros podrán apuntarse hasta la fecha límite.',
    },
    { value: 'BOX_ANNOUNCEMENT', label: 'Anuncio', copy: 'Comunica algo a todo el box.' },
  ];

  readonly trainingTagOptions: readonly TrainingTagOption[] = [
    { value: 'HYROX', label: 'HYROX' },
    { value: 'FUERZA', label: 'Fuerza' },
    { value: 'CARRERA', label: 'Carrera' },
    { value: 'CLASE', label: 'Clase' },
    { value: 'DESCANSO_ACTIVO', label: 'Descanso activo' },
    { value: 'OTRO', label: 'Otro' },
  ];

  readonly selectedPostType = signal<BoxPostType>('BOX_WOD');
  readonly isChallenge = computed(() => this.selectedPostType() === 'BOX_CHALLENGE');

  ngOnInit(): void {
    this.postForm = this.fb.group({
      postType: ['BOX_WOD' as BoxPostType, Validators.required],
      title: ['', [Validators.required, Validators.maxLength(150)]],
      description: ['', Validators.required],
      trainingTag: [''],
      challengeDeadline: [''],
    });

    this.userService.getProfile().subscribe({
      next: profile => {
        if (profile.role !== 'BOX_ADMIN') {
          void this.router.navigate(['/feed'], { replaceUrl: true });
          return;
        }
        this.checkingAccess.set(false);
      },
      error: () => void this.router.navigate(['/feed'], { replaceUrl: true }),
    });
  }

  selectPostType(type: BoxPostType): void {
    if (this.submitting()) return;
    this.postForm.get('postType')!.setValue(type);
    this.selectedPostType.set(type);
    this.updateDeadlineValidator();
  }

  private updateDeadlineValidator(): void {
    const control = this.postForm.get('challengeDeadline')!;
    if (this.isChallenge()) {
      control.setValidators([Validators.required]);
    } else {
      control.clearValidators();
      control.setValue('');
    }
    control.updateValueAndValidity();
  }

  private buildPayload(): NewBoxPostRequest {
    const formValue = this.postForm.getRawValue() as {
      postType: BoxPostType;
      title: string;
      description: string;
      trainingTag: TrainingTag | '';
      challengeDeadline: string;
    };

    const payload: NewBoxPostRequest = {
      postType: formValue.postType,
      title: formValue.title.trim(),
      description: formValue.description.trim(),
    };

    if (formValue.trainingTag) payload.trainingTag = formValue.trainingTag;
    if (formValue.postType === 'BOX_CHALLENGE' && formValue.challengeDeadline) {
      payload.challengeDeadline = new Date(formValue.challengeDeadline).toISOString();
    }

    return payload;
  }

  onSubmit(): void {
    if (this.postForm.invalid || this.submitting()) {
      this.postForm.markAllAsTouched();
      return;
    }
    this.backendMessage.set(null);
    this.submitting.set(true);

    this.postService.createBoxPost(this.buildPayload()).subscribe({
      next: () => void this.router.navigate(['/feed'], { replaceUrl: true }),
      error: err => {
        this.submitting.set(false);
        const errBody = err.error;
        this.backendMessage.set(
          errBody?.message ?? errBody?.error ?? 'No se pudo publicar el contenido. Inténtalo de nuevo.'
        );
      },
    });
  }
}
