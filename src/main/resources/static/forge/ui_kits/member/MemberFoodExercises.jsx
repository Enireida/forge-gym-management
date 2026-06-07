// Forge — Member Web Portal — Workouts plan + Exercise library + Food log
// Workouts: highlights today's day, adds Sunday, lets you actually log the
//   weights / reps you performed — persisted per date.
// Exercise library: free-weights, bodyweight AND 8 gym machines.
// Food log: persisted via useLocalState. AI calorie estimator via window.claude.complete.

const { M_SAGE, M_SAGE_900, M_SAGE_800, M_SAGE_700, M_SAGE_500, M_SAGE_LIGHT, M_SAGE_50,
        M_BONE_50, M_BONE_25, M_BONE_100, M_BONE_200, M_BONE_500, M_BONE_700,
        M_FG, M_ACCENT } = window.M_TOKENS;
const { today, fmtDate, ymd } = window.M_HELPERS;
const useLocalState = window.useLocalState;
const readLocal = window.readLocal;
const MImageSlot = window.MemberImageSlot;

// ============================================================
// MemberWorkouts — weekly plan + actually log what you did today
// ============================================================
const WEEKLY_PLAN = [
  { day:'Monday',    focus:'Push',            exs:['Bench Press · 5×5','Overhead Press · 5×5','DB Fly · 3×12','Tricep Pushdown · 3×12'] },
  { day:'Tuesday',   focus:'Cardio',          exs:['Treadmill steady · 30 min','Rowing intervals · 15 min'] },
  { day:'Wednesday', focus:'Pull',            exs:['Pull-up · 4×8','Barbell Row · 5×5','Face Pull · 3×15','DB Curl · 3×12'] },
  { day:'Thursday',  focus:'Rest / mobility', exs:['Foam roll · 15 min','Yoga flow · 20 min'] },
  { day:'Friday',    focus:'Legs',            exs:['Squat · 5×5','RDL · 4×8','Hip Thrust · 3×12','Calf Raise · 3×15'] },
  { day:'Saturday',  focus:'HIIT',            exs:['Tabata circuit · 20 min','Core finisher · 10 min'] },
  { day:'Sunday',    focus:'Active recovery', exs:['Walk · 45 min','Light stretching · 15 min'] },
];

function MemberWorkouts() {
  // Today's day-name. JS getDay() returns 0..6 starting Sunday — re-map so Monday is 0.
  const jsDay = today().getDay(); // 0=Sun … 6=Sat
  const todayIdx = (jsDay + 6) % 7; // 0=Mon … 6=Sun

  const todayKey = ymd(today());
  const [log, setLog] = useLocalState('workoutLog', {});
  const todaysItems = log[todayKey] || [];

  // Ensure today is seeded with the plan if not present.
  React.useEffect(() => {
    if (!log[todayKey]) {
      setLog({ ...log, [todayKey]: WEEKLY_PLAN[todayIdx].exs.map(name => ({ name, done:false, sets:[] })) });
    }
  }, []); // run once

  const updateToday = (items) => setLog({ ...log, [todayKey]: items });
  const toggleDone = (i) => updateToday(todaysItems.map((x, idx) => idx === i ? { ...x, done: !x.done } : x));
  const [expanded, setExpanded] = React.useState(null);
  const updateSet = (i, si, field, value) => {
    const items = todaysItems.map((x, idx) => {
      if (idx !== i) return x;
      const sets = [...x.sets];
      sets[si] = { ...sets[si], [field]: value };
      return { ...x, sets };
    });
    updateToday(items);
  };
  const addSet = (i) => updateToday(todaysItems.map((x, idx) => idx === i ? { ...x, sets: [...x.sets, { wt:'', reps:'' }] } : x));
  const removeSet = (i, si) => updateToday(todaysItems.map((x, idx) => idx === i ? { ...x, sets: x.sets.filter((_,j) => j!==si) } : x));

  const finishedToday = todaysItems.filter(x => x.done).length;

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:6}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:24, margin:0, color:M_FG}}>Your weekly plan</h3>
          <span style={{padding:'4px 10px', borderRadius:6, background:M_SAGE_50, color:M_SAGE_700, fontSize:11, fontWeight:600, letterSpacing:'0.08em', fontFamily:'Geist Mono, monospace'}}>BUILT BY REA</span>
        </div>
        <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 18px'}}>Updated 2 days ago · Focus: hypertrophy + conditioning. Today is highlighted; tap an exercise to log the sets you performed.</p>
        <div style={{display:'grid', gridTemplateColumns:'repeat(2,1fr)', gap:12}}>
          {WEEKLY_PLAN.map((d,i)=>{
            const isToday = i === todayIdx;
            const items = isToday ? todaysItems : null;
            return (
              <div key={i} style={{padding:'14px 16px', background: isToday ? '#fff' : M_BONE_25, border:`${isToday?'2px':'1px'} solid ${isToday?M_ACCENT:M_BONE_200}`, borderRadius:10, position:'relative'}}>
                {isToday && <span style={{position:'absolute', top:-9, left:14, padding:'2px 8px', background:M_ACCENT, color:'#fff', fontSize:9.5, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', borderRadius:4, fontWeight:600}}>TODAY</span>}
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline', marginBottom:8}}>
                  <span style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:18, color:M_FG}}>{d.day}</span>
                  <span style={{fontSize:11, color:isToday?M_ACCENT:M_SAGE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', fontWeight:600, textTransform:'uppercase'}}>
                    {isToday && items ? `${finishedToday}/${items.length} done` : d.focus}
                  </span>
                </div>
                <ul style={{margin:0, padding:0, listStyle:'none'}}>
                  {(isToday && items ? items : d.exs.map(name => ({ name, done:false, sets:[] }))).map((e,j)=>{
                    const itemKey = `${i}-${j}`;
                    const isExp = expanded === itemKey && isToday;
                    return (
                      <li key={j} style={{borderTop: j>0?`1px solid ${M_BONE_200}`:0}}>
                        <div onClick={()=>isToday && toggleDone(j)}
                             style={{padding:'7px 0', fontSize:12, color: e.done ? M_BONE_500 : M_FG, display:'flex', alignItems:'center', gap:8, cursor:isToday?'pointer':'default', textDecoration: e.done?'line-through':'none'}}>
                          <span style={{width:14, height:14, borderRadius:4, border:`1.5px solid ${e.done?M_SAGE_500:M_BONE_500}`, background:e.done?M_SAGE_500:'#fff', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                            {e.done && <i data-lucide="check" style={{width:9, height:9, color:'#fff'}}></i>}
                          </span>
                          <span style={{flex:1}}>{e.name}</span>
                          {isToday && (
                            <button onClick={(ev)=>{ev.stopPropagation(); setExpanded(isExp ? null : itemKey);}}
                                    style={{background:'transparent', border:0, fontSize:10, color:isExp?M_ACCENT:M_BONE_700, cursor:'pointer', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', padding:'2px 6px'}}>
                              {isExp ? '− HIDE' : `+ LOG${e.sets.length?` (${e.sets.length})`:''}`}
                            </button>
                          )}
                        </div>
                        {isExp && (
                          <div style={{padding:'8px 0 12px 22px', borderBottom:`1px dashed ${M_BONE_200}`}}>
                            {e.sets.length === 0 && <div style={{fontSize:11, color:M_BONE_500, marginBottom:8, fontStyle:'italic'}}>No sets logged yet.</div>}
                            {e.sets.map((s, si) => (
                              <div key={si} style={{display:'flex', gap:6, alignItems:'center', marginBottom:6}}>
                                <span style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', width:34}}>SET {si+1}</span>
                                <input type="number" placeholder="kg" value={s.wt} onChange={ev=>updateSet(j, si, 'wt', ev.target.value)} style={{width:54, padding:'5px 8px', borderRadius:6, border:`1px solid ${M_BONE_100}`, fontSize:11, fontFamily:'Geist Mono, monospace', outline:'none', color:M_FG}}/>
                                <span style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace'}}>kg ×</span>
                                <input type="number" placeholder="reps" value={s.reps} onChange={ev=>updateSet(j, si, 'reps', ev.target.value)} style={{width:54, padding:'5px 8px', borderRadius:6, border:`1px solid ${M_BONE_100}`, fontSize:11, fontFamily:'Geist Mono, monospace', outline:'none', color:M_FG}}/>
                                <span style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace'}}>reps</span>
                                <button onClick={()=>removeSet(j, si)} style={{marginLeft:'auto', width:20, height:20, borderRadius:4, background:'transparent', border:`1px solid ${M_BONE_100}`, color:M_BONE_700, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}>
                                  <i data-lucide="x" style={{width:10, height:10}}></i>
                                </button>
                              </div>
                            ))}
                            <button onClick={()=>addSet(j)} style={{padding:'5px 10px', borderRadius:6, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:10.5, fontWeight:600, color:M_FG, cursor:'pointer', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>+ ADD SET</button>
                          </div>
                        )}
                      </li>
                    );
                  })}
                </ul>
              </div>
            );
          })}
        </div>
      </div>

      <div style={{background:M_SAGE, color:M_BONE_50, borderRadius:14, padding:'22px 28px', display:'flex', alignItems:'center', gap:16}}>
        <i data-lucide="book-open" style={{width:24, height:24, color:M_SAGE_LIGHT}}></i>
        <div style={{flex:1}}>
          <div style={{fontFamily:'Instrument Serif, serif', fontSize:22, lineHeight:1.2, margin:0, color:M_BONE_50}}>Need to know how a lift is done?</div>
          <div style={{fontSize:13, color:'#D2E0CB', marginTop:4}}>Open the Exercises tab for setup, form cues, and common mistakes — covers free weights, bodyweight, and gym machines.</div>
        </div>
        <button onClick={()=>window.dispatchEvent(new CustomEvent('forge:nav', {detail:'exercises'}))}
                style={{padding:'10px 16px', borderRadius:8, background:M_ACCENT, color:'#fff', border:0, fontSize:12, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>
          Open library →
        </button>
      </div>
    </div>
  );
}

// ============================================================
// Exercise library — free weights + bodyweight + 8 gym machines
// ============================================================
const EXERCISES = [
  // ----- Free weights -----
  { id:'squat', name:'Back Squat', type:'Free weights', equip:'Barbell + rack',
    target:['Quads','Glutes','Hamstrings','Core'], difficulty:'Intermediate',
    brief:'The king of leg lifts. Bar on traps, descend until thighs parallel, drive up through mid-foot.',
    steps:[
      'Set the bar 2–3 cm below shoulder height in the rack.',
      'Step under the bar, place it on your upper traps (not your neck), unrack and step back.',
      'Stance just outside hip-width, toes turned out 15–25°.',
      'Brace your core, descend by sitting back and down — knees track over toes.',
      'Drop until your hip crease passes your knees, then drive up through the whole foot.',
      'Lock out hips and knees at the top. Breathe. Repeat.',
    ],
    mistakes:['Knees caving inward','Heels lifting off the floor','Looking up — keep neck neutral'],
    tips:'Film a side-on set every 4 weeks to check depth and bar path.' },
  { id:'bench', name:'Bench Press', type:'Free weights', equip:'Barbell + flat bench',
    target:['Chest','Front delts','Triceps'], difficulty:'Intermediate',
    brief:'Lie flat, retract scapulae, lower the bar to mid-chest with control, press up to lockout.',
    steps:[
      'Lie back so your eyes are directly under the bar.',
      'Plant feet flat. Squeeze shoulder blades together and down into the bench.',
      'Grip the bar slightly wider than shoulder width. Unrack to over your shoulders.',
      'Lower the bar in a controlled arc to your lower-mid chest — elbows roughly 45° from your torso.',
      'Pause briefly when the bar touches the chest.',
      'Press up and slightly back toward your face. Lock out elbows.',
    ],
    mistakes:['Bouncing the bar off the chest','Flared elbows (90°)','Losing the upper-back arch'],
    tips:'Always use a spotter or safety arms when training near your max.' },
  { id:'deadlift', name:'Conventional Deadlift', type:'Free weights', equip:'Barbell + plates',
    target:['Posterior chain','Glutes','Lats','Grip'], difficulty:'Advanced',
    brief:'Hinge at the hips, grip the bar over mid-foot, drive the floor away while the bar slides up your legs.',
    steps:[
      'Bar over mid-foot, about 2 cm from your shins.',
      'Hinge at the hips, soften the knees, grip just outside your legs.',
      'Drop hips until shins touch the bar. Chest up, lats engaged.',
      'Take a big breath, brace, and push the floor away.',
      'Bar travels in a straight vertical line up your legs.',
      'Stand tall, squeeze glutes. Lower in reverse — hips back first, then knees.',
    ],
    mistakes:['Rounding the lower back','Jerking the bar off the floor','Hyperextending at the top'],
    tips:'Reset between every rep. Deadlifts are 5 sets of 1 — not 1 set of 5 grindy reps.' },
  { id:'ohp', name:'Overhead Press', type:'Free weights', equip:'Barbell',
    target:['Shoulders','Triceps','Upper chest','Core'], difficulty:'Intermediate',
    brief:'Standing barbell press from the front rack to lockout overhead. Squeeze glutes, ribs down.',
    steps:[
      'Bar racked at upper chest, hands just outside shoulder width.',
      'Elbows slightly in front of the bar. Wrists straight.',
      'Brace core, squeeze glutes — no lower-back arch.',
      'Press the bar straight up, moving your head slightly back so the bar can pass.',
      'At lockout the bar is directly over your mid-foot, head "through the window".',
      'Lower under control back to the front rack.',
    ],
    mistakes:['Pressing the bar in front of you (not overhead)','Hyperextending the lower back','Bending the wrists'],
    tips:'Press in front of a mirror until you trust the bar path.' },
  { id:'rdl', name:'Romanian Deadlift (RDL)', type:'Free weights', equip:'Barbell or dumbbells',
    target:['Hamstrings','Glutes','Lower back'], difficulty:'Intermediate',
    brief:'Hip-hinge, soft knees, bar slides down the thighs until you feel a big stretch in the hamstrings.',
    steps:[
      'Stand tall with the bar at hip height, hands just outside hips.',
      'Soften the knees about 15° — keep that bend through the whole rep.',
      'Push the hips straight back, sliding the bar down the front of your thighs.',
      'Stop when your hamstrings reach their stretch limit (usually mid-shin).',
      'Drive your hips forward to stand tall. Squeeze glutes at the top.',
    ],
    mistakes:['Rounding the back','Squatting the weight','Bar drifting away from the legs'],
    tips:'RDLs are felt, not muscled. If your lower back is doing the work, lighten the load.' },
  { id:'goblet', name:'Goblet Squat', type:'Free weights', equip:'Single dumbbell or kettlebell',
    target:['Quads','Glutes','Core'], difficulty:'Beginner',
    brief:'Hold a dumbbell at your chest, squat between your knees with an upright torso.',
    steps:[
      'Hold a dumbbell vertically at your chest, both palms cupping the top end.',
      'Stand slightly wider than hip-width, toes turned out a touch.',
      'Brace your core, then squat straight down — elbows pass between your knees.',
      'Pause for a beat at the bottom.',
      'Drive up through the whole foot to stand tall.',
    ],
    mistakes:['Letting the chest collapse forward','Knees caving inward','Heels rising'],
    tips:'The goblet position teaches the upright torso a back squat needs. Master this before the bar.' },
  { id:'dbrow', name:'Dumbbell Row', type:'Free weights', equip:'Dumbbell + bench',
    target:['Lats','Mid-back','Rear delts','Biceps'], difficulty:'Beginner',
    brief:'One knee and hand on a bench, row a dumbbell from a long-arm hang up to your hip.',
    steps:[
      'Place left knee and left hand on a flat bench. Right foot on the floor.',
      'Pick up the dumbbell with your right hand — arm fully extended.',
      'Keep your back flat, parallel to the floor.',
      'Row the dumbbell up to your hip, leading with the elbow.',
      'Squeeze the shoulder blade at the top, then lower under control.',
    ],
    mistakes:['Twisting the torso to throw the weight up','Pulling with the arm instead of the back','Rounded upper back'],
    tips:'Imagine you\'re starting a lawnmower — that\'s the path.' },

  // ----- Bodyweight -----
  { id:'pullup', name:'Pull-up', type:'Bodyweight', equip:'Pull-up bar',
    target:['Lats','Mid-back','Biceps'], difficulty:'Intermediate',
    brief:'Hang from a bar, pull your chest toward it by driving your elbows down and back.',
    steps:[
      'Grip the bar slightly wider than shoulder width, palms facing away.',
      'Hang with arms fully extended, shoulders pulled down (not shrugged).',
      'Drive your elbows down and toward your hips, pulling your chest up to the bar.',
      'Chin clears the bar at the top.',
      'Lower under control to a full hang. That\'s one rep.',
    ],
    mistakes:['Kipping/swinging momentum','Half-reps that never reach a full hang','Shrugging instead of pulling with the lats'],
    tips:'If you can\'t do a full rep yet: 3 sets of 5-second slow lowers from the top position.' },
  { id:'plank', name:'Plank', type:'Bodyweight', equip:'Floor mat',
    target:['Core','Shoulders'], difficulty:'Beginner',
    brief:'Forearms and toes on the floor, body in a straight line — keep glutes squeezed and ribs pulled down.',
    steps:[
      'Forearms on the floor, elbows directly under shoulders.',
      'Legs extended, balanced on the balls of your feet.',
      'Body forms a straight line from heel to head.',
      'Squeeze glutes, tuck the pelvis slightly, pull ribs toward hips.',
      'Hold steady, breathing normally, for the prescribed time.',
    ],
    mistakes:['Hips sagging','Hips piking up','Holding your breath'],
    tips:'30 quality seconds beats 2 minutes with a sagging back. Stop when form breaks.' },
  { id:'boxjump', name:'Box Jump', type:'Bodyweight', equip:'Plyo box',
    target:['Power','Glutes','Quads','Calves'], difficulty:'Intermediate',
    brief:'Explode up from a quarter squat onto a box, land soft, step (don\'t jump) back down.',
    steps:[
      'Stand about 30 cm in front of a stable box.',
      'Drop into a quarter squat, arms swinging back.',
      'Explode up, swinging the arms forward and pulling the knees up.',
      'Land softly on top of the box — both feet flat, hips and knees bent to absorb.',
      'Stand tall on the box, then step (don\'t jump) back down.',
    ],
    mistakes:['Jumping back down — eats your Achilles','Landing with locked knees','Choosing a box that\'s too high'],
    tips:'Start lower than you think. Box jumps are a power lift, not an ego lift.' },

  // ----- Machines (gym equipment) -----
  { id:'machine-chest', name:'Chest Press Machine', type:'Machine', equip:'Plate-loaded or selectorized machine',
    target:['Chest','Front delts','Triceps'], difficulty:'Beginner',
    brief:'Seat adjusted so handles meet at mid-chest. Press out, control the negative back to a deep stretch.',
    steps:[
      'Adjust the seat height so the handles align with the middle of your chest, not your shoulders.',
      'Sit back, plant feet flat, press shoulder blades into the pad.',
      'Grip the handles with wrists straight and elbows slightly below shoulder height.',
      'Press the handles forward to nearly-locked elbows (don\'t hyperextend).',
      'Lower under control over 2–3 seconds until you feel a stretch in the chest.',
      'Keep your back against the pad the entire time — don\'t shrug forward.',
    ],
    mistakes:['Seat too low or too high → wrong shoulder angle','Lifting hips off the seat to push harder','Bouncing the stack at the bottom'],
    tips:'If you feel it in your front shoulders more than chest, drop the seat one notch.' },
  { id:'machine-lat', name:'Lat Pulldown', type:'Machine', equip:'Cable pulldown station',
    target:['Lats','Mid-back','Biceps'], difficulty:'Beginner',
    brief:'Wide grip, pull the bar to your upper chest by driving your elbows down — lean back slightly, not all the way.',
    steps:[
      'Adjust the thigh pad so you sit firmly anchored.',
      'Take a slightly wider than shoulder-width grip on the bar, palms forward.',
      'Sit tall, lean back about 15° from vertical and hold that angle.',
      'Pull the bar down to your upper chest, leading with the elbows driving toward your hips.',
      'Squeeze the lats at the bottom for a count, then control the bar back up to a full stretch.',
    ],
    mistakes:['Pulling the bar behind the neck (shoulder risk)','Rocking the torso back and forth','Pulling with the arms instead of the back'],
    tips:'If you can\'t feel your lats, lighten the weight by 30% and slow each rep down.' },
  { id:'machine-legpress', name:'Leg Press', type:'Machine', equip:'45° sled or horizontal press',
    target:['Quads','Glutes','Hamstrings'], difficulty:'Beginner',
    brief:'Feet shoulder-width, mid-foot pressing through the platform. Lower until knees reach ~90°, then press back.',
    steps:[
      'Sit back with your hips fully in the seat, lower back pressed against the pad.',
      'Place feet shoulder-width apart on the platform, mid-way up.',
      'Release the safety handles and lower the platform under control.',
      'Stop when your knees reach about 90° — your lower back must stay flat against the pad.',
      'Press through the whole foot (not the toes) back to nearly-locked knees. Don\'t fully lock out.',
    ],
    mistakes:['Lower back rounding off the pad at the bottom','Hands pulling the knees apart','Locking knees fully at the top'],
    tips:'Feet higher on the platform = more glute/hamstring. Lower = more quad. Pick the one you need.' },
  { id:'machine-legext', name:'Leg Extension', type:'Machine', equip:'Quad extension machine',
    target:['Quads'], difficulty:'Beginner',
    brief:'Isolation for the quads. Sit tall, extend knees fully, control the descent.',
    steps:[
      'Adjust the seat so the back of your knees sit at the edge of the seat pad.',
      'Set the shin pad just above your ankle, not on your foot.',
      'Sit tall, grip the side handles, brace your core.',
      'Extend your knees fully until your legs are straight — squeeze the quads at the top for 1 count.',
      'Lower under control over 2 seconds. Don\'t let the stack slam.',
    ],
    mistakes:['Using too much weight and bouncing the start','Lifting hips off the seat to swing the weight','Not pausing at the top'],
    tips:'Great for warming up the knees before squats — light weight, high reps.' },
  { id:'machine-legcurl', name:'Lying Leg Curl', type:'Machine', equip:'Prone leg curl machine',
    target:['Hamstrings','Calves'], difficulty:'Beginner',
    brief:'Lie face-down, curl the pad toward your glutes by contracting the hamstrings — keep hips pinned.',
    steps:[
      'Lie face-down with knees just past the bench edge.',
      'Place the roller pad on the back of your ankles (Achilles area).',
      'Grip the handles, push hips down into the pad.',
      'Curl your heels toward your glutes by contracting the hamstrings.',
      'Squeeze at the top, then lower under control over 2 seconds.',
    ],
    mistakes:['Hips lifting off the bench','Using momentum to fling the weight up','Partial reps that never stretch the hamstrings'],
    tips:'Point your toes for more hamstring; flex your feet for more calf involvement.' },
  { id:'machine-seatedrow', name:'Seated Cable Row', type:'Machine', equip:'Low cable + V-handle',
    target:['Mid-back','Lats','Rear delts','Biceps'], difficulty:'Beginner',
    brief:'Sit tall, pull the handle to your sternum by squeezing your shoulder blades together. Don\'t lean back to cheat.',
    steps:[
      'Sit with feet planted on the platform, knees slightly bent.',
      'Grab the V-handle, sit tall with a long spine — not arched, not rounded.',
      'Pull the handle to the bottom of your sternum, driving your elbows back along your sides.',
      'Squeeze your shoulder blades together at the back of every rep for 1 count.',
      'Let the handle travel forward into a full stretch, but don\'t round your lower back.',
    ],
    mistakes:['Rocking the torso forward and backward','Pulling to the belly instead of the sternum','Shrugging shoulders up to the ears'],
    tips:'If you can\'t feel your mid-back working, drop the weight and pause 2 seconds at the squeeze.' },
  { id:'machine-smith', name:'Smith Machine Squat', type:'Machine', equip:'Smith machine + bar',
    target:['Quads','Glutes'], difficulty:'Beginner',
    brief:'A fixed bar path makes Smith squats forgiving for beginners and a great quad-focus tool with feet slightly forward.',
    steps:[
      'Set the bar at upper-chest height on the Smith.',
      'Step under the bar, place it across your upper back (not your neck).',
      'Walk your feet 15–20 cm forward of the bar — this lets you squat down with vertical shins.',
      'Unrack by rotating wrists, brace your core.',
      'Squat down until thighs are parallel, knees tracking over toes.',
      'Drive up to standing, rotate wrists to re-rack the bar safely.',
    ],
    mistakes:['Feet directly under the bar — that compresses knees uncomfortably','Forgetting to re-engage the safety hooks before stepping out','Going too heavy too fast because the bar is "guided"'],
    tips:'Smith squats are not free squats. Use them as an accessory or for higher-rep volume work.' },
  { id:'machine-cable', name:'Cable Crossover', type:'Machine', equip:'Dual-cable pulley station',
    target:['Chest','Front delts'], difficulty:'Intermediate',
    brief:'Both pulleys set high. Step forward and bring the handles together in front of your hips in a soft arc.',
    steps:[
      'Set both pulleys at the highest setting.',
      'Grip one handle in each hand, step one foot forward to brace yourself.',
      'Start with arms wide, slight bend in the elbows — keep that bend the whole rep.',
      'Bring both handles together in front of your hips, crossing slightly for a peak squeeze.',
      'Hold the squeeze for 1 count, then let the arms travel back open under control.',
    ],
    mistakes:['Straightening the elbows mid-rep (turns it into a triceps move)','Using momentum to fling the handles together','Letting the shoulders shrug forward at the top'],
    tips:'Lower pulleys = upper-chest focus. Higher pulleys = lower-chest focus. Try both.' },
];

const DIFFICULTY_COLOR = {
  'Beginner':     ['#E8EFE5','#4A7A4F'],
  'Intermediate': ['#F4E7CC','#8A6628'],
  'Advanced':     ['#F1D8D4','#8E342C'],
};
const TYPE_COLOR = {
  'Free weights': ['#EEEEEA', '#4A4A45'],
  'Bodyweight':   ['#EEEEEA', '#4A4A45'],
  'Machine':      ['#D2E0CB', '#2E5535'],
};

// ============================================================
// MemberExercises — library + modal with next/prev navigation
// ============================================================
function MemberExercises() {
  const [open, setOpen] = React.useState(null);
  const [filter, setFilter] = React.useState('All');
  const [typeFilter, setTypeFilter] = React.useState('All');
  const [q, setQ] = React.useState('');

  const filters = ['All','Beginner','Intermediate','Advanced'];
  const types   = ['All','Free weights','Bodyweight','Machine'];
  const shown = EXERCISES.filter(e =>
    (filter==='All' || e.difficulty===filter) &&
    (typeFilter==='All' || e.type===typeFilter) &&
    (q==='' || e.name.toLowerCase().includes(q.toLowerCase()) || e.target.join(' ').toLowerCase().includes(q.toLowerCase()))
  );

  React.useEffect(() => { setTimeout(() => window.lucide && window.lucide.createIcons(), 30); }, [open, filter, typeFilter, q]);

  const openIndex = open ? shown.findIndex(e => e.id === open.id) : -1;
  const prev = () => openIndex > 0 && setOpen(shown[openIndex - 1]);
  const next = () => openIndex >= 0 && openIndex < shown.length - 1 && setOpen(shown[openIndex + 1]);

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:16, marginBottom:14}}>
          <div>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:24, margin:0, color:M_FG}}>Exercise library</h3>
            <p style={{fontSize:13, color:M_BONE_700, margin:'4px 0 0'}}>Free weights, bodyweight, and gym machines. Tap any card for setup, form cues, and common mistakes.</p>
          </div>
          <div style={{position:'relative', flexShrink:0}}>
            <i data-lucide="search" style={{width:13, height:13, position:'absolute', left:11, top:11, color:M_BONE_500}}></i>
            <input value={q} onChange={e=>setQ(e.target.value)} placeholder="Search by name or muscle…"
                   style={{height:34, width:240, padding:'0 12px 0 30px', borderRadius:8, border:`1px solid ${M_BONE_100}`, background:M_BONE_25, fontSize:12, fontFamily:'inherit', outline:'none', color:M_FG}}/>
          </div>
        </div>
        <div style={{display:'flex', gap:8, marginBottom:8, flexWrap:'wrap'}}>
          <span style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', alignSelf:'center', marginRight:4, textTransform:'uppercase'}}>Type</span>
          {types.map(t => (
            <button key={t} onClick={()=>setTypeFilter(t)}
                    style={{padding:'6px 12px', borderRadius:999, background:typeFilter===t?M_SAGE:'#fff', color:typeFilter===t?M_BONE_50:M_FG, border:`1px solid ${typeFilter===t?M_SAGE:M_BONE_100}`, fontSize:11.5, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>{t}</button>
          ))}
        </div>
        <div style={{display:'flex', gap:8, marginBottom:18, flexWrap:'wrap'}}>
          <span style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', alignSelf:'center', marginRight:4, textTransform:'uppercase'}}>Level</span>
          {filters.map(f => (
            <button key={f} onClick={()=>setFilter(f)}
                    style={{padding:'6px 12px', borderRadius:999, background:filter===f?M_SAGE:'#fff', color:filter===f?M_BONE_50:M_FG, border:`1px solid ${filter===f?M_SAGE:M_BONE_100}`, fontSize:11.5, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>{f}</button>
          ))}
        </div>

        {shown.length === 0 ? (
          <div style={{padding:'48px 16px', textAlign:'center', color:M_BONE_500, fontStyle:'italic', fontFamily:'Instrument Serif, serif', fontSize:18}}>No exercises match those filters.</div>
        ) : (
          <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:14}}>
            {shown.map(ex => {
              const [bg, fg] = DIFFICULTY_COLOR[ex.difficulty];
              const [tBg, tFg] = TYPE_COLOR[ex.type] || ['#EEEEEA','#4A4A45'];
              return (
                <div key={ex.id} onClick={()=>setOpen(ex)}
                     style={{padding:14, background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:12, cursor:'pointer', transition:'all 160ms', display:'flex', flexDirection:'column'}}
                     onMouseEnter={e=>{ e.currentTarget.style.borderColor = M_ACCENT; e.currentTarget.style.transform = 'translateY(-2px)'; }}
                     onMouseLeave={e=>{ e.currentTarget.style.borderColor = M_BONE_200; e.currentTarget.style.transform = 'translateY(0)'; }}>
                  <MImageSlot label={ex.name} ratio="16/10" radius={8} accent={M_SAGE}/>
                  <div style={{display:'flex', gap:6, marginTop:10, flexWrap:'wrap'}}>
                    <span style={{padding:'2px 7px', borderRadius:4, background:tBg, color:tFg, fontSize:9.5, fontWeight:600, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>{ex.type.toUpperCase()}</span>
                    <span style={{padding:'2px 7px', borderRadius:4, background:bg, color:fg, fontSize:9.5, fontWeight:600, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>{ex.difficulty.toUpperCase()}</span>
                  </div>
                  <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline', marginTop:8, marginBottom:4}}>
                    <span style={{fontFamily:'Instrument Serif, serif', fontSize:18, color:M_FG, lineHeight:1.1}}>{ex.name}</span>
                  </div>
                  <div style={{fontSize:11, color:M_BONE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', marginBottom:8, textTransform:'uppercase'}}>{ex.target.slice(0,3).join(' · ')}</div>
                  <p style={{fontSize:12.5, color:M_FG, lineHeight:1.5, margin:'0 0 12px', flex:1}}>{ex.brief}</p>
                  <button style={{padding:'7px 10px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', justifyContent:'center', gap:6}}>
                    <i data-lucide="book-open" style={{width:12, height:12}}></i>Full instructions
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {open && <ExerciseModal exercise={open} onClose={()=>setOpen(null)} onPrev={openIndex>0?prev:null} onNext={openIndex<shown.length-1?next:null} position={`${openIndex+1} / ${shown.length}`}/>}
    </div>
  );
}

function ExerciseModal({ exercise, onClose, onPrev, onNext, position }) {
  React.useEffect(() => {
    setTimeout(() => window.lucide && window.lucide.createIcons(), 30);
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowLeft' && onPrev) onPrev();
      if (e.key === 'ArrowRight' && onNext) onNext();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onPrev, onNext]);
  const [bg, fg] = DIFFICULTY_COLOR[exercise.difficulty];
  return (
    <div onClick={onClose} style={{position:'fixed', inset:0, background:'rgba(14,26,17,0.55)', backdropFilter:'blur(4px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:100, padding:24}}>
      <div onClick={e=>e.stopPropagation()} style={{background:'#fff', borderRadius:18, maxWidth:880, width:'100%', maxHeight:'88vh', overflow:'auto', boxShadow:'0 28px 60px rgba(14,26,17,0.32)', display:'grid', gridTemplateColumns:'1.1fr 1.4fr', position:'relative'}}>
        <div style={{padding:'28px 28px', background:M_BONE_25, borderRight:`1px solid ${M_BONE_200}`}}>
          <MImageSlot label={exercise.name} ratio="4/5" radius={12} accent={M_SAGE}/>
          <div style={{marginTop:18, display:'flex', gap:6, flexWrap:'wrap'}}>
            <span style={{padding:'3px 9px', borderRadius:6, background:bg, color:fg, fontSize:10, fontWeight:600, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>{exercise.difficulty.toUpperCase()}</span>
            <span style={{padding:'3px 9px', borderRadius:6, background:M_SAGE_50, color:M_SAGE_700, fontSize:10, fontWeight:600, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>{exercise.equip.toUpperCase()}</span>
          </div>
          <div style={{marginTop:14}}>
            <div style={{fontSize:10, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:6}}>Target muscles</div>
            <div style={{display:'flex', gap:6, flexWrap:'wrap'}}>
              {exercise.target.map(t => (
                <span key={t} style={{padding:'4px 10px', borderRadius:999, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:11, color:M_FG, fontWeight:500}}>{t}</span>
              ))}
            </div>
          </div>
        </div>

        <div style={{padding:'28px 32px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:14, gap:14}}>
            <div style={{minWidth:0, flex:1}}>
              <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:4}}>{exercise.type} · how to perform · {position}</div>
              <h2 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:34, lineHeight:1, margin:0, color:M_FG, letterSpacing:'-0.02em'}}>{exercise.name}</h2>
            </div>
            <div style={{display:'flex', gap:6, flexShrink:0}}>
              <button onClick={onPrev} disabled={!onPrev} title="Previous (←)"
                      style={{width:32, height:32, borderRadius:8, background:M_BONE_25, border:`1px solid ${M_BONE_100}`, cursor:onPrev?'pointer':'default', opacity:onPrev?1:0.4, display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide="chevron-left" style={{width:14, height:14, color:M_FG}}></i>
              </button>
              <button onClick={onNext} disabled={!onNext} title="Next (→)"
                      style={{width:32, height:32, borderRadius:8, background:M_BONE_25, border:`1px solid ${M_BONE_100}`, cursor:onNext?'pointer':'default', opacity:onNext?1:0.4, display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide="chevron-right" style={{width:14, height:14, color:M_FG}}></i>
              </button>
              <button onClick={onClose} title="Close (Esc)"
                      style={{width:32, height:32, borderRadius:8, background:M_BONE_25, border:`1px solid ${M_BONE_100}`, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide="x" style={{width:14, height:14, color:M_FG}}></i>
              </button>
            </div>
          </div>

          <p style={{fontSize:14, color:M_FG, lineHeight:1.55, margin:'0 0 20px'}}>{exercise.brief}</p>

          <div style={{marginBottom:20}}>
            <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:10}}>Setup & execution</div>
            <ol style={{margin:0, padding:0, listStyle:'none'}}>
              {exercise.steps.map((s,i)=>(
                <li key={i} style={{display:'flex', gap:12, padding:'8px 0', borderBottom: i<exercise.steps.length-1?`1px solid ${M_BONE_200}`:0}}>
                  <span style={{width:24, height:24, borderRadius:6, background:M_SAGE, color:M_SAGE_LIGHT, fontSize:11, fontWeight:600, fontFamily:'Geist Mono, monospace', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>{String(i+1).padStart(2,'0')}</span>
                  <span style={{fontSize:13, color:M_FG, lineHeight:1.55}}>{s}</span>
                </li>
              ))}
            </ol>
          </div>

          <div style={{padding:'14px 16px', background:'#F1D8D4', border:'1px solid #E5B8B3', borderRadius:10, marginBottom:14}}>
            <div style={{fontSize:11, color:'#8E342C', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8, display:'flex', alignItems:'center', gap:6}}>
              <i data-lucide="alert-triangle" style={{width:12, height:12}}></i>Avoid
            </div>
            <ul style={{margin:0, paddingLeft:18, fontSize:12.5, color:'#5C1F19', lineHeight:1.6}}>
              {exercise.mistakes.map((m,i)=><li key={i}>{m}</li>)}
            </ul>
          </div>

          <div style={{padding:'14px 16px', background:M_SAGE_50, border:`1px solid #D2E0CB`, borderRadius:10, fontSize:13, color:M_SAGE_700, lineHeight:1.55, display:'flex', gap:10}}>
            <i data-lucide="lightbulb" style={{width:14, height:14, color:M_SAGE_700, flexShrink:0, marginTop:2}}></i>
            <div><strong style={{color:M_FG}}>Coach tip · </strong>{exercise.tips}</div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ============================================================
// MemberFood — persisted food log with AI calorie estimation
// ============================================================
function MemberFood() {
  const [meals, setMeals] = useLocalState('meals', [
    { id:1, date: ymd(today()), time:'08:14', label:'Breakfast', desc:'Greek yogurt 200g, honey 1 tbsp, blueberries 80g, walnuts 20g', kcal:340, protein:18, carbs:38, fat:14, source:'AI' },
    { id:2, date: ymd(today()), time:'12:48', label:'Lunch',     desc:'Grilled chicken breast 180g, brown rice 150g cooked, mixed salad with olive oil', kcal:520, protein:48, carbs:54, fat:14, source:'AI' },
  ]);
  const settings = readLocal('settings', { goalKcal:1800, goalProtein:130, goalCarbs:180, goalFat:60 });
  const DAILY_GOAL = { kcal: settings.goalKcal || 1800, protein: settings.goalProtein || 130, carbs: settings.goalCarbs || 180, fat: settings.goalFat || 60 };

  const [desc, setDesc] = React.useState('');
  const [label, setLabel] = React.useState('Snack');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [preview, setPreview] = React.useState(null);

  const todayKey = ymd(today());
  const todaysMeals = meals.filter(m => m.date === todayKey);
  const totals = todaysMeals.reduce((a,m) => ({
    kcal: a.kcal + m.kcal, protein: a.protein + (m.protein||0),
    carbs: a.carbs + (m.carbs||0), fat: a.fat + (m.fat||0),
  }), { kcal:0, protein:0, carbs:0, fat:0 });

  const estimate = async () => {
    if (!desc.trim() || busy) return;
    setBusy(true); setError(null); setPreview(null);
    try {
      const prompt = `You are a registered-dietitian-grade calorie estimator. The user logged the following meal:

"${desc.trim()}"

Estimate the totals for the WHOLE meal as described (sum all portions). Respond with ONLY a single JSON object, no prose, no markdown fences. Schema:
{"kcal": <integer>, "protein_g": <integer>, "carbs_g": <integer>, "fat_g": <integer>, "confidence": "low"|"medium"|"high", "note": "<one short sentence on assumptions>"}

If the description is too vague to estimate, return confidence "low" but still give your best guess based on typical portion sizes.`;
      const raw = await window.claude.complete(prompt);
      const match = raw.match(/\{[\s\S]*\}/);
      if (!match) throw new Error('Bad reply');
      const obj = JSON.parse(match[0]);
      setPreview({
        kcal: Math.round(obj.kcal),
        protein: Math.round(obj.protein_g),
        carbs: Math.round(obj.carbs_g),
        fat: Math.round(obj.fat_g),
        confidence: obj.confidence,
        note: obj.note,
      });
    } catch (e) {
      setError('Could not estimate — try adding portion sizes (e.g. "200g", "1 cup").');
    } finally {
      setBusy(false);
    }
  };

  const saveMeal = () => {
    if (!preview) return;
    const now = new Date();
    const time = `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
    setMeals([{
      id: Date.now(), date: todayKey, time, label, desc: desc.trim(),
      kcal: preview.kcal, protein: preview.protein, carbs: preview.carbs, fat: preview.fat,
      source: 'AI', confidence: preview.confidence,
    }, ...meals]);
    setDesc(''); setPreview(null); setError(null);
  };

  const removeMeal = (id) => setMeals(meals.filter(m => m.id !== id));

  const Macro = ({label, val, goal, color}) => {
    const pct = goal > 0 ? Math.min(1, val/goal) : 0;
    return (
      <div style={{padding:'14px 16px', background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:10}}>
        <div style={{fontSize:11, color:M_BONE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase', marginBottom:6}}>{label}</div>
        <div style={{display:'flex', alignItems:'baseline', gap:4, marginBottom:8}}>
          <span style={{fontFamily:'Instrument Serif, serif', fontSize:28, color:M_FG, lineHeight:1}}>{val}</span>
          <span style={{fontSize:11, color:M_BONE_700}}>/ {goal}{label==='Calories'?' kcal':' g'}</span>
        </div>
        <div style={{height:5, background:M_BONE_100, borderRadius:3, overflow:'hidden'}}>
          <div style={{width:`${pct*100}%`, height:'100%', background:color, transition:'width 320ms'}}></div>
        </div>
      </div>
    );
  };

  // Past days summary — last 6 days excluding today.
  const pastDays = (() => {
    const out = [];
    for (let i = 1; i <= 6; i++) {
      const d = new Date(); d.setDate(d.getDate() - i);
      const k = ymd(d);
      const dayMeals = meals.filter(m => m.date === k);
      if (dayMeals.length) {
        const t = dayMeals.reduce((a,m) => ({ kcal: a.kcal + m.kcal, n: a.n + 1 }), { kcal:0, n:0 });
        out.push({ key:k, label:fmtDate(d).split(',')[0].slice(0,3) + ' ' + d.getDate(), kcal:t.kcal, count:t.n });
      }
    }
    return out;
  })();

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:14}}>
          <div>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:24, margin:0, color:M_FG}}>Today's intake</h3>
            <div style={{fontSize:12, color:M_BONE_700, marginTop:4}}>Goal set by Rea · {DAILY_GOAL.kcal} kcal · adjust in Settings</div>
          </div>
          <div style={{textAlign:'right'}}>
            <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:M_FG, letterSpacing:'-0.02em'}}>{totals.kcal}</div>
            <div style={{fontSize:11, color:totals.kcal>DAILY_GOAL.kcal?'#A8413A':M_SAGE_500, fontFamily:'Geist Mono, monospace', fontWeight:600, marginTop:4}}>
              {totals.kcal>DAILY_GOAL.kcal ? `+${totals.kcal-DAILY_GOAL.kcal} OVER` : `${DAILY_GOAL.kcal-totals.kcal} LEFT`}
            </div>
          </div>
        </div>
        <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:12}}>
          <Macro label="Calories" val={totals.kcal}    goal={DAILY_GOAL.kcal}    color={M_ACCENT}/>
          <Macro label="Protein"  val={totals.protein} goal={DAILY_GOAL.protein} color={M_SAGE_500}/>
          <Macro label="Carbs"    val={totals.carbs}   goal={DAILY_GOAL.carbs}   color={'#B8893A'}/>
          <Macro label="Fat"      val={totals.fat}     goal={DAILY_GOAL.fat}     color={'#4B6B7A'}/>
        </div>
      </div>

      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:14}}>
          <div style={{width:34, height:34, borderRadius:10, background:M_ACCENT, display:'flex', alignItems:'center', justifyContent:'center'}}>
            <i data-lucide="sparkles" style={{width:16, height:16, color:'#fff'}}></i>
          </div>
          <div>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:M_FG}}>Log a meal</h3>
            <div style={{fontSize:12, color:M_BONE_700, marginTop:2}}>Describe what you ate — AI estimates calories and macros for you. Saved permanently.</div>
          </div>
        </div>

        <div style={{display:'flex', gap:8, marginBottom:10}}>
          {['Breakfast','Lunch','Dinner','Snack'].map(l => (
            <button key={l} onClick={()=>setLabel(l)}
                    style={{padding:'7px 12px', borderRadius:8, background:label===l?M_SAGE:'#fff', color:label===l?M_BONE_50:M_FG, border:`1px solid ${label===l?M_SAGE:M_BONE_100}`, fontSize:12, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>{l}</button>
          ))}
        </div>

        <textarea value={desc} onChange={e=>{setDesc(e.target.value); setPreview(null); setError(null);}}
                  placeholder='e.g. "200g grilled salmon, 150g brown rice, a small green salad with olive oil"'
                  style={{width:'100%', minHeight:74, padding:'12px 14px', borderRadius:10, border:`1px solid ${M_BONE_100}`, fontSize:13, fontFamily:'inherit', resize:'vertical', boxSizing:'border-box', outline:'none', color:M_FG, background:M_BONE_25, lineHeight:1.55}}/>

        {error && <div style={{padding:'10px 14px', background:'#F1D8D4', border:'1px solid #E5B8B3', borderRadius:8, fontSize:12, color:'#A8413A', marginTop:10}}>{error}</div>}

        {preview && (
          <div style={{marginTop:14, padding:'14px 16px', background:M_SAGE_50, border:`1px solid #D2E0CB`, borderRadius:10}}>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:10}}>
              <div style={{fontSize:11, color:M_SAGE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase'}}>AI estimate · {preview.confidence} confidence</div>
              <button onClick={estimate} style={{background:'transparent', border:0, fontSize:11, color:M_SAGE_700, cursor:'pointer', fontWeight:500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>Re-estimate ↻</button>
            </div>
            <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:10, marginBottom:10}}>
              {[['kcal',preview.kcal],['protein',preview.protein+'g'],['carbs',preview.carbs+'g'],['fat',preview.fat+'g']].map(([k,v])=>(
                <div key={k} style={{padding:'10px 12px', background:'#fff', borderRadius:8, border:`1px solid #D2E0CB`}}>
                  <div style={{fontSize:10, color:M_SAGE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{k}</div>
                  <div style={{fontFamily:'Instrument Serif, serif', fontSize:24, color:M_FG, lineHeight:1, marginTop:4}}>{v}</div>
                </div>
              ))}
            </div>
            {preview.note && <div style={{fontSize:11.5, color:M_BONE_700, fontStyle:'italic', marginBottom:10}}>"{preview.note}"</div>}
            <button onClick={saveMeal} style={{padding:'10px 16px', borderRadius:8, background:M_SAGE, color:M_BONE_50, border:0, fontSize:13, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>Add to today's log</button>
          </div>
        )}

        {!preview && (
          <div style={{display:'flex', gap:8, marginTop:10, alignItems:'center'}}>
            <button onClick={estimate} disabled={busy || !desc.trim()} style={{padding:'10px 16px', borderRadius:8, background: busy||!desc.trim()?M_BONE_100:M_ACCENT, color:'#fff', border:0, fontSize:13, fontWeight:600, cursor:busy||!desc.trim()?'default':'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', gap:8}}>
              {busy ? <><i data-lucide="loader" style={{width:13, height:13}}></i>Estimating…</> : <><i data-lucide="sparkles" style={{width:13, height:13}}></i>Estimate with AI</>}
            </button>
            <span style={{fontSize:11, color:M_BONE_500, fontStyle:'italic'}}>Add portion sizes (g, ml, cups) for the best estimate.</span>
          </div>
        )}
      </div>

      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:M_FG}}>Today's meals</h3>
          <span style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{todaysMeals.length} logged · Rea can review</span>
        </div>
        {todaysMeals.length === 0 ? (
          <div style={{padding:'40px 16px', textAlign:'center', color:M_BONE_500, fontStyle:'italic', fontFamily:'Instrument Serif, serif', fontSize:18}}>No meals logged yet today.</div>
        ) : (
          todaysMeals.map((m,i) => (
            <div key={m.id} style={{display:'flex', alignItems:'flex-start', gap:14, padding:'14px 0', borderBottom: i<todaysMeals.length-1?`1px solid ${M_BONE_200}`:0}}>
              <div style={{width:54, padding:'8px 0', textAlign:'center', flexShrink:0}}>
                <div style={{fontFamily:'Geist Mono, monospace', fontSize:11, color:'#8FB286', letterSpacing:'0.06em'}}>{m.time}</div>
                <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:14, color:M_FG, marginTop:2}}>{m.label}</div>
              </div>
              <div style={{flex:1, minWidth:0}}>
                <div style={{fontSize:13, color:M_FG, lineHeight:1.5}}>{m.desc}</div>
                <div style={{display:'flex', gap:14, marginTop:6, fontSize:11, color:M_BONE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>
                  <span><strong style={{color:M_FG}}>{m.kcal}</strong> KCAL</span>
                  <span>{m.protein}P</span><span>{m.carbs}C</span><span>{m.fat}F</span>
                  {m.source==='AI' && <span style={{color:M_ACCENT}}>● AI EST</span>}
                </div>
              </div>
              <button onClick={()=>removeMeal(m.id)} title="Remove"
                      style={{width:28, height:28, borderRadius:6, background:'transparent', border:`1px solid ${M_BONE_100}`, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide="trash-2" style={{width:13, height:13, color:M_BONE_700}}></i>
              </button>
            </div>
          ))
        )}
      </div>

      {pastDays.length > 0 && (
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:20, margin:'0 0 14px', color:M_FG}}>Past days</h3>
          <div style={{display:'grid', gridTemplateColumns:`repeat(${pastDays.length},1fr)`, gap:10}}>
            {pastDays.map(d => (
              <div key={d.key} style={{padding:'12px 14px', background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:10}}>
                <div style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{d.label}</div>
                <div style={{display:'flex', alignItems:'baseline', gap:4, marginTop:6}}>
                  <span style={{fontFamily:'Instrument Serif, serif', fontSize:22, color:M_FG, lineHeight:1}}>{d.kcal}</span>
                  <span style={{fontSize:10, color:M_BONE_700}}>kcal</span>
                </div>
                <div style={{fontSize:10, color:M_BONE_700, fontFamily:'Geist Mono, monospace', marginTop:2}}>{d.count} meal{d.count===1?'':'s'}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div style={{background:M_SAGE, color:M_BONE_50, borderRadius:14, padding:'18px 24px', display:'flex', alignItems:'center', gap:14}}>
        <div style={{width:36, height:36, borderRadius:10, background:'rgba(74,122,79,0.18)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
          <i data-lucide="eye" style={{width:16, height:16, color:M_SAGE_LIGHT}}></i>
        </div>
        <div style={{flex:1}}>
          <div style={{fontSize:13, fontWeight:600, color:M_BONE_50}}>Your trainer Rea sees this log</div>
          <div style={{fontSize:12, color:'#D2E0CB', marginTop:2}}>She uses it to adjust your plan. Be honest — there is no "cheat day" judgment.</div>
        </div>
      </div>
    </div>
  );
}

window.MemberWorkouts = MemberWorkouts;
window.MemberExercises = MemberExercises;
window.MemberFood = MemberFood;
