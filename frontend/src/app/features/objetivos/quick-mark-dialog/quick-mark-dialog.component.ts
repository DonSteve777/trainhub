import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Goal, GoalUnit } from '../objetivos.mock';
import { ObjetivosService } from '../objetivos.service';
import { formatValue, parseSeconds } from '../objetivos.utils';

@Component({
  selector: 'app-quick-mark-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule, MatSnackBarModule, FormsModule],
  templateUrl: './quick-mark-dialog.component.html',
  styleUrl: './quick-mark-dialog.component.scss',
})
export class QuickMarkDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<QuickMarkDialogComponent>);
  private readonly objetivosService = inject(ObjetivosService);
  private readonly snackBar = inject(MatSnackBar);

  readonly goals = signal<Goal[]>(this.objetivosService.activeGoalsForMe());
  readonly selectedGoalId = signal<number | null>(this.goals()[0]?.id ?? null);
  readonly valueInput = signal('');
  readonly note = signal('');
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

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

  close(): void {
    this.dialogRef.close();
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

    const mark = this.objetivosService.addMark({
      goalId: goal.id,
      value: parsed,
      note: this.note(),
    });

    this.submitting.set(false);

    if (!mark) {
      this.errorMessage.set('No se pudo registrar la marca.');
      return;
    }

    this.snackBar.open(
      `Marca registrada: ${formatValue(mark.value, goal.unit)} · ${goal.title}`,
      'Cerrar',
      { duration: 3500 },
    );
    this.dialogRef.close({ goalId: goal.id, mark });
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
