import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Goal, GoalUnit } from '../objetivos.models';
import { ObjetivosService } from '../objetivos.service';
import { formatValue, parseSeconds } from '../objetivos.utils';

@Component({
  selector: 'app-nueva-marca',
  standalone: true,
  imports: [MatSnackBarModule, FormsModule, RouterLink],
  templateUrl: './nueva-marca.component.html',
  styleUrl: './nueva-marca.component.scss',
})
export class NuevaMarcaComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly objetivosService = inject(ObjetivosService);
  private readonly snackBar = inject(MatSnackBar);

  readonly goals = signal<Goal[]>([]);
  readonly selectedGoalId = signal<number | null>(null);
  readonly valueInput = signal('');
  readonly note = signal('');
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly loading = this.objetivosService.loading;

  readonly selectedGoal = computed(() => {
    const id = this.selectedGoalId();
    return this.goals().find(g => g.id === id) ?? null;
  });

  readonly valueHint = computed(() => {
    const goal = this.selectedGoal();
    if (!goal) return '';
    return this.hintForUnit(goal.unit);
  });

  readonly valuePlaceholder = computed(() => {
    const goal = this.selectedGoal();
    if (!goal) return '';
    return this.placeholderForUnit(goal.unit);
  });

  ngOnInit(): void {
    const refresh = () => {
      const active = this.objetivosService.activeGoalsForMe();
      this.goals.set(active);
      if (this.selectedGoalId() == null || !active.some(g => g.id === this.selectedGoalId())) {
        this.selectedGoalId.set(active[0]?.id ?? null);
      }
    };

    if (this.objetivosService.goals().length === 0) {
      this.objetivosService.loadGoals().subscribe({ next: refresh });
    } else {
      refresh();
    }
  }

  formatGoalValue(goal: Goal): string {
    return formatValue(goal.targetValue, goal.unit);
  }

  selectGoal(id: number): void {
    this.selectedGoalId.set(id);
    this.valueInput.set('');
    this.errorMessage.set(null);
  }

  onGoalChange(id: number): void {
    if (!Number.isFinite(id)) return;
    this.selectGoal(id);
  }

  submit(): void {
    const goal = this.selectedGoal();
    if (!goal || this.submitting()) return;

    const parsed = this.parseValue(this.valueInput().trim(), goal.unit);
    if (parsed === null) {
      this.errorMessage.set(this.invalidValueMessage(goal.unit));
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const note = this.note().trim();
    this.objetivosService
      .addMark({
        goalId: goal.id,
        value: parsed,
        note: note || undefined,
      })
      .subscribe({
        next: mark => {
          this.submitting.set(false);
          this.snackBar.open(
            `Marca registrada: ${formatValue(mark.value, goal.unit)} · ${goal.title}`,
            'Cerrar',
            { duration: 3500 },
          );
          void this.router.navigate(['/objetivos']);
        },
        error: err => {
          this.submitting.set(false);
          const errBody = err.error;
          this.errorMessage.set(
            errBody?.message ?? 'No se pudo registrar la marca.',
          );
        },
      });
  }

  private parseValue(raw: string, unit: GoalUnit): number | null {
    if (!raw) return null;
    if (unit === 'time') return parseSeconds(raw);
    const n = Number(raw.replace(',', '.'));
    if (!Number.isFinite(n) || n < 0) return null;
    return n;
  }

  private hintForUnit(unit: GoalUnit): string {
    switch (unit) {
      case 'time':
        return 'Formato h:mm:ss o m:ss (se guarda en segundos)';
      case 'kg':
        return 'Kilogramos (puedes usar decimales)';
      case 'meters':
        return 'Metros';
      case 'reps':
        return 'Repeticiones';
      default:
        return '';
    }
  }

  private placeholderForUnit(unit: GoalUnit): string {
    switch (unit) {
      case 'time':
        return '1:32:00';
      case 'kg':
        return '92.5';
      case 'meters':
        return '1000';
      case 'reps':
        return '100';
      default:
        return '';
    }
  }

  private invalidValueMessage(unit: GoalUnit): string {
    if (unit === 'time') {
      return 'Introduce un tiempo válido (p. ej. 1:32:00 o 8:45).';
    }
    return 'Introduce un valor numérico válido.';
  }
}
