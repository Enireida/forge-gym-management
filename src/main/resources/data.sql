-- ============================================================
-- Forge - Seed data  (MERGE = idempotent, safe to re-run)
-- Loaded automatically by Spring Boot after schema creation.
-- ============================================================

-- Users
MERGE INTO users (id, name, email, password, role, created_at) KEY(id) VALUES
  (1, 'Admin User',   'admin@gym.com',   'admin123',   'ADMIN',   CURRENT_TIMESTAMP),
  (2, 'John Trainer', 'trainer@gym.com', 'trainer123', 'TRAINER', CURRENT_TIMESTAMP),
  (3, 'Maria Member', 'maria@gym.com',   'client123',  'CLIENT',  CURRENT_TIMESTAMP),
  (4, 'Erion Hoxha',  'erion@gym.com',   'client123',  'CLIENT',  CURRENT_TIMESTAMP),
  (5, 'Anna Trainer', 'anna@gym.com',    'trainer123', 'TRAINER', CURRENT_TIMESTAMP);

-- Membership plans  (allow_rfid + allow_ai_coach gate the door-card and AI Coach features)
MERGE INTO membership_plans (id, name, description, price, duration_months, is_active, allow_rfid, allow_ai_coach) KEY(id) VALUES
  (1, 'Basic',    'Gym access with 4-digit PIN entry',                                29.99,  1, TRUE, FALSE, FALSE),
  (2, 'Standard', 'Basic + RFID card entry, sauna and locker',                        49.99,  1, TRUE, TRUE,  FALSE),
  (3, 'Premium',  'Standard + AI Coach + 2 PT sessions / month',                      79.99,  1, TRUE, TRUE,  TRUE),
  (4, 'Annual',   'One full year, all Premium perks included',                       499.00, 12, TRUE, TRUE,  TRUE),
  (5, 'Student',  '20% off Standard with RFID, requires student ID (no AI Coach)',    39.99,  1, TRUE, TRUE,  FALSE);

-- Staff  (bio, certifications and photo_data now populated for trainer profiles)
MERGE INTO staff (id, name, email, phone, role, specialty, salary, hire_date, status, user_id, bio, certifications, photo_data) KEY(id) VALUES
  (1, 'John Trainer',  'trainer@gym.com', '+355691234567', 'Trainer',      'Strength training', 1200.00, '2024-01-15', 'Active', 2,
      'Powerlifting champion with 8+ years coaching beginners to advanced lifters. Loves the squat rack and helping members hit their first 100kg deadlift.',
      'NSCA-CPT, Precision Nutrition L1, Powerlifting Coach',
      'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=400&fit=crop'),
  (2, 'Anna Trainer',  'anna@gym.com',    '+355691234568', 'Trainer',      'Yoga and pilates',  1100.00, '2024-03-01', 'Active', 5,
      'Certified yoga and pilates instructor with a calm, technique-first approach. Specialises in mobility, posture correction and stress relief.',
      'RYT-500 Yoga, Pilates Mat L2, Mobility Specialist',
      'https://images.unsplash.com/photo-1594381898411-846e7d193883?w=400&h=400&fit=crop'),
  (3, 'Mark Reception','mark@gym.com',    '+355691234569', 'Receptionist', NULL,                 800.00, '2024-02-10', 'Active', NULL, NULL, NULL, NULL),
  (4, 'Sara Manager',  'sara@gym.com',    '+355691234570', 'Manager',      NULL,                1800.00, '2023-09-01', 'Active', NULL, NULL, NULL, NULL);

-- Members
MERGE INTO members (id, name, email, phone, pin, rfid_code, plan_id, user_id, status, start_date, expiry_date, weight_kg, height_cm, created_at) KEY(id) VALUES
  (1, 'Maria Member', 'maria@gym.com',  '+355681111111', '1001', '0123456789', 3, 3, 'ACTIVE',  DATEADD(MONTH,-1,CURRENT_DATE), DATEADD(MONTH,5,CURRENT_DATE),  65.5, 168, CURRENT_TIMESTAMP),
  (2, 'Erion Hoxha',  'erion@gym.com',  '+355682222222', '1002', '0123456790', 2, 4, 'ACTIVE',  DATEADD(MONTH,-2,CURRENT_DATE), DATEADD(MONTH,4,CURRENT_DATE),  82.0, 180, CURRENT_TIMESTAMP),
  (3, 'Klara Bardhi', 'klara@gym.com',  '+355683333333', '1003', '0123456791', 1, NULL,'ACTIVE', DATEADD(DAY,-10,CURRENT_DATE),  DATEADD(DAY,20,CURRENT_DATE),   58.0, 162, CURRENT_TIMESTAMP),
  (4, 'Bledar Kola',  'bledar@gym.com', '+355684444444', '1004', '0123456792', 4, NULL,'ACTIVE', DATEADD(MONTH,-3,CURRENT_DATE), DATEADD(MONTH,9,CURRENT_DATE),  90.0, 185, CURRENT_TIMESTAMP),
  (5, 'Vera Lala',    'vera@gym.com',   '+355685555555', '1005', '0123456793', 5, NULL,'ACTIVE', DATEADD(DAY,-25,CURRENT_DATE),  DATEADD(DAY,5,CURRENT_DATE),    55.0, 170, CURRENT_TIMESTAMP),
  (6, 'Toni Doci',    'toni@gym.com',   '+355686666666', '1006', '0123456794', 1, NULL,'EXPIRED',DATEADD(MONTH,-3,CURRENT_DATE), DATEADD(MONTH,-1,CURRENT_DATE), 78.0, 175, CURRENT_TIMESTAMP);

-- Equipment
MERGE INTO equipment (id, name, category, quantity, status, purchase_date, last_maintenance, notes) KEY(id) VALUES
  (1, 'Treadmill',     'Cardio',    8, 'Operational', '2023-06-01', '2025-01-15', 'Brand: TechnoGym'),
  (2, 'Bench Press',   'Strength',  4, 'Operational', '2023-05-10', NULL,         NULL),
  (3, 'Dumbbell Set',  'Strength', 30, 'Operational', '2023-04-01', NULL,         '5kg-50kg pairs'),
  (4, 'Rowing Machine','Cardio',    3, 'Maintenance',  '2024-02-20', '2025-04-01','Cable replacement scheduled'),
  (5, 'Squat Rack',    'Strength',  2, 'Operational', '2024-01-10', NULL,         NULL);

-- PT packages
MERGE INTO pt_packages (id, name, sessions, price, is_active, description) KEY(id) VALUES
  (1, 'Starter (5 sessions)',  5,  149.00, TRUE, 'Perfect for beginners'),
  (2, 'Pro (10 sessions)',    10,  279.00, TRUE, 'Best value'),
  (3, 'Elite (20 sessions)',  20,  499.00, TRUE, 'For serious athletes');

-- PT subscriptions
MERGE INTO pt_subscriptions (id, member_id, trainer_id, package_id, sessions_remaining, sessions_used, start_date, status) KEY(id) VALUES
  (1, 1, 1, 2, 7, 3, DATEADD(WEEK,-3,CURRENT_DATE), 'Active'),
  (2, 2, 2, 1, 4, 1, DATEADD(WEEK,-1,CURRENT_DATE), 'Active');

-- PT sessions
MERGE INTO pt_sessions (id, subscription_id, trainer_id, session_date, session_time, type, notes) KEY(id) VALUES
  (1, 1, 1, DATEADD(DAY,-14,CURRENT_DATE), '17:00:00', 'Strength', 'Upper body, focus on bench press and rows'),
  (2, 1, 1, DATEADD(DAY,-7, CURRENT_DATE), '17:00:00', 'Strength', 'Lower body, squats and deadlift technique'),
  (3, 1, 1, DATEADD(DAY,-2, CURRENT_DATE), '17:30:00', 'HIIT',     '20-min HIIT followed by core circuit'),
  (4, 2, 2, DATEADD(DAY,-3, CURRENT_DATE), '18:00:00', 'Yoga',     'Mobility and flexibility session');

-- Payments
MERGE INTO payments (id, member_id, amount, method, status, description, date, created_at) KEY(id) VALUES
  (1, 1, 79.99,  'Card',     'Completed', 'Premium plan',        DATEADD(DAY,-30,CURRENT_DATE), CURRENT_TIMESTAMP),
  (2, 1, 279.00, 'Card',     'Completed', 'PT Pro 10 sessions',  DATEADD(DAY,-21,CURRENT_DATE), CURRENT_TIMESTAMP),
  (3, 2, 49.99,  'Cash',     'Completed', 'Standard plan',       DATEADD(DAY,-60,CURRENT_DATE), CURRENT_TIMESTAMP),
  (4, 3, 29.99,  'Cash',     'Completed', 'Basic plan',          DATEADD(DAY,-10,CURRENT_DATE), CURRENT_TIMESTAMP),
  (5, 4, 499.00, 'Transfer', 'Completed', 'Annual plan',         DATEADD(DAY,-90,CURRENT_DATE), CURRENT_TIMESTAMP),
  (6, 5, 39.99,  'Card',     'Completed', 'Student plan',        DATEADD(DAY,-25,CURRENT_DATE), CURRENT_TIMESTAMP),
  (7, 1, 79.99,  'Card',     'Completed', 'Premium renewal',     DATEADD(DAY,-2, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Attendance
MERGE INTO attendance (id, member_id, date, check_in, check_out) KEY(id) VALUES
  (1, 1, CURRENT_DATE,                  '08:30:00', '10:00:00'),
  (2, 2, CURRENT_DATE,                  '09:00:00', '10:30:00'),
  (3, 3, CURRENT_DATE,                  '17:30:00', NULL),
  (4, 1, DATEADD(DAY,-1,CURRENT_DATE),  '08:00:00', '09:30:00'),
  (5, 4, DATEADD(DAY,-1,CURRENT_DATE),  '18:00:00', '19:30:00');

-- Measurements
MERGE INTO measurements (id, member_id, date, weight_kg, height_cm, body_fat_pct, muscle_mass, chest, waist, hips, notes) KEY(id) VALUES
  (1, 1, DATEADD(MONTH,-2,CURRENT_DATE), 68.0, 168, 24.0, 28.0, 92, 72, 95, 'Initial assessment'),
  (2, 1, DATEADD(MONTH,-1,CURRENT_DATE), 66.0, 168, 22.5, 29.0, 91, 70, 94, 'Good progress'),
  (3, 1, CURRENT_DATE,                   65.5, 168, 21.0, 30.0, 90, 68, 93, 'On track'),
  (4, 2, DATEADD(MONTH,-1,CURRENT_DATE), 84.0, 180, 18.0, 38.0,100, 82, 95, 'Starting'),
  (5, 2, CURRENT_DATE,                   82.0, 180, 16.5, 40.0,102, 80, 95, 'Lean and gain');

-- Exercises
MERGE INTO exercises (id, name, muscle_group, difficulty, equipment, instructions, image_url, sets_reps, rest_seconds) KEY(id) VALUES
  (1,  'Bench Press',     'Chest',     'Intermediate','Barbell',   'Lie on bench, lower bar to chest under control, then press it back up.',                                   'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600','4 x 8-10',90),
  (2,  'Back Squat',      'Legs',      'Intermediate','Barbell',   'Bar across upper back, descend until thighs are parallel, drive through heels to stand.',                  'https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=600','5 x 5',   120),
  (3,  'Deadlift',        'Back',      'Advanced',    'Barbell',   'Hinge at the hips, keep neutral spine, lift bar by extending hips and knees together.',                   'https://images.unsplash.com/photo-1517344884509-a0c97ec11bcc?w=600','5 x 3',   180),
  (4,  'Pull-up',         'Back',      'Intermediate','Bar',       'Hang from bar with overhand grip, pull chin above bar, lower under control.',                              'https://images.unsplash.com/photo-1598971639058-fab3c3109a00?w=600','3 x AMRAP',90),
  (5,  'Push-up',         'Chest',     'Beginner',    'None',      'Plank position, lower chest to floor keeping body straight, then press up.',                              'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=600','3 x 12-15',60),
  (6,  'Plank',           'Core',      'Beginner',    'None',      'Forearms on floor, body straight from head to heels, brace core and hold.',                               'https://images.unsplash.com/photo-1599058917765-a780eda07a3e?w=600','3 x 45 sec',45),
  (7,  'Dumbbell Curl',   'Arms',      'Beginner',    'Dumbbells', 'Curl weights to shoulders bending elbows, keep elbows close to torso.',                                   'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=600','3 x 10-12',60),
  (8,  'Shoulder Press',  'Shoulders', 'Intermediate','Dumbbells', 'Press dumbbells overhead from shoulder height until arms fully extended, lower under control.',            'https://images.unsplash.com/photo-1652363722833-509b3aac287b?w=600','4 x 8-10',90),
  (9,  'Lat Pulldown',    'Back',      'Beginner',    'Cable',     'Sit at machine, pull bar to upper chest while squeezing shoulder blades.',                                 'https://images.unsplash.com/photo-1581122584612-713f89daa8eb?w=600','4 x 10-12',75),
  (10, 'Treadmill Run',   'Cardio',    'Beginner',    'Treadmill', 'Warm up walking, then run at conversational pace for 20-40 minutes.',                                      'https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=600','20-40 min',0),
  (11, 'Lunges',          'Legs',      'Beginner',    'Bodyweight','Step forward, lower back knee toward floor keeping front knee over ankle, push back to start.',            'https://images.unsplash.com/photo-1434608519344-49d77a699e1d?w=600','3 x 12 each',60),
  (12, 'Russian Twist',   'Core',      'Intermediate','Plate',     'Sit with knees bent, lean back slightly, twist torso side to side holding plate.',                        'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?w=600','3 x 20',   45),
  (13, 'Romanian Deadlift','Legs',     'Intermediate','Barbell',   'Bar in front of thighs, hinge at hips with slight knee bend, lower to mid-shin, drive hips forward.',     'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=600','4 x 8-10',90),
  (14, 'Burpee',          'Full Body', 'Advanced',    'None',      'Squat, kick to plank, push-up, jump feet forward, jump up. Repeat.',                                      'https://images.unsplash.com/photo-1599058917212-d750089bc07e?w=600','4 x 10',   60),
  (15, 'Bike Sprint',     'Cardio',    'Intermediate','Bike',      '30s all-out sprint, 60s easy pedalling. Repeat 8 rounds.',                                                 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=600','8 rounds', 60);

-- Workout plans
MERGE INTO workout_plans (id, name, level, goal, duration_weeks, sessions_per_week, description, image_url) KEY(id) VALUES
  (1,'Beginner Full-Body',   'Beginner',    'General fitness',8, 3,'A gentle full-body programme for newcomers.',        'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=900'),
  (2,'Strength Hypertrophy', 'Intermediate','Muscle gain',   12, 4,'Compound lifts with progressive overload.',          'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=900'),
  (3,'Cardio Burner',        'Beginner',    'Weight loss',    6, 4,'High-intensity cardio with mobility work.',          'https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=900'),
  (4,'Core Crusher',         'Intermediate','Core strength',  4, 3,'Daily 20-minute core circuits.',                     'https://images.unsplash.com/photo-1599058917765-a780eda07a3e?w=900'),
  (5,'Powerlifter Focus',    'Advanced',    'Maximum strength',16,4,'Squat / bench / deadlift periodisation.',           'https://images.unsplash.com/photo-1517344884509-a0c97ec11bcc?w=900');

-- Plan ↔ Exercise relationships (insert-only, no natural key so skip duplicates)
MERGE INTO workout_plan_exercises (plan_id, exercise_id) KEY(plan_id, exercise_id) VALUES
  (1,5),(1,11),(1,6),(1,7),(1,9),
  (2,1),(2,2),(2,3),(2,4),(2,8),(2,13),
  (3,10),(3,14),(3,15),(3,11),(3,6),
  (4,6),(4,12),(4,5),
  (5,1),(5,2),(5,3),(5,4),(5,13);

-- Announcements
MERGE INTO announcements (id, title, message, audience, priority, created_at) KEY(id) VALUES
  (1,'New equipment arrived!','Two new treadmills are now in the cardio area.',        'all',    'normal',CURRENT_TIMESTAMP),
  (2,'Holiday opening hours', 'On May 1st the gym will close at 14:00.',               'all',    'high',  CURRENT_TIMESTAMP),
  (3,'Yoga class added',      'Anna offers a new yoga class every Tuesday 18:00.',     'members','normal',CURRENT_TIMESTAMP);

-- ============================================================
-- Demo data for the rich-feature tables (so Maria can test
-- everything without entering anything by hand).
-- ============================================================

-- Trainer 1-on-1 slots (next ~2 weeks)
MERGE INTO trainer_slots (id, trainer_id, slot_date, start_time, end_time, status, notes, booked_by_member_id, created_at) KEY(id) VALUES
  (1, 1, DATEADD(DAY, 1,  CURRENT_DATE), '08:00:00', '09:00:00', 'AVAILABLE', 'Strength fundamentals',     NULL, CURRENT_TIMESTAMP),
  (2, 1, DATEADD(DAY, 1,  CURRENT_DATE), '17:00:00', '18:00:00', 'AVAILABLE', 'Hypertrophy session',       NULL, CURRENT_TIMESTAMP),
  (3, 1, DATEADD(DAY, 3,  CURRENT_DATE), '09:00:00', '10:00:00', 'AVAILABLE', 'Powerlifting technique',    NULL, CURRENT_TIMESTAMP),
  (4, 1, DATEADD(DAY, 5,  CURRENT_DATE), '18:00:00', '19:00:00', 'AVAILABLE', NULL,                        NULL, CURRENT_TIMESTAMP),
  (5, 1, DATEADD(DAY, 8,  CURRENT_DATE), '08:00:00', '09:00:00', 'AVAILABLE', 'Open session',              NULL, CURRENT_TIMESTAMP),
  (6, 2, DATEADD(DAY, 2,  CURRENT_DATE), '07:30:00', '08:30:00', 'AVAILABLE', 'Morning vinyasa flow',      NULL, CURRENT_TIMESTAMP),
  (7, 2, DATEADD(DAY, 4,  CURRENT_DATE), '18:30:00', '19:30:00', 'AVAILABLE', 'Mobility & stretching',     NULL, CURRENT_TIMESTAMP),
  (8, 2, DATEADD(DAY, 6,  CURRENT_DATE), '18:30:00', '19:30:00', 'AVAILABLE', 'Pilates mat',               NULL, CURRENT_TIMESTAMP),
  (9, 2, DATEADD(DAY, 9,  CURRENT_DATE), '07:30:00', '08:30:00', 'AVAILABLE', NULL,                        NULL, CURRENT_TIMESTAMP),
  (10,2, DATEADD(DAY, 11, CURRENT_DATE), '17:30:00', '18:30:00', 'AVAILABLE', 'Posture correction',        NULL, CURRENT_TIMESTAMP);

-- Group classes (next 2 weeks, a mix of trainers and categories)
MERGE INTO gym_classes (id, name, description, trainer_id, date_time, duration_minutes, capacity, category, active, created_at) KEY(id) VALUES
  (1, 'Morning Vinyasa Yoga', 'Energising flow to start your day with breath and movement.',                  2, DATEADD(DAY, 1,  CURRENT_TIMESTAMP), 60, 20, 'Yoga',     TRUE, CURRENT_TIMESTAMP),
  (2, 'Strength 101',         'Beginner-friendly strength circuit covering all main lifts.',                   1, DATEADD(DAY, 2,  CURRENT_TIMESTAMP), 60, 12, 'Strength', TRUE, CURRENT_TIMESTAMP),
  (3, 'HIIT Burn',            'High-intensity intervals to torch calories in 30 minutes.',                     1, DATEADD(DAY, 3,  CURRENT_TIMESTAMP), 30, 16, 'HIIT',     TRUE, CURRENT_TIMESTAMP),
  (4, 'Sunset Pilates',       'Mat pilates focused on core stability and posture.',                            2, DATEADD(DAY, 4,  CURRENT_TIMESTAMP), 50, 18, 'Pilates',  TRUE, CURRENT_TIMESTAMP),
  (5, 'Cardio Spin',          'Indoor cycling with music — climbs and sprints.',                               1, DATEADD(DAY, 5,  CURRENT_TIMESTAMP), 45, 14, 'Cardio',   TRUE, CURRENT_TIMESTAMP),
  (6, 'Power Yoga',           'Stronger, faster yoga flow with arm balances.',                                 2, DATEADD(DAY, 7,  CURRENT_TIMESTAMP), 60, 20, 'Yoga',     TRUE, CURRENT_TIMESTAMP),
  (7, 'Heavy Strength',       'Compound lift session — squat, bench, deadlift.',                               1, DATEADD(DAY, 9,  CURRENT_TIMESTAMP), 75,  8, 'Strength', TRUE, CURRENT_TIMESTAMP),
  (8, 'Stretch & Recover',    'Slow flow with mobility drills for active recovery days.',                      2, DATEADD(DAY, 11, CURRENT_TIMESTAMP), 45, 20, 'Yoga',     TRUE, CURRENT_TIMESTAMP);

-- Class bookings (Maria booked 2; Erion booked 1)
MERGE INTO class_bookings (id, member_id, gym_class_id, booked_at, status) KEY(id) VALUES
  (1, 1, 1, DATEADD(DAY,-1,CURRENT_TIMESTAMP), 'CONFIRMED'),
  (2, 1, 4, DATEADD(DAY,-2,CURRENT_TIMESTAMP), 'CONFIRMED'),
  (3, 2, 3, DATEADD(DAY,-1,CURRENT_TIMESTAMP), 'CONFIRMED');

-- Class ratings (Maria left 2 reviews for past classes; Erion left 1)
MERGE INTO class_ratings (id, class_id, member_id, rating, comment, created_at) KEY(id) VALUES
  (1, 1, 1, 5, 'Loved the breathing cues, Anna is amazing!', DATEADD(DAY,-3,CURRENT_TIMESTAMP)),
  (2, 4, 1, 4, 'Tough but rewarding pilates session.',        DATEADD(DAY,-5,CURRENT_TIMESTAMP)),
  (3, 1, 2, 5, 'Perfect way to start the morning.',           DATEADD(DAY,-3,CURRENT_TIMESTAMP));

-- Class waitlist (Klara is waiting for Heavy Strength which has limited capacity)
MERGE INTO class_waitlist (id, class_id, member_id, position, notified, created_at) KEY(id) VALUES
  (1, 7, 3, 1, FALSE, DATEADD(DAY,-1,CURRENT_TIMESTAMP));

-- Workout log for Maria (id=1): mix of types, last ~3 weeks, builds a current streak
MERGE INTO workout_log (id, member_id, workout_type, start_time, end_time, duration_seconds, active_calories, total_calories, avg_heart_rate, max_heart_rate, distance_meters, step_count, source, notes, synced_at) KEY(id) VALUES
  (1,  1, 'STRENGTH', DATEADD(HOUR,-1,CURRENT_TIMESTAMP),                              CURRENT_TIMESTAMP,                                 3600, 320, 380, 132, 158, NULL,  3200, 'apple_watch', 'Upper body session',           CURRENT_TIMESTAMP),
  (2,  1, 'RUNNING',  DATEADD(DAY,-1,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-1,CURRENT_TIMESTAMP),                 2100, 380, 420, 150, 172, 5000,  6800, 'apple_watch', 'Easy 5km morning run',         CURRENT_TIMESTAMP),
  (3,  1, 'HIIT',     DATEADD(DAY,-2,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-2,CURRENT_TIMESTAMP),                 1500, 290, 330, 158, 178, NULL,  1800, 'apple_watch', 'Tabata circuit',               CURRENT_TIMESTAMP),
  (4,  1, 'YOGA',     DATEADD(DAY,-4,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-4,CURRENT_TIMESTAMP),                 3000, 180, 220,  95, 120, NULL,  1100, 'apple_watch', 'Recovery flow',                CURRENT_TIMESTAMP),
  (5,  1, 'STRENGTH', DATEADD(DAY,-5,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-5,CURRENT_TIMESTAMP),                 3900, 340, 400, 138, 160, NULL,  3300, 'apple_watch', 'Lower body, squats PR',        CURRENT_TIMESTAMP),
  (6,  1, 'CARDIO',   DATEADD(DAY,-7,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-7,CURRENT_TIMESTAMP),                 2400, 360, 400, 145, 168, 8000,  5400, 'apple_watch', 'Spin class',                   CURRENT_TIMESTAMP),
  (7,  1, 'STRENGTH', DATEADD(DAY,-9,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),              DATEADD(DAY,-9,CURRENT_TIMESTAMP),                 3300, 300, 350, 132, 156, NULL,  2900, 'apple_watch', 'Push day',                     CURRENT_TIMESTAMP),
  (8,  1, 'CYCLING',  DATEADD(DAY,-11,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-11,CURRENT_TIMESTAMP),                2700, 400, 440, 142, 165, 18000, 4200, 'apple_watch', 'Outdoor ride along the lake',  CURRENT_TIMESTAMP),
  (9,  1, 'HIIT',     DATEADD(DAY,-13,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-13,CURRENT_TIMESTAMP),                1500, 280, 310, 156, 175, NULL,  1900, 'apple_watch', 'Quick burnout',                CURRENT_TIMESTAMP),
  (10, 1, 'YOGA',     DATEADD(DAY,-14,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-14,CURRENT_TIMESTAMP),                3600, 200, 240,  92, 118, NULL,  1200, 'apple_watch', 'Hatha class',                  CURRENT_TIMESTAMP),
  (11, 1, 'STRENGTH', DATEADD(DAY,-16,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-16,CURRENT_TIMESTAMP),                4200, 360, 420, 138, 162, NULL,  3500, 'apple_watch', 'Pull day, deadlift focus',     CURRENT_TIMESTAMP),
  (12, 1, 'RUNNING',  DATEADD(DAY,-18,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-18,CURRENT_TIMESTAMP),                1800, 320, 360, 152, 170, 4500,  6100, 'apple_watch', 'Tempo run',                    CURRENT_TIMESTAMP),
  (13, 1, 'STRENGTH', DATEADD(DAY,-20,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-20,CURRENT_TIMESTAMP),                3600, 320, 380, 134, 158, NULL,  3000, 'apple_watch', 'Full body',                    CURRENT_TIMESTAMP),
  (14, 1, 'CARDIO',   DATEADD(DAY,-22,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-22,CURRENT_TIMESTAMP),                2700, 320, 370, 140, 162, 6500,  7000, 'apple_watch', 'Stairmaster + rowing',         CURRENT_TIMESTAMP),
  (15, 1, 'YOGA',     DATEADD(DAY,-25,DATEADD(HOUR,-1,CURRENT_TIMESTAMP)),             DATEADD(DAY,-25,CURRENT_TIMESTAMP),                3300, 190, 230,  94, 118, NULL,  1000, 'apple_watch', 'Yin yoga',                     CURRENT_TIMESTAMP);

-- Food logs for Maria today
MERGE INTO food_logs (id, member_id, log_date, meal_type, food_name, quantity_g, calories, protein_g, carbs_g, fat_g, created_at) KEY(id) VALUES
  (1, 1, CURRENT_DATE, 'Breakfast', 'Oats',          80,  300,  10, 56,  6,  DATEADD(HOUR,-8, CURRENT_TIMESTAMP)),
  (2, 1, CURRENT_DATE, 'Breakfast', 'Greek Yogurt', 150,  130,  14,  8,  4,  DATEADD(HOUR,-8, CURRENT_TIMESTAMP)),
  (3, 1, CURRENT_DATE, 'Breakfast', 'Banana',       120,  107,   1, 27,  0,  DATEADD(HOUR,-8, CURRENT_TIMESTAMP)),
  (4, 1, CURRENT_DATE, 'Lunch',     'Chicken Breast',180, 297,  56,  0,  6, DATEADD(HOUR,-4, CURRENT_TIMESTAMP)),
  (5, 1, CURRENT_DATE, 'Lunch',     'Rice',         150,  195,   4, 42,  1,  DATEADD(HOUR,-4, CURRENT_TIMESTAMP)),
  (6, 1, CURRENT_DATE, 'Lunch',     'Broccoli',     100,   34,   3,  7,  0,  DATEADD(HOUR,-4, CURRENT_TIMESTAMP)),
  (7, 1, CURRENT_DATE, 'Snack',     'Almonds',       30,  174,   6,  6, 15, DATEADD(HOUR,-2, CURRENT_TIMESTAMP)),
  (8, 1, CURRENT_DATE, 'Snack',     'Apple',        180,   94,   0, 25,  0,  DATEADD(HOUR,-2, CURRENT_TIMESTAMP)),
  -- Yesterday
  (9, 1, DATEADD(DAY,-1,CURRENT_DATE), 'Breakfast', 'Egg',          100, 155, 13, 1, 11, DATEADD(DAY,-1,CURRENT_TIMESTAMP)),
  (10,1, DATEADD(DAY,-1,CURRENT_DATE), 'Lunch',     'Salmon',       150, 309, 31, 0, 20, DATEADD(DAY,-1,CURRENT_TIMESTAMP)),
  (11,1, DATEADD(DAY,-1,CURRENT_DATE), 'Lunch',     'Pasta',        120, 158, 6,  31, 1, DATEADD(DAY,-1,CURRENT_TIMESTAMP)),
  (12,1, DATEADD(DAY,-1,CURRENT_DATE), 'Dinner',    'Lentils',      150, 174, 13, 30, 1, DATEADD(DAY,-1,CURRENT_TIMESTAMP));

-- Water logs for Maria (last 3 days)
MERGE INTO water_logs (id, member_id, log_date, glasses, ml_total, created_at) KEY(id) VALUES
  (1, 1, CURRENT_DATE,                  5, 1250, CURRENT_TIMESTAMP),
  (2, 1, DATEADD(DAY,-1,CURRENT_DATE),  8, 2000, DATEADD(DAY,-1,CURRENT_TIMESTAMP)),
  (3, 1, DATEADD(DAY,-2,CURRENT_DATE),  6, 1500, DATEADD(DAY,-2,CURRENT_TIMESTAMP));

-- Fitness goals for Maria
MERGE INTO fitness_goals (id, member_id, goal_type, title, target_value, current_value, unit, start_date, target_date, status, created_at) KEY(id) VALUES
  (1, 1, 'WEIGHT_LOSS',        'Lose 3kg by summer',            3.0,  1.5, 'kg',       DATEADD(DAY,-21,CURRENT_DATE), DATEADD(DAY,60,CURRENT_DATE),  'ACTIVE',    DATEADD(DAY,-21,CURRENT_TIMESTAMP)),
  (2, 1, 'WORKOUT_FREQUENCY',  'Train 12 times this month',    12.0,  8.0, 'workouts', DATEADD(DAY,-25,CURRENT_DATE), DATEADD(DAY, 7,CURRENT_DATE),  'ACTIVE',    DATEADD(DAY,-25,CURRENT_TIMESTAMP)),
  (3, 1, 'DISTANCE',           'Run 50km total',               50.0, 50.0, 'km',       DATEADD(DAY,-60,CURRENT_DATE), DATEADD(DAY,-5,CURRENT_DATE),  'COMPLETED', DATEADD(DAY,-60,CURRENT_TIMESTAMP));

-- Notifications for Maria (mix of read / unread, varied types)
MERGE INTO notifications (id, member_id, notif_type, title, message, link_url, is_read, created_at) KEY(id) VALUES
  (1, 1, 'CLASS_REMINDER',    'Class booked',          'You''re booked for Morning Vinyasa Yoga on Mon at 07:30.',    '/client/classes',  FALSE, DATEADD(HOUR,-3,CURRENT_TIMESTAMP)),
  (2, 1, 'GOAL_ACHIEVED',     'Goal achieved!',        'Nice work — you completed: Run 50km total.',                  '/client/goals',    FALSE, DATEADD(DAY,-1,CURRENT_TIMESTAMP)),
  (3, 1, 'ANNOUNCEMENT',      'Welcome to Forge',      'Thanks for joining the Premium plan — enjoy the AI Coach!',   NULL,               TRUE,  DATEADD(DAY,-2,CURRENT_TIMESTAMP)),
  (4, 1, 'NEW_CLASS',         'New class available',   'Anna added a Pilates class every Thursday at 18:30.',         '/client/classes',  TRUE,  DATEADD(DAY,-4,CURRENT_TIMESTAMP)),
  (5, 1, 'TRAINER_SESSION',   'PT session confirmed',  'John confirmed your PT session on Friday at 17:00.',          '/client/pt',       TRUE,  DATEADD(DAY,-5,CURRENT_TIMESTAMP)),
  (6, 1, 'WAITLIST_AVAILABLE','A spot opened up!',     'A place is now free in Heavy Strength — grab it!',            '/client/classes',  FALSE, DATEADD(HOUR,-6,CURRENT_TIMESTAMP));
