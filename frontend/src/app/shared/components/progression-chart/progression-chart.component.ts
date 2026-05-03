import { Component, Input, OnChanges } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { TimeEntryDto } from '../../../core/services/user.service';

@Component({
  selector: 'app-progression-chart',
  standalone: true,
  imports: [BaseChartDirective],
  templateUrl: './progression-chart.component.html',
  styleUrl: './progression-chart.component.scss',
})
export class ProgressionChartComponent implements OnChanges {
  @Input({ required: true }) entries: TimeEntryDto[] = [];
  @Input({ required: true }) label = '';
  @Input({ required: true }) color = '#FFC107';

  chartData: ChartData<'line'> = { datasets: [] };
  chartOptions: ChartOptions<'line'> = {};

  ngOnChanges(): void {
    if (this.entries.length > 0) {
      this.buildChart();
    }
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

  private formatDate(isoString: string): string {
    const d = new Date(isoString);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
  }

  private buildChart(): void {
    const points = this.entries.map((e) => ({
      x: new Date(e.date).getTime(),
      y: e.time,
    }));

    this.chartData = {
      datasets: [
        {
          data: points,
          borderColor: this.color,
          backgroundColor: this.color + '28',
          fill: true,
          tension: 0.35,
          pointRadius: 3,
          pointBackgroundColor: this.color,
          borderWidth: 2,
        },
      ],
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      parsing: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: (items) => this.formatDate(new Date(items[0].parsed.x ?? 0).toISOString()),
            label: (ctx) => ` ${this.formatTime(ctx.parsed.y as number)}`,
          },
        },
      },
      scales: {
        x: {
          type: 'linear',
          ticks: {
            callback: (val) => this.formatDate(new Date(val as number).toISOString()),
            color: 'rgba(255,255,255,0.5)',
            maxTicksLimit: 5,
            font: { size: 10 },
          },
          grid: { color: 'rgba(255,255,255,0.07)' },
          border: { color: 'transparent' },
        },
        y: {
          ticks: {
            callback: (val) => this.formatTime(val as number),
            color: 'rgba(255,255,255,0.5)',
            maxTicksLimit: 5,
            font: { size: 10 },
          },
          grid: { color: 'rgba(255,255,255,0.07)' },
          border: { color: 'transparent' },
        },
      },
    };
  }
}
