# Uso de código en SCSS
_Exported on 3/31/2026 at 16:37:40 GMT+2 from Cursor (2.6.13)_

---

**User**

@frontend/src/styles.scss:13-29 cómo debo usar esto en mis scss?

---

**Cursor**

En ese bloque estás aplicando el **tema global de Angular Material (Material 3)** sobre `html`. La idea es: **lo declaras una vez** y luego, en el resto de tus `.scss`, **consumes** los *tokens CSS* (variables) que genera `mat.theme(...)`.

## Qué hace y cómo se usa
- **`@include mat.theme(( ... ))`**: genera **CSS variables** (tokens) con tus paletas (`primary`, `secondary`, etc.), tipografía y densidad.
- Al ponerlo en `html { ... }` (o `:root { ... }`) esas variables quedan **disponibles globalmente**.

## Cómo consumirlo en tus SCSS (lo normal)
En cualquier componente/estilo, usa los tokens CSS:

```scss
.mi-boton {
  background: var(--mat-sys-primary);
  color: var(--mat-sys-on-primary);
}

.card {
  background: var(--mat-sys-surface);
  color: var(--mat-sys-on-surface);
  border: 1px solid var(--mat-sys-outline);
}
```

## Cuándo usar `mat.*` vs `var(--mat-sys-*)`
- **Para “pintar” tus propios componentes**: usa `var(--mat-sys-...)` como arriba.
- **Para componentes Material (mat-button, mat-form-field, etc.)**: normalmente no necesitas hacer nada; ellos ya leen esos tokens. Si quieres ajustar algo, suele ser con overrides/variables, no re-incluyendo `mat.theme`.

## Recomendaciones prácticas
- **Déjalo en `styles.scss`** (global) y **no lo repitas** en otros archivos.
- Si quieres compatibilidad más estándar, puedes moverlo a `:root { ... }` en vez de `html { ... }` (funciona igual en la mayoría de casos).

Si me pegas tu `frontend/src/styles.scss` completo (o me dices si estás usando Material 3 + `mat.theme` de Angular 17/18/19), te digo exactamente qué tokens tienes disponibles y cómo nombrarlos para tus estilos.

---

**User**

@frontend/src/styles.scss Aquí tienes el archvio completo para que me digas exáctamente qué tokens tengo disponibles y cómo nombrarlos para mis estilos

---

**Cursor**

Voy a leer `frontend/src/styles.scss` y también `frontend/src/styles/_theme.scss` (donde normalmente se definen paletas/temas). Con eso te digo qué tokens CSS se están generando y te dejo una chuleta de nombres típicos para usarlos en tus SCSS.

En tu proyecto tienes **2 familias de “tokens”** disponibles en SCSS (realmente son **variables CSS** que consumes con `var(...)`):

- **Tokens de Angular Material (M3)**: los genera `@include mat.theme(...)` en `styles.scss` y empiezan por `--mat-sys-...`
- **Tokens propios de marca (Trainhub)**: los defines tú en `styles/_theme.scss` dentro de `:root` y empiezan por `--trainhub-...`

### 1) Tokens `--trainhub-*` (los tienes 100% definidos)
Estos son exactamente los que ya existen en `frontend/src/styles/_theme.scss`:

```137:186:c:\xxx\dev\tfg\repo\frontend\src\styles\_theme.scss
:root {
  --trainhub-accent: #FF5722;
  --trainhub-accent-light: #FF8A65;
  --trainhub-accent-dark: #E64A19;
  --trainhub-success: #66BB6A;
  --trainhub-success-light: #81C784;
  --trainhub-success-dark: #4CAF50;
  --trainhub-warning: #FFA726;
  --trainhub-warning-light: #FFB74D;
  --trainhub-warning-dark: #FF9800;
  --trainhub-error: #EF5350;
  --trainhub-error-light: #E57373;
  --trainhub-error-dark: #F44336;
  --trainhub-info: #29B6F6;
  --trainhub-info-light: #4FC3F7;
  --trainhub-info-dark: #0288D1;
  --trainhub-achievement-gold: #FFD700;
  --trainhub-achievement-silver: #C0C0C0;
  --trainhub-achievement-bronze: #CD7F32;
  --trainhub-gradient-energy: linear-gradient(135deg, #FF5722 0%, #FFC107 100%);
  --trainhub-gradient-fire: linear-gradient(135deg, #F44336 0%, #FF5722 100%);
  --trainhub-gradient-success: linear-gradient(135deg, #66BB6A 0%, #4CAF50 100%);
  --trainhub-surface-dark: #1a1412;
  --trainhub-surface-medium: #2d2522;
  --trainhub-surface-light: #3f3733;
  --trainhub-shadow-sm: 0 1px 3px rgba(255, 87, 34, 0.12);
  --trainhub-shadow-md: 0 4px 6px rgba(255, 87, 34, 0.16);
  --trainhub-shadow-lg: 0 10px 20px rgba(255, 87, 34, 0.2);
  --trainhub-shadow-xl: 0 20px 40px rgba(255, 87, 34, 0.24);
  --trainhub-opacity-hover: 0.08;
  --trainhub-opacity-focus: 0.12;
  --trainhub-opacity-selected: 0.16;
  --trainhub-opacity-disabled: 0.38;
}
```

**Cómo nombrarlos/consumirlos en tus estilos** (cualquier `.scss`):

```scss
.card {
  background: var(--trainhub-surface-medium);
  box-shadow: var(--trainhub-shadow-md);
}

.badge--success {
  background: var(--trainhub-success);
  color: #000; // o usa tokens mat "on-..." si quieres consistencia
}

.hero {
  background: var(--trainhub-gradient-energy);
}
```

### 2) Tokens `--mat-sys-*` (Material 3) que te crea `mat.theme(...)`
En `frontend/src/styles.scss` estás generándolos aquí:

```13:28:c:\xxx\dev\tfg\repo\frontend\src\styles.scss
html {
  height: 100%;
  @include mat.theme(
    (
      color: (
        primary: theme.$trainhub-primary-palette,
        secondary: theme.$trainhub-secondary-palette,
        tertiary: theme.$trainhub-tertiary-palette,
        error: theme.$trainhub-error-palette,
        neutral: theme.$trainhub-neutral-palette,
        neutral-variant: theme.$trainhub-neutral-variant-palette,
      ),
      typography: Roboto,
      density: 0,
    )
  );
}
```

Eso habilita (entre otros) los **tokens de sistema más usados**:
- **Colores**: `--mat-sys-primary`, `--mat-sys-on-primary`, `--mat-sys-secondary`, `--mat-sys-on-secondary`, `--mat-sys-tertiary`, `--mat-sys-error`, `--mat-sys-on-error`, etc.
- **Superficies**: `--mat-sys-surface`, `--mat-sys-on-surface`, `--mat-sys-surface-container-*`, `--mat-sys-outline`, …
- **Tipografía**: `--mat-sys-body-medium`, `--mat-sys-title-large`, etc. (tú ya usas `var(--mat-sys-body-medium)`)

Ejemplos reales (alineados con tu `body` actual):

```31:45:c:\xxx\dev\tfg\repo\frontend\src\styles.scss
body {
  color-scheme: dark;
  background-color: var(--trainhub-surface-dark);
  color: var(--mat-sys-on-surface);
  font: var(--mat-sys-body-medium);
  margin: 0;
  height: 100%;
}
```

**Cómo nombrarlos/consumirlos en tus estilos**:

```scss
.primary-action {
  background: var(--mat-sys-primary);
  color: var(--mat-sys-on-primary);
}

.muted {
  color: var(--mat-sys-on-surface-variant);
}

.outlined {
  border: 1px solid var(--mat-sys-outline);
}
```

### Nota importante sobre “qué tokens exactos existen”
Los `--trainhub-*` son los de la lista anterior (fijos).  
Los `--mat-sys-*` dependen de Angular Material, pero **con `mat.theme(...)` (M3) esa convención `--mat-sys-...` es la correcta** y ya estás usando dos de ellos (`--mat-sys-on-surface`, `--mat-sys-body-medium`), así que están activos.

Si quieres, te digo **la lista exacta** de `--mat-sys-*` generados en tu build (sin adivinar) si me pegas el CSS compilado donde aparezcan (normalmente en `frontend/dist/.../styles-*.css`) o me dices si prefieres que te indique “los imprescindibles” para color/surface/typography (10–20 tokens) y con eso estandarizas todo tu SCSS.

