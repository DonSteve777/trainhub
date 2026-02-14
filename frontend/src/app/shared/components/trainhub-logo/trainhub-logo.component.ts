import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * TrainHub Logo Component
 * 
 * Logo de TrainHub recreado con HTML y CSS puro.
 * Inspirado en el diseño original y usando la paleta de colores definida.
 * 
 * @example
 * <app-trainhub-logo></app-trainhub-logo>
 * 
 * @example Con tamaño personalizado
 * <app-trainhub-logo size="small"></app-trainhub-logo>
 * 
 * @example Solo icono sin texto
 * <app-trainhub-logo [iconOnly]="true"></app-trainhub-logo>
 */
@Component({
  selector: 'app-trainhub-logo',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trainhub-logo.component.html',
  styleUrl: './trainhub-logo.component.scss'
})
export class TrainhubLogoComponent {
  /**
   * Tamaño del logo
   * - 'small': 100px
   * - 'medium': 150px
   * - 'default': 200px
   * - 'large': 250px
   */
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  
  /**
   * Mostrar solo el icono sin el texto
   */
  @Input() iconOnly: boolean = false;
  
  /**
   * Obtiene las clases CSS para el logo basadas en los inputs
   */
  get logoClasses(): string {
    const classes: string[] = [];
    
    if (this.size !== 'medium') {
      classes.push(`size-${this.size}`);
    }
    
    if (this.iconOnly) {
      classes.push('icon-only');
    }
    
    return classes.join(' ');
  }
}
