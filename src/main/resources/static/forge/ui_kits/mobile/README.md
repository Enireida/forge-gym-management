# Forge Mobile App — Member UI Kit

iOS member app. Premium wellness feel: bone canvas, sage accents, Instrument Serif italic display headlines, generous whitespace.

## Screens
1. **Login** — bilingual ready, sage focus state
2. **Home / Today** — greeting, membership card with PIN + QR, quick actions, HealthKit stats from Apple Watch
3. **Progress** — weight chart over 30 days with sage gradient, body measurements, AI plateau callout
4. **Check-in** — kiosk-style sage panel with PIN entry + RFID prompt, on-screen keypad
5. **AI Coach chat** — message thread, data-grounded responses with inline metric pills, suggested replies
6. **Food tracker** — calorie summary, macro rings, "snap a meal" CTA (AI vision), meal log with AI badge

## How to read
Each screen lives inside an `IOSFrame` (see `index.html`). Frames are presented in a horizontal scroll for compactness.

## Design DNA
- All headlines use `Instrument Serif` italic for the *highlighted* word, regular roman for the rest
- Numbers use `Geist Mono` for tabular feel
- Sage 800 is the primary brand surface (kiosk + check-in + chat send)
- Copper appears as the secondary accent only (active states, the "AI" / "snap" affordance)
- Cards: white surface, 1px line-1 border, radius 16-24
