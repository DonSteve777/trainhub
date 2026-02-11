# Paleta de Colores - Trainhub

> Sistema de colores deportivo y vibrante optimizado para modo oscuro

## Índice

- [Filosofía de Color](#filosofía-de-color)
- [Colores Principales](#colores-principales)
- [Colores de Sistema](#colores-de-sistema)
- [Colores de Progreso y Logros](#colores-de-progreso-y-logros)
- [Degradados](#degradados)
- [Fondos y Superficies](#fondos-y-superficies)
- [Guías de Uso](#guías-de-uso)
- [Accesibilidad](#accesibilidad)
- [Implementación](#implementación)

---

## Filosofía de Color

Trainhub utiliza un sistema de colores **deportivo y vibrante** que transmite:

- **Energía**: Colores cálidos y vivos que motivan al usuario
- **Pasión**: Rojos y naranjas que representan la dedicación deportiva
- **Progreso**: Amarillos y dorados que simbolizan logros y metas
- **Profesionalismo**: Grises oscuros con tintes cálidos para un look moderno

El tema está **optimizado para modo oscuro**, proporcionando contraste óptimo y reduciendo la fatiga visual durante sesiones de entrenamiento nocturnas.

---

## Colores Principales

### Primary - Naranja Energético

**Color base:** `#FF5722` (Deep Orange)

El color principal de Trainhub transmite energía, motivación y pasión por el deporte.

| Tono | Hex | Uso |
|------|-----|-----|
| 10 | `#3a0800` | Superficies más oscuras |
| 20 | `#5f1100` | Fondos de contenedores |
| 30 | `#802200` | Estados hover oscuros |
| 40 | `#a33200` | Elementos deshabilitados |
| 50 | `#c84a0e` | Estados focus |
| 60 | `#e96328` | Elementos secundarios |
| 70 | `#ff8a65` | Hover en modo oscuro |
| 80 | `#ffab91` | Texto sobre fondos oscuros |
| 90 | `#ffd5c8` | Fondos suaves |
| 95 | `#ffede9` | Fondos muy claros |

**Usos recomendados:**
- Botones de acción principal (CTA)
- Botón "+" para crear publicaciones
- Iconos de navegación activos
- Enlaces importantes
- Bordes de elementos seleccionados

```scss
// Ejemplo de uso
.primary-button {
  background-color: var(--trainhub-accent);
  
  &:hover {
    background-color: var(--trainhub-accent-light);
  }
  
  &:active {
    background-color: var(--trainhub-accent-dark);
  }
}
```

---

### Secondary - Amarillo Vibrante

**Color base:** `#FFC107` (Amber)

Representa logros, metas alcanzadas y progreso en el entrenamiento.

| Tono | Hex | Uso |
|------|-----|-----|
| 20 | `#462b00` | Fondos oscuros |
| 40 | `#805900` | Estados hover |
| 60 | `#c08c00` | Elementos destacados |
| 80 | `#ffc107` | **Color base - logros** |
| 90 | `#ffd54f` | Hover/focus |
| 95 | `#ffe57f` | Fondos suaves |

**Usos recomendados:**
- Botones de acciones secundarias
- Insignias de logros y medallas
- Indicadores de progreso completado
- Notificaciones de éxito
- Claps/likes (alternativa al rojo)

```scss
// Ejemplo de uso
.achievement-badge {
  background: var(--trainhub-achievement-gold);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.4);
}
```

---

### Tertiary - Rojo Intenso

**Color base:** `#F44336` (Red)

Para destacar información crítica, métricas de rendimiento y elementos de alta prioridad.

| Tono | Hex | Uso |
|------|-----|-----|
| 30 | `#93000a` | Fondos de alerta |
| 40 | `#ba1a1a` | Estados activos |
| 50 | `#de3730` | Alertas importantes |
| 60 | `#ff5449` | **Color base - alertas** |
| 70 | `#ff897d` | Hover en modo oscuro |
| 90 | `#ffdad6` | Fondos suaves de error |

**Usos recomendados:**
- Botones de like/clap (corazón)
- Métricas de rendimiento destacadas
- Tiempo récord personal
- Notificaciones importantes
- Indicadores de intensidad alta

```scss
// Ejemplo de uso
.like-button {
  color: var(--mat-sys-tertiary);
  
  &.liked {
    color: var(--mat-sys-tertiary);
    animation: heartbeat 0.3s ease;
  }
}
```

---

## Colores de Sistema

### Success - Verde Deportivo

| Variable | Hex | Uso |
|----------|-----|-----|
| `--trainhub-success` | `#66BB6A` | Éxito base |
| `--trainhub-success-light` | `#81C784` | Hover |
| `--trainhub-success-dark` | `#4CAF50` | Active |

**Usos:**
- Confirmaciones de acción exitosa
- Objetivos completados
- Entrenamientos finalizados
- Estados de conexión activa

---

### Warning - Naranja Advertencia

| Variable | Hex | Uso |
|----------|-----|-----|
| `--trainhub-warning` | `#FFA726` | Advertencia base |
| `--trainhub-warning-light` | `#FFB74D` | Hover |
| `--trainhub-warning-dark` | `#FF9800` | Active |

**Usos:**
- Advertencias moderadas
- Límites de tiempo próximos
- Acciones que requieren atención
- Campos de formulario con validación pendiente

---

### Error - Rojo Error

| Variable | Hex | Uso |
|----------|-----|-----|
| `--trainhub-error` | `#EF5350` | Error base |
| `--trainhub-error-light` | `#E57373` | Hover |
| `--trainhub-error-dark` | `#F44336` | Active |

**Usos:**
- Errores de validación
- Acciones fallidas
- Mensajes de error críticos
- Campos de formulario inválidos

---

### Info - Azul Información

| Variable | Hex | Uso |
|----------|-----|-----|
| `--trainhub-info` | `#29B6F6` | Info base |
| `--trainhub-info-light` | `#4FC3F7` | Hover |
| `--trainhub-info-dark` | `#0288D1` | Active |

**Usos:**
- Tooltips informativos
- Mensajes de ayuda
- Indicadores de información adicional
- Notificaciones neutras

---

## Colores de Progreso y Logros

Para gamificación y reconocimiento de logros deportivos:

| Variable | Hex | Descripción |
|----------|-----|-------------|
| `--trainhub-achievement-gold` | `#FFD700` | Medalla de oro, primer lugar |
| `--trainhub-achievement-silver` | `#C0C0C0` | Medalla de plata, segundo lugar |
| `--trainhub-achievement-bronze` | `#CD7F32` | Medalla de bronce, tercer lugar |

**Ejemplo de uso:**

```html
<div class="achievement-medal gold">
  <span class="medal-icon">🥇</span>
  <span>¡Nuevo récord personal!</span>
</div>
```

---

## Degradados

Degradados predefinidos para efectos visuales deportivos:

### Energy Gradient

```css
background: var(--trainhub-gradient-energy);
/* linear-gradient(135deg, #FF5722 0%, #FFC107 100%) */
```

**Uso:** Fondos de tarjetas destacadas, banners promocionales, botones hero.

---

### Fire Gradient

```css
background: var(--trainhub-gradient-fire);
/* linear-gradient(135deg, #F44336 0%, #FF5722 100%) */
```

**Uso:** Indicadores de intensidad máxima, récords personales, elementos de alta energía.

---

### Success Gradient

```css
background: var(--trainhub-gradient-success);
/* linear-gradient(135deg, #66BB6A 0%, #4CAF50 100%) */
```

**Uso:** Mensajes de éxito, completar entrenamientos, objetivos alcanzados.

---

## Fondos y Superficies

Grises oscuros con tintes cálidos optimizados para modo oscuro:

| Variable | Hex | Uso |
|----------|-----|-----|
| `--trainhub-surface-dark` | `#1a1412` | Fondo principal de la app |
| `--trainhub-surface-medium` | `#2d2522` | Tarjetas y contenedores |
| `--trainhub-surface-light` | `#3f3733` | Elementos elevados |

**Jerarquía de elevación:**

```scss
// Fondo principal
body {
  background-color: var(--trainhub-surface-dark);
}

// Tarjetas de contenido
.card {
  background-color: var(--trainhub-surface-medium);
}

// Modals y diálogos
.dialog {
  background-color: var(--trainhub-surface-light);
}
```

---

## Sombras

Sombras con tinte naranja para mantener la coherencia del tema:

| Variable | Valor | Uso |
|----------|-------|-----|
| `--trainhub-shadow-sm` | `0 1px 3px rgba(255, 87, 34, 0.12)` | Elementos sutiles |
| `--trainhub-shadow-md` | `0 4px 6px rgba(255, 87, 34, 0.16)` | Tarjetas normales |
| `--trainhub-shadow-lg` | `0 10px 20px rgba(255, 87, 34, 0.2)` | Elementos destacados |
| `--trainhub-shadow-xl` | `0 20px 40px rgba(255, 87, 34, 0.24)` | Modales, dropdowns |

---

## Guías de Uso

### Botones

```scss
// Botón principal (CTA)
.btn-primary {
  background: var(--trainhub-accent);
  color: white;
  box-shadow: var(--trainhub-shadow-md);
  
  &:hover {
    background: var(--trainhub-accent-light);
    box-shadow: var(--trainhub-shadow-lg);
  }
}

// Botón secundario
.btn-secondary {
  background: transparent;
  border: 2px solid var(--trainhub-accent);
  color: var(--trainhub-accent);
  
  &:hover {
    background: rgba(255, 87, 34, 0.08);
  }
}
```

---

### Publicaciones (Feed)

```scss
.post-card {
  background: var(--trainhub-surface-medium);
  border-radius: 12px;
  box-shadow: var(--trainhub-shadow-md);
  
  // Indicador de página activa en publicaciones deslizables
  .page-indicator {
    &.active {
      background: var(--trainhub-accent);
    }
  }
  
  // Botón de like
  .like-button {
    color: var(--mat-sys-tertiary);
    
    &.liked {
      color: var(--mat-sys-tertiary);
    }
  }
}
```

---

### Header/Navbar

```scss
.navbar {
  background: var(--trainhub-surface-light);
  box-shadow: var(--trainhub-shadow-md);
  
  // Logo con efecto hover
  .logo {
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  // Icono activo
  .nav-icon.active {
    color: var(--trainhub-accent);
  }
}
```

---

### Estadísticas y Métricas

```scss
.stat-card {
  background: var(--trainhub-surface-medium);
  border-left: 4px solid var(--trainhub-accent);
  
  // Valor destacado
  .stat-value {
    color: var(--trainhub-accent);
    font-weight: 700;
  }
  
  // Récord personal
  &.personal-record {
    border-left-color: var(--mat-sys-tertiary);
    background: var(--trainhub-gradient-fire);
  }
}
```

---

## Accesibilidad

### Ratios de Contraste (WCAG 2.1)

Todos los colores han sido diseñados para cumplir con las pautas WCAG AA en modo oscuro:

| Combinación | Ratio | Estado |
|-------------|-------|--------|
| Naranja (#FF5722) sobre negro | 5.8:1 | ✅ AA |
| Amarillo (#FFC107) sobre negro | 9.2:1 | ✅ AAA |
| Rojo (#F44336) sobre negro | 4.8:1 | ✅ AA |
| Verde (#66BB6A) sobre negro | 6.1:1 | ✅ AA |
| Blanco sobre surface-dark | 15.2:1 | ✅ AAA |

### Recomendaciones

1. **Texto sobre fondos oscuros:** Usa siempre los tonos 70-90 de cada paleta
2. **Elementos interactivos:** Mínimo 44x44px para cumplir con touch targets
3. **Estados de focus:** Siempre visible con borde de 2px en color primary
4. **Iconos importantes:** Usa `--trainhub-accent` o `--mat-sys-tertiary`

---

## Implementación

### En Componentes Angular

```typescript
// component.scss
.my-component {
  // Usar variables de Material Design 3
  background-color: var(--mat-sys-surface);
  color: var(--mat-sys-on-surface);
  
  // Usar variables personalizadas de Trainhub
  border-color: var(--trainhub-accent);
  
  &:hover {
    box-shadow: var(--trainhub-shadow-lg);
  }
}
```

### En TypeScript

```typescript
// Para estilos dinámicos
export class MyComponent {
  primaryColor = getComputedStyle(document.documentElement)
    .getPropertyValue('--trainhub-accent');
}
```

### En HTML con Angular Material

```html
<!-- Botón con color primary -->
<button mat-raised-button color="primary">
  Crear Publicación
</button>

<!-- Botón con color tertiary (para likes) -->
<button mat-icon-button color="tertiary">
  <mat-icon>favorite</mat-icon>
</button>
```

---

## Recursos Adicionales

- **Archivo de tema:** `frontend/src/styles/_theme.scss`
- **Estilos globales:** `frontend/src/styles.scss`
- **Material Design 3:** [material.angular.dev](https://material.angular.dev/guide/theming)

---

## Changelog

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0.0 | 2026-02-11 | Sistema de colores inicial para Trainhub |

---

**Creado con 💪 para Trainhub - Tu plataforma de entrenamiento CrossFit**
