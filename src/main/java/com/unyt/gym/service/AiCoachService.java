package com.unyt.gym.service;

import com.unyt.gym.entity.*;
import com.unyt.gym.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forge AI Fitness Coach — conversational, personalised, data-driven.
 *
 * Design principles:
 *  1. Every response references the user's ACTUAL data (numbers, dates, trends).
 *  2. Tone is warm and direct — a real coach, not a FAQ bot.
 *  3. Responses end with a follow-up or next action to keep dialogue flowing.
 *  4. Trainers can ask about their own assigned clients by name.
 *  5. Clients cannot access other members' data — privacy is enforced silently.
 */
@Service
public class AiCoachService {

    private final MeasurementRepository    measurementRepo;
    private final WorkoutLogRepository     workoutLogRepo;
    private final AttendanceRepository     attendanceRepo;
    private final PTSubscriptionRepository ptRepo;
    private final StaffRepository          staffRepo;
    private final MemberRepository         memberRepo;

    public AiCoachService(MeasurementRepository measurementRepo,
                          WorkoutLogRepository workoutLogRepo,
                          AttendanceRepository attendanceRepo,
                          PTSubscriptionRepository ptRepo,
                          StaffRepository staffRepo,
                          MemberRepository memberRepo) {
        this.measurementRepo = measurementRepo;
        this.workoutLogRepo  = workoutLogRepo;
        this.attendanceRepo  = attendanceRepo;
        this.ptRepo          = ptRepo;
        this.staffRepo       = staffRepo;
        this.memberRepo      = memberRepo;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC ENTRY POINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Called when a CLIENT sends a message.
     * Builds a full picture of that member and responds conversationally.
     */
    public String respondForClient(Member member, String rawMessage) {
        String msg = rawMessage.trim();

        // Privacy guard — if they're asking about someone else by name
        if (mentionsAnotherPerson(msg, firstName(member.getName()))) {
            return privacyRefusal(firstName(member.getName()));
        }

        MemberCtx ctx = buildMemberContext(member);
        return routeClientMessage(ctx, msg);
    }

    /**
     * Called when a TRAINER sends a message.
     * Trainers can ask general coaching questions OR inquire about a specific client.
     */
    public String respondForTrainer(Staff trainer, String rawMessage) {
        String msg = rawMessage.trim();

        // Load this trainer's active PT clients
        List<PTSubscription> mySubs = ptRepo.findByTrainerId(trainer.getId())
            .stream()
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
            .collect(Collectors.toList());

        // Try to detect if the trainer is asking about a specific client
        Optional<PTSubscription> mentionedClient = findMentionedClient(msg, mySubs);
        if (mentionedClient.isPresent()) {
            return trainerClientSummary(trainer, mentionedClient.get().getMember(), mySubs);
        }

        // "My clients" / "klientët e mi" — list overview
        if (has(msg, "my clients","klientët","lista","how are they","si janë","overview","all clients","të gjithë")) {
            return trainerClientListOverview(trainer, mySubs);
        }

        // Otherwise: general coaching assistant response
        return routeTrainerMessage(trainer, msg, mySubs);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CLIENT MESSAGE ROUTING
    // ═══════════════════════════════════════════════════════════════════════

    private String routeClientMessage(MemberCtx ctx, String msg) {
        String m = msg.toLowerCase();

        // "How are you?" — empathetic separate handler
        if (isHowAreYou(m))            return clientHowAreYou(ctx);
        if (isGreeting(m))             return greetClient(ctx);
        if (has(m,"help","çfarë mund","what can you","capabilities","ndihmë","shiko","çfarë di","si funksionon"))
                                       return showClientHelp(ctx);

        // Food calorie lookup — if the user names a specific food AND uses calorie/nutrition context
        Optional<String> foodInfo = lookupFood(m);
        if (foodInfo.isPresent() && has(m,"kalori","calori","kcal","protein","carb","fat","macro",
                                          "nutrition","sa ka","how much","how many","çfarë ka","sa gram",
                                          "sa kcal","calories","makro"))
                                       return clientFoodCaloriesFound(ctx, foodInfo.get());

        if (has(m,"bmi","weight","pesë","peshë","kg","kilogram","dhjamë","overweight","obese","obezitet",
                   "rëndoj","humbas peshë","lose weight","gain weight","body fat","trashësi"))
                                       return clientBmiAndWeight(ctx);
        if (has(m,"progress","progres","si kam shkuar","ndryshim","improvement","results","rezultate",
                   "trend","si jam duke shkuar","si po shkon"))
                                       return clientProgressSummary(ctx);
        if (has(m,"calori","kalori","kcal","burn","energy","energji","sa duhet të ha","how much should i eat",
                   "sa kalori duhet","daily calorie","target calori"))
                                       return clientCalories(ctx);
        if (has(m,"nutrition","ushqim","diet","dietë","food","ushqimet","protein","carb","fat","macro",
                   "çfarë të ha","what to eat","mënyrë të ushqyerit","si të hash"))
                                       return clientNutrition(ctx);
        if (has(m,"cardio","running","vrapim","vrap","aerobic","heart rate","endurance","stamina",
                   "breathless","frymëmarrje","vrap","biçikletë","bike","cycling","zone 2"))
                                       return clientCardio(ctx);
        if (has(m,"strength","muscle","muskul","weights","lifting","force","squat","deadlift","bench",
                   "forcë","pushup","pull-up","trup","forco","forcë","force me"))
                                       return clientStrength(ctx);
        if (has(m,"rest","recovery","çlodhje","gjumë","sleep","tired","lodhem","lodhet","sore","dhemb",
                   "recuper","deload","muscle soreness","overtraining"))
                                       return clientRecovery(ctx);
        if (has(m,"motivat","motivim","give up","quit","nuk mundem","vështirë","hard time","struggling",
                   "depressed","feel bad","ndihem keq","i can't","nuk mund","heq dorë","dorëzuar"))
                                       return clientMotivation(ctx);
        if (has(m,"goal","qëllim","target","plan me","dua të","want to","i want","i'd like","i need",
                   "kam nevojë","kam si qëllim","synimin tim"))
                                       return clientGoals(ctx);
        if (has(m,"pt","trainer","trajner","personal","coach","seancë","session remaining","sessions left",
                   "trajnerin tim","seancat e mia"))
                                       return clientPtInfo(ctx);
        if (has(m,"attendance","check-in","visit","sa herë","how many times","gym how","herë në palest",
                   "sa vizita","vizitat e mia"))
                                       return clientAttendance(ctx);
        if (has(m,"workout","ushtrim","training","trening","today","sot","what should i do","çfarë të bëj",
                   "exercise","plan","stërvitje","si stërvitem","stërvitem sot"))
                                       return clientWorkoutSuggestion(ctx);
        if (has(m,"membership","plan","abonim","expire","skadoj","my plan","plani im","abonimi","antarësia"))
                                       return clientMembershipInfo(ctx);

        return clientFallback(ctx, msg);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CLIENT RESPONSES — conversational, data-driven
    // ═══════════════════════════════════════════════════════════════════════

    private String greetClient(MemberCtx ctx) {
        String tod = timeOfDay();
        String opening;
        if (ctx.workoutsLast7 >= 3)
            opening = String.format("Looks like you've had a solid week — %d sessions logged already.", ctx.workoutsLast7);
        else if (ctx.checkinsLast30 == 0)
            opening = "I see you haven't been in for a while. No judgment — let's get things back on track.";
        else
            opening = String.format("You've checked in %d times this month, which is a start.", ctx.checkinsLast30);

        return String.format(
            "Good %s, **%s**! 👋 %s\n\n" +
            "I'm your Forge coach — and I can see your actual training data, so anything I tell you is based on *your* numbers, not generic advice.\n\n" +
            "What's on your mind? You can ask me about your progress, what to train today, nutrition, or anything else.",
            tod, ctx.name, opening);
    }

    private String showClientHelp(MemberCtx ctx) {
        return String.format(
            "Sure, %s — here's what I can do for you:\n\n" +
            "• **Your progress** — BMI, weight trend, body composition over time\n" +
            "• **Today's workout** — based on what you've done this week\n" +
            "• **Nutrition** — calorie target, protein, macros personalised to your stats\n" +
            "• **Cardio or strength** — specific plans at your current level\n" +
            "• **Recovery** — when to rest, sleep, how to manage soreness\n" +
            "• **Your PT** — sessions remaining, how to make the most of them\n" +
            "• **Goals** — help setting realistic, data-backed targets\n" +
            "• **Motivation** — when things feel hard\n\n" +
            "I know your membership plan (%s), your training frequency, and your body measurements. " +
            "So whatever you ask, I'll answer with *your* data in mind — not textbook generics.",
            ctx.name, ctx.plan);
    }

    private String clientBmiAndWeight(MemberCtx ctx) {
        if (ctx.bmi == null) {
            return String.format(
                "I don't have weight and height measurements on file for you yet, %s. " +
                "Once your trainer logs a measurement, I can give you a proper BMI analysis and body composition picture.\n\n" +
                "If you want to estimate it yourself right now, your BMI = weight(kg) ÷ height(m)². " +
                "Healthy range is 18.5–24.9. What's your approximate weight and height?",
                ctx.name);
        }

        String category = bmiCategory(ctx.bmi);
        String personalTake;

        if (ctx.bmi < 18.5) {
            personalTake = String.format(
                "At %.1f kg and a BMI of %.1f, you're on the lighter side. " +
                "For most people in your position the priority is building lean mass — not just eating more, " +
                "but eating *more protein* while progressively lifting heavier. " +
                "Aim for a 300–400 kcal surplus with 2g protein per kg of bodyweight.",
                ctx.latestWeight, ctx.bmi);
        } else if (ctx.bmi < 25) {
            personalTake = String.format(
                "A BMI of %.1f puts you in the healthy range — %s is where you want to be. " +
                "At %.1f kg, the focus shouldn't be the scale number. " +
                "I'd be thinking about body composition: maintaining muscle while keeping body fat in check. " +
                "You're doing that through good training consistency and adequate protein.",
                ctx.bmi, ctx.name, ctx.latestWeight);
        } else if (ctx.bmi < 30) {
            personalTake = String.format(
                "BMI of %.1f — you're in the overweight range at %.1f kg. " +
                "The honest truth is that 0.5–0.75 kg/week loss is realistic and sustainable. " +
                "That means a ~400 kcal daily deficit, combined with strength training to preserve muscle. " +
                "Crash diets don't work — the research is clear on that.",
                ctx.bmi, ctx.latestWeight);
        } else {
            personalTake = String.format(
                "With a BMI of %.1f, there's meaningful work to do — but it's very doable. " +
                "The biggest lever is consistency: getting in the gym 3× a week and keeping protein high. " +
                "Don't try to fix everything at once. Start with showing up.",
                ctx.bmi);
        }

        StringBuilder sb = new StringBuilder(personalTake);

        // Weight trend
        if (ctx.weightChange != null && Math.abs(ctx.weightChange) > 0.1) {
            if (ctx.weightChange < 0)
                sb.append(String.format("\n\nSince your last measurement you're down **%.1f kg** — that's real progress. Keep the same approach.", Math.abs(ctx.weightChange)));
            else
                sb.append(String.format("\n\nYou're up **%.1f kg** since last time. Worth checking if that's muscle gain (good) or creeping body fat (less ideal — check your training frequency and diet).", ctx.weightChange));
        }

        // Body fat if available
        if (ctx.latestBodyFat != null)
            sb.append(String.format("\n\nBody fat on file: **%.1f%%**. %s",
                ctx.latestBodyFat,
                ctx.latestBodyFat > 25 ? "There's room to bring this down through consistent cardio and diet." :
                ctx.latestBodyFat < 12 ? "That's quite lean — make sure you're eating enough to perform well." :
                "That's a solid range to be in."));

        sb.append(String.format("\n\nYou're at **%s** — ", category));
        sb.append("is there a specific weight goal you're working towards, or more of a body composition change?");

        return sb.toString();
    }

    private String clientProgressSummary(MemberCtx ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Here's where you actually stand, %s:\n\n", ctx.name));

        // Training frequency
        if (ctx.workoutsLast30 >= 12)
            sb.append(String.format("**Training:** %d sessions in 30 days — that's professional-level consistency. Seriously.\n\n", ctx.workoutsLast30));
        else if (ctx.workoutsLast30 >= 8)
            sb.append(String.format("**Training:** %d sessions last month — solid. You're averaging roughly 2×/week, which is enough to see real change.\n\n", ctx.workoutsLast30));
        else if (ctx.workoutsLast30 >= 4)
            sb.append(String.format("**Training:** %d sessions last month. That's a start, but 3×/week would be the turning point for most goals. What's getting in the way?\n\n", ctx.workoutsLast30));
        else
            sb.append(String.format("**Training:** %d sessions last month. Honesty: this frequency won't move the needle much. What's been stopping you?\n\n", ctx.workoutsLast30));

        // Check-ins
        sb.append(String.format("**Gym visits:** %d check-ins this month.\n\n", ctx.checkinsLast30));

        // Body stats
        if (ctx.bmi != null) {
            sb.append(String.format("**BMI:** %.1f (%s)\n", ctx.bmi, bmiCategory(ctx.bmi)));
        }
        if (ctx.latestWeight != null)
            sb.append(String.format("**Weight:** %.1f kg", ctx.latestWeight));
        if (ctx.weightChange != null && Math.abs(ctx.weightChange) > 0.1) {
            sb.append(ctx.weightChange < 0
                ? String.format(" — down %.1f kg since last check ✅", Math.abs(ctx.weightChange))
                : String.format(" — up %.1f kg since last check", ctx.weightChange));
        }
        if (ctx.latestWeight != null) sb.append("\n");
        if (ctx.latestBodyFat != null)
            sb.append(String.format("**Body fat:** %.1f%%\n", ctx.latestBodyFat));
        if (ctx.latestMuscleMass != null)
            sb.append(String.format("**Muscle mass:** %.1f kg\n", ctx.latestMuscleMass));

        // PT
        if (ctx.ptActive > 0)
            sb.append(String.format("\n**PT sessions:** %d active subscription(s) — make sure you're using them.\n", ctx.ptActive));

        // Overall verdict
        sb.append("\n");
        if (ctx.workoutsLast30 >= 8 && ctx.weightChange != null && ctx.weightChange < 0)
            sb.append("Overall: you're moving in the right direction. Keep the frequency, keep the diet in check.");
        else if (ctx.workoutsLast30 >= 8)
            sb.append("Frequency is there — the next lever is probably nutrition. Are you tracking what you eat?");
        else
            sb.append("The clearest thing I can tell you: frequency is the first problem to solve. Everything else comes after.");

        return sb.toString();
    }

    private String clientCalories(MemberCtx ctx) {
        if (ctx.latestWeight == null) {
            return String.format(
                "I need your weight to calculate this properly, %s. Ask your trainer to log a measurement, " +
                "or tell me your approximate weight and I'll give you a target.\n\n" +
                "Rough rule while you wait: **bodyweight (kg) × 30** gives your rough maintenance calories for a moderately active person.",
                ctx.name);
        }

        double h   = ctx.latestHeight != null ? ctx.latestHeight : 172.0;
        // Mifflin-St Jeor: +5 for male, -161 for female, +5 as default (unknown/other)
        double bmrConstant = "Female".equalsIgnoreCase(ctx.gender) ? -161.0 : 5.0;
        double bmr = 10 * ctx.latestWeight + 6.25 * h - 5 * ctx.ageYears + bmrConstant;
        double activityMult = ctx.workoutsLast7 >= 4 ? 1.65 : ctx.workoutsLast7 >= 2 ? 1.55 : 1.40;
        int tdee = (int)(bmr * activityMult);

        String activityNote = ctx.workoutsLast7 >= 4 ? "you've been training frequently this week" :
                              ctx.workoutsLast7 >= 2 ? "your training frequency this week" :
                              "your lighter week of training";

        return String.format(
            "Based on your stats (%.0f kg, %.0f cm) and %s, here's your target:\n\n" +
            "• **Maintenance:** ~%,d kcal/day\n" +
            "• **Fat loss** (0.5 kg/week): ~%,d kcal/day\n" +
            "• **Lean bulk** (slow muscle gain): ~%,d kcal/day\n\n" +
            "Protein first: hit **%.0f–%.0f g/day** regardless of goal. " +
            "Everything else — carbs and fats — fills the remaining calories.\n\n" +
            "One thing worth knowing: these are estimates based on formulas. " +
            "Track for 2 weeks and adjust based on what the scale actually does. " +
            "The formula gets you close; your body tells you the truth.",
            ctx.latestWeight, h, activityNote,
            tdee, tdee - 450, tdee + 300,
            ctx.latestWeight * 1.6, ctx.latestWeight * 2.0);
    }

    private String clientNutrition(MemberCtx ctx) {
        double w = ctx.latestWeight != null ? ctx.latestWeight : 75.0;
        boolean hasWeight = ctx.latestWeight != null;

        return String.format(
            "Let me give you something practical, %s:\n\n" +
            "**Protein — your most important number**\n" +
            "Aim for **%.0f–%.0f g/day** (1.6–2g per kg of your bodyweight%s). " +
            "This preserves muscle whether you're cutting or bulking. " +
            "Good sources: chicken, eggs, Greek yogurt, cottage cheese, fish, legumes.\n\n" +
            "**Carbs**\n" +
            "These are your training fuel. Don't cut them out — time them around your workouts. " +
            "Oats, rice, sweet potato, fruit. More on training days, fewer on rest days.\n\n" +
            "**Fats**\n" +
            "Minimum **%.0f g/day** — this is a floor, not a ceiling. " +
            "Hormones, joints, and brain function all need fat. Olive oil, nuts, avocado, fish.\n\n" +
            "**Hydration**\n" +
            "2–3L baseline + 400–500ml for every hour you train. " +
            "If your urine is dark, you're behind.\n\n" +
            "What's your current eating pattern like? " +
            "I can be more specific if I know whether you're tracking, meal prepping, or winging it.",
            ctx.name,
            w * 1.6, w * 2.0, hasWeight ? "" : " — update your measurements for a precise number",
            w * 0.8);
    }

    private String clientCardio(MemberCtx ctx) {
        String level = ctx.workoutsLast7 >= 4 ? "advanced" :
                       ctx.workoutsLast7 >= 2 ? "intermediate" : "beginner";

        String specific;
        if ("beginner".equals(level)) {
            specific = "Right now the most effective thing is simply getting consistent — even 20 minutes of brisk walking counts. " +
                       "Start with **2 cardio sessions/week**, moderate pace, 25–30 minutes each. " +
                       "Don't sprint on day one and burn out by day three.";
        } else if ("intermediate".equals(level)) {
            specific = "You're active enough to benefit from structured cardio. " +
                       "Try mixing: **1 HIIT session** (20 min — 30s hard, 60s easy, repeat) + **2 steady-state sessions** (30–40 min, conversational pace). " +
                       "HIIT burns more calories per minute; steady-state builds your aerobic base and aids recovery.";
        } else {
            specific = "At your training volume, the smart addition is **Zone 2 cardio** — " +
                       "60–70% of max heart rate, 40–60 minutes, 2×/week. " +
                       "This builds mitochondrial density, speeds recovery between sessions, and improves fat oxidation. " +
                       "Most people don't do this and they're leaving serious gains on the table.";
        }

        return String.format(
            "%s, based on your %d sessions this week:\n\n%s\n\n" +
            "**One thing most people get wrong:** doing cardio before weights tanks strength performance. " +
            "If you're doing both in one session, lift first — always.\n\n" +
            "What's your cardio goal? Fat loss, general fitness, endurance for a specific event?",
            ctx.name, ctx.workoutsLast7, specific);
    }

    private String clientStrength(MemberCtx ctx) {
        return String.format(
            "Strength training 101 for you, %s — keeping it practical:\n\n" +
            "**The foundation is compound movements.** " +
            "Squat, deadlift, bench, row, overhead press. " +
            "These hit the most muscle mass per exercise and drive the biggest strength adaptations.\n\n" +
            "**Progressive overload is the only mechanism that matters.** " +
            "Each week, try to do *slightly more* — an extra rep, 2.5kg more weight, better form. " +
            "Without this, you're maintaining, not improving.\n\n" +
            "**Rep ranges:** " +
            "3–5 reps builds max strength. 6–12 reps builds muscle (hypertrophy) most efficiently. " +
            "12–20 reps builds muscle endurance and works well for legs.\n\n" +
            "**Given your frequency of %d sessions/week:** " +
            "%s\n\n" +
            "What muscle group or lift do you specifically want to improve?",
            ctx.name, ctx.workoutsLast7,
            ctx.workoutsLast7 >= 4 ? "You have the volume for an upper/lower split or PPL programme." :
            ctx.workoutsLast7 >= 2 ? "Full-body 2–3× per week is perfect for your schedule — hit everything each session." :
            "Even 2 full-body sessions a week will produce real results. Consistency over frequency.");
    }

    private String clientRecovery(MemberCtx ctx) {
        String urgency = ctx.workoutsLast7 >= 6 ?
            String.format("**I'm going to be direct with you:** %d sessions this week is too much without adequate recovery. " +
                "Muscle doesn't grow during the workout — it grows when you rest. " +
                "Overtraining doesn't make you stronger, it breaks you down.", ctx.workoutsLast7) :
            ctx.workoutsLast7 >= 4 ?
            String.format("You've had %d sessions this week — solid work. Recovery at this volume is genuinely important, not optional.", ctx.workoutsLast7) :
            "Even at lower training volumes, recovery quality determines how well you adapt.";

        return String.format(
            "%s\n\n" +
            "**Sleep is the biggest lever you're not paying for.** " +
            "7–9 hours triggers 70–80% of daily growth hormone release. " +
            "You can't out-supplement poor sleep.\n\n" +
            "**Muscle soreness (DOMS):** " +
            "Feeling sore 24–48 hours after training is normal. " +
            "If you're still sore 72+ hours later, that's a recovery problem — likely too much volume, poor nutrition, or not enough sleep.\n\n" +
            "**Active recovery on rest days:** " +
            "15–20 minutes of light walking, mobility, or yoga — not nothing, not more training. " +
            "Blood flow clears waste products and speeds repair.\n\n" +
            "**Signs you need a deload week:** " +
            "Performance dropping, everything feels heavy, mood is off, sleep quality declining.\n\n" +
            "How's your sleep been lately? And are you eating enough post-workout to actually recover?",
            urgency);
    }

    private String clientMotivation(MemberCtx ctx) {
        // Personalise based on their actual data
        String dataPoint;
        if (ctx.workoutsLast30 >= 10)
            dataPoint = String.format(
                "I'm going to start with something real: you've logged **%d workouts in the last 30 days**. " +
                "That's not nothing — that's actual effort, documented. " +
                "Whatever you're feeling right now, that history is real.",
                ctx.workoutsLast30);
        else if (ctx.checkinsLast30 >= 5)
            dataPoint = String.format(
                "You've walked through those gym doors **%d times this month**. " +
                "Every single one of those was a decision to show up when it would've been easier not to.",
                ctx.checkinsLast30);
        else if (ctx.weightChange != null && ctx.weightChange < -0.5)
            dataPoint = String.format(
                "Your data tells a story you might be ignoring: you've lost **%.1f kg** since your last measurement. " +
                "That's real, measurable change. It means what you're doing is working.",
                Math.abs(ctx.weightChange));
        else
            dataPoint = "I want to be honest with you: the hardest part of fitness isn't the workout itself. " +
                        "It's the moment before you start, when everything in you wants to do something easier.";

        return String.format(
            "%s, %s\n\n" +
            "The way I see it — the fact that you're asking means you haven't quit. " +
            "People who've truly given up don't ask for motivation.\n\n" +
            "One thing worth knowing: motivation is inconsistent by design. " +
            "**Discipline** — just doing it when motivation isn't there — is what creates results. " +
            "Every person you look up to in the gym has days where they don't want to be there. " +
            "They come anyway.\n\n" +
            "What specifically feels hard right now? " +
            "Is it the training itself, time, energy, results not coming fast enough?",
            dataPoint, ctx.name);
    }

    private String clientGoals(MemberCtx ctx) {
        // Suggest a goal based on their actual data
        String suggestedGoal;
        if (ctx.bmi != null && ctx.bmi > 25 && ctx.latestWeight != null)
            suggestedGoal = String.format(
                "Based on your current weight (%.1f kg, BMI %.1f), " +
                "a realistic 3-month goal would be losing **%.1f–%.1f kg** — " +
                "that's 0.5–0.75 kg/week, which is sustainable without muscle loss.",
                ctx.latestWeight, ctx.bmi, ctx.latestWeight * 0.03, ctx.latestWeight * 0.05);
        else if (ctx.workoutsLast30 < 8)
            suggestedGoal = "Your most impactful goal right now isn't about weight or body fat — " +
                            "it's **consistency**. Three sessions per week for 8 weeks straight. " +
                            "That's the real foundation everything else is built on.";
        else if (ctx.latestMuscleMass != null && ctx.latestBodyFat != null && ctx.latestBodyFat > 20)
            suggestedGoal = String.format(
                "With body fat at %.1f%% and solid training frequency, a **body recomposition** goal makes sense — " +
                "maintain or slightly reduce weight while building muscle and dropping fat simultaneously.",
                ctx.latestBodyFat);
        else
            suggestedGoal = "Given your training history, a **performance goal** might be more motivating than a weight goal — " +
                            "something like increasing your squat, running a faster 5k, or hitting a specific pull-up count.";

        return String.format(
            "Let's make your goal actually work for you, %s.\n\n" +
            "%s\n\n" +
            "A few principles that make the difference:\n" +
            "• Put a **number and a date** on it — 'get fitter' doesn't work, 'lose 5 kg by August 15' does\n" +
            "• Make it **challenging but reachable** — something you'd estimate at 70% probability of achieving\n" +
            "• Focus on **behaviours, not outcomes** — 'train 3× a week' is in your control; 'lose 5 kg' isn't entirely\n\n" +
            "What's the outcome you're actually picturing when you imagine being in the best shape of your life?",
            ctx.name, suggestedGoal);
    }

    private String clientPtInfo(MemberCtx ctx) {
        if (ctx.ptActive == 0) {
            return String.format(
                "You don't currently have an active PT subscription, %s.\n\n" +
                "A personal trainer is genuinely worth it if you:\n" +
                "• Don't know how to structure a programme\n" +
                "• Have specific goals you're not hitting on your own\n" +
                "• Need someone to hold you accountable\n" +
                "• Want correct technique from the start (much cheaper than physio)\n\n" +
                "Ask at reception about the PT packages. They vary by session count and trainer.",
                ctx.name);
        }

        return String.format(
            "You have **%d active PT subscription(s)**, %s — make them count.\n\n" +
            "How to get the most from PT:\n" +
            "• **Come with an agenda** — what do you want to work on this session specifically?\n" +
            "• **Be honest** about how you're feeling, what's working, what isn't\n" +
            "• **Log everything** — your trainer tracks progress better when sessions are recorded\n" +
            "• **Ask questions** — you're paying for expertise, not just someone to count reps\n\n" +
            "Check **My PT** in the sidebar to see your session history and remaining sessions.",
            ctx.ptActive, ctx.name);
    }

    private String clientAttendance(MemberCtx ctx) {
        String verdict;
        if (ctx.checkinsLast30 >= 16)
            verdict = "That's elite-level consistency — genuinely impressive. The gym is clearly a non-negotiable part of your life.";
        else if (ctx.checkinsLast30 >= 10)
            verdict = "That's solid. You're averaging 2–3 visits a week, which is enough volume to see real results if training quality is there.";
        else if (ctx.checkinsLast30 >= 5)
            verdict = "You're showing up, which is step one. The question is what would it take to get that to 10–12 visits next month?";
        else
            verdict = "Let's be real about this: at this frequency the gym isn't yet a habit. " +
                      "That's okay — habits take time to build. " +
                      "Try anchoring gym time to something fixed in your week: same days, same time, non-negotiable.";

        return String.format(
            "**Gym visits last 30 days:** %d check-ins\n" +
            "**Workouts logged:** %d sessions\n\n" +
            "%s\n\n" +
            "One thing worth thinking about: are you going often enough but not logging workouts, " +
            "or skipping sessions entirely? Those are two different problems to solve.",
            ctx.checkinsLast30, ctx.workoutsLast30, verdict);
    }

    private String clientWorkoutSuggestion(MemberCtx ctx) {
        String today;
        if (ctx.workoutsLast7 >= 5) {
            today = String.format(
                "Honestly, %s — with %d sessions this week you should consider today a **recovery day** unless you're feeling genuinely good. " +
                "If you're coming in anyway: light cardio (20 min walk/bike), mobility work, or some gentle core. " +
                "Pushing hard when your body needs rest is a step backward, not forward.",
                ctx.name, ctx.workoutsLast7);
        } else if (ctx.workoutsLast7 == 0) {
            today = String.format(
                "You haven't logged anything this week — so **any training is the right training** today, %s. " +
                "Don't overthink the programme. " +
                "Full-body session: pick one squat, one hinge, one push, one pull. " +
                "3 sets of 8–10 each. Done. That's a genuinely effective session.",
                ctx.name);
        } else {
            today = String.format(
                "%d sessions logged this week so far — good. " +
                "For today I'd suggest either:\n\n" +
                "• **Upper body** (if you trained legs last session)\n" +
                "• **Lower body** (if you trained upper last)\n" +
                "• **Full-body** if you did cardio last time\n\n" +
                "Warm up 5–10 minutes, do 3–4 compound movements, 3 sets each. " +
                "Quality over quantity.", ctx.workoutsLast7);
        }

        return today + "\n\nCheck the **Exercises & Plans** section in the sidebar for guided routines you can follow.";
    }

    private String clientMembershipInfo(MemberCtx ctx) {
        String expiry = ctx.expiryDate != null
            ? String.format("Your plan expires on **%s**", ctx.expiryDate)
            : "Your plan expiry isn't on file";

        long daysLeft = ctx.expiryDate != null
            ? ChronoUnit.DAYS.between(LocalDate.now(), ctx.expiryDate)
            : -1;

        String urgency = daysLeft > 30 ? " — you have plenty of time." :
                         daysLeft > 0  ? String.format(" — **%d days left**, worth renewing soon.", daysLeft) :
                         ctx.expiryDate != null ? " — this may have expired, check with reception." : ".";

        return String.format(
            "Your current plan: **%s**\n" +
            "Status: **%s**\n" +
            "%s%s\n\n" +
            "%s\n\n" +
            "For any changes to your plan, speak to reception or the admin. " +
            "Is there anything about your membership you want to understand better?",
            ctx.plan, ctx.status, expiry, urgency,
            ctx.ptActive > 0
                ? String.format("PT: **%d active subscription(s)** — visible in your My PT section.", ctx.ptActive)
                : "No active PT subscription at the moment.");
    }

    // ── "How are you?" — empathetic, personalised ──────────────────────────
    private String clientHowAreYou(MemberCtx ctx) {
        String dataPoint;
        if (ctx.workoutsLast7 >= 4)
            dataPoint = String.format(
                "I can see you've already logged **%d sessions** this week — so on the training front, " +
                "*you're* clearly doing something right. That's what matters.",
                ctx.workoutsLast7);
        else if (ctx.checkinsLast30 >= 10)
            dataPoint = String.format(
                "Your data shows **%d gym visits** this month — showing up consistently like that takes real effort and discipline.",
                ctx.checkinsLast30);
        else if (ctx.weightChange != null && ctx.weightChange < -0.3)
            dataPoint = String.format(
                "Your numbers tell a good story — you're down **%.1f kg** since your last measurement. " +
                "That kind of real, measurable progress is something to feel proud of.",
                Math.abs(ctx.weightChange));
        else
            dataPoint = "I'm an AI, so I don't experience feelings the way you do — but I'm sharp, focused, and genuinely here for *you* right now.";

        return String.format(
            "I appreciate you asking, **%s**! 😊 %s\n\n" +
            "But more importantly — how are *you* feeling? Both physically and mentally. " +
            "The mental side of fitness is just as real as the training itself.\n\n" +
            "Energy good? Tired? Stressed? Sore? Motivated or struggling?\n\n" +
            "Tell me how you're actually doing and I'll give you advice that actually fits your situation right now. " +
            "I'm here for you. 💙",
            ctx.name, dataPoint);
    }

    // ── Food calorie lookup ──────────────────────────────────────────────────
    private String clientFoodCaloriesFound(MemberCtx ctx, String foodData) {
        return String.format(
            "Here you go, **%s**:\n\n%s\n\n" +
            "_Remember: these are approximate per 100g values. Cooking method and portion size matter a lot._\n\n" +
            "Want me to calculate how this fits into your personal daily calorie target? " +
            "Just ask: *\"sa kalori duhet të ha?\"* / *\"what's my calorie target?\"*",
            ctx.name, foodData);
    }

    private Optional<String> lookupFood(String msg) {
        // Poultry
        if (has(msg,"chicken breast","gjoks pule","gjoksi i pules","fileto pule","pule e zier","pule e bardhë","white chicken"))
            return opt("**Chicken Breast / Gjoks Pule** — per 100g:\n• **165 kcal** | Protein: **31g** | Carbs: 0g | Fat: 3g\n_Typical 150g serving ≈ **248 kcal**. One of the best lean protein sources — zero carbs, almost no fat._");
        if (has(msg,"chicken","pule","pullë") && !has(msg,"breast","gjoks","fileto"))
            return opt("**Chicken (whole/thighs)** — per 100g:\n• **215 kcal** | Protein: **18g** | Carbs: 0g | Fat: 15g\n_Most of the fat is in the skin — removing it saves ~60 kcal per serving._");
        // Meat
        if (has(msg,"beef","mish viçi","viçi","steak","biftek","mish lope"))
            return opt("**Beef / Mish Viçi** — per 100g:\n• **250 kcal** | Protein: **26g** | Carbs: 0g | Fat: 17g\n_Typical 150g serving ≈ **375 kcal**. Great protein but higher in saturated fat — lean cuts (sirloin/fillet) are better choices._");
        if (has(msg,"pork","mish derri","derri"))
            return opt("**Pork / Mish Derri** — per 100g:\n• **242 kcal** | Protein: **27g** | Carbs: 0g | Fat: 14g\n_Typical 150g serving ≈ **363 kcal**._");
        // Fish
        if (has(msg,"salmon","peshk salmon"))
            return opt("**Salmon** — per 100g:\n• **208 kcal** | Protein: **20g** | Carbs: 0g | Fat: **13g** (mostly omega-3)\n_150g fillet ≈ **312 kcal**. Excellent for joints, brain, and anti-inflammation. Eat 2× a week._");
        if (has(msg,"tuna","ton peshk","ton i konservuar"))
            return opt("**Tuna (canned in water) / Ton** — per 85g tin:\n• **99 kcal** | Protein: **22g** | Carbs: 0g | Fat: **1g**\n_One of the best calorie-to-protein ratios of any food. Very affordable. Choose water-packed, not oil._");
        if (has(msg,"fish","peshk") && !has(msg,"salmon","ton","tuna"))
            return opt("**White fish (cod/tilapia/bass)** — per 100g:\n• ~**82 kcal** | Protein: **18g** | Carbs: 0g | Fat: 1g\n_150g serving ≈ **123 kcal**. Lowest calorie protein source available — ideal for cutting._");
        // Eggs & Dairy
        if (has(msg,"egg white","e bardhë veze","albumen","albuminë"))
            return opt("**Egg White / E Bardhë Veze** — per egg (~33g):\n• **17 kcal** | Protein: **4g** | Carbs: 0g | Fat: 0g\n_Pure protein, zero fat. 4 egg whites = 68 kcal, 16g protein. Great for low-calorie high-protein eating._");
        if (has(msg,"egg","vezë","veze","vezet") && !has(msg,"white","e bardhë"))
            return opt("**Egg / Vezë** — 1 medium egg (~55g):\n• **85 kcal** | Protein: **7g** | Carbs: 0.5g | Fat: **6g**\n_Complete protein with all essential amino acids. Yolk has the fat AND most vitamins — don't skip it unless counting very tight._");
        if (has(msg,"milk","qumësht","qumesht","qumështin"))
            return opt("**Milk / Qumësht (whole)** — per 100ml:\n• **61 kcal** | Protein: 3g | Carbs: 5g | Fat: 3g\n_200ml glass ≈ **122 kcal**. Skimmed milk: ~35 kcal/100ml. Good post-workout option._");
        if (has(msg,"greek yogurt","kos grek","yogurt grek","yogurt","kosi","kos"))
            return opt("**Greek Yogurt / Kos** — per 100g:\n• **59 kcal** | Protein: **10g** | Carbs: 4g | Fat: 0g\n_150g serving ≈ **89 kcal**. Excellent protein-to-calorie ratio + probiotics for gut health. Add to shakes or eat alone._");
        if (has(msg,"cheese","djath","djathi","feta","kaçkavall"))
            return opt("**Feta Cheese / Djath** — per 100g:\n• **264 kcal** | Protein: 14g | Carbs: 4g | Fat: **21g**\n_30g portion ≈ **79 kcal**. Flavourful — small amounts go a long way. Kaçkavall is similar (~400 kcal/100g)._");
        if (has(msg,"whey","protein powder","proteinë pluhur","shake proteine","protein shake"))
            return opt("**Whey Protein Powder** — per 30g scoop:\n• ~**120 kcal** | Protein: **25g** | Carbs: 3g | Fat: 2g\n_A supplement, not a food replacement. Use it when you genuinely can't hit protein targets through meals. 1 scoop + 200ml milk = ~240 kcal, 28g protein._");
        // Grains
        if (has(msg,"rice","oriz","orizi","orizin"))
            return opt("**Rice (cooked) / Oriz** — per 100g:\n• **130 kcal** | Protein: 3g | Carbs: **28g** | Fat: 0g\n_Typical 180g bowl ≈ **234 kcal**. Clean carb for training days. White vs brown — both are fine, brown has more fibre._");
        if (has(msg,"pasta","makarona","makaron","spaghetti","noodle"))
            return opt("**Pasta (cooked) / Makarona** — per 100g:\n• **131 kcal** | Protein: 5g | Carbs: **25g** | Fat: 1g\n_180g serving ≈ **236 kcal**. Good pre-workout meal combined with a protein source (chicken/tuna)._");
        if (has(msg,"bread","bukë","buke","toast","bukën"))
            return opt("**Bread / Bukë** — per slice (~30g):\n• **80 kcal** | Protein: 3g | Carbs: 15g | Fat: 1g\n_Wholegrain/dark bread = more fibre, better satiety. White bread is fine but less filling._");
        if (has(msg,"oat","tërshërë","tershëre","porridge","musli","qull"))
            return opt("**Oats / Tërshërë** — per 100g dry (typical bowl is ~80g):\n• **389 kcal** | Protein: **17g** | Carbs: 66g | Fat: 7g\n_80g dry ≈ **311 kcal** — excellent slow-release energy, keeps you full for hours. Best breakfast for training days._");
        // Vegetables & Fruit
        if (has(msg,"french fries","patate të skuqura","patate te skuqura","chips","cips") && has(msg,"patate","potato","fries","chips","cips"))
            return opt("**French Fries / Patate të Skuqura** — per 100g:\n• **312 kcal** | Fat: **15g** | Carbs: 41g\n_⚠️ The oil triples the calories vs. boiled potato. A large portion (200g) = ~624 kcal. Enjoy occasionally._");
        if (has(msg,"sweet potato","patate e ëmbël","patate e embel","yam"))
            return opt("**Sweet Potato** — per 100g:\n• **86 kcal** | Protein: 2g | Carbs: 20g | Fat: 0g\n_150g serving ≈ **129 kcal**. Better micronutrient profile than white potato — vitamin A, potassium, fibre._");
        if (has(msg,"potato","patate","patatja") && !has(msg,"fries","skuqura","sweet","ëmbël","chips"))
            return opt("**Potato (boiled) / Patate** — per 100g:\n• **77 kcal** | Protein: 2g | Carbs: 17g | Fat: 0g\n_200g serving ≈ **154 kcal**. Very filling per calorie when boiled/baked. One of the most satiating foods._");
        if (has(msg,"banana","banane","bananet"))
            return opt("**Banana / Banane** — 1 medium (~118g):\n• **105 kcal** | Protein: 1g | Carbs: **27g** (natural sugar) | Fat: 0g\n_Best eaten **before a workout** for quick energy. High glycemic — not ideal as a bedtime snack._");
        if (has(msg,"apple","mollë","molla","mollën"))
            return opt("**Apple / Mollë** — 1 medium (~182g):\n• **95 kcal** | Protein: 0.5g | Carbs: 25g | Fat: 0g\n_High in fibre — keeps you full despite low calories. Great snack choice for weight loss._");
        if (has(msg,"orange","portokall","portokalli","mandarine"))
            return opt("**Orange / Portokall** — 1 medium (~130g):\n• **62 kcal** | Vitamin C: 70mg | Carbs: 15g | Fat: 0g\n_Good source of vitamin C and hydration. Eat the whole fruit — juice removes the fibre._");
        if (has(msg,"avocado","avokado"))
            return opt("**Avocado / Avokado** — half (~100g):\n• **160 kcal** | Protein: 2g | Carbs: 9g | Fat: **15g** (healthy mono-unsaturated)\n_Nutrient-dense with healthy fats. Calorie-rich — track portions when cutting._");
        if (has(msg,"tomato","domate","domaten","domatja"))
            return opt("**Tomato / Domate** — per 100g:\n• **18 kcal** | Carbs: 4g | Fat: 0g\n_Almost calorie-free — eat freely. High in lycopene (antioxidant). Great for salads and volume eating._");
        if (has(msg,"salad","sallatë","sallate","sallatën") && !has(msg,"caesar","nicoise"))
            return opt("**Mixed Green Salad (no dressing)** — per 100g:\n• ~**20 kcal**\n_⚠️ Dressings are where salads get dangerous: caesar dressing adds 150–200 kcal/tbsp. Use olive oil + lemon = lower calorie option._");
        if (has(msg,"spinach","spinaq","kale","gjelbërim"))
            return opt("**Spinach / Spinaq** — per 100g:\n• **23 kcal** | Protein: 3g | Iron: 2.7mg\n_Eat without counting. Incredibly nutrient-dense — iron, magnesium, vitamins K and A. Add to everything._");
        if (has(msg,"broccoli","brokoli","lulelakra","cauliflower"))
            return opt("**Broccoli / Brokoli** — per 100g:\n• **34 kcal** | Protein: 3g | Fibre: 2.6g\n_Volume eating champion — you can eat 500g for only 170 kcal. Keeps you full and covers multiple vitamins._");
        if (has(msg,"cucumber","trangull","kastravec"))
            return opt("**Cucumber / Trangull** — per 100g:\n• **15 kcal** | Water content: 96%\n_Almost zero calories. Great for volume eating and hydration. Perfect snack when hunger hits._");
        if (has(msg,"corn","misër","miser","kukuruz"))
            return opt("**Corn / Misër** — per 100g:\n• **86 kcal** | Protein: 3g | Carbs: 19g | Fat: 1g\n_Moderate carb source. Whole corn is better than corn syrup/processed corn products._");
        // Fats & Oils
        if (has(msg,"olive oil","vaj ulliri","vaji i ullirit","extra virgin"))
            return opt("**Olive Oil / Vaj Ulliri** — per 10ml (1 tablespoon):\n• **88 kcal** | Fat: **10g** (mostly mono-unsaturated)\n_⚠️ Healthy fat, but extremely calorie-dense. Every extra tablespoon counts. Measure, don't pour._");
        if (has(msg,"butter","gjalpë","gjalp","gjalpën"))
            return opt("**Butter / Gjalpë** — per 10g (1 tablespoon):\n• **72 kcal** | Fat: **8g** (saturated)\n_Adds up fast in cooking. Substitute with Greek yogurt, applesauce, or just reduce amounts._");
        if (has(msg,"walnut","arra","arrat") && !has(msg,"peanut","almond","bajame","cashew"))
            return opt("**Walnuts / Arra** — per 28g (small handful):\n• **185 kcal** | Protein: 4g | Fat: 18g (omega-3)\n_Extremely easy to overeat — pre-portion them. Despite high calories, excellent for brain and heart health._");
        if (has(msg,"almond","bajame","bajamen","bajamja"))
            return opt("**Almonds / Bajame** — per 28g (small handful):\n• **164 kcal** | Protein: 6g | Fat: 14g | Fibre: 3.5g\n_Great snack — protein + healthy fat keeps you satisfied. Still easy to overeat, so measure._");
        if (has(msg,"peanut butter","gjalpë kikiriku","kikiriku","peanut"))
            return opt("**Peanut Butter** — per 32g (2 tablespoons):\n• **188 kcal** | Protein: **8g** | Fat: 16g | Carbs: 6g\n_High-calorie but satiating. Great for bulking — dangerous in large amounts when cutting._");
        // Legumes
        if (has(msg,"beans","fasule","fasulë","fasulën","lentil","thjerrëza","chickpea","qiqer","legume"))
            return opt("**Beans / Fasule (cooked)** — per 100g:\n• **127 kcal** | Protein: **8g** | Carbs: 23g | Fat: 0g | Fibre: 6g\n_Excellent plant-based protein + fibre. Very filling, very affordable. Perfect for vegetarians._");
        // Fast food & Treats
        if (has(msg,"pizza"))
            return opt("**Pizza** — per 100g:\n• **266 kcal** | Protein: 11g | Carbs: 33g | Fat: 10g\n_A typical slice (~150g) ≈ **400 kcal**. Two slices = 800 kcal easily. Opt for thin crust + fewer toppings._");
        if (has(msg,"burger","hamburger"))
            return opt("**Burger / Hamburger** — per 100g:\n• **295 kcal** | Protein: 17g | Carbs: 24g | Fat: 14g\n_A full burger meal with fries can be 1000–1400 kcal in one sitting. Enjoy as an occasional treat._");
        if (has(msg,"chocolate","çokollatë","çokolat","çokolata"))
            return opt("**Chocolate / Çokollatë** — per 100g:\n• **546 kcal** | Fat: 31g | Sugar: 48g\n_A 40g bar ≈ **218 kcal**. Dark chocolate 70%+ has more cocoa, less sugar, and some antioxidant benefit._");
        if (has(msg,"ice cream","akullore","akulloren"))
            return opt("**Ice Cream / Akullore** — per 100g:\n• **207 kcal** | Fat: 11g | Sugar: 24g\n_2-scoop serving (~150g) ≈ **310 kcal**. A treat, not a staple — enjoy without guilt occasionally._");
        if (has(msg,"cookie","biskota","biskote","biskotë"))
            return opt("**Cookies / Biskota** — per 30g (~2 cookies):\n• **146 kcal** | Sugar: 12g | Fat: 6g\n_Easy to eat a whole pack without noticing. Pre-portion if you're tracking._");
        if (has(msg,"protein bar","bar proteine","snack bar","energy bar"))
            return opt("**Protein Bar** — per ~60g bar:\n• ~**200 kcal** | Protein: 20g | Carbs: 22g | Fat: 7g\n_Read the label — some are candy bars in disguise. Look for 20g+ protein and <25g carbs per bar._");
        // Drinks
        if (has(msg,"coffee","kafe","kafja","kafën") && !has(msg,"latte","cappuccino","frappe"))
            return opt("**Black Coffee / Kafe** — per cup (240ml):\n• **2 kcal**\n_⚠️ What destroys it: milk adds 30–60 kcal, sugar adds 16 kcal/tsp, syrups add 50–100 kcal each. A frappe can hit 500 kcal._");
        if (has(msg,"latte","cappuccino","frappe"))
            return opt("**Latte / Cappuccino / Frapuccino** — per 300–400ml:\n• **150–500 kcal** (wide range!)\n_A large latte with whole milk: ~250 kcal. A sweetened frappe: 400–600 kcal. Ask for oat/skimmed milk + no syrup._");
        if (has(msg,"beer","birrë","birre"))
            return opt("**Beer / Birrë** — per 330ml can:\n• **142 kcal** | Carbs: 11g | Alcohol: 14g\n_⚠️ Alcohol provides 7 kcal/g — almost as calorie-dense as fat. A night out can easily be 800–1200 extra kcal._");
        if (has(msg,"juice","lëng frutash","lëng","smoothie"))
            return opt("**Fruit Juice / Lëng Frutash** — per 200ml:\n• **90 kcal** | Sugar: ~20g\n_⚠️ Whole fruit is always better — juice removes all the fibre and concentrates the sugar. Eat the orange, don't drink it._");
        if (has(msg,"rice cake","kek orizi","snack orizi"))
            return opt("**Rice Cake** — per cake (~9g):\n• **35 kcal** | Carbs: 7g | Protein: 0.7g\n_Low calorie snack but not very filling alone — pair with Greek yogurt or peanut butter for better satiety._");

        return Optional.empty();
    }

    private Optional<String> opt(String s) { return Optional.of(s); }

    private String clientFallback(MemberCtx ctx, String original) {
        return String.format(
            "Hmm, nuk e kuptova plotësisht, **%s** — no worries, let's try again! 😊\n\n" +
            "I understand English and Albanian, and I can help with:\n\n" +
            "🏋️ **Training / Stërvitja:** *\"çfarë të bëj sot?\"* / *\"what should I train today?\"*\n" +
            "📊 **Progress / Progresi:** *\"si kam shkuar?\"* / *\"how's my progress?\"*\n" +
            "🍗 **Nutrition / Ushqimi:** *\"sa kalori duhet të ha?\"* / *\"what should I eat?\"*\n" +
            "🔢 **Food calories:** *\"sa kalori ka pule?\"* / *\"calories in chicken?\"*\n" +
            "💪 **Strength / Forcë:** *\"si të forcohem?\"* / *\"how to build muscle?\"*\n" +
            "❤️ **Cardio / Kardio:** *\"ushtrime kardio\"* / *\"cardio workout\"*\n" +
            "😴 **Recovery / Çlodhja:** *\"si të çlodhemi?\"* / *\"how to recover faster?\"*\n" +
            "🎯 **Goals / Qëllimet:** *\"dua të humbas peshë\"* / *\"I want to lose weight\"*\n" +
            "💬 **Just chat:** *\"si je?\"* / *\"how are you?\"*\n\n" +
            "Çfarë doje të pyesje? / What were you trying to ask?",
            ctx.name);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TRAINER RESPONSES
    // ═══════════════════════════════════════════════════════════════════════

    private String routeTrainerMessage(Staff trainer, String msg, List<PTSubscription> mySubs) {
        String m = msg.toLowerCase();
        String tName = firstName(trainer.getName());

        if (isGreeting(m))  return greetTrainer(trainer, mySubs);
        if (has(m,"help","what can","capabilities")) return trainerHelp(trainer, mySubs);
        if (has(m,"program","programme","periodis","split","training plan","training structure"))
            return String.format(
                "Programming principles for your clients, %s:\n\n" +
                "**Beginner (< 1 year):** Full-body 3×/week. Master movement patterns before loading. " +
                "Linear progression works perfectly here — they'll add weight every session.\n\n" +
                "**Intermediate (1–3 years):** Upper/lower 4× or PPL. Introduce undulating periodisation. " +
                "Start managing weekly volume by muscle group (10–20 sets/muscle/week).\n\n" +
                "**Advanced (3+ years):** Daily undulating periodisation, block periodisation, or specialisation phases. " +
                "Linear progression stops working — you need planned variation.\n\n" +
                "**The universal rule:** specificity + progressive overload + recovery. " +
                "Every good programme has these three. Everything else is detail.\n\n" +
                "You have **%d active client(s)** right now — want me to give you a specific recommendation for one of them?",
                tName, mySubs.size());
        if (has(m,"nutrition","protein","macro","calori","diet","ushqim"))
            return String.format(
                "Nutrition guidance you can apply with clients, %s:\n\n" +
                "**Protein** is the most important variable to nail first. " +
                "1.6–2.2 g/kg of bodyweight covers the vast majority of goals. " +
                "Spread across 3–4 meals for optimal muscle protein synthesis.\n\n" +
                "**For fat loss clients:** create a deficit through food reduction, not training volume spikes. " +
                "400–500 kcal/day deficit, protein high, strength training maintained. " +
                "Cardio is the adjustable variable — not the primary driver.\n\n" +
                "**For muscle gain:** surplus of 200–350 kcal, protein at 2g/kg, progressive overload in training. " +
                "Weight gain > 0.5 kg/week for a beginner means too much fat being added.\n\n" +
                "Want me to calculate specific targets for one of your clients?", tName);
        if (has(m,"motivat","client not coming","dropping out","attendance","showing up"))
            return String.format(
                "Client retention is genuinely one of the hardest parts of this job, %s.\n\n" +
                "What I know from the data: dropout risk is highest at weeks 3–6. " +
                "That's when novelty wears off and the initial motivation fades.\n\n" +
                "**What works:**\n" +
                "• Set **process goals** (show up X times/week) alongside outcome goals\n" +
                "• Make early sessions **successful** — don't destroy clients in week one\n" +
                "• **Check in** between sessions — a message asking how they're feeling goes a long way\n" +
                "• Connect training to their **identity** (\"you're someone who trains\") not just the outcome\n" +
                "• Celebrate visible progress immediately — don't wait for big milestones\n\n" +
                "Is there a specific client you're worried about losing?", tName);

        return String.format(
            "Good question, %s. I'm specialised in fitness coaching — ask me about programming, " +
            "nutrition, client strategies, exercise technique, or recovery.\n\n" +
            "You can also ask me about any of your **%d active client(s)** by name — I can pull up their full profile. " +
            "For example: *\"How is [client name] doing?\"*",
            tName, mySubs.size());
    }

    private String greetTrainer(Staff trainer, List<PTSubscription> mySubs) {
        String tod = timeOfDay();
        String clientSummary = mySubs.isEmpty()
            ? "You don't have any active PT clients assigned yet."
            : String.format("You have **%d active PT client(s)** right now.", mySubs.size());

        String specialty = trainer.getSpecialty() != null
            ? String.format(" I see your specialty is **%s**.", trainer.getSpecialty())
            : "";

        return String.format(
            "Good %s, **%s**!%s\n\n" +
            "%s I can help you with programming, client nutrition, technique cues, retention strategies, " +
            "or give you a detailed breakdown of any of your clients.\n\n" +
            "What do you need?",
            tod, firstName(trainer.getName()), specialty, clientSummary);
    }

    private String trainerHelp(Staff trainer, List<PTSubscription> mySubs) {
        return String.format(
            "Here's what I can do for you, %s:\n\n" +
            "**About your clients:**\n" +
            "• Ask *\"How is [name] doing?\"* — I'll pull their full profile: BMI, weight trend, training frequency, PT sessions\n" +
            "• Ask *\"Give me an overview of all my clients\"* — summary of everyone\n\n" +
            "**General coaching:**\n" +
            "• Programme design (beginner/intermediate/advanced)\n" +
            "• Nutrition targets for different goals\n" +
            "• Exercise technique and cues\n" +
            "• Retention and motivation strategies\n" +
            "• Recovery and deload planning\n\n" +
            "**Your active clients (%d):** %s",
            firstName(trainer.getName()), mySubs.size(),
            mySubs.isEmpty() ? "none assigned yet." :
            mySubs.stream()
                  .map(s -> firstName(s.getMember().getName()))
                  .collect(Collectors.joining(", ")));
    }

    /** Full profile of a specific client for the trainer. */
    private String trainerClientSummary(Staff trainer, Member member, List<PTSubscription> allSubs) {
        MemberCtx ctx = buildMemberContext(member);
        String tName = firstName(trainer.getName());

        // Find the specific subscription for this member
        Optional<PTSubscription> sub = allSubs.stream()
            .filter(s -> s.getMember().getId().equals(member.getId()))
            .findFirst();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("**%s — Client Profile**\n\n", member.getName()));

        // PT subscription info
        if (sub.isPresent()) {
            sb.append(String.format("**PT Package:** %s · %d sessions remaining · %d used\n",
                sub.get().getPtPackage().getName(),
                sub.get().getSessionsRemaining(),
                sub.get().getSessionsUsed()));
        }
        sb.append(String.format("**Plan:** %s · **Status:** %s\n\n", ctx.plan, ctx.status));

        // Body stats
        if (ctx.bmi != null || ctx.latestWeight != null) {
            sb.append("**Body Stats:**\n");
            if (ctx.latestWeight != null) {
                sb.append(String.format("• Weight: %.1f kg", ctx.latestWeight));
                if (ctx.weightChange != null && Math.abs(ctx.weightChange) > 0.1)
                    sb.append(ctx.weightChange < 0
                        ? String.format(" (↓ %.1f kg since last measurement ✅)", Math.abs(ctx.weightChange))
                        : String.format(" (↑ %.1f kg since last measurement)", ctx.weightChange));
                sb.append("\n");
            }
            if (ctx.bmi != null) sb.append(String.format("• BMI: %.1f (%s)\n", ctx.bmi, bmiCategory(ctx.bmi)));
            if (ctx.latestBodyFat != null) sb.append(String.format("• Body fat: %.1f%%\n", ctx.latestBodyFat));
            if (ctx.latestMuscleMass != null) sb.append(String.format("• Muscle mass: %.1f kg\n", ctx.latestMuscleMass));
            sb.append("\n");
        } else {
            sb.append("**Body Stats:** No measurements logged yet. Consider taking a baseline.\n\n");
        }

        // Training frequency
        sb.append("**Training Activity:**\n");
        sb.append(String.format("• Workouts (last 30 days): **%d sessions**\n", ctx.workoutsLast30));
        sb.append(String.format("• Gym check-ins (last 30 days): **%d visits**\n", ctx.checkinsLast30));
        sb.append(String.format("• This week: **%d sessions**\n\n", ctx.workoutsLast7));

        // Coach assessment
        sb.append("**Assessment:**\n");
        if (ctx.workoutsLast30 >= 10 && (ctx.weightChange == null || ctx.weightChange <= 0))
            sb.append("Consistency is strong and trending in the right direction. Focus on progressive overload.");
        else if (ctx.workoutsLast30 >= 8)
            sb.append("Frequency is decent. Check if nutrition is aligned with their goal — that's likely the limiting factor.");
        else if (ctx.workoutsLast30 >= 4)
            sb.append("Attendance is inconsistent. Worth having a direct conversation about barriers in the next session.");
        else
            sb.append("⚠️ Low training frequency this month. This client needs a check-in — attendance is the primary issue.");

        sb.append(String.format("\n\nWant a specific recommendation for %s's programme or next session?",
            firstName(member.getName())));

        return sb.toString();
    }

    /** Overview of all the trainer's clients. */
    private String trainerClientListOverview(Staff trainer, List<PTSubscription> mySubs) {
        if (mySubs.isEmpty()) {
            return String.format(
                "You don't have any active PT clients assigned yet, %s. " +
                "When a member's PT subscription gets assigned to you, they'll appear here " +
                "and I'll be able to give you their full profile.",
                firstName(trainer.getName()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("**Your %d active client(s), %s:**\n\n",
            mySubs.size(), firstName(trainer.getName())));

        for (PTSubscription sub : mySubs) {
            Member m = sub.getMember();
            MemberCtx ctx = buildMemberContext(m);

            String trend = ctx.weightChange != null && Math.abs(ctx.weightChange) > 0.1
                ? (ctx.weightChange < 0 ? " ↓" : " ↑")
                : "";

            String freq = ctx.workoutsLast30 >= 10 ? "🟢 Active" :
                          ctx.workoutsLast30 >= 5  ? "🟡 Moderate" : "🔴 Infrequent";

            sb.append(String.format("**%s** — %s · %d sessions left · %s%s\n",
                m.getName(),
                sub.getPtPackage().getName(),
                sub.getSessionsRemaining(),
                freq, trend));
        }

        sb.append("\nAsk me *\"How is [name] doing?\"* for a full breakdown on any of them.");
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PRIVACY
    // ═══════════════════════════════════════════════════════════════════════

    private boolean mentionsAnotherPerson(String msg, String ownFirstName) {
        String m = msg.toLowerCase();
        // Patterns that suggest asking about someone else
        if (!has(m, "i ", "my ", "me ", "i'm", "im ", "si jam", "si kam", "për mua"))
            return false; // probably not asking about self
        // If the message says "how is [name]" or "tell me about [name]" and name ≠ own name
        // Simple heuristic: if they write a proper noun that isn't their own first name
        // after keywords like "how is", "about", "for", "si po"
        for (String trigger : new String[]{"how is ", "about ", "si po ", "si është ", "tell me about "}) {
            int idx = m.indexOf(trigger);
            if (idx >= 0) {
                String rest = msg.substring(idx + trigger.length()).trim();
                String candidate = rest.split("[\\s,?!.]")[0];
                if (!candidate.isBlank() && !candidate.equalsIgnoreCase(ownFirstName) && Character.isUpperCase(candidate.charAt(0)))
                    return true;
            }
        }
        return false;
    }

    private String privacyRefusal(String clientName) {
        return String.format(
            "I can only share your own data, %s — other members' information is private. " +
            "That's a firm rule regardless of the question.\n\n" +
            "Is there something about *your* progress, training, or health I can help with?",
            clientName);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CONTEXT BUILDER
    // ═══════════════════════════════════════════════════════════════════════

    private MemberCtx buildMemberContext(Member member) {
        MemberCtx ctx = new MemberCtx();
        ctx.name    = firstName(member.getName());
        ctx.plan    = member.getPlan() != null ? member.getPlan().getName() : "Standard";
        ctx.status  = member.getStatus() != null ? member.getStatus().name() : "ACTIVE";
        ctx.expiryDate = member.getExpiryDate();
        ctx.gender  = member.getGender();

        // Most recent measurement (and trend vs. previous)
        List<Measurement> measurements = measurementRepo.findByMemberIdOrderByDateDesc(member.getId());
        if (!measurements.isEmpty()) {
            Measurement latest = measurements.get(0);
            ctx.latestWeight     = latest.getWeightKg();
            ctx.latestHeight     = latest.getHeightCm();
            ctx.latestBodyFat    = latest.getBodyFatPct();
            ctx.latestMuscleMass = latest.getMuscleMass();

            if (ctx.latestWeight != null && ctx.latestHeight != null && ctx.latestHeight > 0) {
                double hm = ctx.latestHeight / 100.0;
                ctx.bmi = ctx.latestWeight / (hm * hm);
            }

            if (measurements.size() >= 2) {
                Measurement prev = measurements.get(1);
                if (latest.getWeightKg() != null && prev.getWeightKg() != null)
                    ctx.weightChange = latest.getWeightKg() - prev.getWeightKg();
            }
        }

        // Workout frequency
        LocalDateTime since30 = LocalDateTime.now().minusDays(30);
        LocalDateTime since7  = LocalDateTime.now().minusDays(7);
        ctx.workoutsLast30 = (int) workoutLogRepo.countByMemberIdAndStartTimeAfter(member.getId(), since30);
        ctx.workoutsLast7  = (int) workoutLogRepo.countByMemberIdAndStartTimeAfter(member.getId(), since7);

        // Attendance
        ctx.checkinsLast30 = (int) attendanceRepo.countByMemberIdAndDateAfter(
            member.getId(), LocalDate.now().minusDays(30));

        // Active PT subscriptions
        ctx.ptActive = (int) ptRepo.findByMemberId(member.getId()).stream()
            .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
            .count();

        // Age from birthdate (falls back to 28 if not set)
        if (member.getBirthDate() != null) {
            ctx.ageYears = (int) ChronoUnit.YEARS.between(member.getBirthDate(), LocalDate.now());
        }

        return ctx;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TRAINER CLIENT NAME MATCHING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tries to find a trainer's client whose name appears in the message.
     * Checks both first name and full name, case-insensitive.
     */
    private Optional<PTSubscription> findMentionedClient(String msg, List<PTSubscription> subs) {
        String m = msg.toLowerCase();
        for (PTSubscription sub : subs) {
            String fullName  = sub.getMember().getName().toLowerCase();
            String first     = firstName(sub.getMember().getName()).toLowerCase();
            if (m.contains(first) || m.contains(fullName))
                return Optional.of(sub);
        }
        return Optional.empty();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private boolean isGreeting(String msg) {
        return has(msg, "hello","hi ","hi!","hey ","hey,","hej","hei","përshëndetje","mirëdita",
                        "mirëmëngjes","tungjatjeta","good morning","good afternoon","good evening",
                        "howdy","salve","ciao","yo ");
    }

    private boolean isHowAreYou(String msg) {
        return has(msg, "si je","si jeni","si po je","si po jeni","si ndihesh","si ndihet","a je mirë",
                        "how are you","how r u","how r you","you ok?","r u ok","how do you feel",
                        "how u doing","how you doing","çfarë bën","si po kalon","si kalon","si shkon");
    }

    private boolean has(String msg, String... kws) {
        for (String kw : kws) { if (msg.contains(kw)) return true; }
        return false;
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "there";
        return fullName.trim().split("\\s+")[0];
    }

    private String timeOfDay() {
        int h = LocalTime.now().getHour();
        return h < 12 ? "morning" : h < 17 ? "afternoon" : "evening";
    }

    private String bmiCategory(double bmi) {
        if (bmi < 18.5) return "underweight";
        if (bmi < 25.0) return "healthy weight";
        if (bmi < 30.0) return "overweight";
        return "obese range";
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CONTEXT INNER CLASS
    // ═══════════════════════════════════════════════════════════════════════

    private static class MemberCtx {
        String     name, plan, status;
        String     gender; // "Female", "Male", "Other" — null if not set
        LocalDate  expiryDate;
        Double     latestWeight, latestHeight, latestBodyFat, latestMuscleMass;
        Double     bmi, weightChange;
        int        workoutsLast30, workoutsLast7, checkinsLast30, ptActive;
        int        ageYears = 28; // default fallback if birthdate unknown
    }
}
