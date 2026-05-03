# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev       # Start Vite dev server (HMR)
npm run build     # Type-check then bundle for production (tsc && vite build)
npm run preview   # Serve the production build locally
```

There is no test runner or lint script configured.

## Architecture

This is a React 18 + TypeScript + Vite + Tailwind CSS v3 marketing dashboard called "Pulse". All data is static mock data — there is no backend or API.

**Navigation** is handled entirely via `useState<NavPage>` in `App.tsx`. There is no router; `activePage` is passed down through `AppLayout` and pages are conditionally rendered. Adding a new page means adding a value to the `NavPage` union in `src/types/index.ts`, a nav entry in `Sidebar.tsx`, and a conditional render in `App.tsx`.

**Theme** (`light` | `dark`) is managed by `useTheme` (`src/hooks/useTheme.ts`), which persists to `localStorage` under the key `pulse-theme` and applies the Tailwind `dark` class to `<html>`. Dark mode is class-based throughout — every element uses paired `bg-*` / `dark:bg-*` utilities.

**Design system** lives in `tailwind.config.ts`. Key token groups:
- `neon.*` — eight accent colors (red, pink, orange, yellow, green, cyan, blue, purple) used for per-item color coding.
- `surface.light-*` / `surface.dark-*` — background layers (base → raised → card → hover → border → muted).
- `ink.light-*` / `ink.dark-*` — text hierarchy (primary, secondary, muted).
- `shadow-neon-*` — glow shadows for neon accents.

Components that need per-accent Tailwind classes define local lookup maps (e.g. `accentText`, `accentDot`, `accentBar`) as `Record<StatCardAccent, string>` — this keeps full class strings in source so Tailwind's scanner can detect them. Never construct these class names dynamically (e.g. `` `text-neon-${accent}` ``) or they will be purged from the production bundle.

**Types** are split by domain under `src/types/`:
- `index.ts` — barrel file; defines `Theme` and `NavPage`, re-exports everything from the domain files. All existing imports from `../types` or `../../types` resolve here.
- `CampaignTypes.ts` — campaign, stat card, platform, and performance types.
- `RegisterTypes.ts` — `LoginAs` and `RegisterForm`.
- When adding new types, create or update the appropriate domain file and let `index.ts` re-export it. Never add domain types directly to `index.ts`. Page-level components should import directly from the domain file (e.g. `from '../types/RegisterTypes'`); shared components may import from `index.ts`.

All mock data is in `src/data/mockData.ts`.

**Component layers:**
- `src/components/layout/` — `AppLayout` (shell), `Sidebar` (fixed left nav), `Topbar` (fixed top bar).
- `src/components/ui/` — primitives: `Card`, `Button`, `Badge`, `StatCard`, `NavItem`, `ThemeToggle`, `icons`.
- `src/pages/` — page-level compositions. Each page imports from `ui` and `data`. Pages that contain multiple related sub-views use a **container page** pattern (see below).

**Container page pattern:** when a feature has multiple sub-views (e.g. login + register), create individual leaf pages (`Login.tsx`, `Register.tsx`) and a container page (`Auth.tsx`) that owns the view-switching state and wires the leaves together. The container exposes a single callback to `App.tsx` (e.g. `onAuth`). `App.tsx` only ever imports and renders the container — it has no knowledge of the internal sub-views. Apply this pattern whenever adding a new multi-view feature.
