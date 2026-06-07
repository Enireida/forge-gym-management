# Codebase digest — gym-management-system

**Stack:** Spring Boot + Thymeleaf (NOT React, despite project guide). Java backend, server-rendered HTML templates. CSS in static/css/app.css.

## Original brand
- "FitManager Pro" — orange (#f97316) + dark slate (#0f172a) palette
- User wants pivot to green+beige wellness palette, brand renamed "Forge"

## Original CSS tokens (will replace)
- --bg #0f172a, --bg-soft #1e293b
- --primary #f97316, --primary-hover #ea580c
- --secondary #8b5cf6, --success #10b981, --warning #f59e0b, --danger #ef4444
- --text #1e293b, --text-soft #64748b, --border #e2e8f0
- font: Segoe UI, system stack
- shadow: 0 4px 6px -1px rgba(0,0,0,0.1)
- radii: 8px (small), 14px (card), 20px (feature)

## Layout systems
- **Dashboard:** grid 240px sidebar + 64px topbar + main. Sidebar dark bg, orange accent on active link border-left.
- **Auth:** centered card on dark gradient bg.
- **Landing:** hero (dark gradient, animated runner SVG, floating chips with emoji), features grid (8 cards w/ emoji icons), about (gradient block + bullet list), plans (3 cards, middle "popular"), testimonials (dark section with star reviews), contact (info rows + form).

## Templates inventory
- landing.html — hero, features, about, plans, testimonials, contact, footer
- login.html — auth card
- kiosk.html — RFID door check-in display
- fragments/layout.html — shared dashboard chrome
- admin/: home, members, payments, plans, staff, equipment, exercises, reports, announcements
- trainer/: home, members, measurements, exercises, pt, pt-detail, plan-detail
- client/: home, progress, membership, plan-detail, pt, exercises
- error.html

## Key features (from landing)
- Member mgmt with PIN + RFID
- RFID door kiosk
- Payments & plans (multiple tiers)
- Personal training packages
- Live analytics + Word export
- Equipment & maintenance
- Workout library
- Progress tracking + BMI

## Original copy tone
- Bold gym-bro: "Train Hard. Manage Smart.", "🔥 #1 gym software"
- Heavy emoji usage in features, contact, testimonials
- Mixed English (codebase) — user wants Albanian + English toggle
- Stats: 2,500+ members, 50+ trainers, 99.9% uptime
- Albanian context: "Rruga e Durrësit, Tirana", +355 phone, "across Albania and beyond"

## What we're CHANGING
- Brand: FitManager Pro → **Forge**
- Palette: orange/dark → **green + beige wellness** (premium, sophisticated)
- Tone: bold gym-bro → **premium, sophisticated, formal-but-warm**
- Emoji-heavy → **proper iconography** (Lucide CDN, line-stroke, sophisticated)
- Add: AI Fitness Assistant, Mobile App, Apple Watch HealthKit, Food Tracker w/ AI
- Add: Albanian/English toggle

## Layout fragments (sidebars)
**Admin sidebar groups:**
- MAIN: Dashboard, Members, Staff
- OPERATIONS: Plans, Payments, Equipment, Announcements
- INSIGHTS: Reports, Exercise Library, Door Kiosk, Database (H2)

**Trainer sidebar (TRAINER):** Dashboard, My Members, Measurements, PT Sessions, Exercise Library

**Client sidebar (MEMBER AREA):** Home, My Membership, My Progress, My PT, Exercises & Plans

**Topbar:** title (left) + user name + avatar (initial) + Logout button

## Admin home structure
- 4 stat cards (Total Members, Active Members[success], Check-ins Today[warning], Total Revenue[purple])
- 2-col grid: Revenue trend (line chart, span 2) + Member status (doughnut)
- Top spenders table
- Quick actions row of buttons (Manage members, Open Door Kiosk, Full reports, Download Word report)
- Uses Chart.js with brand colors

## Client home structure
- Welcome alert banner with door PIN
- 3 stat cards (Plan, Status[success], Expires on[warning])
- Latest announcements list (alert-info style)

## Login page
- Centered auth-card on dark gradient
- FitManager logo, subtitle, error alert (conditional)
- email + password inputs, Sign In button
- "Back to home" link
- Demo accounts panel (admin/trainer/client)

## Door Kiosk
- Full-screen dark gradient
- Big "Fit**Manager**" logo, "Welcome to the gym"
- Center card with state: pulse (waiting), success (green glow), bye (orange glow), error (red glow)
- 96px emoji icons (📡 ✅ 👋 ⚠️)
- Live clock top-right (tabular-nums)
- RFID reader = keyboard input. Buffer keystrokes, ends on Enter
- POST /api/members/checkin-rfid → returns action checkin/checkout
- Auto-resets to scan state after 5s

## Admin/members page
- "Add new member" form-card: name, email, phone, plan select, RFID code (10 digits)
- Members table: Name, Email, Phone, PIN, RFID badge, Plan, Status badge, Expiry, [Assign Card][Delete] actions
- Modal for "Assign RFID card" with scan input + Save/Cancel

## Trainer home
- Welcome alert showing trainer specialty
- 3 stat cards: Total Members, My PT Subscriptions[success], Sessions Recorded[warning]
- "My PT clients" table: Member, Package, Used, Remaining (bold), Status, [Open]
- "My recent sessions" table: Date, Member, Type[info badge], Notes

## Client/progress page
- "Body measurements over time" table: Date, Weight, Height, BMI, Body fat %, Notes
- BMI Calculator card: Weight + Height inputs → POST /api/bmi → alert-info result with BMI, category, advice

## Admin/exercises page
- "Add new exercise" form: name, muscle group select (Chest/Back/Legs/Shoulders/Arms/Core/Full Body/Cardio), difficulty (Beginner/Intermediate/Advanced), equipment, image URL (Unsplash), sets x reps, instructions textarea
- Exercises grid (.exercise-grid) of .exercise-card with image header + name + badges (muscle group [info], difficulty [purple], setsReps) + instructions + Delete
- Workout Plans grid (.plan-grid) of .plan-tile with .plan-hero (color-coded: beginner=green, intermediate=amber, advanced=red) showing name + "level • goal", body with description + exercise count

## Admin/payments page
- 2 stat cards (Revenue 30d [success], Total payments)
- All payments table: Date, Member, Amount (bold $), Method, Status [success badge], Description

## Endpoints used
- POST /api/members/checkin-rfid
- POST /api/members/{id}/rfid (assign card)
- POST /api/bmi
- GET /api/stats/summary, /api/stats/revenue-by-month
- GET /admin/reports/download (Word export)

## Demo accounts (use these in member screens)
- admin@gym.com / admin123
- trainer@gym.com / trainer123
- maria@gym.com / client123

## What we're KEEPING (architectural)
- Dashboard 240px sidebar + topbar layout pattern
- Three-role split (admin/trainer/member)
- Component vocabulary: stat-card, plan-tile, exercise-card, badge, btn, alert, sidebar-link
- Section structure on landing (hero, features, about, plans, testimonials, contact, footer)
- Login + kiosk surfaces
