# Forge — Gym Management System

[![CI](https://github.com/Enireida/forge-gym-management/actions/workflows/ci.yml/badge.svg)](https://github.com/Enireida/forge-gym-management/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F)

**Most gyms in Albania still run on paper notebooks and WhatsApp. Forge replaces that with one web app for the whole gym: front desk, trainers and members.**

Built solo by [Enireida Reci](https://www.linkedin.com/in/enireida-reci-b900b1340/) as a Spring Boot monolith: ~13k lines of Java, 28 JPA entities, 19 controllers, and a bilingual (English / Albanian) server-rendered UI.

---

## What it does

| Portal | Main features |
|---|---|
| **Admin** | Member registration with webcam photo · membership plans & payments with automatic expiry · class scheduling with waitlists · staff & equipment · analytics dashboard (Chart.js) · downloadable `.docx` reports (Apache POI) |
| **Trainer** | Weekly availability calendar · personal-training session log · exercise library · client progress and measurements |
| **Member** | Workout logging (manual or CSV import from Samsung Health, Fitbit, Garmin, Strava, Google Fit) · wearable sync (Strava / Google Fit / Fitbit OAuth2, Apple Watch REST endpoint) · food & water log · body measurements · class and PT booking · data-driven coach chat |
| **Kiosk** | Door check-in at `/kiosk` with a 4-digit PIN or RFID card code |

**Security:** BCrypt (cost 12) password hashing · email OTP for sign-up, login and password reset · CSRF interceptor on all state-changing requests · role-based route guards (ADMIN / TRAINER / MEMBER).

> The coach is rule-based: it answers using the member's own measurements, workouts, attendance and PT history. Members can only see their own data; trainers can ask about their assigned clients.

## Tech stack

- **Backend:** Java 17 (records, sealed interfaces, pattern matching), Spring Boot 3.2.5 — Web MVC + REST, Data JPA, Validation, Mail
- **Data:** H2 (file-based, PostgreSQL-compatible SQL), Spring Data JPA, raw `JdbcTemplate` for analytics queries
- **Frontend:** Thymeleaf templates, Chart.js — no separate build step
- **Integrations:** Strava, Google Fit, Fitbit (OAuth2), Gmail SMTP, Apache POI
- **Testing / CI:** JUnit 5, Mockito, GitHub Actions

## Design decisions

| Pattern | Where | Why |
|---|---|---|
| Observer / event bus | `pattern/EventBus` | Check-ins, payments and bookings trigger notifications without the services knowing about each other |
| Strategy | `pattern/PricingStrategy` | Standard, student, corporate and family pricing; a new rule doesn't touch existing code |
| Factory | `pattern/NotificationFactory` | One entry point for email, push and in-app notifications |
| Builder | `pattern/PaymentReceiptBuilder` | Readable construction of complex receipts |

Other choices:
- **Raw JDBC for reports.** Analytics queries use `JdbcTemplate` (`jdbc/RawReportDao`) where JPQL would be awkward and slow.
- **No full Spring Security.** Only the crypto module is used; sessions and role guards are handled explicitly so the auth flow is easy to follow.
- **Runs with zero setup.** Without email or OAuth credentials, email falls back to console logging and wearable sync shows a clear "not configured" message.

## Run it locally

Requirements: Java 17+. No database to install.

```bash
git clone https://github.com/Enireida/forge-gym-management.git
cd forge-gym-management
./mvnw spring-boot:run
```

Open http://localhost:8080. Demo accounts (password `password`):

| Role | Email |
|---|---|
| Admin | `admin@forge.al` |
| Trainer | `trainer@forge.al` |
| Member | `member@forge.al` |

Sample CSV files for the workout import are in [`data/sample-imports`](data/sample-imports).

<details>
<summary>Optional: email and wearable credentials</summary>

Set these as environment variables, or put them in `src/main/resources/application-local.properties` (gitignored):

| Variable | Purpose |
|---|---|
| `FORGE_MAIL_USERNAME`, `FORGE_MAIL_PASSWORD`, `FORGE_MAIL_FROM`, `FORGE_MAIL_ADMIN` | Gmail SMTP for OTP and booking emails |
| `FORGE_MAIL_DEV_MODE=true` | Log emails instead of sending them |
| `STRAVA_CLIENT_ID`, `STRAVA_CLIENT_SECRET` | Strava import |
| `GOOGLEFIT_CLIENT_ID`, `GOOGLEFIT_CLIENT_SECRET` | Google Fit import |
| `FITBIT_CLIENT_ID`, `FITBIT_CLIENT_SECRET` | Fitbit sync |

The H2 console is at `/h2-console` (JDBC URL `jdbc:h2:file:~/.forge/data/gymdb`, user `sa`, empty password).
</details>

## Tests

```bash
./mvnw test
```

30 unit tests in 5 classes cover the pricing strategies, event bus, receipt builder, pagination and top-N utilities. They run without Spring or a database, and GitHub Actions runs them on every push.

## Project structure

```
src/main/java/com/unyt/gym/
├── config/          MVC config, CSRF interceptor, password encoder, data seeding
├── controller/api/  REST endpoints (coach, BMI, members, stats, wearable sync)
├── controller/web/  Thymeleaf controllers (admin, trainer, member, kiosk, auth, reports)
├── entity/          JPA entities centred on Member
├── repository/      Spring Data repositories
├── service/         Business logic (auth/OTP, payments, attendance, PT, coach, reports, integrations)
├── jdbc/            RawReportDao — analytics via JdbcTemplate
├── pattern/         Observer, Strategy, Factory, Builder
├── generic/         PaginatedResult<T>, TopNFinder
└── exception/       Domain exceptions + global handler
```

## Roadmap

- Integration tests for the payment and check-in flows (`@SpringBootTest` + Testcontainers)
- PostgreSQL profile and Docker image
- Public demo deployment

---

Originally built as my graduation project in Software Engineering at the University of New York Tirana (2026).
