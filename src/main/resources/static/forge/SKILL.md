# Forge Design System — Skill

A premium wellness design system for **Forge**, a gym management platform (UNyT Bachelor thesis project). Sage green + bone palette, Instrument Serif italic display, Geist sans/mono. Built around an existing Spring Boot + Thymeleaf codebase and extends to React Native mobile, Apple Watch, and AI fitness assistant surfaces.

## When to invoke

Use this design system whenever the user asks for:
- Forge / SmartGym / FitManager UI work (web, mobile, watch, marketing)
- A new screen, component, or flow inside the gym management product
- Variations on existing screens (admin dashboard, member portal, trainer view, mobile app)
- Marketing material — landing pages, pricing, contact, B2B sales for gym owners
- AI Fitness Assistant or food-tracker surfaces
- Bilingual (Albanian/English) gym software

## Key files to read first

1. **`README.md`** — orientation: brand voice, content fundamentals, visual foundations, file index
2. **`colors_and_type.css`** — design tokens (CSS variables): copy this file alongside any new HTML
3. **`assets/ICONOGRAPHY.md`** — when to use Lucide vs custom marks
4. **`_notes/codebase-digest.md`** — what already exists in the user's Spring Boot codebase
5. **`_notes/user-direction.md`** — locked product/brand decisions

## UI kits

| Surface | Path | Notes |
|---|---|---|
| Marketing site | `ui_kits/marketing/index.html` | Bilingual EN/SQ toggle, hero, AI showcase, pricing |
| Mobile member app | `ui_kits/mobile/index.html` | 6 iOS screens in horizontal scroll |
| Web admin | `ui_kits/admin/index.html` | KPIs, revenue chart, attendance heatmap, activity |
| Web trainer | `ui_kits/trainer/index.html` | Sessions today + assigned members |
| Web member portal | `ui_kits/member/index.html` | Same sidebar shell as admin/trainer |
| Apple Watch | `ui_kits/watch/index.html` | 4 HealthKit-driven faces |

## Visual rules (do not deviate)

- **Display headlines** = `Instrument Serif` regular weight, with the *emphasized* word in italic and colored `var(--sage-700)` or `var(--copper-400)` on dark
- **UI text + body** = `Geist`
- **Numbers, time, code, PIN, kcal** = `Geist Mono` (tabular feel)
- **Sage 800** (`#1F3D2B`) is the primary brand surface — kiosk, hero card, primary buttons
- **Copper 500** (`#C77A4A`) is the only secondary accent — use sparingly: dot accents, AI indicator, "Next" badges
- **Bone canvas** (`#F5F1E8`) for everything else — never pure white as page background; pure white only for cards
- **Borders** are 1px `#E5DDC8` (bone-100) at radius 14–24
- **No drop shadows** on flat UI; soft shadows only for floating chips/popovers (`var(--shadow-3)`)

## Component patterns

- **Greetings always personal**: "Hey, *Elira*", "Welcome back, *Arben*" with italic name
- **Status pills** sage-50 bg / sage-700 fg for positive, copper-tinted for AI/attention
- **Bilingual copy** — when adding strings to marketing site, provide both EN and SQ keys
- **Empty states** use Instrument Serif italic for warmth

## Things to avoid

- Pure black (#000) — use `#1A1814` (fg-1)
- Pure white page background — use bone canvas
- Hard gradients across cards (only on hero / CTA banner backgrounds)
- More than 2 colors competing in one card
- Lucide icons larger than 22px in product UI
- Adding new colors without checking `colors_and_type.css` first

## Albanian content notes

- Toggle UI: "EN / SQ" — defaults to EN in marketing, SQ in product
- Cities/locations: Tirane, Durres, Vlore
- Sample names to use: Elira, Driton, Arben, Rea, Besnik, Mirjeta, Anila, Ardit
- Currency: Euro (€) — gym pricing in Albania uses both Lek and Euro but Forge uses €
- Formal but warm tone: avoid slang, avoid corporate-cold ("Greetings, valued customer")
