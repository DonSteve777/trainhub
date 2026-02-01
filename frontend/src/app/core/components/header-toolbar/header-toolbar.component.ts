import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-header-toolbar',
  imports: [MatToolbarModule],
  templateUrl: './header-toolbar.component.html',
  styleUrl: './header-toolbar.component.scss'
})
export class HeaderToolbarComponent {}
