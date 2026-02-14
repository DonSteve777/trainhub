# TrainHub Logo Component

Logo de TrainHub recreado completamente con HTML y CSS puro. Este componente es una réplica fiel del logotipo original, utilizando la paleta de colores oficial definida para el proyecto.

## 🎨 Características

- ✅ **100% HTML y CSS** - Sin dependencias de imágenes
- 🎯 **Paleta de colores oficial** - Usa las variables CSS de TrainHub
- 📱 **Responsive** - Múltiples tamaños predefinidos
- ✨ **Efectos interactivos** - Animaciones hover sutiles
- ♿ **Accesible** - Cumple con estándares de contraste
- 🔧 **Configurable** - Props para personalizar tamaño y estilo

## 🚀 Uso Básico

### En Angular

```typescript
// Importar en tu componente
import { TrainhubLogoComponent } from './shared/components/trainhub-logo/trainhub-logo.component';

@Component({
  // ...
  imports: [TrainhubLogoComponent]
})
```

```html
<!-- Logo con configuración por defecto -->
<app-trainhub-logo></app-trainhub-logo>
```

### Tamaños disponibles

```html
<!-- Pequeño (100px) -->
<app-trainhub-logo size="small"></app-trainhub-logo>

<!-- Mediano (150px) - Por defecto -->
<app-trainhub-logo size="medium"></app-trainhub-logo>

<!-- Grande (250px) -->
<app-trainhub-logo size="large"></app-trainhub-logo>
```

### Solo icono (sin texto)

```html
<app-trainhub-logo [iconOnly]="true"></app-trainhub-logo>

<!-- Combinado: icono pequeño sin texto -->
<app-trainhub-logo size="small" [iconOnly]="true"></app-trainhub-logo>
```

## 📦 Estructura del Logo

El logo está compuesto por varios elementos SVG y CSS:

1. **Anillo exterior azul** - 4 segmentos con animación de rotación en hover
2. **Anillo naranja** - Efecto de resplandor con las sombras oficiales
3. **Círculo interior** - Fondo degradado oscuro
4. **Mancuernas** - Elementos grises con efecto 3D
5. **Letra "M"** - En naranja (#FF5722)
6. **Flecha de progreso** - En azul (#29B6F6)
7. **Texto "TRAINHUB"** - TRAIN en azul, HUB en naranja
8. **Subtítulo** - "HYROX STATS & COMMUNITY"

## 🎨 Colores Utilizados

```scss
// Colores principales del logo
--trainhub-accent: #FF5722       // Naranja (M, HUB, anillo medio)
--trainhub-accent-light: #FF8A65 // Naranja claro (degradados)
--trainhub-info: #29B6F6         // Azul (TRAIN, segmentos, flecha)
--trainhub-info-light: #4FC3F7   // Azul claro (degradados)

// Elementos secundarios
#1e2a3a → #0f1419  // Degradado del círculo interior
#6b6b6b → #4a4a4a  // Degradado de mancuernas (gris)
```

## ✨ Efectos Interactivos

Al pasar el cursor sobre el logo:

- 🔄 **Anillo exterior** - Rotación continua suave
- 💡 **Anillo naranja** - Resplandor intensificado
- ✨ **Letra M y flecha** - Efecto de brillo

## 📱 Responsive

El componente se adapta a diferentes tamaños:

| Tamaño | Icono | Texto Principal | Subtítulo |
|--------|-------|-----------------|-----------|
| Small  | 100px | 2rem            | 0.625rem  |
| Medium | 150px | 2.5rem          | 0.75rem   |
| Default| 200px | 3.5rem          | 0.875rem  |
| Large  | 250px | 4.5rem          | 1rem      |

## 🖼️ Vista Previa

Para ver todas las variantes del logo, abre el archivo de demo:

```
frontend/trainhub-logo-demo.html
```

Este archivo HTML standalone muestra:
- Logo completo con todos los tamaños
- Variantes solo icono
- Efectos hover en acción

## 🎯 Casos de Uso

### Navbar

```html
<!-- Logo pequeño en la barra de navegación -->
<nav class="navbar">
  <app-trainhub-logo size="small" [iconOnly]="true"></app-trainhub-logo>
</nav>
```

### Landing Page

```html
<!-- Logo grande en la página de bienvenida -->
<header class="hero">
  <app-trainhub-logo size="large"></app-trainhub-logo>
</header>
```

### Loading Screen

```html
<!-- Logo mediano con animación para pantalla de carga -->
<div class="loading-screen">
  <app-trainhub-logo size="medium"></app-trainhub-logo>
  <p>Cargando...</p>
</div>
```

### Favicon / App Icon

```html
<!-- Icono pequeño para usar como favicon -->
<app-trainhub-logo size="small" [iconOnly]="true"></app-trainhub-logo>
```

## 🔧 Personalización Avanzada

Si necesitas personalizar más allá de las props disponibles, puedes:

### Cambiar colores individuales

```scss
// En tu componente .scss
::ng-deep .trainhub-logo {
  --trainhub-accent: #your-color;
  --trainhub-info: #your-color;
}
```

### Añadir clases personalizadas

```html
<div class="my-custom-wrapper">
  <app-trainhub-logo></app-trainhub-logo>
</div>
```

```scss
.my-custom-wrapper .trainhub-logo {
  transform: scale(1.2);
  filter: brightness(1.1);
}
```

## 📋 Props del Componente

| Prop | Tipo | Default | Descripción |
|------|------|---------|-------------|
| `size` | `'small' \| 'medium' \| 'large'` | `'medium'` | Tamaño del logo |
| `iconOnly` | `boolean` | `false` | Mostrar solo el icono sin texto |

## 🎨 Variables CSS Disponibles

El componente utiliza las siguientes variables CSS que puedes sobrescribir:

```scss
--trainhub-accent          // Color naranja principal
--trainhub-accent-light    // Naranja claro para hover
--trainhub-accent-dark     // Naranja oscuro para active
--trainhub-info            // Azul para elementos informativos
--trainhub-info-light      // Azul claro
--mat-sys-outline          // Color del subtítulo
--mat-sys-surface          // Fondo del componente
--mat-sys-on-surface       // Color del texto
```

## 🐛 Troubleshooting

### El logo no se ve correctamente

1. Verifica que las variables CSS de TrainHub estén cargadas
2. Asegúrate de que `styles/_theme.scss` está importado en `styles.scss`
3. Comprueba que el componente es standalone y está importado correctamente

### Las animaciones no funcionan

1. Verifica que no hay `prefers-reduced-motion` activo
2. Comprueba que los navegadores soportan las animaciones CSS

### Los colores no coinciden

1. Verifica que estás usando las últimas variables CSS de `_theme.scss`
2. Asegúrate de que no hay estilos globales sobrescribiendo los colores

## 📚 Referencias

- [Paleta de Colores TrainHub](../../../../design/ColorPalette.md)
- [Theme Scss](./../../../styles/_theme.scss)
- [Logotipo Original](../../../../design/UI/scali%20images/logotipo.jpg)

## 🤝 Contribuir

Si encuentras algún problema o tienes sugerencias:

1. Verifica que el logo coincide con el diseño original
2. Asegúrate de usar los colores de la paleta oficial
3. Mantén la accesibilidad (contraste WCAG AA mínimo)
4. Documenta cualquier cambio significativo

---

**Creado con 💪 para TrainHub**
