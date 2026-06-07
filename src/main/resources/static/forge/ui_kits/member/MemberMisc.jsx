// Forge — Member Web Portal — Trainer chat + Settings/Profile
// Trainer chat = persisted member ↔ trainer conversation. The trainer side is
// simulated with a canned welcome thread; new messages from the member persist.
// Settings = profile, daily targets, language, notification toggles.

const { M_SAGE, M_SAGE_900, M_SAGE_800, M_SAGE_700, M_SAGE_500, M_SAGE_LIGHT, M_SAGE_50,
        M_BONE_50, M_BONE_25, M_BONE_100, M_BONE_200, M_BONE_500, M_BONE_700,
        M_FG, M_ACCENT } = window.M_TOKENS;
const { today, ymd } = window.M_HELPERS;
const useLocalState = window.useLocalState;

// ============================================================
// MemberTrainer — chat with assigned trainer (Rea)
// ============================================================
function MemberTrainer() {
  const seed = [
    { from:'trainer', text:'Welcome Elira — I checked your plan, see you Tuesday 09:30 for HIIT.', t:'Mon · 08:14' },
    { from:'trainer', text:'Try to keep dinner protein-forward tonight (160–200g chicken or fish).', t:'Mon · 18:02' },
    { from:'me',      text:'Got it, thanks! Will salmon be ok?', t:'Mon · 18:08' },
    { from:'trainer', text:'Perfect. Pair with broccoli and rice. We can recalibrate macros after Friday\'s weigh-in.', t:'Mon · 18:09' },
  ];
  const [messages, setMessages] = useLocalState('trainerChat', seed);
  const [input, setInput] = React.useState('');
  const scrollRef = React.useRef(null);

  React.useEffect(() => {
    if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    setTimeout(() => window.lucide && window.lucide.createIcons(), 30);
  }, [messages]);

  const send = (e) => {
    e && e.preventDefault();
    const txt = input.trim();
    if (!txt) return;
    const now = new Date();
    const stamp = `${['Sun','Mon','Tue','Wed','Thu','Fri','Sat'][now.getDay()]} · ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`;
    setMessages([...messages, { from:'me', text:txt, t:stamp }]);
    setInput('');
  };

  return (
    <div style={{padding:'28px 32px 64px', display:'flex', justifyContent:'center'}}>
      <div style={{maxWidth:740, width:'100%', background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', display:'flex', flexDirection:'column', height:'calc(100vh - 160px)'}}>
        <div style={{display:'flex', alignItems:'center', gap:14, marginBottom:18, paddingBottom:14, borderBottom:`1px solid ${M_BONE_200}`}}>
          <div style={{width:48, height:48, borderRadius:'50%', background:M_SAGE_50, color:M_SAGE_700, display:'flex', alignItems:'center', justifyContent:'center', fontSize:14, fontWeight:600}}>RH</div>
          <div style={{flex:1}}>
            <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:24, color:M_FG, lineHeight:1}}>Rea Hoxha</div>
            <div style={{fontSize:12, color:M_SAGE_500, display:'flex', alignItems:'center', gap:6, marginTop:2}}>
              <span style={{width:6, height:6, borderRadius:'50%', background:M_SAGE_500}}></span>Your trainer · usually replies within 2h
            </div>
          </div>
          <button onClick={()=>window.dispatchEvent(new CustomEvent('forge:nav', {detail:'workouts'}))}
                  style={{padding:'7px 12px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', gap:6}}>
            <i data-lucide="dumbbell" style={{width:12, height:12}}></i>See plan
          </button>
        </div>

        <div ref={scrollRef} style={{flex:1, overflowY:'auto', paddingRight:6, marginBottom:14}}>
          {messages.map((m,i) => (
            m.from === 'trainer' ? (
              <div key={i} style={{display:'flex', gap:10, marginBottom:14, maxWidth:'85%'}}>
                <div style={{width:28, height:28, borderRadius:'50%', background:M_SAGE_50, color:M_SAGE_700, display:'flex', alignItems:'center', justifyContent:'center', fontSize:10, fontWeight:600, flexShrink:0}}>RH</div>
                <div>
                  <div style={{background:M_BONE_25, border:`1px solid ${M_BONE_200}`, padding:'10px 14px', borderRadius:'4px 14px 14px 14px', fontSize:14, color:M_FG, lineHeight:1.55}}>{m.text}</div>
                  <div style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', marginTop:4, marginLeft:4}}>{m.t}</div>
                </div>
              </div>
            ) : (
              <div key={i} style={{display:'flex', flexDirection:'column', alignItems:'flex-end', marginBottom:14}}>
                <div style={{background:M_SAGE, color:M_BONE_50, padding:'10px 14px', borderRadius:'14px 14px 4px 14px', fontSize:14, maxWidth:'80%', lineHeight:1.5}}>{m.text}</div>
                <div style={{fontSize:10, color:M_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', marginTop:4, marginRight:4}}>{m.t}</div>
              </div>
            )
          ))}
        </div>

        <form onSubmit={send} style={{display:'flex', gap:8, background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:999, padding:'6px 6px 6px 16px', alignItems:'center'}}>
          <input value={input} onChange={e=>setInput(e.target.value)} placeholder="Message Rea…" style={{flex:1, fontSize:13, color:M_FG, background:'transparent', border:0, outline:'none', fontFamily:'inherit'}}/>
          <button type="submit" disabled={!input.trim()} style={{width:34, height:34, borderRadius:'50%', background: !input.trim()?M_BONE_100:M_ACCENT, border:0, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', cursor:!input.trim()?'default':'pointer'}}>
            <i data-lucide="arrow-up" style={{width:14, height:14}}></i>
          </button>
        </form>
      </div>
    </div>
  );
}

// ============================================================
// MemberSettings — profile, daily targets, language, notifications
// ============================================================
const DEFAULT_SETTINGS = {
  firstName:'Elira', lastName:'Krasniqi',
  email:'elira.krasniqi@example.com', phone:'+355 69 234 5678',
  language:'EN',
  goalKcal:1800, goalProtein:130, goalCarbs:180, goalFat:60, goalWeight:72,
  notif: { coach:true, trainer:true, renewal:true, marketing:false },
  plan:'Premium', daysLeft:23,
};

function MemberSettings() {
  const [s, setS] = useLocalState('settings', DEFAULT_SETTINGS);
  const update = (patch) => setS({ ...s, ...patch });
  const updateNotif = (key, value) => setS({ ...s, notif: { ...s.notif, [key]: value } });

  const [savedFlash, setSavedFlash] = React.useState(false);
  const flashSaved = () => { setSavedFlash(true); setTimeout(()=>setSavedFlash(false), 1400); };

  return (
    <div style={{padding:'28px 32px 64px', maxWidth:900, margin:'0 auto'}}>
      {/* Profile */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', marginBottom:16}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:18}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:24, margin:0, color:M_FG}}>Profile</h3>
          {savedFlash && <span style={{fontSize:11, color:M_SAGE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>● SAVED</span>}
        </div>
        <div style={{display:'flex', gap:24, alignItems:'flex-start'}}>
          <div style={{width:88, height:88, borderRadius:'50%', background:M_SAGE_50, color:M_SAGE_700, display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontSize:34}}>
            {(s.firstName[0]+s.lastName[0]).toUpperCase()}
          </div>
          <div style={{flex:1, display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
            <Field label="First name" value={s.firstName} onChange={v=>{update({firstName:v}); flashSaved();}}/>
            <Field label="Last name"  value={s.lastName}  onChange={v=>{update({lastName:v}); flashSaved();}}/>
            <Field label="Email"      value={s.email}     onChange={v=>{update({email:v}); flashSaved();}}/>
            <Field label="Phone"      value={s.phone}     onChange={v=>{update({phone:v}); flashSaved();}}/>
          </div>
        </div>
      </div>

      {/* Language */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', marginBottom:16}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 6px', color:M_FG}}>Language</h3>
        <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 14px'}}>The whole interface, AI coach replies and trainer messages will use this language.</p>
        <div style={{display:'flex', gap:8}}>
          {[{code:'EN', name:'English'},{code:'SQ', name:'Shqip'}].map(o => (
            <button key={o.code} onClick={()=>{update({language:o.code}); flashSaved();}}
                    style={{padding:'10px 18px', borderRadius:8, background:s.language===o.code?M_SAGE:'#fff', color:s.language===o.code?M_BONE_50:M_FG, border:`1px solid ${s.language===o.code?M_SAGE:M_BONE_100}`, fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', gap:8}}>
              <span style={{fontFamily:'Geist Mono, monospace', fontWeight:600}}>{o.code}</span>
              <span>{o.name}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Daily targets */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', marginBottom:16}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 6px', color:M_FG}}>Daily targets</h3>
        <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 14px'}}>Used by the food log and the AI coach. Rea can override these from her dashboard.</p>
        <div style={{display:'grid', gridTemplateColumns:'repeat(5,1fr)', gap:12}}>
          <NumField label="Calories"   suffix="kcal" value={s.goalKcal}    onChange={v=>{update({goalKcal:v}); flashSaved();}}/>
          <NumField label="Protein"    suffix="g"    value={s.goalProtein} onChange={v=>{update({goalProtein:v}); flashSaved();}}/>
          <NumField label="Carbs"      suffix="g"    value={s.goalCarbs}   onChange={v=>{update({goalCarbs:v}); flashSaved();}}/>
          <NumField label="Fat"        suffix="g"    value={s.goalFat}     onChange={v=>{update({goalFat:v}); flashSaved();}}/>
          <NumField label="Goal weight" suffix="kg"  value={s.goalWeight}  onChange={v=>{update({goalWeight:v}); flashSaved();}}/>
        </div>
      </div>

      {/* Notifications */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', marginBottom:16}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:M_FG}}>Notifications</h3>
        {[
          ['coach',    'AI coach insights',    'When the coach detects a plateau or has a new suggestion.'],
          ['trainer',  'Trainer messages',     'New messages from Rea, session reminders, plan updates.'],
          ['renewal',  'Membership reminders', 'A nudge in the last week before your membership expires.'],
          ['marketing','Gym news & events',    'Group classes, new equipment, special events at the gym.'],
        ].map(([key, title, sub]) => (
          <div key={key} style={{display:'flex', alignItems:'center', gap:14, padding:'12px 0', borderBottom:`1px solid ${M_BONE_200}`}}>
            <div style={{flex:1}}>
              <div style={{fontSize:13, fontWeight:600, color:M_FG}}>{title}</div>
              <div style={{fontSize:12, color:M_BONE_700, marginTop:2}}>{sub}</div>
            </div>
            <Toggle on={s.notif[key]} onChange={v=>{updateNotif(key, v); flashSaved();}}/>
          </div>
        ))}
      </div>

      {/* Danger zone */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 6px', color:M_FG}}>Account</h3>
        <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 14px'}}>Reset wipes all your locally-stored data (chat history, meals, completed workouts). Membership state is unaffected.</p>
        <div style={{display:'flex', gap:8}}>
          <button onClick={()=>alert('Password change link sent to your email (demo)')}
                  style={{padding:'10px 16px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>
            Change password
          </button>
          <button onClick={()=>{
                    if (confirm('Wipe all locally-stored data? This clears chat history, meals, completed workouts, and any pending requests.')) {
                      Object.keys(localStorage).filter(k => k.startsWith('forge.')).forEach(k => localStorage.removeItem(k));
                      location.reload();
                    }
                  }}
                  style={{padding:'10px 16px', borderRadius:8, background:'#fff', border:`1px solid #E5B8B3`, fontSize:12, fontWeight:500, color:'#A8413A', cursor:'pointer', fontFamily:'inherit'}}>
            Reset local data
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({ label, value, onChange }) {
  return (
    <label style={{display:'block'}}>
      <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase', marginBottom:6}}>{label}</div>
      <input value={value} onChange={e=>onChange(e.target.value)}
             style={{width:'100%', padding:'10px 12px', borderRadius:8, border:`1px solid ${M_BONE_100}`, fontSize:13, fontFamily:'inherit', outline:'none', color:M_FG, background:M_BONE_25, boxSizing:'border-box'}}/>
    </label>
  );
}
function NumField({ label, suffix, value, onChange }) {
  return (
    <label style={{display:'block', padding:'14px 16px', background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:10}}>
      <div style={{fontSize:11, color:M_BONE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase', marginBottom:6}}>{label}</div>
      <div style={{display:'flex', alignItems:'baseline', gap:4}}>
        <input type="number" value={value} onChange={e=>onChange(+e.target.value || 0)}
               style={{width:'100%', padding:'2px 0', border:0, outline:'none', background:'transparent', fontFamily:'Instrument Serif, serif', fontSize:24, color:M_FG, fontWeight:400}}/>
        <span style={{fontSize:11, color:M_BONE_700, flexShrink:0}}>{suffix}</span>
      </div>
    </label>
  );
}
function Toggle({ on, onChange }) {
  return (
    <button onClick={()=>onChange(!on)}
            style={{width:42, height:24, borderRadius:999, background:on?M_ACCENT:M_BONE_100, border:0, cursor:'pointer', position:'relative', flexShrink:0, transition:'background 160ms'}}>
      <span style={{position:'absolute', top:3, left:on?21:3, width:18, height:18, borderRadius:'50%', background:'#fff', transition:'left 160ms', boxShadow:'0 1px 2px rgba(0,0,0,0.2)'}}></span>
    </button>
  );
}

window.MemberTrainer = MemberTrainer;
window.MemberSettings = MemberSettings;
