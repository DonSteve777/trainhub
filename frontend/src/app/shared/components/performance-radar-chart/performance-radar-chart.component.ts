import { Component, Input, OnChanges } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

export interface RadarSegment {
  label: string;
  allTimes: number[];
  athleteTime: number;
}

@Component({
  selector: 'app-performance-radar-chart',
  standalone: true,
  imports: [BaseChartDirective],
  templateUrl: './performance-radar-chart.component.html',
  styleUrl: './performance-radar-chart.component.scss',
})
export class PerformanceRadarChartComponent implements OnChanges {
  @Input({ required: true }) segments: RadarSegment[] = [];

  chartData: ChartData<'radar'> = { datasets: [] };
  chartOptions: ChartOptions<'radar'> = {};

  ngOnChanges(): void {
    if (this.segments.length > 0) {
      this.buildChart();
    }
  }

  private percentileFor(allTimes: number[], athleteTime: number): number {
    if (allTimes.length === 0) return 0;
    return Math.round((allTimes.filter((t) => t > athleteTime).length / allTimes.length) * 100);
  }

  private formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  private buildChart(): void {
    const values = this.segments.map((seg) => this.percentileFor(seg.allTimes, seg.athleteTime));
    const labels = this.segments.map((seg) => seg.label);

    this.chartData = {
      labels,
      datasets: [
        {
          data: values,
          borderColor: '#FFC107',
          backgroundColor: 'rgba(255, 193, 7, 0.25)',
          pointBackgroundColor: '#FFC107',
          pointBorderColor: '#FFC107',
          borderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: (items) => this.segments[items[0].dataIndex].label,
            label: (ctx) => {
              const seg = this.segments[ctx.dataIndex];
              const pct = Math.round((ctx.parsed as { r: number }).r);
              return [
                `  Time      ${this.formatTime(seg.athleteTime)}`,
                `  Cohort    Top ${pct}%`,
              ];
            },
          },
        },
      },
      scales: {
        r: {
          min: 0,
          max: 100,
          ticks: { stepSize: 25, display: false },
          grid: { color: 'rgba(255,255,255,0.1)' },
          angleLines: { color: 'rgba(255,255,255,0.15)' },
          pointLabels: {
            color: 'rgba(255,255,255,0.7)',
            font: { size: 11 },
          },
        },
      },
    };
  }
}
