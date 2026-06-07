...................................................................................
                 FORGE
            Gym Management System
           Author: Enireida Reci
...................................................................................

"Most Albanian gyms still run on handwritten notebooks and WhatsApp.
 Forge is built to change that."

...................................................................................
WHAT IS FORGE?
...................................................................................

Forge is a full-stack gym management platform built in Java 17 /
Spring Boot 3.2.5. It covers every corner of a gym's daily operations:
member registration with webcam photo, subscription & payment tracking,
RFID door kiosk check-in, personal trainer scheduling, group fitness
class booking, multi-source workout logging, food & water logging,
body measurement tracking, and a context-aware AI fitness coach.

Three distinct portals live inside the same application, each
with its own dashboard and navigation:

  * Admin      — full visibility over members, staff, plans, classes,
                 equipment, payments, analytics, and reports.
  * Trainer    — availability calendar, PT session log, exercise
                 library, progress notes for assigned clients.
  * Member     — personal health hub: workouts, food, measurements,
                 class bookings, PT sessions, AI coach, wearables.

The UI is bilingual (English / Albanian) and ships a dark-themed
Thymeleaf frontend — no separate build step required.

...................................................................................
QUICK START
...................................................................................

Prerequisites: Java 17+, Maven 3.9+. No database installation needed.

1. Clone the repository and enter the directory.

2. (Optional) For real email and wearable integrations, copy
   src/main/resources/application-local.properties.example and fill
   in your credentials. The app runs without these — email falls back
   to console logging, wearable sync shows a "not configured" message.

3. Start the application:

      mvn spring-boot:run

   On first run, Hibernate creates all tables and data.sql seeds
   demo accounts. The database is stored at:

      ~/.forge/data/gymdb   (outside OneDrive to avoid lock-file issues)

4. Open http://localhost:8080

   Demo accounts (password: "password" for all):

      admin@forge.al     — Administrator
      trainer@forge.al   — Trainer
      member@forge.al    — Member

5. H2 console: http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:file:~/.forge/data/gymdb
   User: sa  |  Password: (empty)

...................................................................................
TECH STACK
...................................................................................

  Java 17               Records, sealed interfaces, pattern matching,
                        text blocks.

  Spring Boot 3.2.5     Web (MVC + REST), Data JPA, JDBC, Thymeleaf,
                        Validation, Mail, DevTools.

  H2 (file-based)       Persisted database in LEGACY mode.
                        AUTO_SERVER=TRUE allows hot-reload without
                        "already in use" errors. SQL is portable to
                        PostgreSQL via a one-line config change.

  Spring Data JPA       ORM layer — 20+ entity classes centred on Member.
  Raw JDBC (JdbcTemplate) Analytics queries (RawReportDao) where JPQL
                        would lose its dignity.

  Thymeleaf             Server-side HTML rendering. Bilingual templates
                        (English / Albanian). No bundler, no Node.js.

  Lombok                Reduces boilerplate — @Data, @Builder, etc.

  Apache POI 5.2.5      Generates downloadable .docx report files from
                        the admin analytics screen.

  Spring Security Crypto BCrypt password hashing. Full Spring Security
                        is not pulled in — session cookies are managed
                        manually to keep the configuration transparent.

  Spring Mail           Gmail SMTP for email OTP verification,
                        forgot-password, and booking confirmations.
                        Credentials come from environment variables.

  Strava OAuth2         Import recent activities via the Strava API
  Google Fit OAuth2     Import workouts from Google Fit
  Fitbit OAuth2         Sync workouts and health metrics from Fitbit
  Apple Watch REST      Device-agnostic REST endpoint for HealthKit push

  JUnit 5 + Mockito     Unit tests for all design pattern classes and
                        generic utilities (5 test classes, 25+ cases).

...................................................................................
FEATURE OVERVIEW
...................................................................................

ADMIN PORTAL
  - Member CRUD with live webcam photo capture on registration
  - Membership plan management (plan types, durations, pricing)
  - Payment recording with automatic expiry calculation
  - Staff management (trainers + support staff)
  - Group fitness class scheduling and waitlist management
  - Equipment inventory tracking
  - Announcement broadcast to all members
  - Push notifications to individual members or all
  - Admin analytics dashboard (Chart.js) — revenue, attendance,
    top classes, membership trends
  - Downloadable .docx report via Apache POI

TRAINER PORTAL
  - Weekly availability calendar for PT slot management
  - Personal Training session log with notes
  - Exercise library (add / edit custom exercises)
  - View body measurements and progress for assigned clients
  - Message assigned clients

MEMBER PORTAL
  - AI Fitness Coach — context-aware conversational assistant built on
    the member's actual data (measurements, workouts, attendance, PT).
    Trainers can also query their own clients by name.
  - Workout logging: manual entry or CSV import (Samsung Health,
    Google Fit, Fitbit, Garmin, Strava export formats supported via
    CsvFieldMapper)
  - Wearable sync: Fitbit, Google Fit, Strava (OAuth2), Apple Watch
    (REST push endpoint)
  - Food log with per-food calorie calculation (FoodCalorieHelper)
  - Water intake tracker
  - Body measurement tracking with trend view
  - Fitness goals (create and track progress)
  - Group fitness class booking (with waitlist if full)
  - PT booking — browse trainer availability and request sessions
  - Notification preferences and inbox
  - Membership details and payment history

AUTHENTICATION & SECURITY
  - Email OTP verification on registration (6-digit code, 15-min TTL)
  - Login OTP option for extra security
  - Forgot-password via emailed OTP
  - BCrypt-hashed passwords (cost 12)
  - CSRF token interceptor on all state-changing requests
  - Role-based route guarding (ADMIN / TRAINER / MEMBER)

KIOSK
  - RFID door kiosk at /kiosk — members enter a 4-digit PIN or a
    10-digit card code to log attendance

...................................................................................
DESIGN PATTERNS
...................................................................................

Pattern               Class                       Why
--------------------  --------------------------  ----------------------
Observer / EventBus   pattern/EventBus.java        Decoupled member-event
                                                   notifications (check-in,
                                                   payment, class booking)
                                                   across multiple listeners.

Factory               pattern/NotificationFactory  Builds Notification
                                                   objects of different
                                                   types (EMAIL, PUSH,
                                                   IN_APP) from a single
                                                   factory method.

Builder               pattern/PaymentReceiptBuilder Fluent construction of
                                                   complex payment receipt
                                                   objects — avoids
                                                   telescoping constructors.

Strategy              pattern/PricingStrategies    Pluggable pricing rules
                      pattern/PricingStrategy      (standard, student,
                                                   corporate, family).
                                                   New rules add without
                                                   touching existing code.

Generic utilities:
  generic/PaginatedResult<T>   — type-safe paginated result wrapper
                                 used by REST endpoints.
  generic/TopNFinder           — finds the top-N items from any list
                                 without sorting the whole collection.

...................................................................................
PROJECT STRUCTURE
...................................................................................

src/main/java/com/unyt/gym/
  config/           Spring config — MVC, password encoder, CSRF,
                    startup data seeding, event listener registration
  controller/api/   REST controllers (AI coach, BMI, members,
                    stats, wearable sync)
  controller/web/   Thymeleaf controllers (admin, trainer, member,
                    kiosk, auth, reports, integrations)
  entity/           20+ JPA entities centred on Member
  repository/       Spring Data repositories (one per entity)
  service/          Business logic — auth, AI coach, attendance,
                    payments, PT, email/OTP, wearable sync,
                    report generation, notifications
  jdbc/             RawReportDao — analytics queries via JdbcTemplate
  pattern/          Design pattern implementations (see above)
  generic/          PaginatedResult<T>, TopNFinder
  dto/              BmiRequest / BmiResponse (REST DTOs)
  exception/        Domain exceptions + GlobalExceptionHandler
  util/             CsvFieldMapper — maps CSV columns to workout fields

src/main/resources/
  application.properties      Main config (H2, mail, OAuth2 keys)
  application-local.properties (gitignored) — real credentials
  data.sql                    Idempotent seed data (MERGE statements)
  templates/                  Thymeleaf HTML (admin/, trainer/, client/,
                              fragments/)
  static/                     CSS, JS, uploads, design system preview

src/test/java/com/unyt/gym/
  EventBusTest                Observer fire-and-receive verification
  PaymentReceiptBuilderTest   Builder chain + field assertion
  PricingStrategiesTest       All pricing rule branches
  PaginatedResultTest         Pagination slice correctness
  TopNFinderTest              Top-N vs brute-force sort comparison

...................................................................................
ENVIRONMENT VARIABLES
...................................................................................

The following variables are read from the OS environment (or
application-local.properties, which is gitignored):

  FORGE_MAIL_USERNAME    Gmail address used to send OTP emails
  FORGE_MAIL_PASSWORD    16-character Gmail App Password
  FORGE_MAIL_FROM        Display "From" address
  FORGE_MAIL_ADMIN       Admin alert address
  FORGE_MAIL_DEV_MODE    Set to "true" to log emails instead of sending

  STRAVA_CLIENT_ID       From https://www.strava.com/settings/api
  STRAVA_CLIENT_SECRET

  GOOGLEFIT_CLIENT_ID    From Google Cloud Console (Fitness API enabled)
  GOOGLEFIT_CLIENT_SECRET

  FITBIT_CLIENT_ID       From https://dev.fitbit.com/apps/new
  FITBIT_CLIENT_SECRET

The app starts and runs fully without any of these set. Email falls
back to console logging; wearable sync returns a friendly error page.

...................................................................................
RUNNING THE TESTS
...................................................................................

  mvn test

All five test classes are pure unit tests (no Spring context, no
database). They run in under two seconds.

...................................................................................
DONE
...................................................................................