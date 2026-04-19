import { Component, Input, OnChanges } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-race-distribution-chart',
  standalone: true,
  imports: [BaseChartDirective],
  templateUrl: './race-distribution-chart.component.html',
  styleUrl: './race-distribution-chart.component.scss',
})
export class RaceDistributionChartComponent implements OnChanges {
  @Input({ required: true }) allTimes: number[] = [];
  @Input({ required: true }) athleteTime = 0;
  @Input({ required: true }) label = '';
  @Input({ required: true }) color = '#FFC107';

  chartData: ChartData<'line'> = { datasets: [] };
  chartOptions: ChartOptions<'line'> = {};
  athletePercentile = 0;

  ngOnChanges(): void {
    if (this.allTimes.length > 0) {
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

  private buildChart(): void {
    const sorted = [...this.allTimes].sort((a, b) => a - b);
    const total = sorted.length;

    const cdfPoints = sorted.map((t, i) => ({ x: t, y: ((i + 1) / total) * 100 }));

    this.athletePercentile = Math.round(
      (sorted.filter((t) => t <= this.athleteTime).length / total) * 100,
    );

    this.chartData = {
      datasets: [
        {
          data: cdfPoints,
          borderColor: this.color,
          backgroundColor: this.color + '28',
          fill: true,
          tension: 0.35,
          pointRadius: 0,
          borderWidth: 2,
        },
        {
          data: [
            { x: this.athleteTime, y: 0 },
            { x: this.athleteTime, y: 100 },
          ],
          borderColor: 'rgba(255,255,255,0.85)',
          backgroundColor: 'transparent',
          fill: false,
          tension: 0,
          pointRadius: 0,
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
          filter: (item) => item.datasetIndex === 0,
          callbacks: {
            title: (items) => this.formatTime(items[0].parsed.x as number),
            label: (ctx) => ` ${Math.round(ctx.parsed.y as number)}% de participantes`,
          },
        },
      },
      scales: {
        x: {
          type: 'linear',
          ticks: {
            callback: (val) => this.formatTime(val as number),
            color: 'rgba(255,255,255,0.5)',
            maxTicksLimit: 5,
            font: { size: 10 },
          },
          grid: { color: 'rgba(255,255,255,0.07)' },
          border: { color: 'transparent' },
        },
        y: {
          min: 0,
          max: 100,
          ticks: {
            callback: (val) => `${val}%`,
            color: 'rgba(255,255,255,0.5)',
            stepSize: 25,
            font: { size: 10 },
          },
          grid: { color: 'rgba(255,255,255,0.07)' },
          border: { color: 'transparent' },
        },
      },
    };
  }
}
