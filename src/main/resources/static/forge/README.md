# Forge — Design System

> A premium wellness platform for modern fitness centers. Built bilingual (English / Albanian), warm-but-sophisticated, sage-green and bone-beige.

---

## What is Forge?

**Forge** is a complete digital platform for modern fitness centers — replacing the paper-based processes used by most Albanian gyms with a unified system across web, mobile, and Apple Watch.

The product spans **six surfaces**:

| Surface | Audience | Description |
|---|---|---|
| **Marketing site** | Prospective members + gym owners (B2C + B2B) | Landing, pricing, contact, the public face |
| **Mobile app** | Members | iOS + responsive web, login, check-in, progress, AI coach, food tracking |
| **Admin dashboard** | Gym owners / managers | Members, payments, equipment, plans, reports, announcements |
| **Trainer dashboard** | Personal trainers / coaches | Assigned clients, measurements, PT sessions, exercise library |
| **Member portal (web)** | Members | Home, membership, progress, PT, exercises |
| **Door kiosk** | At the front-door RFID reader | Tap-to-enter check-in display |
| **Apple Watch** | Members | Steps + heart rate via HealthKit (read-only) |

### Three roles, one database
- **Administrator** — full access; manages members, staff, payments, plans, equipment
- **Trainer** — sees assigned clients only; logs measurements + PT sessions
- **Member** — sees own data; checks in, tracks progress, talks to AI coach

### What makes Forge different
1. **Premium wellness aesthetic** — sage green + warm beige, not the typical gym-bro orange/black
2. **Bilingual UI** — Albanian and English, full toggle
3. **AI Fitness Assistant** — Claude / OpenAI powered, knows your profile (weight, goals, BMI)
4. **Food tracker with AI** — log meals, AI estimates macros, suggests improvements
5. **Apple Watch integration** — steps + heart rate auto-displayed (HealthKit, no custom Watch app)
6. **RFID + PIN check-in** — tap card or type 4-digit PIN at the door

---

## Sources

This design system was created from:
- **Codebase**: `gym-management-system/` (Spring Boot + Thymeleaf — see `_notes/codebase-digest.md` for the full inventory). The original codebase ships under the working name "FitManager Pro" with an orange/dark palette; we have **renamed → Forge** and **repalleted → sage + beige** as part of this design system.
- **Project guide**: `uploads/SmartGym_ProjectGuide.txt` — the bachelor thesis brief from UNyT Albania describing the full feature scope, timeline, and tech stack.
- **No Figma was provided.** All UI kit screens are recreations of the Thymeleaf templates with the new visual language.

> ⚠️ The original code calls itself "FitManager Pro" / "Smart Gym". The brand chosen for the design system is **Forge**. When refactoring the production codebase, search-and-replace the old brand strings.

---

## Index — what's in this folder

```
README.md                  ← you are here
SKILL.md                   ← cross-compatible skill for Claude Code
colors_and_type.css        ← all design tokens (CSS vars)
fonts/                     ← Instrument Serif + Geist + Geist Mono (Google Fonts)
assets/                    ← logos, brand marks, generic imagery
preview/                   ← Design System tab cards
ui_kits/
  marketing/               ← public website (landing, pricing, contact)
  mobile/                  ← member mobile app (works on phone + laptop)
  admin/                   ← admin dashboard
  trainer/                 ← trainer dashboard
  member/                  ← member web portal
  watch/                   ← Apple Watch HealthKit display
  kiosk/                   ← door RFID check-in
slides/                    ← (none — no slide template was provided)
_notes/
  codebase-digest.md       ← architectural extract from the original codebase
```

Each `ui_kits/<product>/` has its own `README.md`, an `index.html` interactive demo, and individual JSX component files.

---

## CONTENT FUNDAMENTALS

### Voice
**Premium · grounded · warm.** We sound like a thoughtful coach who has been doing this for twenty years, not a hype-merchant Instagram fitness brand. We use full sentences. We don't use exclamation marks except in the mobile app's check-in confirmation ("Welcome back, Maria!"). We assume our reader is a busy adult — not a teenager.

### Tone calibration
- **Marketing site:** confident, declarative, slightly editorial. Short sentences. "Train with intent." not "Crush your goals!! 💪"
- **Member app:** quietly motivating. "Today's session, ready when you are." not "Time to get jacked!"
- **Admin/trainer dashboards:** factual, neutral, dense. "12 members renewed this week." Numbers and labels, not adjectives.
- **AI Fitness Assistant:** the calm expert. Speaks in short paragraphs. Never ALL CAPS. Names what it sees in the data ("Your weight has held steady for three weeks — that's a plateau, here's how we move past it.")

### Casing
- **Sentence case for everything UI** — buttons, headers, navigation labels, form labels. ("Add new member", not "Add New Member" or "ADD NEW MEMBER".)
- **Eyebrow labels** are the one exception — `12px · 600 · 0.18em tracking · UPPERCASE` ("MAIN", "OPERATIONS", "INSIGHTS" in the sidebar).
- **Brand name** is always *Forge* with a capital F, never FORGE, never forge.

### Pronouns
- Use **you** when addressing the user — "your members", "your check-ins today".
- Use **we** sparingly, only on the marketing site when describing Forge as a company. Avoid "we" in the admin/trainer/member apps.
- Avoid "I" entirely except in AI Assistant replies.

### Bilingual rules (Albanian + English)
- Both translations live side-by-side; the user toggles. We never machine-translate at runtime.
- Albanian is **first-class**, not a second-class translation. Numbers use Albanian formatting (1.250,00 lekë; 18.420 € for euros).
- Date format: **DD MMM YYYY** ("12 Maj 2026" / "12 May 2026"). Never US-style "5/12/26".
- Currency: default **€ (EUR)**, with **Lekë (ALL)** as a setting. Never $.
- Phone: **+355 6X XXX XXXX** for Albanian numbers.

### No emoji in product UI
The original codebase used emoji (💪 🔥 ❤️) as feature icons. We've replaced them with Lucide line icons. Emoji is allowed in **member-to-trainer chat messages** and **announcement bodies** (user-generated). Never in chrome, navigation, headings, or buttons.

### Copy examples
| Don't say | Say instead |
|---|---|
| "🔥 #1 gym software in the region" | "The platform behind Albania's modern fitness centers." |
| "Crush your workout!" | "Today's session is ready." |
| "Get jacked, get ripped" | "Train with intent. Track what matters." |
| "OMG you missed leg day" | "It's been five days since your last lower-body session." |
| "Card not recognised 😢" | "We don't recognize that card. Please contact reception." |

---

## VISUAL FOUNDATIONS

### Palette philosophy
**Sage green + warm bone.** Two families do all the work, with copper as a single warm accent for energy moments. We do **not** use the typical fitness palette of black + neon orange. The system reads as a **premium wellness club**, not a hardcore gym — but the type and density keep it confident, never spa-soft.

- **Sage** (`--sage-50` → `--sage-950`) is the brand spine. `sage-800 #1F3D2B` is *the* Forge color — used for primary buttons, sidebars, hero text, the logo mark.
- **Bone** (`--bone-25` → `--bone-950`) is the neutral. `bone-50 #F5F1E8` is the canvas. `bone-950 #1A1814` is body text — a warm near-black, never `#000`.
- **Copper** (`--copper-500 #C77A4A`) is the energy accent. Used for the AI Assistant moments, "Start workout" CTAs, energy-positive deltas. **Used sparingly** — never two coppers on the same screen.

### Type system
**Instrument Serif × Geist × Geist Mono.**
- **Display** (`Instrument Serif`, 400 italic): all H1/H2 in marketing, hero numbers in member app, big titles. The italic tail of "Forge*.*" — that period in italic sage — is our signature flourish, not a ligature trick.
- **Body** (`Geist`, 300/400/500/600/700): all UI body text, buttons, labels.
- **Numeric** (`Geist Mono`, tabular-nums): all stats, clocks, BPM, steps, weights, currency. **Always tabular-nums** so digits don't shift between values.

### Spacing rhythm
8-pt grid (`--sp-1 4` → `--sp-32 128`). Components breathe — generous padding, never cramped. Default card padding is `--sp-5 20` to `--sp-6 24`. Section gaps `--sp-12 48` on mobile, `--sp-20 80` on desktop.

### Backgrounds
- **No gradients** in product chrome. (Marketing hero uses one subtle radial sage tint, that's it.)
- **No background patterns or textures** by default. Surface = `#FFFFFF` on canvas = `bone-50`.
- **Full-bleed photography** for marketing hero and feature sections — warm, natural light, real Albanian gyms when possible. Never cool-tinted stock fitness photos. Slight warm grain (~3% noise overlay) acceptable on hero images.
- **Dark sections** use `sage-900 #18271B` not pure black. Sidebar, kiosk, login auth-card backdrop.

### Borders
- **1px borders** with `--line-1 (bone-200)` for default separators, `--line-2 (bone-300)` for stronger emphasis.
- **No double borders.** No dashed borders except on placeholder upload zones.
- **Card style:** `1px solid var(--line-1)` + `--shadow-2`. Never both a heavy border AND a heavy shadow.

### Corner radii
- Buttons: `--r-md 12px`
- Cards: `--r-lg 16px` or `--r-xl 20px` (feature cards)
- Modals + hero blocks: `--r-2xl 28px`
- Pills, status badges, avatars: `--r-pill`
- **Never** mixed radii on the same component (no "rounded top, square bottom").

### Shadows
**Forge stays low.** Most cards sit on `--shadow-2`. Hover lifts to `--shadow-3`. Modals get `--shadow-4`. **No glow effects** except the kiosk success/bye states (large outer green/copper glow, ~60px blur).

### Hover & press states
- **Hover:** primary button → bg shifts from `sage-800` to `sage-900`. Cards → shadow-2 → shadow-3 + 1px translate-Y up. Links → color shift, no underline change.
- **Press / active:** scale to 0.98 + bg darkens. 80ms.
- **Focus:** 3px ring of `sage-100` outside the element. Never default browser blue.

### Animation
- **Easing:** `cubic-bezier(0.2, 0, 0, 1)` (out) for most. Spring for AI Assistant moments. Never bouncy on data-heavy UIs.
- **Duration:** `220ms` is the default. `120ms` for instant feedback (button press), `380ms` for layout shifts.
- **No fade-in-on-scroll**. Content is there when you arrive.
- The kiosk card has a 2s pulse on the waiting state — the only ambient motion in the system.

### Transparency & blur
- Used **rarely**. Sidebar overlay on mobile uses `rgba(26,24,20,0.4) + backdrop-filter: blur(8px)`.
- AI Assistant chat-bubbles never use transparency.
- The marketing hero floating chips on the runner illustration use `rgba(245,241,232,0.08) + 12px blur` — that's the only place.

### Layout rules
- **1280px max content width** on desktop.
- **240px sidebar** on dashboards (admin/trainer/member). Sidebar is dark sage, sticky, always visible ≥ 1024px.
- **64px topbar** with section title left, user + logout right.
- **Mobile breakpoints:** `< 640px` (phone), `640–1024` (tablet), `≥ 1024` (desktop).
- **Mobile sidebar collapses** behind a hamburger; the bottom-tab nav (Home / Progress / PT / Profile) is the primary mobile navigation.

### Imagery vibe
- **Warm light, natural skin tones.** Golden hour, never blue-tinted.
- **Real action, real focus** — people training, not posed model shots.
- **Black and white as a secondary** for member-progress photo timelines.
- **No stock-photo high-fives** or generic cardio-with-headphones shots.

---

## ICONOGRAPHY

### System: Lucide
**Lucide** (formerly Feather) — line icons, 1.6 stroke. Loaded from CDN: `https://unpkg.com/lucide@latest`. We picked this because:
1. The original codebase uses emoji as icons — these need to be replaced everywhere.
2. Lucide has every fitness/admin glyph we need (`dumbbell`, `heart-pulse`, `footprints`, `flame`, `qr-code`, `credit-card`, `calendar`, `users`, `sparkles`, `utensils`, `apple`).
3. Stroke style matches the editorial-but-modern type pairing.

### Usage rules
- **Default size:** 22px in 14px-text contexts; 16px inline next to body copy; 32px for feature/hero icons.
- **Color:** `var(--sage-800)` on light surfaces, `var(--bone-50)` on dark surfaces. Never copper on icons (copper is for fills, not strokes).
- **Stroke:** `1.6` is the default. Never increase to 2 — keeps the editorial feel.
- **Avoid filled variants.** Forge is a stroke-icon system.

### Emoji policy
- **No emoji in chrome** (nav, buttons, headers, feature icons, status messages).
- **Emoji allowed** in user-generated content: chat messages between trainer ↔ member, announcement bodies written by admins. The platform doesn't strip them, but we don't display them in our own copy.
- **No flag emoji for the language toggle.** We use the strings `EN` / `SQ` instead.

### Substitutions to flag
The codebase used these emoji as feature icons. Replaced with Lucide:

| Original emoji | Lucide replacement |
|---|---|
| 👥 | `users` |
| 📡 | `wifi` (RFID) / `qr-code` |
| 💳 | `credit-card` |
| 🏋️ | `dumbbell` |
| 📊 | `bar-chart-3` |
| 🧰 | `wrench` |
| 📚 | `book-open` |
| 📈 | `line-chart` |
| ❤️ | `heart-pulse` |
| 🔥 | `flame` (use **only** for streaks, not as decoration) |
| ✨ | `sparkles` (AI Assistant) |
| 🍎 | `apple` (food tracker) |
