import { Component, Input, OnChanges } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartDataset, ChartOptions } from 'chart.js';
import { TimeEntryDto, FriendTimeEntryDto } from '../../../core/services/user.service';

export interface FriendSeries {
  username: string;
  entries: FriendTimeEntryDto[];
}

/** Paleta para los marcadores de amigos (contrasta con los colores de las 3 gráficas) */
const FRIEND_COLORS = [
  '#4CAF50',
  '#FF9800',
  '#00BCD4',
  '#E91E63',
  '#9C27B0',
  '#00E676',
  '#FF6D00',
  '#18FFFF',
];

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
  @Input() friendsData: FriendSeries[] = [];

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

    const mainDataset: ChartDataset<'line'> = {
      data: points,
      borderColor: this.color,
      backgroundColor: this.color + '28',
      fill: true,
      tension: 0.35,
      pointRadius: 3,
      pointBackgroundColor: this.color,
      borderWidth: 2,
      label: this.label,
    };

    const friendDatasets: ChartDataset<'line'>[] = this.friendsData.map((friend, i) => ({
      data: friend.entries.map((e) => ({
        x: new Date(e.date).getTime(),
        y: e.time,
      })),
      label: friend.username,
      borderColor: 'transparent',
      backgroundColor: 'transparent',
      showLine: false,
      fill: false,
      tension: 0,
      pointRadius: 6,
      pointHoverRadius: 8,
      pointBackgroundColor: FRIEND_COLORS[i % FRIEND_COLORS.length],
      pointBorderColor: 'rgba(255,255,255,0.85)',
      pointBorderWidth: 1.5,
    }));

    this.chartData = {
      datasets: [mainDataset, ...friendDatasets],
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      parsing: false,
      interaction: {
        mode: 'nearest',
        intersect: true,
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: (items) =>
              this.formatDate(new Date(items[0].parsed.x ?? 0).toISOString()),
            label: (ctx) => {
              const time = this.formatTime(ctx.parsed.y as number);
              if (ctx.datasetIndex === 0) {
                return ` ${time}`;
              }
              return ` ${ctx.dataset.label}: ${time}`;
            },
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
