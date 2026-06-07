// Trainer Dashboard — sidebar trimmed, focus on assigned members + measurements + PT

const T_SAGE = '#0E1A11';
const T_SAGE_700 = '#18271B';
const T_SAGE_500 = '#2E5535';
const T_SAGE_50 = '#E8EFE5';
const T_BONE_25 = '#0E1A11';
const T_BONE_100 = '#1A2E1A';
const T_BONE_500 = '#8FB286';
const T_BONE_700 = '#D2E0CB';
const T_FG = '#0A0A0A';
const T_COPPER = '#4A7A4F';

function TrainerSidebar({ active, onSelect }) {
  const items = [
    { id:'home', label:'Today', ico:'sun' },
    { id:'members', label:'My members', ico:'users', count:18 },
    { id:'measure', label:'Measurements', ico:'ruler' },
    { id:'pt', label:'PT sessions', ico:'dumbbell', count:6 },
    { id:'workouts', label:'Workout templates', ico:'list-checks' },
    { id:'exercises', label:'Exercise library', ico:'book-open' },
    { id:'chat', label:'Chat', ico:'message-square', dot:true },
  ];
  return (
    <aside style={{width:240, background:T_BONE_25, borderRight:`1px solid ${T_BONE_100}`, display:'flex', flexDirection:'column', flexShrink:0}}>
      <div style={{padding:'22px 22px', display:'flex', alignItems:'center', gap:10}}>
        <svg width="22" height="22" viewBox="0 0 32 32"><path d="M6 26V6h20" stroke="#FAFAFA" strokeWidth="3.2" fill="none"/><path d="M6 16h12" stroke="#FAFAFA" strokeWidth="3.2"/><circle cx="24" cy="24" r="2.2" fill={T_COPPER}/></svg>
        <span style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:20, color:'#FAFAFA'}}>forge<span style={{color:T_COPPER}}>.</span></span>
        <span style={{marginLeft:'auto', fontSize:10, padding:'2px 7px', borderRadius:4, background:'#D2E0CB', color:T_COPPER, fontWeight:600, letterSpacing:'0.06em', textTransform:'uppercase'}}>Trainer</span>
      </div>
      <nav style={{flex:1, padding:'8px'}}>
        {items.map(it => (
          <a key={it.id} href="#" onClick={(e)=>{e.preventDefault(); onSelect && onSelect(it.id);}} style={{display:'flex', alignItems:'center', gap:10, padding:'9px 12px', borderRadius:8, background: active===it.id ? 'rgba(74,122,79,0.14)' : 'transparent', color: active===it.id ? '#4A7A4F' : T_BONE_700, fontSize:13, fontWeight: active===it.id ? 600 : 500, textDecoration:'none', marginBottom:2, cursor:'pointer'}}>
            <i data-lucide={it.ico} style={{width:15, height:15}}></i>
            <span style={{flex:1}}>{it.label}</span>
            {it.count !== undefined && <span style={{fontFamily:'Geist Mono, monospace', fontSize:10, color:T_BONE_500}}>{it.count}</span>}
            {it.dot && <span style={{width:6, height:6, borderRadius:'50%', background:T_COPPER}}></span>}
          </a>
        ))}
      </nav>
      <div style={{padding:'12px 16px', borderTop:`1px solid ${T_BONE_100}`, display:'flex', alignItems:'center', gap:10}}>
        <div style={{width:32, height:32, borderRadius:'50%', background:T_COPPER, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600}}>RH</div>
        <div style={{flex:1}}>
          <div style={{fontSize:12, fontWeight:600, color:'#FAFAFA'}}>Rea Hoxha</div>
          <div style={{fontSize:10, color:T_BONE_500}}>Lead trainer</div>
        </div>
      </div>
    </aside>
  );
}

function TrainerHome() {
  const sessions = [
    { time:'08:00', name:'Driton Mehmeti', kind:'Strength · Push', dur:'60 min', remaining:'4 left', status:'next' },
    { time:'09:30', name:'Elira Krasniqi', kind:'HIIT · full body', dur:'45 min', remaining:'7 left' },
    { time:'11:00', name:'Mirjeta Lleshi', kind:'Mobility & core', dur:'45 min', remaining:'2 left' },
    { time:'15:00', name:'Besnik Hoxha', kind:'Strength · Pull', dur:'60 min', remaining:'9 left' },
  ];
  const members = [
    { n:'Driton M.', goal:'Lean bulk', last:'2d', wt:84.2, dwt:'+0.8' },
    { n:'Elira K.', goal:'Cut · 5 kg', last:'1d', wt:78.4, dwt:'−1.8' },
    { n:'Mirjeta L.', goal:'Mobility', last:'5d', wt:62.1, dwt:'−0.4', alert:true },
    { n:'Besnik H.', goal:'Strength', last:'1d', wt:91.6, dwt:'+1.2' },
    { n:'Anila S.', goal:'Postpartum', last:'3d', wt:67.0, dwt:'+0.2' },
    { n:'Ardit V.', goal:'Endurance', last:'1d', wt:74.3, dwt:'−0.6' },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:20}}>
        <div style={{background:T_SAGE, color:'#FAFAFA', borderRadius:14, padding:'24px 28px', position:'relative', overflow:'hidden'}}>
          <div style={{position:'absolute', top:-30, right:-30, width:140, height:140, borderRadius:'50%', background:'rgba(74,122,79,0.18)'}}></div>
          <div style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB', marginBottom:6, fontWeight:600}}>Today · Sunday, May 11</div>
          <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:42, lineHeight:1, margin:'0 0 14px', letterSpacing:'-0.02em'}}>4 sessions, <em style={{color:T_COPPER}}>3h 30m</em> on the floor.</h1>
          <div style={{display:'flex', gap:24, position:'relative'}}>
            <div><div style={{fontSize:11, color:'#D2E0CB'}}>Next up</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:18, fontWeight:500}}>08:00 · Driton</div></div>
            <div><div style={{fontSize:11, color:'#D2E0CB'}}>Members</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:18, fontWeight:500}}>18 active</div></div>
            <div><div style={{fontSize:11, color:'#D2E0CB'}}>Open requests</div><div style={{fontFamily:'Geist Mono, monospace', fontSize:18, fontWeight:500}}>2</div></div>
          </div>
        </div>
        <div style={{background:'#fff', border:`1px solid ${T_BONE_100}`, borderRadius:14, padding:'18px 22px'}}>
          <div style={{fontSize:12, color:T_BONE_700, marginBottom:6}}>This week</div>
          <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:T_FG, marginBottom:14}}>23 sessions</div>
          <div style={{display:'flex', alignItems:'flex-end', gap:6, height:50}}>
            {[3,5,4,6,3,2,0].map((v,i)=>(<div key={i} style={{flex:1, height:`${v*10+8}px`, background:i===6?T_BONE_100:T_SAGE_500, borderRadius:'3px 3px 0 0', opacity: i===6?1:0.7}}></div>))}
          </div>
          <div style={{display:'flex', justifyContent:'space-between', fontSize:10, color:T_BONE_500, marginTop:6, fontFamily:'Geist Mono, monospace'}}>
            <span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span><span>S</span>
          </div>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1.1fr 1fr', gap:16}}>
        <div style={{background:'#fff', border:`1px solid ${T_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:T_FG}}>Today's sessions</h3>
            <a href="#" style={{fontSize:12, color:T_SAGE_700, fontWeight:500, textDecoration:'none'}}>See week →</a>
          </div>
          {sessions.map((s, i) => (
            <div key={i} style={{display:'flex', alignItems:'center', gap:14, padding:'12px 0', borderBottom: i<sessions.length-1?`1px solid ${T_BONE_100}`:0}}>
              <div style={{width:54, textAlign:'center'}}>
                <div style={{fontFamily:'Geist Mono, monospace', fontSize:14, fontWeight:600, color: s.status==='next'?T_COPPER:T_FG}}>{s.time}</div>
                <div style={{fontSize:9, color:T_BONE_500, fontFamily:'Geist Mono, monospace'}}>{s.dur}</div>
              </div>
              <div style={{width:1, height:36, background: s.status==='next'?T_COPPER:T_BONE_100}}></div>
              <div style={{flex:1, minWidth:0}}>
                <div style={{fontSize:13, fontWeight:600, color:T_FG, display:'flex', alignItems:'center', gap:8}}>
                  {s.name}
                  {s.status==='next' && <span style={{padding:'1px 7px', borderRadius:4, background:'#D2E0CB', color:T_COPPER, fontSize:9, letterSpacing:'0.06em', fontWeight:700, textTransform:'uppercase'}}>Next</span>}
                </div>
                <div style={{fontSize:12, color:T_BONE_700}}>{s.kind} · <span style={{color:T_BONE_500}}>{s.remaining} of plan</span></div>
              </div>
              <button style={{padding:'6px 12px', background:'#fff', border:`1px solid ${T_BONE_100}`, borderRadius:8, fontSize:11, fontWeight:600, color:T_FG, cursor:'pointer'}}>Open plan</button>
            </div>
          ))}
        </div>

        <div style={{background:'#fff', border:`1px solid ${T_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:T_FG}}>My members</h3>
            <a href="#" style={{fontSize:12, color:T_SAGE_700, fontWeight:500, textDecoration:'none'}}>All 18 →</a>
          </div>
          <div style={{display:'grid', gridTemplateColumns:'1fr auto auto', gap:'8px 14px', fontSize:11, color:T_BONE_500, fontWeight:600, letterSpacing:'0.08em', textTransform:'uppercase', paddingBottom:8, borderBottom:`1px solid ${T_BONE_100}`, marginBottom:6}}>
            <span>Member · Goal</span><span style={{textAlign:'right'}}>Weight</span><span style={{textAlign:'right'}}>Last</span>
          </div>
          {members.map((m, i) => (
            <div key={i} style={{display:'grid', gridTemplateColumns:'1fr auto auto', gap:'4px 14px', padding:'10px 0', alignItems:'center', borderBottom: i<members.length-1?`1px solid ${T_BONE_100}`:0}}>
              <div style={{display:'flex', alignItems:'center', gap:10, minWidth:0}}>
                <div style={{width:30, height:30, borderRadius:'50%', background: m.alert ? '#D2E0CB' : T_SAGE_50, color: m.alert ? T_COPPER : T_SAGE_700, display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600, flexShrink:0}}>{m.n.split(' ').map(s=>s[0]).join('')}</div>
                <div style={{minWidth:0}}>
                  <div style={{fontSize:13, fontWeight:600, color:T_FG, lineHeight:1.2}}>{m.n}</div>
                  <div style={{fontSize:11, color:T_BONE_500, lineHeight:1.2}}>{m.goal}</div>
                </div>
              </div>
              <div style={{textAlign:'right'}}>
                <div style={{fontFamily:'Geist Mono, monospace', fontSize:13, color:T_FG}}>{m.wt}<span style={{fontSize:9, color:T_BONE_500, marginLeft:2}}>kg</span></div>
                <div style={{fontSize:10, fontWeight:600, color: m.dwt.startsWith('+') ? '#A8413A' : '#4A7A4F', fontFamily:'Geist Mono, monospace'}}>{m.dwt}</div>
              </div>
              <div style={{fontSize:11, color: m.alert ? T_COPPER : T_BONE_500, fontWeight: m.alert ? 600 : 400, fontFamily:'Geist Mono, monospace'}}>{m.last}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

window.TrainerSidebar = TrainerSidebar;
window.TrainerHome = TrainerHome;
