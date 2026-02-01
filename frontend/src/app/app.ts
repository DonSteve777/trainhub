import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderToolbarComponent } from './core/components/header-toolbar/header-toolbar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderToolbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('frontend');
}
