// Forge — Member Web Portal — sidebar shell + home + shared helpers + persistence
// useLocalState is the single source of truth for everything that survives a refresh:
//   forge.meals, forge.coachChat, forge.workoutLog, forge.measurementReq,
//   forge.membershipReq, forge.coachLastSeen, forge.settings.

const M_SAGE       = '#0E1A11';
const M_SAGE_900   = '#18271B';
const M_SAGE_800   = '#1A2E1A';
const M_SAGE_700   = '#2E5535';
const M_SAGE_500   = '#4A7A4F';
const M_SAGE_LIGHT = '#6A9466';
const M_SAGE_50    = '#E8EFE5';
const M_BONE_50    = '#FAFAFA';
const M_BONE_25    = '#F5F5F3';
const M_BONE_100   = '#EEEEEA';
const M_BONE_200   = '#F0F0EC';
const M_BONE_500   = '#6A6A65';
const M_BONE_700   = '#4A4A45';
const M_FG         = '#0E1A11';
const M_ACCENT     = '#4A7A4F';   // single accent — replaces old M_COPPER

// ----- useLocalState -----------------------------------------------------
// Drop-in for useState that persists to localStorage under "forge.<key>".
function useLocalState(key, initial) {
  const fullKey = 'forge.' + key;
  const [v, setV] = React.useState(() => {
    try {
      const raw = localStorage.getItem(fullKey);
      if (raw == null) return typeof initial === 'function' ? initial() : initial;
      return JSON.parse(raw);
    } catch { return typeof initial === 'function' ? initial() : initial; }
  });
  const setAndPersist = React.useCallback((nv) => {
    setV(prev => {
      const next = typeof nv === 'function' ? nv(prev) : nv;
      try { localStorage.setItem(fullKey, JSON.stringify(next)); } catch {}
      return next;
    });
  }, [fullKey]);
  return [v, setAndPersist];
}

// One-shot reader for components that don't need reactivity.
function readLocal(key, fallback) {
  try {
    const raw = localStorage.getItem('forge.' + key);
    return raw == null ? fallback : JSON.parse(raw);
  } catch { return fallback; }
}

// ----- Date / formatting helpers ----------------------------------------
function today() { return new Date(); }
function fmtDate(d, { withDay = true } = {}) {
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const days   = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
  const dayName = days[d.getDay()];
  return withDay ? `${dayName}, ${d.getDate()} ${months[d.getMonth()]}` : `${d.getDate()} ${months[d.getMonth()]} ${d.getFullYear()}`;
}
function ymd(d) { return d.toISOString().slice(0,10); }

// -------------------- Sidebar --------------------
function MemberSidebar({ active, onSelect }) {
  // Coach unread = has the chat changed since last visit?
  const [lastSeen] = useLocalState('coachLastSeen', 0);
  const chat = readLocal('coachChat', []);
  const lastMsgTs = chat.length ? (chat[chat.length-1].ts || 0) : 0;
  const hasUnread = active !== 'coach' && lastMsgTs > lastSeen;

  const items = [
    { id:'home',       label:'Home',         ico:'house' },
    { id:'progress',   label:'Progress',     ico:'trending-up' },
    { id:'workouts',   label:'Workouts',     ico:'dumbbell' },
    { id:'exercises',  label:'Exercises',    ico:'book-open' },
    { id:'food',       label:'Food log',     ico:'utensils' },
    { id:'coach',      label:'AI coach',     ico:'sparkles', dot: hasUnread },
    { id:'trainer',    label:'Trainer chat', ico:'message-circle' },
    { id:'membership', label:'Membership',   ico:'id-card' },
    { id:'settings',   label:'Settings',     ico:'settings' },
  ];

  const settings = readLocal('settings', { firstName:'Elira', lastName:'Krasniqi', plan:'Premium', daysLeft:23 });
  const initials = (settings.firstName[0] + settings.lastName[0]).toUpperCase();

  return (
    <aside style={{width:240, background:M_SAGE, borderRight:`1px solid ${M_SAGE_800}`, display:'flex', flexDirection:'column', flexShrink:0}}>
      <div style={{padding:'22px 22px', display:'flex', alignItems:'center', gap:10}}>
        <svg width="22" height="22" viewBox="0 0 32 32"><path d="M6 26V6h20" stroke="#FAFAFA" strokeWidth="3.2" fill="none"/><path d="M6 16h12" stroke="#FAFAFA" strokeWidth="3.2"/><circle cx="24" cy="24" r="2.2" fill={M_ACCENT}/></svg>
        <span style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:20, color:'#FAFAFA'}}>forge<span style={{color:M_ACCENT}}>.</span></span>
      </div>
      <nav style={{flex:1, padding:'8px', overflowY:'auto'}}>
        {items.map(it => (
          <a key={it.id} href="#"
             onClick={(e)=>{e.preventDefault(); onSelect && onSelect(it.id);}}
             style={{display:'flex', alignItems:'center', gap:10, padding:'9px 12px', borderRadius:8,
                     background: active===it.id ? 'rgba(74,122,79,0.16)' : 'transparent',
                     color: active===it.id ? M_ACCENT : '#D2E0CB',
                     fontSize:13, fontWeight: active===it.id ? 600 : 500,
                     textDecoration:'none', marginBottom:2, cursor:'pointer'}}>
            <i data-lucide={it.ico} style={{width:15, height:15}}></i>
            <span style={{flex:1}}>{it.label}</span>
            {it.dot && <span style={{width:6, height:6, borderRadius:'50%', background:M_ACCENT}}></span>}
          </a>
        ))}
      </nav>
      <div style={{padding:'12px 16px', borderTop:`1px solid ${M_SAGE_800}`, display:'flex', alignItems:'center', gap:10}}>
        <div style={{width:32, height:32, borderRadius:'50%', background:M_ACCENT, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600}}>{initials}</div>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:12, fontWeight:600, color:'#FAFAFA'}}>{settings.firstName} {settings.lastName}</div>
          <div style={{fontSize:10, color:'#8FB286'}}>{settings.plan} · {settings.daysLeft}d left</div>
        </div>
        <button onClick={()=>{ if (confirm('Sign out? (demo)')) { /* sign-out demo */ } }}
                title="Sign out"
                style={{width:28, height:28, borderRadius:6, background:'transparent', border:`1px solid ${M_SAGE_800}`, color:'#D2E0CB', cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}>
          <i data-lucide="log-out" style={{width:13, height:13}}></i>
        </button>
      </div>
    </aside>
  );
}

// -------------------- Home --------------------
function MemberHome({ onNavigate }) {
  const settings = readLocal('settings', { firstName:'Elira', dailyKcal:1800 });
  const meals = readLocal('meals', []);
  const todayKey = ymd(today());
  const todaysMeals = meals.filter(m => m.date === todayKey);
  const kcalToday = todaysMeals.reduce((s,m) => s + m.kcal, 0);

  // Streak = consecutive days with at least one check-in (proxy: any logged meal counts).
  const checkInDays = readLocal('checkIns', []); // [{date:'YYYY-MM-DD'}]
  const streak = (() => {
    let n = 0; const d = today();
    while (true) {
      const k = ymd(d);
      if (checkInDays.some(x => x.date === k)) { n++; d.setDate(d.getDate()-1); }
      else if (n === 0) { d.setDate(d.getDate()-1); }   // grace: still allow if today empty
      else break;
      if (n > 365) break;
    }
    return n || 12; // demo fallback
  })();

  const dateLabel = fmtDate(today());

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16, marginBottom:20}}>
        <div style={{background:M_SAGE, color:'#FAFAFA', borderRadius:14, padding:'28px 30px', position:'relative', overflow:'hidden'}}>
          <div style={{position:'absolute', top:-40, right:-40, width:180, height:180, borderRadius:'50%', background:'rgba(74,122,79,0.16)'}}></div>
          <div style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB', marginBottom:8, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>{dateLabel}</div>
          <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:48, lineHeight:1, margin:'0 0 14px', letterSpacing:'-0.02em', color:'#FAFAFA'}}>Welcome back, <em style={{color:M_SAGE_LIGHT}}>{settings.firstName}</em>.</h1>
          <p style={{fontSize:14, color:'#D2E0CB', maxWidth:420, margin:'0 0 24px', lineHeight:1.55}}>You're down 1.8 kg since April and your coach has a new suggestion for this week.</p>
          <div style={{display:'flex', gap:32, position:'relative'}}>
            <div><div style={{fontSize:11, color:'#D2E0CB', fontFamily:'Geist Mono, monospace'}}>YOUR PIN</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:24, fontWeight:500, letterSpacing:'0.16em'}}>4 7 2 9</div></div>
            <div><div style={{fontSize:11, color:'#D2E0CB', fontFamily:'Geist Mono, monospace'}}>CHECK-IN STREAK</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:24, fontWeight:500}}>{streak} <span style={{fontSize:14, color:'#8FB286'}}>days</span></div></div>
            <div><div style={{fontSize:11, color:'#D2E0CB', fontFamily:'Geist Mono, monospace'}}>KCAL TODAY</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:24, fontWeight:500}}>{kcalToday}<span style={{fontSize:14, color:'#8FB286'}}> / {settings.dailyKcal || 1800}</span></div></div>
          </div>
        </div>
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', display:'flex', flexDirection:'column'}}>
          <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:14}}>
            <div style={{width:34, height:34, borderRadius:10, background:M_ACCENT, display:'flex', alignItems:'center', justifyContent:'center'}}><i data-lucide="sparkles" style={{width:16, height:16, color:'#fff'}}></i></div>
            <div>
              <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:18, color:M_FG}}>Coach</div>
              <div style={{fontSize:10, color:M_SAGE_500, display:'flex', alignItems:'center', gap:4}}><span style={{width:5, height:5, borderRadius:'50%', background:M_SAGE_500}}></span>Online</div>
            </div>
          </div>
          <p style={{fontSize:13, color:M_FG, lineHeight:1.55, margin:'0 0 14px'}}>Your weight has held at <strong>78.4 kg</strong> for 9 days. I'd swap Tuesday's chest for legs and pull bedtime earlier this week.</p>
          <div style={{marginTop:'auto', display:'flex', gap:8}}>
            <button onClick={()=>onNavigate && onNavigate('coach')}
                    style={{flex:1, padding:'9px 12px', borderRadius:8, background:M_SAGE, color:'#FAFAFA', border:0, fontSize:12, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>
              Open chat →
            </button>
            <button onClick={()=>onNavigate && onNavigate('workouts')}
                    style={{padding:'9px 12px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:600, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>
              View plan
            </button>
          </div>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16}}>
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:10}}>
            <div>
              <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:4}}>Weight · 30 days</div>
              <div style={{display:'flex', alignItems:'baseline', gap:8}}>
                <span style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:M_FG, letterSpacing:'-0.02em'}}>78.4</span>
                <span style={{fontSize:13, color:M_BONE_500}}>kg</span>
                <span style={{padding:'2px 8px', borderRadius:4, background:'#E8EFE5', color:'#4A7A4F', fontSize:11, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>−1.8 kg</span>
              </div>
            </div>
            <button onClick={()=>onNavigate && onNavigate('progress')}
                    style={{background:'transparent', border:0, fontSize:12, color:M_SAGE_700, fontWeight:500, cursor:'pointer', padding:'4px 0', fontFamily:'inherit'}}>
              Full progress →
            </button>
          </div>
          {(() => {
            const points = [80.2,79.8,79.5,79.6,79.1,78.7,78.4,78.5,78.4,78.4];
            const max = Math.max(...points), min = Math.min(...points);
            const w = 600, h = 100;
            const path = points.map((p,i)=>`${i===0?'M':'L'} ${(i/(points.length-1))*w} ${h-((p-min)/(max-min))*h}`).join(' ');
            return (
              <svg viewBox={`0 0 ${w} ${h+10}`} width="100%" height="140" style={{marginTop:8}}>
                <defs><linearGradient id="memgrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor={M_SAGE_500} stopOpacity="0.22"/><stop offset="100%" stopColor={M_SAGE_500} stopOpacity="0"/></linearGradient></defs>
                <path d={`${path} L ${w} ${h} L 0 ${h} Z`} fill="url(#memgrad)"/>
                <path d={path} stroke={M_SAGE} strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
                <circle cx={w} cy={h-((points[points.length-1]-min)/(max-min))*h} r="4" fill={M_SAGE}/>
              </svg>
            );
          })()}
        </div>
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
            <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase'}}>Today · Apple Watch</div>
            <span title="Apple Watch integration is wired up by your trainer at onboarding"
                  style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', display:'flex', alignItems:'center', gap:4}}>
              <i data-lucide="watch" style={{width:11, height:11}}></i>HEALTHKIT
            </span>
          </div>
          <div style={{display:'flex', flexDirection:'column', gap:14}}>
            <Stat ico="footprints" label="Steps"      val="8,247" sub="/ 10k" pct={0.82} color={M_SAGE_500}/>
            <Stat ico="heart-pulse" label="Resting HR" val="62"    sub="bpm"  pct={null}/>
            <Stat ico="flame"      label="Active kcal" val="412"   sub="/ 600" pct={0.69} color={M_SAGE_LIGHT}/>
          </div>
        </div>
      </div>
    </div>
  );
}

function Stat({ico, label, val, sub, pct, color}) {
  return (
    <div style={{display:'flex', alignItems:'center', gap:12}}>
      <div style={{width:34, height:34, borderRadius:10, background:M_SAGE_50, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
        <i data-lucide={ico} style={{width:16, height:16, color:M_SAGE_700}}></i>
      </div>
      <div style={{flex:1, minWidth:0}}>
        <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{label}</div>
        <div style={{display:'flex', alignItems:'baseline', gap:4, marginTop:2}}>
          <span style={{fontFamily:'Geist Mono, monospace', fontSize:16, fontWeight:500, color:M_FG}}>{val}</span>
          <span style={{fontSize:11, color:M_BONE_500}}>{sub}</span>
        </div>
      </div>
      {pct !== null && (
        <svg viewBox="0 0 36 36" width="34" height="34">
          <circle cx="18" cy="18" r="14" stroke="#EEEEEA" strokeWidth="2.5" fill="none"/>
          <circle cx="18" cy="18" r="14" stroke={color} strokeWidth="2.5" fill="none" strokeDasharray={`${88*pct} 88`} strokeLinecap="round" transform="rotate(-90 18 18)"/>
        </svg>
      )}
    </div>
  );
}

// Tiny inline striped placeholder used by exercise/machine cards.
function ImageSlot({ label, ratio = '4/3', radius = 10, accent = M_SAGE }) {
  return (
    <div style={{
      aspectRatio: ratio, borderRadius: radius,
      background: `repeating-linear-gradient(135deg, ${M_BONE_25} 0 10px, ${M_BONE_100} 10px 20px)`,
      border: `1px solid ${M_BONE_100}`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      position: 'relative', overflow: 'hidden'
    }}>
      <span style={{position:'absolute', top:8, left:10, fontSize:9, letterSpacing:'0.14em', color:M_BONE_500, fontFamily:'Geist Mono, monospace', textTransform:'uppercase'}}>Photo</span>
      <span style={{padding:'4px 10px', background:accent, color:'#FAFAFA', fontSize:10, fontFamily:'Geist Mono, monospace', letterSpacing:'0.12em', borderRadius:4, textTransform:'uppercase'}}>{label}</span>
    </div>
  );
}

window.MemberSidebar = MemberSidebar;
window.MemberHome = MemberHome;
window.MemberImageSlot = ImageSlot;
window.MemberStat = Stat;
window.useLocalState = useLocalState;
window.readLocal = readLocal;
window.M_TOKENS = { M_SAGE, M_SAGE_900, M_SAGE_800, M_SAGE_700, M_SAGE_500, M_SAGE_LIGHT, M_SAGE_50, M_BONE_50, M_BONE_25, M_BONE_100, M_BONE_200, M_BONE_500, M_BONE_700, M_FG, M_ACCENT, M_COPPER: M_ACCENT };
window.M_HELPERS = { today, fmtDate, ymd };
