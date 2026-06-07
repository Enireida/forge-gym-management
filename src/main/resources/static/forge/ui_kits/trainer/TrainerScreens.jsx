// ============================================================
// Forge Trainer — all screens, fully functional with shared state
// ============================================================

// ============ MEMBER DETAIL ============
function TrainerMemberDetail({ memberId, onBack, members, onLogClick }) {
  const m = members.find(x => x.id === memberId);
  if (!m) return null;
  const measurements = [
    { d:'12 May', wt:m.wt, ht:m.ht, bf:m.bf, waist:m.waist, chest:m.chest, notes:'On plan, energy levels strong' },
    { d:'05 May', wt:(m.wt+0.3).toFixed(1), ht:m.ht, bf:(m.bf+0.4).toFixed(1), waist:m.waist+1, chest:m.chest, notes:'Slight uptick, hydration good' },
    { d:'28 Apr', wt:(m.wt+0.7).toFixed(1), ht:m.ht, bf:(m.bf+0.8).toFixed(1), waist:m.waist+1, chest:m.chest-1, notes:'—' },
    { d:'21 Apr', wt:(m.wt+1.2).toFixed(1), ht:m.ht, bf:(m.bf+1.1).toFixed(1), waist:m.waist+2, chest:m.chest-1, notes:'Started block 3 of program' },
  ];
  const sessions = [
    { d:'12 May · 09:30', t:'HIIT · full body', dur:45, notes:'5 rounds Tabata · solid form' },
    { d:'09 May · 18:00', t:'Strength · Upper', dur:60, notes:'Bench 60kg · DB row 14kg' },
    { d:'05 May · 09:30', t:'Mobility', dur:45, notes:'Hip openers + thoracic' },
    { d:'02 May · 18:00', t:'Strength · Lower', dur:60, notes:'Goblet squat · Hip thrust' },
  ];

  return (
    <div style={{padding:'24px 32px 64px'}}>
      <button onClick={onBack} style={{display:'inline-flex', alignItems:'center', gap:6, padding:'7px 12px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, fontWeight:500, color:'#1A2E1A', cursor:'pointer', fontFamily:'inherit', marginBottom:16}}>
        <i data-lucide="arrow-left" style={{width:13, height:13}}></i> All clients
      </button>

      <div style={{background:'#0E1A11', color:'#FAFAFA', borderRadius:14, padding:'28px 32px', marginBottom:16, position:'relative', overflow:'hidden'}}>
        <div style={{position:'absolute', top:-40, right:-40, width:200, height:200, borderRadius:'50%', background:'rgba(74,122,79,0.18)'}}></div>
        <div style={{display:'flex', alignItems:'center', gap:18, position:'relative'}}>
          <div style={{width:64, height:64, borderRadius:'50%', background:'#4A7A4F', color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:28}}>{m.n.split(' ').map(w=>w[0]).join('')}</div>
          <div style={{flex:1}}>
            <h2 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:36, lineHeight:1, margin:'0 0 6px', letterSpacing:'-0.02em'}}>{m.n}</h2>
            <div style={{fontSize:13, color:'#D2E0CB'}}>{m.goal} · {m.plan}</div>
          </div>
          <button onClick={onLogClick} style={{padding:'10px 16px', background:'#4A7A4F', color:'#fff', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="ruler" style={{width:13, height:13}}></i>Log measurement
          </button>
        </div>
        <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:18, marginTop:24, paddingTop:18, borderTop:'1px solid rgba(220,228,214,0.18)', position:'relative'}}>
          {[['WEIGHT', m.wt+' kg'],['BODY FAT', m.bf+'%'],['SESSIONS USED', `${m.used}/${m.total}`],['LAST SESSION', m.lastSession]].map(([k,v])=>(
            <div key={k}><div style={{fontSize:10, color:'#8FB286', letterSpacing:'0.10em', fontFamily:'Geist Mono, monospace', marginBottom:4}}>{k}</div><div style={{fontFamily:'Instrument Serif, serif', fontSize:22, color:'#FAFAFA'}}>{v}</div></div>
          ))}
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:16}}>
        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Recent measurements</h3>
          {measurements.map((x,i)=>(
            <div key={i} style={{padding:'12px 0', borderBottom: i<measurements.length-1?'1px solid #F0F0EC':0, display:'flex', alignItems:'center', gap:12}}>
              <div style={{width:56, fontFamily:'Geist Mono, monospace', fontSize:12, color:'#4A4A45'}}>{x.d}</div>
              <div style={{flex:1, fontSize:12, color:'#0E1A11', display:'flex', gap:14}}>
                <span><strong style={{fontFamily:'Geist Mono, monospace'}}>{x.wt}</strong> kg</span>
                <span style={{color:'#6A6A65'}}>BF <strong style={{fontFamily:'Geist Mono, monospace', color:'#0E1A11'}}>{x.bf}%</strong></span>
                <span style={{color:'#6A6A65'}}>W <strong style={{fontFamily:'Geist Mono, monospace', color:'#0E1A11'}}>{x.waist}</strong></span>
              </div>
              <div style={{fontSize:11, color:'#6A6A65', maxWidth:160, textAlign:'right'}}>{x.notes}</div>
            </div>
          ))}
        </div>

        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Session history</h3>
          {sessions.map((s,i)=>(
            <div key={i} style={{padding:'12px 0', borderBottom: i<sessions.length-1?'1px solid #F0F0EC':0}}>
              <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline', marginBottom:3}}>
                <div style={{fontSize:13, fontWeight:600, color:'#0E1A11'}}>{s.t}</div>
                <div style={{fontFamily:'Geist Mono, monospace', fontSize:11, color:'#8FB286'}}>{s.dur} min</div>
              </div>
              <div style={{fontSize:11, color:'#6A6A65'}}>{s.d}</div>
              <div style={{fontSize:12, color:'#4A4A45', marginTop:4}}>{s.notes}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ============ MEMBERS (with working Open) ============
function TrainerMembers({ members, onOpen }) {
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>My clients · {members.length}</h3>
          <div style={{position:'relative'}}>
            <i data-lucide="search" style={{width:14, height:14, position:'absolute', left:12, top:10, color:'#8A8A85'}}></i>
            <input placeholder="Search clients..." style={{height:34, padding:'0 14px 0 34px', border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, width:220, fontFamily:'inherit', outline:'none'}}/>
          </div>
        </div>
        <div style={{display:'grid', gridTemplateColumns:'repeat(2,1fr)', gap:12}}>
          {members.map((m)=>(
            <button key={m.id} onClick={()=>onOpen(m.id)} style={{textAlign:'left', cursor:'pointer', padding:'18px 20px', border:`1px solid ${m.alert?'#4A7A4F':'#F0F0EC'}`, borderRadius:12, background:m.alert?'rgba(74,122,79,0.04)':'#F5F5F3', display:'flex', gap:14, alignItems:'flex-start', fontFamily:'inherit', transition:'all .2s'}} onMouseEnter={e=>e.currentTarget.style.background='#fff'} onMouseLeave={e=>e.currentTarget.style.background=m.alert?'rgba(74,122,79,0.04)':'#F5F5F3'}>
              <div style={{width:44, height:44, borderRadius:'50%', background: m.alert?'#4A7A4F':'#1A2E1A', color:'#FAFAFA', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:18, flexShrink:0}}>{m.n.split(' ').map(w=>w[0]).join('')}</div>
              <div style={{flex:1, minWidth:0}}>
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:4}}>
                  <div style={{fontSize:14, fontWeight:600, color:'#0E1A11'}}>{m.n}</div>
                  <div style={{fontFamily:'Geist Mono, monospace', fontSize:11, color: m.alert?'#4A7A4F':'#8FB286', fontWeight:m.alert?600:500}}>{m.lastSession.toUpperCase()}</div>
                </div>
                <div style={{fontSize:12, color:'#4A4A45', marginBottom:8}}>{m.goal}</div>
                <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:8}}>
                  <div style={{flex:1, height:6, background:'#F0F0EC', borderRadius:3, overflow:'hidden'}}>
                    <div style={{width:`${(m.used/m.total)*100}%`, height:'100%', background:m.alert?'#4A7A4F':'#4A7A4F', borderRadius:3}}></div>
                  </div>
                  <span style={{fontFamily:'Geist Mono, monospace', fontSize:11, color:'#0E1A11', fontWeight:600}}>{m.used}/{m.total}</span>
                </div>
                <div style={{display:'flex', justifyContent:'space-between', fontSize:11, color:'#6A6A65'}}>
                  <span>{m.plan}</span>
                  <span style={{fontFamily:'Geist Mono, monospace'}}>{m.wt} kg</span>
                </div>
              </div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

// ============ MEASUREMENTS ============
function TrainerMeasurements({ members, log, addMeasurement }) {
  const [form, setForm] = React.useState({ member:'', wt:'', ht:'', bf:'', waist:'', chest:'', notes:'' });
  function submit(e) {
    e.preventDefault();
    if (!form.member || !form.wt) { alert('Member and weight are required.'); return; }
    addMeasurement(form);
    setForm({ member:'', wt:'', ht:'', bf:'', waist:'', chest:'', notes:'' });
  }
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <form onSubmit={submit} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Log a new measurement</h3>
        <div style={{display:'grid', gridTemplateColumns:'1.5fr 1fr 1fr 1fr 1fr 1fr', gap:10, marginBottom:10}}>
          <select value={form.member} onChange={e=>setForm({...form, member:e.target.value})} style={inputStyle}>
            <option value="">Select member...</option>
            {members.map(m=><option key={m.id} value={m.n}>{m.n}</option>)}
          </select>
          <input value={form.wt} onChange={e=>setForm({...form, wt:e.target.value})} placeholder="Weight kg" type="number" step="0.1" style={inputStyle}/>
          <input value={form.ht} onChange={e=>setForm({...form, ht:e.target.value})} placeholder="Height cm" type="number" style={inputStyle}/>
          <input value={form.bf} onChange={e=>setForm({...form, bf:e.target.value})} placeholder="Body fat %" type="number" step="0.1" style={inputStyle}/>
          <input value={form.waist} onChange={e=>setForm({...form, waist:e.target.value})} placeholder="Waist cm" type="number" style={inputStyle}/>
          <input value={form.chest} onChange={e=>setForm({...form, chest:e.target.value})} placeholder="Chest cm" type="number" style={inputStyle}/>
        </div>
        <div style={{display:'flex', gap:10}}>
          <input value={form.notes} onChange={e=>setForm({...form, notes:e.target.value})} placeholder="Notes (optional)" style={{...inputStyle, flex:1}}/>
          <button type="submit" style={{padding:'10px 20px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>Save measurement</button>
        </div>
      </form>

      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Recent measurements · {log.length}</h3>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead><tr style={{borderBottom:'1px solid #EEEEEA'}}>
            {['Date','Member','Weight','Height','BMI','Body fat','Waist','Chest','Notes'].map(h=><th key={h} style={thStyle}>{h}</th>)}
          </tr></thead>
          <tbody>
            {log.map((l,i)=>{
              const bmi = l.ht ? (l.wt/((l.ht/100)*(l.ht/100))).toFixed(1) : '—';
              return (
                <tr key={i} style={{borderBottom:'1px solid #F0F0EC'}}>
                  <td style={tdStyle}>{l.d}</td>
                  <td style={{...tdStyle, fontWeight:600, color:'#0E1A11'}}>{l.m}</td>
                  <td style={tdStyle}>{l.wt} <span style={{fontSize:10, color:'#6A6A65'}}>kg</span></td>
                  <td style={tdStyle}>{l.ht || '—'} <span style={{fontSize:10, color:'#6A6A65'}}>{l.ht?'cm':''}</span></td>
                  <td style={{...tdStyle, fontWeight:600}}>{bmi}</td>
                  <td style={tdStyle}>{l.bf?l.bf+'%':'—'}</td>
                  <td style={tdStyle}>{l.waist || '—'}</td>
                  <td style={tdStyle}>{l.chest || '—'}</td>
                  <td style={{...tdStyle, fontSize:11, color:'#6A6A65', maxWidth:220, fontFamily:'inherit'}}>{l.notes || '—'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ============ PT SESSIONS ============
function TrainerPT({ sessions, onMark }) {
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:16, marginBottom:20}}>
        {[
          { l:'Sessions this week', v:sessions.filter(s=>s.status==='Done').length+sessions.filter(s=>s.status==='Upcoming').length },
          { l:'Done', v:sessions.filter(s=>s.status==='Done').length, color:'#4A7A4F' },
          { l:'Upcoming', v:sessions.filter(s=>s.status==='Upcoming').length, color:'#4A7A4F' },
          { l:'Completion rate', v:'94%' },
        ].map((s,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'18px 20px'}}>
            <div style={{fontSize:11, color:'#8FB286', letterSpacing:'0.10em', fontFamily:'Geist Mono, monospace', textTransform:'uppercase', marginBottom:8}}>{s.l}</div>
            <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:s.color||'#0E1A11', letterSpacing:'-0.02em'}}>{s.v}</div>
          </div>
        ))}
      </div>

      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Session log</h3>
        {sessions.map((s,i)=>(
          <div key={i} style={{display:'flex', alignItems:'center', gap:14, padding:'14px 0', borderBottom: i<sessions.length-1?'1px solid #F0F0EC':0}}>
            <div style={{width:96, flexShrink:0}}>
              <div style={{fontFamily:'Geist Mono, monospace', fontSize:13, fontWeight:600, color: s.status==='Upcoming'?'#4A7A4F':'#0E1A11'}}>{s.d}</div>
              <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace'}}>{s.dur} min</div>
            </div>
            <div style={{width:2, height:36, background: s.status==='Upcoming'?'#4A7A4F':'#E8EFE5'}}></div>
            <div style={{flex:1, minWidth:0}}>
              <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:3}}>
                <span style={{fontSize:14, fontWeight:600, color:'#0E1A11'}}>{s.m}</span>
                <span style={{padding:'2px 8px', borderRadius:4, background: s.status==='Done'?'rgba(74,122,79,0.16)':'rgba(74,122,79,0.16)', color:s.status==='Done'?'#4A7A4F':'#1A2E1A', fontSize:10, fontWeight:600, letterSpacing:'0.06em'}}>{s.status.toUpperCase()}</span>
              </div>
              <div style={{fontSize:12, color:'#4A4A45', marginBottom:2}}>{s.type}</div>
              <div style={{fontSize:11, color:'#6A6A65', fontStyle: s.notes==='—'?'italic':'normal'}}>{s.notes}</div>
            </div>
            {s.status==='Upcoming' && <button onClick={()=>onMark(i)} style={{padding:'6px 12px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:6, fontSize:11, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>Mark done</button>}
          </div>
        ))}
      </div>
    </div>
  );
}

// ============ WORKOUT TEMPLATES ============
function TrainerWorkouts({ templates, onUse, onNew }) {
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:16, display:'flex', justifyContent:'space-between', alignItems:'center'}}>
        <div>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>Workout templates · {templates.length}</h3>
          <p style={{fontSize:13, color:'#4A4A45', margin:'4px 0 0'}}>Reusable plans you can assign to any client in one click.</p>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'repeat(2,1fr)', gap:16}}>
        {templates.map((t,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:10}}>
              <div>
                <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:6}}>
                  <span style={{padding:'3px 9px', borderRadius:4, background:t.color, color:'#fff', fontSize:10, fontWeight:600, letterSpacing:'0.08em', fontFamily:'Geist Mono, monospace'}}>{t.level.toUpperCase()}</span>
                  <span style={{padding:'3px 9px', borderRadius:4, background:'#F5F5F3', border:'1px solid #EEEEEA', fontSize:10, fontWeight:600, color:'#4A4A45', letterSpacing:'0.06em', fontFamily:'Geist Mono, monospace'}}>{t.goal.toUpperCase()}</span>
                </div>
                <h4 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:24, margin:0, color:'#0E1A11', letterSpacing:'-0.01em'}}>{t.name}</h4>
              </div>
              <div style={{textAlign:'right'}}>
                <div style={{fontFamily:'Geist Mono, monospace', fontSize:11, color:'#8FB286', letterSpacing:'0.06em'}}>USED BY</div>
                <div style={{fontFamily:'Instrument Serif, serif', fontSize:22, color:'#0E1A11'}}>{t.used}</div>
              </div>
            </div>
            <p style={{fontSize:13, color:'#4A4A45', lineHeight:1.5, margin:'0 0 14px'}}>{t.desc}</p>
            <div style={{display:'flex', gap:8, marginBottom:14, flexWrap:'wrap'}}>
              {t.exs.map((ex,j)=>(<span key={j} style={{padding:'4px 10px', background:'#F5F5F3', border:'1px solid #F0F0EC', borderRadius:999, fontSize:11, color:'#1A2E1A'}}>{ex}</span>))}
            </div>
            <div style={{display:'flex', gap:8, paddingTop:14, borderTop:'1px solid #F0F0EC', fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>
              <span>{t.duration} MIN</span><span>·</span><span>{t.exs.length} EXERCISES</span><span style={{marginLeft:'auto'}}>UPDATED {t.updated}</span>
            </div>
            <div style={{display:'flex', gap:8, marginTop:12}}>
              <button onClick={()=>onUse(t)} style={{flex:1, padding:'9px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:12, fontWeight:500, cursor:'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', justifyContent:'center', gap:6}}>
                <i data-lucide="user-plus" style={{width:12, height:12}}></i>Assign to client
              </button>
              <button style={{padding:'9px 14px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, fontWeight:500, color:'#1A2E1A', cursor:'pointer', fontFamily:'inherit'}}>Edit</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ============ EXERCISES with filters + Add modal ============
function TrainerExercises({ exercises, addExercise }) {
  const [filterMg, setFilterMg] = React.useState('All');
  const [filterDif, setFilterDif] = React.useState('All');
  const [showAdd, setShowAdd] = React.useState(false);
  const groups = ['All','Chest','Back','Legs','Shoulders','Arms','Core','Cardio','Full body'];
  const levels = ['All','Beginner','Intermediate','Advanced'];
  const filtered = exercises.filter(e => (filterMg==='All' || e.mg===filterMg) && (filterDif==='All' || e.dif===filterDif));
  const difColor = (d) => d==='Beginner'?'#4A7A4F':d==='Intermediate'?'#1A2E1A':'#7A2E29';
  const mgColor = (mg) => ({Chest:'#A8413A', Back:'#1A2E1A', Legs:'#2E5535', Shoulders:'#1A2E1A', Arms:'#0E1A11', Core:'#4A7A4F', Cardio:'#4A7A4F', 'Full body':'#4A4A45'})[mg] || '#0E1A11';

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:20, display:'flex', justifyContent:'space-between', alignItems:'center', flexWrap:'wrap', gap:12}}>
        <div>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>Exercise library · {filtered.length} of {exercises.length}</h3>
          <p style={{fontSize:13, color:'#4A4A45', margin:'4px 0 0'}}>Shared across all trainers and members.</p>
        </div>
        <div style={{display:'flex', gap:10, alignItems:'center'}}>
          <select value={filterMg} onChange={e=>setFilterMg(e.target.value)} style={{...inputStyle, height:36}}>
            {groups.map(g=><option key={g} value={g}>{g==='All'?'All groups':g}</option>)}
          </select>
          <select value={filterDif} onChange={e=>setFilterDif(e.target.value)} style={{...inputStyle, height:36}}>
            {levels.map(l=><option key={l} value={l}>{l==='All'?'All levels':l}</option>)}
          </select>
          <button onClick={()=>setShowAdd(true)} style={{padding:'8px 14px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:12, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="plus" style={{width:13, height:13}}></i>Add exercise
          </button>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div style={{background:'#fff', border:'1px dashed #EEEEEA', borderRadius:14, padding:'48px', textAlign:'center'}}>
          <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:24, color:'#6A6A65', marginBottom:8}}>No exercises match.</div>
          <p style={{fontSize:13, color:'#6A6A65', margin:0}}>Try removing a filter or add a new exercise.</p>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:12}}>
          {filtered.map((e,i)=>(
            <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:12, overflow:'hidden'}}>
              <div style={{height:120, background:`linear-gradient(135deg, ${mgColor(e.mg)} 0%, ${e.mg==='Cardio'?'#2E5535':'#0E1A11'} 100%)`, position:'relative', display:'flex', alignItems:'flex-end', padding:14}}>
                <i data-lucide={e.ico||'dumbbell'} style={{width:48, height:48, position:'absolute', top:'50%', left:'50%', transform:'translate(-50%,-50%)', color:'rgba(220,228,214,0.30)'}}></i>
                <span style={{padding:'3px 9px', borderRadius:4, background:'rgba(220,228,214,0.18)', color:'#E8EFE5', fontSize:9.5, fontWeight:600, letterSpacing:'0.06em', fontFamily:'Geist Mono, monospace', backdropFilter:'blur(8px)', position:'relative'}}>{e.mg.toUpperCase()}</span>
              </div>
              <div style={{padding:'14px 16px'}}>
                <div style={{fontSize:14, fontWeight:600, color:'#0E1A11', marginBottom:6}}>{e.n}</div>
                <div style={{display:'flex', gap:6, marginBottom:8, flexWrap:'wrap'}}>
                  <span style={{padding:'2px 7px', borderRadius:4, background:'rgba(0,0,0,0.06)', fontSize:10, color:difColor(e.dif), fontWeight:600, letterSpacing:'0.04em', fontFamily:'Geist Mono, monospace'}}>{e.dif.toUpperCase()}</span>
                  <span style={{padding:'2px 7px', borderRadius:4, background:'rgba(0,0,0,0.06)', fontSize:10, color:'#4A4A45', fontFamily:'Geist Mono, monospace'}}>{e.sets}</span>
                </div>
                <div style={{fontSize:11, color:'#6A6A65', fontFamily:'Geist Mono, monospace'}}>{e.eq}</div>
              </div>
            </div>
          ))}
        </div>
      )}

      {showAdd && <AddExerciseModal onClose={()=>setShowAdd(false)} onAdd={(ex)=>{ addExercise(ex); setShowAdd(false); }}/>}
    </div>
  );
}

function AddExerciseModal({ onClose, onAdd }) {
  const [f, setF] = React.useState({ n:'', mg:'Chest', dif:'Beginner', eq:'', sets:'3×10', ico:'dumbbell' });
  function submit(e) {
    e.preventDefault();
    if (!f.n || !f.eq) { alert('Name and equipment are required.'); return; }
    onAdd(f);
  }
  return (
    <div style={{position:'fixed', inset:0, background:'rgba(14,26,17,0.55)', backdropFilter:'blur(4px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:100, padding:20}}>
      <form onSubmit={submit} style={{background:'#fff', borderRadius:16, width:'100%', maxWidth:540, padding:28, boxShadow:'0 24px 60px rgba(0,0,0,0.30)'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:18}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:28, margin:0, color:'#0E1A11', letterSpacing:'-0.02em'}}>New <em style={{fontStyle:'italic', color:'#2E5535'}}>exercise</em>.</h3>
          <button type="button" onClick={onClose} style={{width:32, height:32, border:0, background:'#F5F5F3', borderRadius:8, cursor:'pointer'}}>
            <i data-lucide="x" style={{width:14, height:14, color:'#4A4A45'}}></i>
          </button>
        </div>
        <div style={{display:'grid', gap:12}}>
          <div><label style={lblStyle}>Name</label><input value={f.n} onChange={e=>setF({...f, n:e.target.value})} placeholder="e.g. Bulgarian split squat" style={{...inputStyle, width:'100%'}}/></div>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
            <div><label style={lblStyle}>Muscle group</label><select value={f.mg} onChange={e=>setF({...f, mg:e.target.value})} style={{...inputStyle, width:'100%'}}><option>Chest</option><option>Back</option><option>Legs</option><option>Shoulders</option><option>Arms</option><option>Core</option><option>Cardio</option><option>Full body</option></select></div>
            <div><label style={lblStyle}>Difficulty</label><select value={f.dif} onChange={e=>setF({...f, dif:e.target.value})} style={{...inputStyle, width:'100%'}}><option>Beginner</option><option>Intermediate</option><option>Advanced</option></select></div>
          </div>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
            <div><label style={lblStyle}>Equipment</label><input value={f.eq} onChange={e=>setF({...f, eq:e.target.value})} placeholder="Barbell · Rack" style={{...inputStyle, width:'100%'}}/></div>
            <div><label style={lblStyle}>Sets × Reps</label><input value={f.sets} onChange={e=>setF({...f, sets:e.target.value})} placeholder="3×10" style={{...inputStyle, width:'100%'}}/></div>
          </div>
        </div>
        <div style={{display:'flex', justifyContent:'flex-end', gap:10, marginTop:20}}>
          <button type="button" onClick={onClose} style={{padding:'10px 16px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, fontWeight:500, color:'#1A2E1A', cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
          <button type="submit" style={{padding:'10px 20px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>Add to library</button>
        </div>
      </form>
    </div>
  );
}

// ============ CHAT — working ============
function TrainerChat({ members, threads, sendMessage }) {
  const [activeId, setActiveId] = React.useState(threads[0]?.memberId || null);
  const [msg, setMsg] = React.useState('');
  const thread = threads.find(t => t.memberId === activeId);
  const member = members.find(m => m.id === activeId);
  const scrollRef = React.useRef(null);
  React.useEffect(() => { if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight; }, [thread?.messages.length]);

  function send(e) {
    e.preventDefault();
    if (!msg.trim() || !activeId) return;
    sendMessage(activeId, msg.trim());
    setMsg('');
  }

  return (
    <div style={{padding:'20px 32px 32px', height:'calc(100vh - 100px)'}}>
      <div style={{display:'grid', gridTemplateColumns:'280px 1fr', gap:0, background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, height:'100%', overflow:'hidden'}}>
        {/* Thread list */}
        <div style={{borderRight:'1px solid #EEEEEA', overflowY:'auto', background:'#F5F5F3'}}>
          <div style={{padding:'16px 18px', borderBottom:'1px solid #EEEEEA', display:'flex', justifyContent:'space-between', alignItems:'center'}}>
            <span style={{fontFamily:'Instrument Serif, serif', fontSize:18, color:'#0E1A11'}}>Conversations</span>
            <span style={{padding:'3px 8px', background:'#4A7A4F', color:'#fff', borderRadius:999, fontSize:10, fontWeight:600, letterSpacing:'0.06em', fontFamily:'Geist Mono, monospace'}}>{threads.filter(t=>t.unread).length} NEW</span>
          </div>
          {threads.map(t => {
            const m = members.find(mm => mm.id === t.memberId);
            if (!m) return null;
            const last = t.messages[t.messages.length-1];
            return (
              <button key={t.memberId} onClick={()=>setActiveId(t.memberId)} style={{width:'100%', textAlign:'left', padding:'14px 16px', background: activeId===t.memberId?'#fff':'transparent', border:0, borderBottom:'1px solid #F0F0EC', cursor:'pointer', display:'flex', gap:12, alignItems:'flex-start', fontFamily:'inherit'}}>
                <div style={{width:36, height:36, borderRadius:'50%', background:'#1A2E1A', color:'#FAFAFA', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:14, flexShrink:0}}>{m.n.split(' ').map(w=>w[0]).join('')}</div>
                <div style={{flex:1, minWidth:0}}>
                  <div style={{display:'flex', justifyContent:'space-between', alignItems:'baseline'}}>
                    <span style={{fontSize:13, fontWeight:600, color:'#0E1A11'}}>{m.n}</span>
                    <span style={{fontFamily:'Geist Mono, monospace', fontSize:10, color:'#8FB286'}}>{last?.t || ''}</span>
                  </div>
                  <div style={{fontSize:12, color:'#6A6A65', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', marginTop:2}}>{last?.text || ''}</div>
                </div>
                {t.unread > 0 && <span style={{padding:'1px 6px', background:'#4A7A4F', color:'#fff', borderRadius:999, fontSize:10, fontWeight:600, fontFamily:'Geist Mono, monospace', alignSelf:'center'}}>{t.unread}</span>}
              </button>
            );
          })}
        </div>

        {/* Chat panel */}
        {thread && member ? (
          <div style={{display:'flex', flexDirection:'column', minHeight:0}}>
            <div style={{padding:'14px 22px', borderBottom:'1px solid #EEEEEA', display:'flex', alignItems:'center', gap:14, background:'#fff'}}>
              <div style={{width:40, height:40, borderRadius:'50%', background:'#1A2E1A', color:'#FAFAFA', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:16}}>{member.n.split(' ').map(w=>w[0]).join('')}</div>
              <div style={{flex:1}}>
                <div style={{fontSize:14, fontWeight:600, color:'#0E1A11'}}>{member.n}</div>
                <div style={{fontSize:11, color:'#4A7A4F', display:'flex', alignItems:'center', gap:5}}><span style={{width:6, height:6, borderRadius:'50%', background:'#4A7A4F'}}></span>Online · {member.goal}</div>
              </div>
              <button style={{width:34, height:34, borderRadius:8, background:'#F5F5F3', border:'1px solid #EEEEEA', cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}><i data-lucide="phone" style={{width:14, height:14, color:'#1A2E1A'}}></i></button>
              <button style={{width:34, height:34, borderRadius:8, background:'#F5F5F3', border:'1px solid #EEEEEA', cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}><i data-lucide="more-horizontal" style={{width:14, height:14, color:'#1A2E1A'}}></i></button>
            </div>

            <div ref={scrollRef} style={{flex:1, overflowY:'auto', padding:'20px 24px', background:'#F5F5F3', display:'flex', flexDirection:'column', gap:10}}>
              <div style={{textAlign:'center', fontFamily:'Geist Mono, monospace', fontSize:10, color:'#8FB286', letterSpacing:'0.10em', margin:'4px 0 12px'}}>TODAY</div>
              {thread.messages.map((m,i)=>(
                m.from === 'me' ? (
                  <div key={i} style={{display:'flex', justifyContent:'flex-end'}}>
                    <div style={{background:'#0E1A11', color:'#FAFAFA', padding:'10px 14px', borderRadius:'14px 14px 4px 14px', maxWidth:'70%', fontSize:13, lineHeight:1.5}}>
                      {m.text}
                      <div style={{fontFamily:'Geist Mono, monospace', fontSize:9, color:'#8FB286', textAlign:'right', marginTop:4, letterSpacing:'0.06em'}}>{m.t}</div>
                    </div>
                  </div>
                ) : (
                  <div key={i} style={{display:'flex', gap:8, alignItems:'flex-end'}}>
                    <div style={{width:26, height:26, borderRadius:'50%', background:'#1A2E1A', color:'#FAFAFA', display:'flex', alignItems:'center', justifyContent:'center', fontSize:10, fontWeight:600, flexShrink:0}}>{member.n.split(' ').map(w=>w[0]).join('')}</div>
                    <div style={{background:'#fff', border:'1px solid #EEEEEA', padding:'10px 14px', borderRadius:'14px 14px 14px 4px', maxWidth:'70%', fontSize:13, color:'#0E1A11', lineHeight:1.5}}>
                      {m.text}
                      <div style={{fontFamily:'Geist Mono, monospace', fontSize:9, color:'#8FB286', marginTop:4, letterSpacing:'0.06em'}}>{m.t}</div>
                    </div>
                  </div>
                )
              ))}
            </div>

            <form onSubmit={send} style={{padding:'14px 18px', borderTop:'1px solid #EEEEEA', display:'flex', gap:10, background:'#fff'}}>
              <button type="button" style={{width:38, height:38, borderRadius:8, background:'#F5F5F3', border:'1px solid #EEEEEA', cursor:'pointer'}}><i data-lucide="paperclip" style={{width:14, height:14, color:'#4A4A45'}}></i></button>
              <input value={msg} onChange={e=>setMsg(e.target.value)} placeholder={`Message ${member.n.split(' ')[0]}...`} style={{flex:1, padding:'0 14px', height:38, border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, fontFamily:'inherit', outline:'none', color:'#0E1A11', background:'#F5F5F3'}}/>
              <button type="submit" disabled={!msg.trim()} style={{padding:'0 18px', background: msg.trim()?'#4A7A4F':'#EEEEEA', color:'#fff', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor: msg.trim()?'pointer':'not-allowed', fontFamily:'inherit', display:'flex', alignItems:'center', gap:6}}>
                <i data-lucide="send" style={{width:13, height:13}}></i>Send
              </button>
            </form>
          </div>
        ) : (
          <div style={{display:'grid', placeItems:'center', background:'#F5F5F3', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:22, color:'#6A6A65'}}>Select a conversation</div>
        )}
      </div>
    </div>
  );
}

// ============ Shared styles ============
const inputStyle = { height:38, padding:'0 12px', border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, fontFamily:'inherit', color:'#0E1A11', background:'#F5F5F3', outline:'none' };
const lblStyle = { display:'block', fontSize:11, fontWeight:600, letterSpacing:'0.10em', textTransform:'uppercase', color:'#8FB286', fontFamily:'Geist Mono, monospace', marginBottom:6 };
const thStyle = { padding:'10px 10px', textAlign:'left', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase' };
const tdStyle = { padding:'12px 10px', fontFamily:'Geist Mono, monospace', fontSize:12, color:'#0E1A11' };

window.TrainerMembers = TrainerMembers;
window.TrainerMemberDetail = TrainerMemberDetail;
window.TrainerMeasurements = TrainerMeasurements;
window.TrainerPT = TrainerPT;
window.TrainerWorkouts = TrainerWorkouts;
window.TrainerExercises = TrainerExercises;
window.TrainerChat = TrainerChat;
