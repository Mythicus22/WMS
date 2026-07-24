---
name: Industrial Logic
colors:
  surface: '#f8f9fa'
  surface-dim: '#d9dadb'
  surface-bright: '#f8f9fa'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f4f5'
  surface-container: '#edeeef'
  surface-container-high: '#e7e8e9'
  surface-container-highest: '#e1e3e4'
  on-surface: '#191c1d'
  on-surface-variant: '#414750'
  inverse-surface: '#2e3132'
  inverse-on-surface: '#f0f1f2'
  outline: '#727781'
  outline-variant: '#c1c7d2'
  surface-tint: '#1261a3'
  primary: '#004275'
  on-primary: '#ffffff'
  primary-container: '#005a9c'
  on-primary-container: '#afd1ff'
  inverse-primary: '#a1c9ff'
  secondary: '#575f67'
  on-secondary: '#ffffff'
  secondary-container: '#d8e1ea'
  on-secondary-container: '#5b646b'
  tertiary: '#003f83'
  on-tertiary: '#ffffff'
  tertiary-container: '#27579d'
  on-tertiary-container: '#b7cfff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d2e4ff'
  primary-fixed-dim: '#a1c9ff'
  on-primary-fixed: '#001c37'
  on-primary-fixed-variant: '#00487f'
  secondary-fixed: '#dbe4ed'
  secondary-fixed-dim: '#bfc8d0'
  on-secondary-fixed: '#141d23'
  on-secondary-fixed-variant: '#3f484f'
  tertiary-fixed: '#d7e3ff'
  tertiary-fixed-dim: '#abc7ff'
  on-tertiary-fixed: '#001b3f'
  on-tertiary-fixed-variant: '#0c458b'
  background: '#f8f9fa'
  on-background: '#191c1d'
  surface-variant: '#e1e3e4'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  headline-sm:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  container-margin: 24px
  gutter: 16px
---

## Brand & Style

The design system is engineered for the high-stakes environment of warehouse operations and enterprise logistics. The brand personality is rooted in **reliability, precision, and efficiency**. It avoids any decorative flourish that might distract from data-heavy workflows, instead leaning into a refined **Corporate/Modern** aesthetic inspired by Material Design 3.

The target audience consists of floor managers, inventory specialists, and logistics directors who require split-second legibility. The UI evokes a sense of "industrial strength" software—stable, predictable, and robust. Design decisions prioritize functional clarity over visual trends, using a systematic approach to hierarchy that ensures complex information is digestible at a glance.

## Colors

The palette is anchored by **Deep Enterprise Blue**, a color associated with stability and professional authority. This primary tone is used for core actions and navigation landmarks. 

The background strategy utilizes a very light grey (`#F8F9FA`) to reduce screen glare during long shifts while maintaining high contrast with the white (`#FFFFFF`) surface containers where data resides. A specialized semantic palette is critical for industrial monitoring:
- **Healthy (Green):** System-go and successful shipments.
- **Warning (Orange):** Low stock or pending delays.
- **Fault (Red):** Critical system errors or safety halts.
- **Maintenance (Yellow):** Scheduled service or non-critical attention required.

Avoid using gradients or decorative tints; stick to solid fills to maintain the "Industrial" look.

## Typography

The design system utilizes **Inter** for all roles. Inter was selected for its exceptional tall x-height and neutral character, which aids in the legibility of alphanumeric strings (like SKU numbers and tracking codes). 

The hierarchy follows a high-density logic:
- **Headlines:** Use SemiBold (600) for clear section identification.
- **Body Text:** Use Regular (400) with generous line heights to ensure long lists of inventory items are readable.
- **Labels:** Use Medium (500) for UI elements like table headers and input labels to differentiate them from user-entered data.
- **Numerical Data:** When displaying metrics or quantities, ensure the `tabular-nums` OpenType feature is active to keep columns aligned.

## Layout & Spacing

The system uses a **12-column fluid grid** for desktop and a **4-column grid** for mobile. A 4px baseline grid ensures consistent vertical rhythm.

Given the high information density requirements of a warehouse suite, internal component padding is kept compact (8px-12px) while external margins between major UI sections are kept wider (24px) to prevent the interface from feeling cluttered. 

**Breakpoints:**
- **Mobile:** Up to 599px (Single column stacked cards).
- **Tablet:** 600px - 1023px (Side-by-side metric tiles).
- **Desktop:** 1024px+ (Full 12-column dashboard).

Data tables should use a "Condensed" mode for inventory views, reducing row heights to 40px to maximize the "above the fold" data visibility.

## Elevation & Depth

This design system uses **Tonal Layers** rather than dramatic shadows to establish hierarchy, aligning with Material Design 3 principles. 

1.  **Level 0 (Background):** `#F8F9FA`. Used for the overall canvas.
2.  **Level 1 (Surface):** `#FFFFFF`. Used for the main content cards and tables.
3.  **Level 2 (Interaction/Floating):** Subtle, low-opacity ambient shadows (0px 2px 4px rgba(0,0,0,0.05)) are used only for elements that require immediate attention, such as dropdown menus or active modal dialogs.

Instead of heavy shadows, use 1px borders in a soft neutral (e.g., `#DEE2E6`) to define container boundaries, maintaining a clean, industrial look.

## Shapes

The design system utilizes **Rounded** geometry (Value 2) to soften the industrial data and make the interface feel modern and premium.

- **Main Cards:** 12px or 16px corner radius (`rounded-lg` to `rounded-xl`).
- **Buttons & Inputs:** 8px corner radius.
- **Status Chips:** Full pill-shape (circular ends) to distinguish them quickly from interactive buttons.

This subtle rounding provides a "tactile" feel that contrasts against the rigid, grid-based layout of the data.

## Components

### Buttons
Primary buttons use the **Deep Enterprise Blue** with white text. They should have a minimum height of 44px to ensure they are "touch-friendly" for tablet users on the warehouse floor. Use "High Emphasis" (filled), "Medium Emphasis" (outlined), and "Low Emphasis" (text-only) styles.

### Input Fields
Inputs must have a permanent visible label (top-aligned) and a 1px border. The focus state uses a 2px Deep Enterprise Blue border. Placeholder text should be a light neutral to avoid confusion with entered data.

### Chips & Badges
Used for status indicators (e.g., "In Transit", "Out of Stock"). They use a light tint of the semantic color as a background and the dark version of the color for text to ensure high contrast and accessibility.

### Cards
Cards are the primary container. They must have a 12-16px radius and a subtle 1px border. No shadows should be used on resting cards; elevation is indicated solely by the white surface against the grey background.

### Data Tables
Tables are the heart of the system. Headers must be "Sticky" during scroll. Row hover states should use a very light blue (`#E7F1FF`) to guide the eye across wide datasets.

### Progress Indicators
Linear progress bars are preferred over circular ones for industrial tasks to represent the "linear" nature of logistics workflows.