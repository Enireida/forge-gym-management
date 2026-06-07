// Forge mobile app — member screens. Each is a self-contained component
// that renders inside an IOSFrame. Premium wellness feel: bone canvas,
// sage accents, Instrument Serif display, generous white space.

const F_SAGE = '#0E1A11';
const F_SAGE_700 = '#18271B';
const F_SAGE_500 = '#4A7A4F';
const F_SAGE_50 = '#E8EFE5';
const F_BONE_50 = '#FAFAFA';
const F_BONE_25 = '#F5F5F3';
const F_BONE_700 = '#4A4A45';
const F_BONE_500 = '#8A8A85';
const F_FG = '#0E0E0C';
const F_COPPER = '#4A7A4F';

// ============================================================
// 1. LOGIN SCREEN
// ============================================================
function MobileLogin() {
  return (
    <div style={{height:'100%', background: F_BONE_50, display:'flex', flexDirection:'column', padding:'80px 28px 32px'}}>
      <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:64}}>
        <svg width="22" height="22" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 26V6h20" stroke={F_SAGE} strokeWidth="3.2" fill="none" strokeLinecap="square"/>
          <path d="M6 16h12" stroke={F_SAGE} strokeWidth="3.2" strokeLinecap="square"/>
          <circle cx="24" cy="24" r="2.2" fill={F_COPPER}/>
        </svg>
        <span style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:22, color:F_FG, letterSpacing:'-0.01em'}}>forge<span style={{color:F_COPPER}}>.</span></span>
      </div>

      <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:46, lineHeight:1, letterSpacing:'-0.025em', margin:'0 0 14px', color:F_FG}}>
        Welcome <em style={{color:F_SAGE_700}}>back</em>.
      </h1>
      <p style={{fontSize:15, lineHeight:1.5, color:F_BONE_700, margin:'0 0 40px', maxWidth:280}}>
        Sign in to track your progress, check in, and chat with your AI coach.
      </p>

      <label style={{fontSize:11, fontWeight:600, letterSpacing:'0.16em', textTransform:'uppercase', color:F_BONE_500, marginBottom:8}}>Email</label>
      <input type="email" placeholder="elira@example.com" defaultValue="elira.k@gmail.com"
        style={{height:52, padding:'0 16px', borderRadius:14, border:'1px solid #D6D6D2', background:'#fff', fontSize:16, color:F_FG, marginBottom:18, fontFamily:'inherit', outline:'none'}} />

      <label style={{fontSize:11, fontWeight:600, letterSpacing:'0.16em', textTransform:'uppercase', color:F_BONE_500, marginBottom:8}}>Password</label>
      <div style={{position:'relative', marginBottom:8}}>
        <input type="password" defaultValue="••••••••••"
          style={{height:52, padding:'0 16px', borderRadius:14, border:`1.5px solid ${F_SAGE_500}`, background:'#fff', fontSize:16, color:F_FG, fontFamily:'inherit', outline:'none', width:'100%', boxShadow:'0 0 0 4px rgba(90,125,79,0.12)'}} />
      </div>
      <a href="#" style={{fontSize:13, color:F_SAGE_700, textDecoration:'none', marginBottom:32}}>Forgot password?</a>

      <button style={{height:54, borderRadius:999, background:F_SAGE, color:F_BONE_50, border:0, fontSize:15, fontWeight:500, fontFamily:'inherit', marginTop:8, cursor:'pointer'}}>Sign in →</button>

      <div style={{flex:1}}></div>
      <div style={{textAlign:'center', fontSize:13, color:F_BONE_700}}>
        New to Forge? <a href="#" style={{color:F_SAGE_700, fontWeight:500, textDecoration:'none'}}>Create account</a>
      </div>
    </div>
  );
}

// ============================================================
// 2. HOME / DASHBOARD
// ============================================================
function MobileHome() {
  return (
    <div style={{height:'100%', background: F_BONE_50, overflowY:'auto', paddingBottom:100}}>
      <div style={{padding:'72px 24px 16px', display:'flex', justifyContent:'space-between', alignItems:'flex-start'}}>
        <div>
          <div style={{fontSize:13, color:F_BONE_700, marginBottom:4}}>Sunday, May 11</div>
          <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:38, lineHeight:1, letterSpacing:'-0.02em', margin:0, color:F_FG}}>
            Hey, <em style={{color:F_SAGE_700}}>Elira</em>.
          </h1>
        </div>
        <div style={{position:'relative'}}>
          <div style={{width:44, height:44, borderRadius:'50%', background: F_SAGE_700, color:F_BONE_50, display:'flex', alignItems:'center', justifyContent:'center', fontWeight:600, fontSize:14}}>EK</div>
          <div style={{position:'absolute', top:0, right:-2, width:10, height:10, borderRadius:'50%', background:F_COPPER, border:`2px solid ${F_BONE_50}`}}></div>
        </div>
      </div>

      {/* Membership card */}
      <div style={{margin:'8px 24px 24px', borderRadius:24, background:F_SAGE, color:F_BONE_50, padding:24, position:'relative', overflow:'hidden'}}>
        <div style={{position:'absolute', top:-30, right:-30, width:140, height:140, borderRadius:'50%', background:'rgba(74,122,79,0.15)'}}></div>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:18, position:'relative'}}>
          <div>
            <div style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB', marginBottom:6}}>Membership · Premium</div>
            <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:22, color:F_BONE_50}}>Active</div>
          </div>
          <div style={{padding:'6px 12px', borderRadius:999, background:'rgba(245,241,232,0.12)', fontSize:11, fontWeight:500, color:F_BONE_50}}>23 days left</div>
        </div>
        <div style={{display:'flex', alignItems:'center', gap:18, position:'relative'}}>
          <div style={{flex:1}}>
            <div style={{fontSize:11, letterSpacing:'0.14em', textTransform:'uppercase', color:'#D2E0CB', marginBottom:4}}>Your PIN</div>
            <div style={{fontFamily:'Geist Mono, monospace', fontSize:30, letterSpacing:'0.18em', fontWeight:500}}>4 7 2 9</div>
          </div>
          <div style={{width:64, height:64, borderRadius:14, background:F_BONE_50, padding:6, display:'flex', alignItems:'center', justifyContent:'center'}}>
            <svg viewBox="0 0 50 50" width="48" height="48">
              {[...Array(25)].map((_, i) => {
                const x = i % 5, y = Math.floor(i / 5);
                const filled = [0,2,3,5,7,8,9,11,13,15,16,17,19,20,22,24].includes(i);
                return filled ? <rect key={i} x={x*10} y={y*10} width="10" height="10" fill={F_FG}/> : null;
              })}
            </svg>
          </div>
        </div>
      </div>

      {/* Quick actions */}
      <div style={{padding:'0 24px 24px', display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
        <button style={{background:'#fff', border:'1px solid #D6D6D2', borderRadius:18, padding:'16px 14px', textAlign:'left', cursor:'pointer'}}>
          <div style={{width:36, height:36, borderRadius:10, background:F_SAGE_50, display:'flex', alignItems:'center', justifyContent:'center', marginBottom:10}}><i data-lucide="qr-code" style={{width:18, height:18, color:F_SAGE_700}}></i></div>
          <div style={{fontSize:14, fontWeight:600, color:F_FG}}>Check in</div>
          <div style={{fontSize:12, color:F_BONE_700}}>Scan at door</div>
        </button>
        <button style={{background:'#fff', border:'1px solid #D6D6D2', borderRadius:18, padding:'16px 14px', textAlign:'left', cursor:'pointer'}}>
          <div style={{width:36, height:36, borderRadius:10, background:'#D2E0CB', display:'flex', alignItems:'center', justifyContent:'center', marginBottom:10}}><i data-lucide="utensils" style={{width:18, height:18, color:F_COPPER}}></i></div>
          <div style={{fontSize:14, fontWeight:600, color:F_FG}}>Log meal</div>
          <div style={{fontSize:12, color:F_BONE_700}}>Snap a photo</div>
        </button>
      </div>

      {/* Today's stats */}
      <div style={{padding:'0 24px 16px'}}>
        <div style={{display:'flex', alignItems:'baseline', justifyContent:'space-between', marginBottom:12}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:F_FG}}>Today</h3>
          <span style={{fontSize:12, color:F_BONE_700}}>From Apple Watch</span>
        </div>
        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:20, overflow:'hidden'}}>
          <StatRow ico="footprints" iconBg="#F2F6EF" iconFg={F_SAGE_700} label="Steps" val="8,247" goal="10,000" pct={0.82} color={F_SAGE_500}/>
          <div style={{height:1, background:'#F0F0EC'}}></div>
          <StatRow ico="heart-pulse" iconBg="#F1D8D4" iconFg="#A8413A" label="Resting heart rate" val="62" unit="bpm" sub="−2 vs last wk" color="#A8413A"/>
          <div style={{height:1, background:'#F0F0EC'}}></div>
          <StatRow ico="flame" iconBg="#D2E0CB" iconFg={F_COPPER} label="Active calories" val="412" unit="kcal" sub="3 of 4 rings closed" color={F_COPPER}/>
        </div>
      </div>
    </div>
  );
}

function StatRow({ ico, iconBg, iconFg, label, val, unit, goal, pct, sub, color }) {
  return (
    <div style={{padding:'14px 18px', display:'flex', alignItems:'center', gap:14}}>
      <div style={{width:36, height:36, borderRadius:10, background:iconBg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
        <i data-lucide={ico} style={{width:18, height:18, color:iconFg}}></i>
      </div>
      <div style={{flex:1, minWidth:0}}>
        <div style={{fontSize:13, color:F_BONE_700, marginBottom:2}}>{label}</div>
        <div style={{display:'flex', alignItems:'baseline', gap:6}}>
          <span style={{fontFamily:'Geist Mono, monospace', fontSize:18, fontWeight:500, color:F_FG, letterSpacing:'-0.01em'}}>{val}</span>
          {unit && <span style={{fontSize:11, color:F_BONE_500}}>{unit}</span>}
          {goal && <span style={{fontSize:11, color:F_BONE_500}}>/ {goal}</span>}
        </div>
      </div>
      {pct !== undefined ? (
        <div style={{width:40, height:40, position:'relative'}}>
          <svg viewBox="0 0 40 40" width="40" height="40">
            <circle cx="20" cy="20" r="16" stroke="#EEEEEA" strokeWidth="3" fill="none"/>
            <circle cx="20" cy="20" r="16" stroke={color} strokeWidth="3" fill="none" strokeDasharray={`${100.5*pct} 100.5`} strokeLinecap="round" transform="rotate(-90 20 20)"/>
          </svg>
        </div>
      ) : (
        <span style={{fontSize:11, color:F_SAGE_500, fontWeight:500}}>{sub}</span>
      )}
    </div>
  );
}

// ============================================================
// Tab bar (used on Home, Progress)
// ============================================================
function TabBar({ active }) {
  const tabs = [
    { id:'home', label:'Home', ico:'house' },
    { id:'progress', label:'Progress', ico:'trending-up' },
    { id:'checkin', label:'', ico:'qr-code', primary:true },
    { id:'food', label:'Food', ico:'utensils' },
    { id:'ai', label:'Coach', ico:'sparkles' },
  ];
  return (
    <div style={{position:'absolute', bottom:0, left:0, right:0, paddingBottom:24, background:'rgba(245,241,232,0.92)', backdropFilter:'blur(12px)', borderTop:'1px solid rgba(0,0,0,0.04)'}}>
      <div style={{display:'flex', alignItems:'flex-end', justifyContent:'space-around', padding:'10px 12px 0'}}>
        {tabs.map(t => t.primary ? (
          <button key={t.id} style={{width:54, height:54, borderRadius:'50%', background:F_SAGE, border:0, marginTop:-18, boxShadow:'0 8px 20px rgba(31,61,43,0.30)', cursor:'pointer'}}>
            <i data-lucide={t.ico} style={{width:22, height:22, color:F_BONE_50}}></i>
          </button>
        ) : (
          <button key={t.id} style={{display:'flex', flexDirection:'column', alignItems:'center', gap:4, background:'transparent', border:0, cursor:'pointer', color: active===t.id ? F_FG : F_BONE_500, padding:'4px 8px'}}>
            <i data-lucide={t.ico} style={{width:20, height:20}}></i>
            <span style={{fontSize:10, fontWeight: active===t.id ? 600 : 500}}>{t.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}

window.MobileLogin = MobileLogin;
window.MobileHome = MobileHome;
window.TabBar = TabBar;
window.F_SAGE = F_SAGE;
window.F_SAGE_700 = F_SAGE_700;
window.F_SAGE_500 = F_SAGE_500;
window.F_SAGE_50 = F_SAGE_50;
window.F_BONE_50 = F_BONE_50;
window.F_BONE_25 = F_BONE_25;
window.F_BONE_700 = F_BONE_700;
window.F_BONE_500 = F_BONE_500;
window.F_FG = F_FG;
window.F_COPPER = F_COPPER;
