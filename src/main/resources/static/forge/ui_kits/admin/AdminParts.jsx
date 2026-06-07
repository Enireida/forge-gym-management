// Forge Web Admin Dashboard — sidebar layout, data-dense

const A_SAGE = '#0E1A11';
const A_SAGE_700 = '#18271B';
const A_SAGE_600 = '#2E5535';
const A_SAGE_500 = '#4A7A4F';
const A_SAGE_50 = '#E8EFE5';
const A_BONE_50 = '#FAFAFA';
const A_BONE_25 = '#0E1A11';
const A_BONE_700 = '#D2E0CB';
const A_BONE_500 = '#8FB286';
const A_BONE_200 = '#1A2E1A';
const A_BONE_100 = '#1A2E1A';
const A_FG = '#0A0A0A';
const A_COPPER = '#4A7A4F';

function AdminSidebar({ active, onSelect }) {
  const groups = [
    { h: 'Overview', items: [
      { id:'home', label:'Dashboard', ico:'layout-dashboard' },
      { id:'reports', label:'Reports', ico:'bar-chart-3' },
    ]},
    { h: 'People', items: [
      { id:'members', label:'Members', ico:'users', count: 247 },
      { id:'trainers', label:'Trainers', ico:'graduation-cap' },
      { id:'staff', label:'Staff', ico:'id-card' },
    ]},
    { h: 'Operations', items: [
      { id:'attendance', label:'Attendance', ico:'door-open' },
      { id:'plans', label:'Plans', ico:'package' },
      { id:'payments', label:'Payments', ico:'credit-card', dot: true },
      { id:'pt', label:'Personal training', ico:'dumbbell' },
      { id:'equipment', label:'Equipment', ico:'wrench' },
      { id:'announcements', label:'Announcements', ico:'megaphone' },
    ]},
  ];
  return (
    <aside style={{width:264, background:A_BONE_25, borderRight:`1px solid ${A_BONE_100}`, display:'flex', flexDirection:'column', flexShrink:0}}>
      <div style={{padding:'22px 22px 28px', display:'flex', alignItems:'center', gap:10}}>
        <svg width="22" height="22" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
          <path d="M6 26V6h20" stroke={A_SAGE} strokeWidth="3.2" fill="none" strokeLinecap="square"/>
          <path d="M6 16h12" stroke={A_SAGE} strokeWidth="3.2" strokeLinecap="square"/>
          <circle cx="24" cy="24" r="2.2" fill={A_COPPER}/>
        </svg>
        <span style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:20, color:A_FG, letterSpacing:'-0.01em'}}>forge<span style={{color:A_COPPER}}>.</span></span>
        <span style={{marginLeft:'auto', fontSize:10, padding:'2px 7px', borderRadius:4, background:A_SAGE_50, color:A_SAGE_700, fontWeight:600, letterSpacing:'0.06em', textTransform:'uppercase'}}>Admin</span>
      </div>

      <div style={{padding:'0 12px 12px'}}>
        <div style={{padding:'8px 12px', background:'#fff', border:`1px solid ${A_BONE_100}`, borderRadius:10, display:'flex', alignItems:'center', gap:10, cursor:'pointer'}}>
          <div style={{width:28, height:28, borderRadius:8, background:A_SAGE, color:A_BONE_50, display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600}}>NT</div>
          <div style={{flex:1, minWidth:0}}>
            <div style={{fontSize:12, fontWeight:600, color:A_FG, lineHeight:1.2}}>North Gym Tirane</div>
            <div style={{fontSize:10, color:A_BONE_500, lineHeight:1.2}}>Rruga e Kavajes 116</div>
          </div>
          <i data-lucide="chevrons-up-down" style={{width:14, height:14, color:A_BONE_500}}></i>
        </div>
      </div>

      <nav style={{flex:1, overflowY:'auto', padding:'4px 8px'}}>
        {groups.map((g, gi) => (
          <div key={gi} style={{marginBottom:18}}>
            <div style={{fontSize:10, fontWeight:600, letterSpacing:'0.16em', textTransform:'uppercase', color:A_BONE_500, padding:'8px 12px 6px'}}>{g.h}</div>
            {g.items.map(it => (
              <a key={it.id} href="#" onClick={(e)=>{e.preventDefault(); onSelect && onSelect(it.id);}} style={{display:'flex', alignItems:'center', gap:10, padding:'8px 12px', borderRadius:8, background: active===it.id ? 'rgba(74,122,79,0.14)' : 'transparent', color: active===it.id ? '#4A7A4F' : A_BONE_700, fontSize:13, fontWeight: active===it.id ? 600 : 500, textDecoration:'none', marginBottom:2, position:'relative', cursor:'pointer'}}>
                <i data-lucide={it.ico} style={{width:15, height:15}}></i>
                <span style={{flex:1}}>{it.label}</span>
                {it.count !== undefined && <span style={{fontFamily:'Geist Mono, monospace', fontSize:10, color:A_BONE_500}}>{it.count}</span>}
                {it.dot && <span style={{width:6, height:6, borderRadius:'50%', background:A_COPPER}}></span>}
              </a>
            ))}
          </div>
        ))}
      </nav>

      <div style={{padding:'12px 16px', borderTop:`1px solid ${A_BONE_100}`, display:'flex', alignItems:'center', gap:10}}>
        <div style={{width:32, height:32, borderRadius:'50%', background:A_COPPER, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600}}>AB</div>
        <div style={{flex:1, minWidth:0}}>
          <div style={{fontSize:12, fontWeight:600, color:A_FG}}>Arben Brahimi</div>
          <div style={{fontSize:10, color:A_BONE_500}}>Owner · Admin</div>
        </div>
        <i data-lucide="settings-2" style={{width:14, height:14, color:A_BONE_500}}></i>
      </div>
    </aside>
  );
}

function AdminTopbar({ title, breadcrumbs }) {
  return (
    <div style={{display:'flex', alignItems:'center', gap:16, padding:'18px 32px', borderBottom:`1px solid ${A_BONE_100}`, background:'#fff', position:'sticky', top:0, zIndex:5}}>
      <div style={{flex:1}}>
        {breadcrumbs && <div style={{fontSize:11, color:A_BONE_500, marginBottom:2, letterSpacing:'0.04em'}}>{breadcrumbs}</div>}
        <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:28, lineHeight:1, letterSpacing:'-0.02em', margin:0, color:A_FG}}>{title}</h1>
      </div>
      <div style={{position:'relative'}}>
        <i data-lucide="search" style={{width:14, height:14, position:'absolute', left:12, top:11, color:A_BONE_500}}></i>
        <input placeholder="Search members, plans…" style={{height:36, width:300, padding:'0 12px 0 34px', borderRadius:8, border:`1px solid ${A_BONE_100}`, background:A_BONE_25, fontSize:13, fontFamily:'inherit', outline:'none', color:A_FG}}/>
        <span style={{position:'absolute', right:8, top:8, fontFamily:'Geist Mono, monospace', fontSize:10, padding:'2px 6px', borderRadius:4, background:'#fff', border:`1px solid ${A_BONE_100}`, color:A_BONE_500}}>⌘K</span>
      </div>
      <button style={{position:'relative', width:36, height:36, borderRadius:8, background:'#fff', border:`1px solid ${A_BONE_100}`, cursor:'pointer'}}>
        <i data-lucide="bell" style={{width:15, height:15, color:A_BONE_700}}></i>
        <span style={{position:'absolute', top:8, right:8, width:6, height:6, borderRadius:'50%', background:A_COPPER}}></span>
      </button>
      <button style={{height:36, padding:'0 14px', borderRadius:8, background:A_SAGE, color:A_BONE_50, border:0, fontSize:13, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
        <i data-lucide="plus" style={{width:14, height:14}}></i>Add member
      </button>
    </div>
  );
}

function KPI({ label, val, unit, delta, deltaLabel, good, ico, color }) {
  return (
    <div style={{background:'#fff', border:`1px solid ${A_BONE_100}`, borderRadius:14, padding:'18px 20px', display:'flex', flexDirection:'column', gap:10}}>
      <div style={{display:'flex', alignItems:'center', gap:10}}>
        <div style={{width:28, height:28, borderRadius:8, background: color || A_SAGE_50, display:'flex', alignItems:'center', justifyContent:'center'}}>
          <i data-lucide={ico} style={{width:14, height:14, color: A_SAGE_700}}></i>
        </div>
        <span style={{fontSize:12, color:A_BONE_700, fontWeight:500}}>{label}</span>
      </div>
      <div style={{display:'flex', alignItems:'baseline', gap:4}}>
        <span style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:A_FG, letterSpacing:'-0.02em'}}>{val}</span>
        {unit && <span style={{fontSize:13, color:A_BONE_500}}>{unit}</span>}
      </div>
      <div style={{fontSize:11, display:'flex', alignItems:'center', gap:6}}>
        <span style={{fontWeight:600, color: good ? '#4A7A4F' : '#A8413A', display:'inline-flex', alignItems:'center', gap:3, padding:'2px 6px', borderRadius:4, background: good ? '#E8EFE5' : '#F1D8D4'}}>
          <i data-lucide={good ? 'arrow-up-right' : 'arrow-down-right'} style={{width:11, height:11}}></i>{delta}
        </span>
        <span style={{color:A_BONE_500}}>{deltaLabel}</span>
      </div>
    </div>
  );
}

function RevenueChart() {
  const data = [
    { m:'Dec', v:14.2 }, { m:'Jan', v:16.5 }, { m:'Feb', v:15.8 },
    { m:'Mar', v:17.4 }, { m:'Apr', v:19.2 }, { m:'May', v:20.6 },
  ];
  const max = 24;
  return (
    <div style={{background:'#fff', border:`1px solid ${A_BONE_100}`, borderRadius:14, padding:'22px 24px', gridColumn:'span 2'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:24}}>
        <div>
          <div style={{fontSize:12, color:A_BONE_700, marginBottom:4}}>Monthly revenue</div>
          <div style={{display:'flex', alignItems:'baseline', gap:8}}>
            <span style={{fontFamily:'Instrument Serif, serif', fontSize:42, lineHeight:1, color:A_FG, letterSpacing:'-0.02em'}}>€20,640</span>
            <span style={{padding:'2px 8px', borderRadius:4, background:'#E8EFE5', color:'#4A7A4F', fontSize:11, fontWeight:600}}>+7.3%</span>
          </div>
        </div>
        <div style={{display:'flex', gap:6, padding:3, background:A_BONE_25, borderRadius:8, border:`1px solid ${A_BONE_100}`}}>
          {['1M','3M','6M','1Y'].map((k,i)=>(<button key={k} style={{padding:'5px 10px', borderRadius:6, background: i===2?'#fff':'transparent', border: i===2?`1px solid ${A_BONE_100}`:'0', fontSize:11, fontWeight:600, color: i===2?A_FG:A_BONE_500, cursor:'pointer'}}>{k}</button>))}
        </div>
      </div>
      <div style={{display:'flex', alignItems:'flex-end', gap:12, height:160, paddingBottom:24, position:'relative', borderBottom:`1px solid ${A_BONE_100}`}}>
        {data.map((d,i)=>(
          <div key={d.m} style={{flex:1, display:'flex', flexDirection:'column', alignItems:'center', gap:6, height:'100%', justifyContent:'flex-end'}}>
            <span style={{fontFamily:'Geist Mono, monospace', fontSize:10, color: i === data.length-1 ? A_FG : A_BONE_500, fontWeight: i === data.length-1 ? 600 : 400}}>€{d.v}k</span>
            <div style={{width:'70%', height:`${(d.v/max)*100}%`, background: i === data.length-1 ? A_SAGE : A_SAGE_500, borderRadius:'6px 6px 0 0', opacity: i === data.length-1 ? 1 : 0.55}}></div>
          </div>
        ))}
      </div>
      <div style={{display:'flex', justifyContent:'space-around', marginTop:8, fontSize:11, color:A_BONE_500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>
        {data.map(d=><span key={d.m}>{d.m.toUpperCase()}</span>)}
      </div>
    </div>
  );
}

function AttendanceCard() {
  const hours = [...Array(13)].map((_, i) => 6 + i);
  const days = ['M','T','W','T','F','S','S'];
  const cells = days.map((_, di) => hours.map((_, hi) => {
    const peak = (hi >= 1 && hi <= 4) || (hi >= 9 && hi <= 11);
    const weekend = di >= 5;
    let v = peak ? 0.6 + Math.random()*0.4 : 0.15 + Math.random()*0.35;
    if (weekend && peak) v *= 0.75;
    return v;
  }));
  return (
    <div style={{background:'#fff', border:`1px solid ${A_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
      <div style={{fontSize:12, color:A_BONE_700, marginBottom:4}}>Busiest hours · this week</div>
      <div style={{display:'flex', alignItems:'baseline', gap:8, marginBottom:18}}>
        <span style={{fontFamily:'Instrument Serif, serif', fontSize:32, lineHeight:1, color:A_FG, letterSpacing:'-0.02em'}}>18:00</span>
        <span style={{fontSize:12, color:A_BONE_500}}>peak hour</span>
      </div>
      <div>
        <div style={{display:'flex', gap:3, marginLeft:22}}>
          {cells[0].map((_, hi) => (
            <div key={hi} style={{flex:1, fontSize:9, color:A_BONE_500, fontFamily:'Geist Mono, monospace', textAlign:'center'}}>{hi % 2 === 0 ? hours[hi] : ''}</div>
          ))}
        </div>
        {days.map((d, di) => (
          <div key={di} style={{display:'flex', gap:3, marginTop:3, alignItems:'center'}}>
            <span style={{width:18, fontSize:10, color:A_BONE_500, fontWeight:500}}>{d}</span>
            {cells[di].map((v, hi) => (
              <div key={hi} style={{flex:1, aspectRatio:'1', borderRadius:3, background: `rgba(31,61,43,${0.06 + v*0.7})`}}></div>
            ))}
          </div>
        ))}
      </div>
      <div style={{display:'flex', alignItems:'center', gap:6, marginTop:14, fontSize:10, color:A_BONE_500}}>
        <span>Less</span>
        {[0.1,0.3,0.5,0.7,0.9].map(v => <div key={v} style={{width:12, height:12, borderRadius:3, background:`rgba(31,61,43,${0.06 + v*0.7})`}}></div>)}
        <span>More</span>
      </div>
    </div>
  );
}

function ActivityFeed() {
  const items = [
    { t:'2m', who:'Elira K.', verb:'checked in', ico:'door-open', color:A_SAGE_700 },
    { t:'14m', who:'Trainer Rea', verb:'logged measurement for Driton M.', ico:'ruler', color:A_BONE_700 },
    { t:'28m', who:'Besnik H.', verb:'paid €89 (Premium · 1mo)', ico:'credit-card', color:A_SAGE_700 },
    { t:'42m', who:'Mirjeta L.', verb:'requested PT — 8 sessions', ico:'dumbbell', color:A_COPPER },
    { t:'1h', who:'System', verb:'5 memberships expire next week', ico:'alert-triangle', color:'#B8893A' },
    { t:'1h', who:'Arben (you)', verb:'updated plan: Standard +€5', ico:'pencil', color:A_BONE_700 },
  ];
  return (
    <div style={{background:'#fff', border:`1px solid ${A_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
        <div style={{fontSize:14, fontWeight:600, color:A_FG}}>Recent activity</div>
        <a href="#" style={{fontSize:12, color:A_SAGE_700, textDecoration:'none', fontWeight:500}}>View all →</a>
      </div>
      {items.map((it, i) => (
        <div key={i} style={{display:'flex', gap:10, padding:'10px 0', borderBottom: i < items.length-1 ? `1px solid ${A_BONE_100}` : 0, alignItems:'flex-start'}}>
          <div style={{width:26, height:26, borderRadius:'50%', background:A_SAGE_50, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <i data-lucide={it.ico} style={{width:13, height:13, color:it.color}}></i>
          </div>
          <div style={{flex:1, minWidth:0}}>
            <div style={{fontSize:13, color:A_FG, lineHeight:1.4}}><strong style={{fontWeight:600}}>{it.who}</strong> {it.verb}</div>
          </div>
          <span style={{fontSize:11, color:A_BONE_500, fontFamily:'Geist Mono, monospace', flexShrink:0}}>{it.t}</span>
        </div>
      ))}
    </div>
  );
}

function ChurnCard() {
  return (
    <div style={{background:A_SAGE, color:A_BONE_50, borderRadius:14, padding:'22px 24px', position:'relative', overflow:'hidden'}}>
      <div style={{position:'absolute', top:-30, right:-30, width:120, height:120, borderRadius:'50%', background:'rgba(74,122,79,0.18)'}}></div>
      <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:14, position:'relative'}}>
        <i data-lucide="alert-triangle" style={{width:14, height:14, color:A_COPPER}}></i>
        <span style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB', fontWeight:600}}>Churn risk</span>
      </div>
      <div style={{fontFamily:'Instrument Serif, serif', fontSize:42, lineHeight:1, marginBottom:8, position:'relative'}}>14 members</div>
      <p style={{fontSize:13, color:'#D2E0CB', lineHeight:1.5, margin:'0 0 18px', position:'relative'}}>Haven't checked in for 30+ days. Send re-engagement SMS or call from the AI agent.</p>
      <button style={{padding:'8px 14px', borderRadius:999, background:A_COPPER, color:'#fff', border:0, fontSize:12, fontWeight:600, cursor:'pointer', position:'relative', fontFamily:'inherit'}}>Send re-engagement SMS →</button>
    </div>
  );
}

window.AdminSidebar = AdminSidebar;
window.AdminTopbar = AdminTopbar;
window.KPI = KPI;
window.RevenueChart = RevenueChart;
window.AttendanceCard = AttendanceCard;
window.ActivityFeed = ActivityFeed;
window.ChurnCard = ChurnCard;
window.A_SAGE = A_SAGE;
window.A_SAGE_700 = A_SAGE_700;
window.A_SAGE_50 = A_SAGE_50;
window.A_BONE_50 = A_BONE_50;
window.A_BONE_25 = A_BONE_25;
window.A_BONE_100 = A_BONE_100;
window.A_BONE_500 = A_BONE_500;
window.A_BONE_700 = A_BONE_700;
window.A_FG = A_FG;
window.A_COPPER = A_COPPER;
