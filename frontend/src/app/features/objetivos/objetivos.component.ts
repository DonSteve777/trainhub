import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { Goal } from './objetivos.models';
import { ObjetivosService } from './objetivos.service';
import {
  ParticipantProgress,
  VsMeDelta,
  deadlineLabel,
  formatDate,
  formatValue,
  friendsProgress,
  meParticipant,
  participantProgress,
  vsMeDelta,
} from './objetivos.utils';

type GoalFilter = 'ACTIVE' | 'ACHIEVED' | 'ALL';

@Component({
  selector: 'app-objetivos',
  standalone: true,
  imports: [MatIconModule, RouterLink],
  templateUrl: './objetivos.component.html',
  styleUrl: './objetivos.component.scss',
})
export class ObjetivosComponent implements OnInit {
  private readonly objetivosService = inject(ObjetivosService);

  readonly goals = this.objetivosService.goals;
  readonly loading = this.objetivosService.loading;
  readonly error = this.objetivosService.error;
  readonly filter = signal<GoalFilter>('ACTIVE');
  readonly selectedId = signal<number | null>(null);

  readonly filteredGoals = computed(() => {
    const f = this.filter();
    const list = this.goals();
    if (f === 'ALL') return list;
    return list.filter(g => g.status === f);
  });

  readonly selectedGoal = computed(() => {
    const id = this.selectedId();
    return this.goals().find(g => g.id === id) ?? null;
  });

  readonly myProgress = computed((): ParticipantProgress | null => {
    const goal = this.selectedGoal();
    if (!goal) return null;
    const me = meParticipant(goal);
    if (!me) return null;
    return participantProgress(goal, me);
  });

  readonly friends = computed((): ParticipantProgress[] => {
    const goal = this.selectedGoal();
    if (!goal) return [];
    return friendsProgress(goal);
  });

  readonly myMarksSorted = computed(() => {
    const goal = this.selectedGoal();
    const me = goal ? meParticipant(goal) : undefined;
    if (!me) return [];
    return [...me.marks].sort(
      (a, b) => new Date(b.recordedAt).getTime() - new Date(a.recordedAt).getTime()
    );
  });

  ngOnInit(): void {
    // #region agent log
    fetch('http://127.0.0.1:7276/ingest/5d8934ba-b284-461b-918f-bda8d35250fc',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'408495'},body:JSON.stringify({sessionId:'408495',runId:'post-fix',location:'objetivos.component.ts:ngOnInit',message:'objetivos init before loadGoals',data:{goalsCountBeforeLoad:this.goals().length,goalIdsBeforeLoad:this.goals().map(g=>g.id),filter:this.filter()},timestamp:Date.now(),hypothesisId:'A,D'})}).catch(()=>{});
    // #endregion
    this.objetivosService.loadGoals().subscribe({
      next: () => {
        // #region agent log
        fetch('http://127.0.0.1:7276/ingest/5d8934ba-b284-461b-918f-bda8d35250fc',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'408495'},body:JSON.stringify({sessionId:'408495',runId:'post-fix',location:'objetivos.component.ts:loadGoals.next',message:'objetivos after loadGoals',data:{goalsCount:this.goals().length,goalIds:this.goals().map(g=>g.id),filteredCount:this.filteredGoals().length,filteredIds:this.filteredGoals().map(g=>g.id),filter:this.filter()},timestamp:Date.now(),hypothesisId:'A,C,D'})}).catch(()=>{});
        // #endregion
        this.ensureSelection();
      },
    });
  }

  selectGoal(id: number): void {
    this.selectedId.set(id);
  }

  setFilter(f: GoalFilter): void {
    this.filter.set(f);
    const list = this.filteredGoals();
    const current = this.selectedId();
    if (!list.some(g => g.id === current)) {
      this.selectedId.set(list[0]?.id ?? null);
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Activo';
      case 'ACHIEVED':
        return 'Logrado';
      case 'EXPIRED':
        return 'Vencido';
      default:
        return status;
    }
  }

  deadlineText(goal: Goal): string {
    return deadlineLabel(goal.deadline, goal.status);
  }

  formatVal(value: number | null, goal: Goal): string {
    if (value === null) return '—';
    return formatValue(value, goal.unit);
  }

  formatMarkDate(iso: string): string {
    return formatDate(iso);
  }

  friendVsMe(fp: ParticipantProgress, goal: Goal): VsMeDelta | null {
    return vsMeDelta(this.myProgress()?.bestValue ?? null, fp.bestValue, goal.direction, goal.unit);
  }

  avatarUrl(url: string | null | undefined, userId: number): string {
    return url?.trim() || `https://i.pravatar.cc/48?u=${userId}`;
  }

  private ensureSelection(): void {
    const current = this.selectedId();
    const list = this.filteredGoals();
    if (current != null && list.some(g => g.id === current)) return;
    const all = this.goals();
    const preferred =
      all.find(g => g.status === 'ACTIVE')?.id ?? all[0]?.id ?? null;
    const inFilter = list[0]?.id ?? preferred;
    this.selectedId.set(inFilter);
  }
}
