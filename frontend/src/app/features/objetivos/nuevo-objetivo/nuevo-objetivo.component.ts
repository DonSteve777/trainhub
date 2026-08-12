import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { GoalDirection, GoalUnit } from '../objetivos.models';
import { ObjetivosService } from '../objetivos.service';
import { formatValue, parseSeconds } from '../objetivos.utils';

interface UnitOption {
  value: GoalUnit;
  label: string;
  metricLabel: string;
  direction: GoalDirection;
  directionHint: string;
}

@Component({
  selector: 'app-nuevo-objetivo',
  standalone: true,
  imports: [MatSnackBarModule, FormsModule, RouterLink],
  templateUrl: './nuevo-objetivo.component.html',
  styleUrl: './nuevo-objetivo.component.scss',
})
export class NuevoObjetivoComponent {
  private readonly router = inject(Router);
  private readonly objetivosService = inject(ObjetivosService);
  private readonly snackBar = inject(MatSnackBar);

  readonly unitOptions: UnitOption[] = [
    {
      value: 'time',
      label: 'Tiempo',
      metricLabel: 'Tiempo total',
      direction: 'lower',
      directionHint: 'Menor es mejor',
    },
    {
      value: 'kg',
      label: 'Kilos',
      metricLabel: 'Carga máxima',
      direction: 'higher',
      directionHint: 'Mayor es mejor',
    },
    {
      value: 'reps',
      label: 'Reps',
      metricLabel: 'Repeticiones',
      direction: 'higher',
      directionHint: 'Mayor es mejor',
    },
    {
      value: 'meters',
      label: 'Metros',
      metricLabel: 'Distancia',
      direction: 'higher',
      directionHint: 'Mayor es mejor',
    },
  ];

  readonly title = signal('');
  readonly description = signal('');
  readonly metricLabel = signal(this.unitOptions[0].metricLabel);
  readonly unit = signal<GoalUnit>('time');
  readonly targetInput = signal('');
  readonly weeks = signal(12);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly metricTouched = signal(false);

  readonly selectedUnit = computed(
    () => this.unitOptions.find(o => o.value === this.unit()) ?? this.unitOptions[0],
  );

  readonly valueHint = computed(() => this.hintForUnit(this.unit()));
  readonly valuePlaceholder = computed(() => this.placeholderForUnit(this.unit()));

  readonly canSubmit = computed(() => {
    return (
      this.title().trim().length > 0 &&
      this.metricLabel().trim().length > 0 &&
      this.targetInput().trim().length > 0 &&
      this.weeks() >= 1 &&
      !this.submitting()
    );
  });

  onUnitChange(unit: GoalUnit): void {
    this.unit.set(unit);
    this.errorMessage.set(null);
    const opt = this.unitOptions.find(o => o.value === unit);
    if (opt && !this.metricTouched()) {
      this.metricLabel.set(opt.metricLabel);
    }
  }

  onMetricLabelChange(value: string): void {
    this.metricTouched.set(true);
    this.metricLabel.set(value);
  }

  submit(): void {
    if (!this.canSubmit()) return;

    const unit = this.unit();
    const parsed = this.parseValue(this.targetInput().trim(), unit);
    if (parsed === null) {
      this.errorMessage.set(this.invalidValueMessage(unit));
      return;
    }

    const weeks = Math.round(Number(this.weeks()));
    if (!Number.isFinite(weeks) || weeks < 1) {
      this.errorMessage.set('Indica un plazo de al menos 1 semana.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    const opt = this.selectedUnit();
    const goal = this.objetivosService.createGoal({
      title: this.title(),
      description: this.description(),
      metricLabel: this.metricLabel(),
      targetValue: parsed,
      unit,
      direction: opt.direction,
      weeks,
    });

    this.submitting.set(false);

    this.snackBar.open(
      `Objetivo creado: ${goal.title} · meta ${formatValue(goal.targetValue, goal.unit)}`,
      'Cerrar',
      { duration: 3500 },
    );
    void this.router.navigate(['/objetivos']);
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
        return '1:30:00';
      case 'kg':
        return '100';
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
      return 'Introduce un tiempo válido (p. ej. 1:30:00 o 8:45).';
    }
    return 'Introduce un valor numérico válido.';
  }
}
