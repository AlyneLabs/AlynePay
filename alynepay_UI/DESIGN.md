---
name: AlynePay
colors:
  surface: '#141313'
  surface-dim: '#141313'
  surface-bright: '#3a3939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2b2a2a'
  surface-container-highest: '#353434'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c4c7c7'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8e9192'
  outline-variant: '#444748'
  surface-tint: '#c8c6c5'
  primary: '#c8c6c5'
  on-primary: '#313030'
  primary-container: '#0f0f0f'
  on-primary-container: '#7d7b7b'
  inverse-primary: '#5f5e5e'
  secondary: '#d2bbff'
  on-secondary: '#3f008e'
  secondary-container: '#6001d1'
  on-secondary-container: '#c9aeff'
  tertiary: '#4cd7f6'
  on-tertiary: '#003640'
  tertiary-container: '#001217'
  on-tertiary-container: '#00889f'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c8c6c5'
  on-primary-fixed: '#1c1b1b'
  on-primary-fixed-variant: '#474646'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#d2bbff'
  on-secondary-fixed: '#25005a'
  on-secondary-fixed-variant: '#5a00c6'
  tertiary-fixed: '#acedff'
  tertiary-fixed-dim: '#4cd7f6'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#141313'
  on-background: '#e5e2e1'
  surface-variant: '#353434'
typography:
  display-lg:
    fontFamily: Geist
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Geist
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Geist
    fontSize: 28px
    fontWeight: '600'
    lineHeight: '1.2'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: '0'
  label-mono:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.0'
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 16px
  md: 24px
  lg: 40px
  xl: 64px
  gutter: 16px
  margin-mobile: 20px
  margin-desktop: 120px
---

## Brand & Style
The design system is engineered for a decentralized offline payment ecosystem, prioritizing security, immediacy, and a futuristic "cosmic" aesthetic. The brand personality is tech-forward, high-fidelity, and sophisticated. 

The visual style is a hybrid of **Minimalism** and **Glassmorphism**, set against a deep Onyx backdrop. It utilizes grainy backdrop blurs and light-refractive borders to simulate the feeling of high-end hardware interfaces or deep-space instrumentation. The UI should feel immersive, using depth and transparency to manage information density without overwhelming the user.

## Colors
This design system operates exclusively in a dark mode environment. The primary background is a true **Onyx Black (#0F0F0F)** to maximize OLED efficiency and provide a canvas for vibrant accents.

- **Primary Canvas:** #0F0F0F.
- **Galaxy Accents:** A gradient spectrum starting from Deep Purple (#7C3AED) through Cosmic Blue (#06B6D4) to Vibrant Magenta (#EC4899). These are used for primary actions, progress indicators, and successful transaction states.
- **Glass Surfaces:** Semi-transparent whites with low opacity (3-8%) are used to create the frosted glass effect.
- **Functional Colors:** Success (Emerald), Error (Rose), and Warning (Amber) are used sparingly with high saturation to pierce through the dark theme.

## Typography
The typography strategy balances technical precision with high readability. 

- **Geist** is used for headlines and display text to evoke a developer-centric, "built-for-builders" feel.
- **Inter** provides a highly legible experience for body text and transactional details.
- **JetBrains Mono** is utilized for transaction hashes, wallet addresses, and "offline-mode" status indicators to reinforce the decentralized nature of the app.

All headings should use tighter letter-spacing to maintain a compact, premium feel. Use "Uppercase" styling for labels to distinguish them from interactive body text.

## Layout & Spacing
The design system utilizes a **fluid grid** model with a base-4 spacing scale.

- **Mobile:** 4-column layout with 20px side margins and 16px gutters.
- **Desktop:** 12-column layout with a maximum container width of 1280px.
- **Rhythm:** Vertical rhythm is strictly enforced in 8px increments. 

Elements are grouped in "Glass Modules" where padding inside a card is typically one step larger than the spacing between cards (e.g., if card gap is 16px, internal padding is 24px) to create a sense of containment.

## Elevation & Depth
Elevation is conveyed through **Glassmorphism** and backdrop blurs rather than traditional shadows. 

- **Tier 1 (Base):** Onyx background (#0F0F0F).
- **Tier 2 (Cards):** Glassmorphic surfaces with a `20px` backdrop blur and a `1px` stroke (white at 8% opacity).
- **Tier 3 (Modals/Overlays):** A more intense backdrop blur (`40px`) with a "grain" texture overlay at 5% opacity to simulate physical frosted glass.
- **Outer Glow:** Active states and "Action" elements use a soft, colored outer glow (diffused shadow) tinted with the primary accent color (#7C3AED) at very low opacity (15%) to indicate light emission.

## Shapes
The shape language is consistently **Rounded**. 

- **Standard Elements:** 0.5rem (8px) for buttons, inputs, and list items.
- **Primary Containers:** 1rem (16px) for cards and modals.
- **Status Pills:** 100px (fully rounded) for chips and status tags.

Avoid sharp 0px corners to maintain the approachable, modern aesthetic. The "Rounded" setting ensures that even high-tech components feel ergonomic and tactile.

## Components

- **Action Buttons:** Use a high-contrast gradient background (Galaxy Gradient). The text should be white or high-contrast black depending on the gradient intensity. Include a subtle inner-glow on hover.
- **Glassmorphic Cards:** Background: `rgba(255, 255, 255, 0.03)`. Border: `1px solid rgba(255, 255, 255, 0.1)`. Backdrop-filter: `blur(20px)`. Add a slight noise texture to the background.
- **Input Fields:** Darker than the background or transparent with a glass border. Labels should use the `label-mono` typography style.
- **Chips:** For status indicators (e.g., "Offline", "Syncing"), use a pill-shape with a subtle border and a small dot indicator.
- **Blurred Overlays:** Use for navigation bars and tab bars. These should sit fixed at the top/bottom with a `32px` blur, allowing content to ghost underneath as the user scrolls.
- **Connectivity Indicator:** A unique component for this system—a glowing pulse effect (magenta-to-blue) that changes frequency based on offline sync status.