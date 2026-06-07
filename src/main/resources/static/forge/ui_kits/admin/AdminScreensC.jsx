// Forge Admin — additional screens: Equipment, Staff, Announcements, Reports

function EquipmentScreen() {
  const eq = [
    { n:'Treadmill Pro X1', cat:'Cardio', count:8, ok:7, maint:1, broken:0, last:'12 May' },
    { n:'Olympic Bench Press', cat:'Strength', count:4, ok:4, maint:0, broken:0, last:'08 May' },
    { n:'Cable Crossover Station', cat:'Strength', count:2, ok:2, maint:0, broken:0, last:'02 May' },
    { n:'Spin Bike Elite', cat:'Cardio', count:12, ok:10, maint:2, broken:0, last:'10 May' },
    { n:'Smith Machine', cat:'Strength', count:2, ok:1, maint:0, broken:1, last:'14 May' },
    { n:'Rowing Erg Concept2', cat:'Cardio', count:6, ok:6, maint:0, broken:0, last:'01 May' },
    { n:'Dumbbell Set · 1-50 kg', cat:'Free weights', count:1, ok:1, maint:0, broken:0, last:'—' },
    { n:'Squat Rack Power', cat:'Strength', count:3, ok:3, maint:0, broken:0, last:'06 May' },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:16, marginBottom:20}}>
        {[
          { l:'Total units', v:'38', sub:'Across 5 categories', ico:'dumbbell' },
          { l:'In service', v:'34', sub:'89% uptime', ico:'check-circle', color:'#4A7A4F' },
          { l:'Scheduled maint.', v:'3', sub:'This week', ico:'wrench', color:'#1A2E1A' },
          { l:'Out of service', v:'1', sub:'Smith Machine #2', ico:'alert-triangle', color:'#A8413A' },
        ].map((s,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'18px 20px'}}>
            <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:10}}>
              <div style={{width:28, height:28, borderRadius:8, background:'#E8EFE5', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide={s.ico} style={{width:14, height:14, color:s.color || '#1A2E1A'}}></i>
              </div>
              <span style={{fontSize:12, color:'#4A4A45'}}>{s.l}</span>
            </div>
            <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:'#0E1A11', letterSpacing:'-0.02em', marginBottom:4}}>{s.v}</div>
            <div style={{fontSize:11, color:'#6A6A65'}}>{s.sub}</div>
          </div>
        ))}
      </div>

      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:14}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>Equipment inventory</h3>
          <button style={{padding:'8px 14px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:12, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="plus" style={{width:13, height:13}}></i>Add equipment
          </button>
        </div>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead><tr style={{borderBottom:'1px solid #EEEEEA'}}>
            {['Equipment','Category','Total','In service','Maint.','Broken','Last service'].map((h,i)=>(<th key={i} style={{padding:'10px 12px', textAlign: i<2 ? 'left' : 'right', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase'}}>{h}</th>))}
          </tr></thead>
          <tbody>
            {eq.map((e,i)=>(
              <tr key={i} style={{borderBottom:'1px solid #F0F0EC'}}>
                <td style={{padding:'14px 12px', fontSize:13, fontWeight:600, color:'#0E1A11'}}>{e.n}</td>
                <td style={{padding:'14px 12px', fontSize:12, color:'#4A4A45'}}><span style={{padding:'3px 9px', borderRadius:4, background:'#F5F5F3', border:'1px solid #EEEEEA', fontSize:11}}>{e.cat}</span></td>
                <td style={{padding:'14px 12px', textAlign:'right', fontFamily:'Geist Mono, monospace', fontSize:13, color:'#0E1A11'}}>{e.count}</td>
                <td style={{padding:'14px 12px', textAlign:'right', fontFamily:'Geist Mono, monospace', fontSize:13, color:'#4A7A4F', fontWeight:600}}>{e.ok}</td>
                <td style={{padding:'14px 12px', textAlign:'right', fontFamily:'Geist Mono, monospace', fontSize:13, color:e.maint?'#1A2E1A':'#6A6A65'}}>{e.maint}</td>
                <td style={{padding:'14px 12px', textAlign:'right', fontFamily:'Geist Mono, monospace', fontSize:13, color:e.broken?'#A8413A':'#6A6A65', fontWeight:e.broken?600:400}}>{e.broken}</td>
                <td style={{padding:'14px 12px', textAlign:'right', fontFamily:'Geist Mono, monospace', fontSize:11, color:'#4A4A45'}}>{e.last}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function StaffScreen() {
  const staff = [
    { n:'Arben Brahimi', role:'Owner · Admin', em:'arben@forge.al', ph:'+355 69 200 1000', hire:'Jan 2024', clients:'—', sp:'Operations', color:'#0E1A11' },
    { n:'Rea Hoxha', role:'Lead trainer', em:'rea@forge.al', ph:'+355 69 200 1001', hire:'Feb 2024', clients:18, sp:'Strength · HIIT', color:'#4A7A4F' },
    { n:'Erion Dautaj', role:'Trainer', em:'erion@forge.al', ph:'+355 69 200 1002', hire:'Apr 2024', clients:12, sp:'Hypertrophy', color:'#4A7A4F' },
    { n:'Klara Hysi', role:'Trainer', em:'klara@forge.al', ph:'+355 69 200 1003', hire:'Aug 2024', clients:9, sp:'Mobility · Yoga', color:'#4A7A4F' },
    { n:'Genti Marku', role:'Reception', em:'genti@forge.al', ph:'+355 69 200 1004', hire:'Mar 2024', clients:'—', sp:'Front desk', color:'#4A4A45' },
    { n:'Aleksandër Plaku', role:'Maintenance', em:'aleks@forge.al', ph:'+355 69 200 1005', hire:'Jun 2024', clients:'—', sp:'Equipment', color:'#4A4A45' },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:16, marginBottom:20}}>
        {staff.map((s,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
            <div style={{display:'flex', alignItems:'center', gap:14, marginBottom:14}}>
              <div style={{width:48, height:48, borderRadius:'50%', background:s.color, color:'#FAFAFA', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:20}}>{s.n.split(' ').map(w=>w[0]).join('')}</div>
              <div style={{flex:1}}>
                <div style={{fontSize:15, fontWeight:600, color:'#0E1A11'}}>{s.n}</div>
                <div style={{fontSize:12, color:'#4A4A45'}}>{s.role}</div>
              </div>
              {typeof s.clients === 'number' && <span style={{padding:'4px 10px', borderRadius:6, background:'#E8EFE5', fontSize:11, fontWeight:600, color:'#1A2E1A', fontFamily:'Geist Mono, monospace'}}>{s.clients} clients</span>}
            </div>
            <div style={{padding:'10px 0', borderTop:'1px solid #F0F0EC', display:'flex', flexDirection:'column', gap:4}}>
              <div style={{fontSize:11, color:'#6A6A65', fontFamily:'Geist Mono, monospace'}}>{s.em}</div>
              <div style={{fontSize:11, color:'#6A6A65', fontFamily:'Geist Mono, monospace'}}>{s.ph}</div>
            </div>
            <div style={{display:'flex', justifyContent:'space-between', paddingTop:10, borderTop:'1px solid #F0F0EC', marginTop:6, fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', textTransform:'uppercase'}}>
              <span>{s.sp}</span><span>Since {s.hire}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function AnnouncementsScreen() {
  const list = [
    { d:'12 May 2026', a:'Arben (you)', t:'Sauna closed Saturday morning', m:'The sauna will be closed for routine maintenance on Saturday 17 May from 06:00 to 12:00. We apologize for the inconvenience.', tags:['Operations'] },
    { d:'08 May 2026', a:'Rea Hoxha', t:'New HIIT class on Tuesdays', m:'Starting May 20, we are launching a new HIIT class every Tuesday at 19:00. Limited to 12 spots — book in the app.', tags:['Classes'] },
    { d:'02 May 2026', a:'Arben (you)', t:'Summer hours starting June 1', m:'Beginning June 1, the gym will be open from 06:00 to 23:30 on weekdays. Weekend hours unchanged.', tags:['Hours'] },
    { d:'24 Apr 2026', a:'Genti Marku', t:'Lost & found reminder', m:'Several water bottles and a pair of headphones are in the reception lost & found. Items not claimed after May 1 will be donated.', tags:['Notice'] },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:20}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Post an announcement</h3>
        <input placeholder="Title" style={{...inputStyle, width:'100%', marginBottom:10}}/>
        <textarea placeholder="Message — visible to all members in their app and on the website..." style={{...inputStyle, width:'100%', height:90, padding:'10px 12px', resize:'vertical'}}></textarea>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:12}}>
          <div style={{display:'flex', gap:8}}>
            <select style={{...inputStyle, width:160}}><option>All members</option><option>Premium only</option><option>By trainer</option></select>
            <button style={{padding:'10px 14px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
              <i data-lucide="bell" style={{width:13, height:13}}></i>Push notification
            </button>
          </div>
          <button style={{padding:'10px 20px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>Publish</button>
        </div>
      </div>

      <div style={{display:'flex', flexDirection:'column', gap:12}}>
        {list.map((a,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'20px 24px'}}>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:8}}>
              <div>
                <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:6}}>
                  <h4 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:20, margin:0, color:'#0E1A11', letterSpacing:'-0.01em'}}>{a.t}</h4>
                  {a.tags.map(t=>(<span key={t} style={{padding:'2px 8px', borderRadius:4, background:'#E8EFE5', fontSize:9.5, fontWeight:600, color:'#1A2E1A', letterSpacing:'0.06em', fontFamily:'Geist Mono, monospace'}}>{t.toUpperCase()}</span>))}
                </div>
                <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', textTransform:'uppercase'}}>{a.d} · BY {a.a.toUpperCase()}</div>
              </div>
              <button style={{padding:'4px 8px', background:'transparent', border:'1px solid #EEEEEA', borderRadius:6, fontSize:11, color:'#1A2E1A', cursor:'pointer'}}>···</button>
            </div>
            <p style={{fontSize:13, color:'#4A4A45', lineHeight:1.55, margin:0, paddingTop:10, borderTop:'1px solid #F0F0EC'}}>{a.m}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

function ReportsScreen() {
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:16, marginBottom:20}}>
        {[
          { l:'Revenue · YTD', v:'€87,420', d:'+18% YoY' },
          { l:'Member retention', v:'87%', d:'+4 points' },
          { l:'Avg. tenure', v:'8.2 mo', d:'+1.1 mo' },
          { l:'NPS', v:'62', d:'+8 pts' },
        ].map((s,i)=>(
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'18px 20px'}}>
            <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8}}>{s.l}</div>
            <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:'#0E1A11', letterSpacing:'-0.02em', marginBottom:6}}>{s.v}</div>
            <div style={{fontSize:11, color:'#4A7A4F', fontWeight:600, fontFamily:'Geist Mono, monospace'}}>{s.d}</div>
          </div>
        ))}
      </div>

      {/* Revenue line chart */}
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:20}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:18}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>Revenue trend · 12 months</h3>
          <button style={{padding:'8px 14px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="download" style={{width:13, height:13}}></i>Download Word report
          </button>
        </div>
        {(() => {
          const pts = [12.4,13.2,14.8,15.6,17.2,18.4,19.1,18.6,17.9,19.4,20.2,20.6];
          const max = 24, w = 900, h = 180;
          const path = pts.map((p,i)=>`${i===0?'M':'L'} ${(i/(pts.length-1))*w} ${h-((p/max)*h)}`).join(' ');
          const labels = ['Jun','Jul','Aug','Sep','Oct','Nov','Dec','Jan','Feb','Mar','Apr','May'];
          return (
            <div>
              <svg viewBox={`0 0 ${w} ${h+24}`} width="100%" height="220">
                <defs><linearGradient id="rg" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#0E1A11" stopOpacity="0.20"/><stop offset="100%" stopColor="#0E1A11" stopOpacity="0"/></linearGradient></defs>
                {[0.25, 0.5, 0.75].map(g => (<line key={g} x1="0" y1={h*g} x2={w} y2={h*g} stroke="#F0F0EC" strokeWidth="1"/>))}
                <path d={`${path} L ${w} ${h} L 0 ${h} Z`} fill="url(#rg)"/>
                <path d={path} stroke="#0E1A11" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
                {pts.map((p,i)=>(<circle key={i} cx={(i/(pts.length-1))*w} cy={h-(p/max)*h} r={i===pts.length-1?5:3} fill={i===pts.length-1?'#4A7A4F':'#0E1A11'}/>))}
              </svg>
              <div style={{display:'flex', justifyContent:'space-between', marginTop:6, fontSize:10, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', textTransform:'uppercase'}}>
                {labels.map(l=><span key={l}>{l}</span>)}
              </div>
            </div>
          );
        })()}
      </div>

      {/* Attendance heatmap */}
      <div style={{display:'grid', gridTemplateColumns:'1.4fr 1fr', gap:16}}>
        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 16px', color:'#0E1A11'}}>Busiest hours · last 30 days</h3>
          {(() => {
            const hours = [...Array(15)].map((_, i) => i + 6);
            const days = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];
            return (
              <div>
                <div style={{display:'flex', gap:3, marginLeft:36}}>
                  {hours.map((hr,hi)=>(<div key={hi} style={{flex:1, fontSize:9, color:'#8FB286', fontFamily:'Geist Mono, monospace', textAlign:'center'}}>{hi%2===0?hr:''}</div>))}
                </div>
                {days.map((d,di)=>(
                  <div key={di} style={{display:'flex', gap:3, marginTop:3, alignItems:'center'}}>
                    <span style={{width:32, fontSize:10, color:'#4A4A45', fontWeight:500}}>{d}</span>
                    {hours.map((_,hi) => {
                      const peak = (hi >= 1 && hi <= 4) || (hi >= 9 && hi <= 12);
                      const weekend = di >= 5;
                      let v = peak ? 0.55 + Math.random()*0.4 : 0.10 + Math.random()*0.30;
                      if (weekend && peak) v *= 0.65;
                      return <div key={hi} style={{flex:1, aspectRatio:'1', borderRadius:3, background:`rgba(14,26,17,${0.06 + v*0.75})`}}></div>;
                    })}
                  </div>
                ))}
                <div style={{display:'flex', alignItems:'center', gap:6, marginTop:14, fontSize:10, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.04em'}}>
                  <span>LESS</span>
                  {[0.10,0.30,0.50,0.70,0.90].map(v => <div key={v} style={{width:14, height:14, borderRadius:3, background:`rgba(14,26,17,${0.06 + v*0.75})`}}></div>)}
                  <span>MORE</span>
                </div>
              </div>
            );
          })()}
        </div>

        <div style={{background:'#0E1A11', color:'#E8EFE5', borderRadius:14, padding:'22px 24px'}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 16px'}}>Member status</h3>
          {(() => {
            const data = [{l:'Active', v:213, c:'#4A7A4F'},{l:'Expiring', v:18, c:'#4A7A4F'},{l:'Expired', v:14, c:'#A8413A'},{l:'Paused', v:2, c:'#6A6A65'}];
            const total = data.reduce((s,d)=>s+d.v,0);
            return (
              <div>
                <div style={{position:'relative', width:160, height:160, margin:'8px auto 16px'}}>
                  <svg viewBox="0 0 100 100" width="160" height="160">
                    <circle cx="50" cy="50" r="44" fill="none" stroke="rgba(220,228,214,0.10)" strokeWidth="8"/>
                    {(() => { let off = 0; return data.map((d,i)=>{ const dash = (d.v/total)*276; const el = <circle key={i} cx="50" cy="50" r="44" fill="none" stroke={d.c} strokeWidth="8" strokeDasharray={`${dash} 276`} strokeDashoffset={-off} transform="rotate(-90 50 50)" strokeLinecap="butt"/>; off += dash; return el; }); })()}
                  </svg>
                  <div style={{position:'absolute', inset:0, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center'}}>
                    <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, color:'#FAFAFA', lineHeight:1}}>247</div>
                    <div style={{fontSize:10, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em'}}>TOTAL</div>
                  </div>
                </div>
                {data.map(d=>(
                  <div key={d.l} style={{display:'flex', justifyContent:'space-between', alignItems:'center', padding:'8px 0', borderTop:'1px solid rgba(220,228,214,0.10)'}}>
                    <span style={{display:'flex', alignItems:'center', gap:8, fontSize:12, color:'#D2E0CB'}}><span style={{width:8, height:8, borderRadius:'50%', background:d.c}}></span>{d.l}</span>
                    <span style={{fontFamily:'Geist Mono, monospace', fontSize:13, color:'#FAFAFA', fontWeight:600}}>{d.v}</span>
                  </div>
                ))}
              </div>
            );
          })()}
        </div>
      </div>
    </div>
  );
}

window.EquipmentScreen = EquipmentScreen;
window.StaffScreen = StaffScreen;
window.AnnouncementsScreen = AnnouncementsScreen;
window.ReportsScreen = ReportsScreen;
