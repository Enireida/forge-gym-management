// Forge — Member Web Portal — Progress, Membership, AI Coach
// All state persists via useLocalState. Coach reads logged meals + workouts
// from localStorage so its replies are grounded in real activity.

const { M_SAGE, M_SAGE_900, M_SAGE_800, M_SAGE_700, M_SAGE_500, M_SAGE_LIGHT, M_SAGE_50,
        M_BONE_50, M_BONE_25, M_BONE_100, M_BONE_200, M_BONE_500, M_BONE_700,
        M_FG, M_ACCENT } = window.M_TOKENS;
const { today, fmtDate, ymd } = window.M_HELPERS;
const useLocalState = window.useLocalState;
const readLocal = window.readLocal;

// ============================================================
// MemberProgress — weight trend, body comp, measurement history
// 30D/3M/6M/1Y now actually slice the data.
// "Request measurement" creates a persisted request the trainer can see.
// ============================================================

// Synthetic weight series — one point per week for the past 12 months.
// Newest first. Built so 30D / 3M / 6M / 1Y all produce a meaningful slice.
const WEIGHT_SERIES = (() => {
  // weight starts at 82.0 a year ago and trends to 78.4 today, with noise.
  const start = 82.0, end = 78.4;
  const weeks = 52;
  const arr = [];
  for (let i = weeks; i >= 0; i--) {
    const t = 1 - i/weeks;          // 0..1
    const trend = start + (end - start) * t;
    const noise = (Math.sin(i*1.3) + Math.cos(i*0.7)) * 0.18;
    const d = new Date(); d.setDate(d.getDate() - i*7);
    arr.push({ date: ymd(d), wt: +(trend + noise).toFixed(1) });
  }
  return arr;
})();

const MEASUREMENTS = [
  { d:'12 May', wt:78.4, ht:168, bmi:27.8, bf:22.4, waist:82, chest:98 },
  { d:'05 May', wt:78.7, ht:168, bmi:27.9, bf:22.8, waist:83, chest:98 },
  { d:'28 Apr', wt:79.1, ht:168, bmi:28.0, bf:23.2, waist:84, chest:97 },
  { d:'21 Apr', wt:79.6, ht:168, bmi:28.2, bf:23.5, waist:84, chest:97 },
  { d:'14 Apr', wt:80.2, ht:168, bmi:28.4, bf:24.1, waist:85, chest:96 },
];

function MemberProgress() {
  const [range, setRange] = React.useState('30D');
  const [measReqs, setMeasReqs] = useLocalState('measurementReq', []);
  const [reqOpen, setReqOpen] = React.useState(false);
  const [reqNote, setReqNote] = React.useState('');

  const RANGE_DAYS = { '30D':30, '3M':90, '6M':180, '1Y':365 };
  const cutoff = new Date(); cutoff.setDate(cutoff.getDate() - RANGE_DAYS[range]);
  const slice = WEIGHT_SERIES.filter(p => new Date(p.date) >= cutoff);
  const pts = slice.map(p => p.wt);
  const currentWt = pts[pts.length - 1];
  const startWt = pts[0];
  const delta = +(currentWt - startWt).toFixed(1);
  const isLoss = delta < 0;

  const submitMeasurementRequest = () => {
    setMeasReqs([{ id: Date.now(), note: reqNote, submitted: fmtDate(today(), {withDay:false}), status:'pending' }, ...measReqs]);
    setReqNote(''); setReqOpen(false);
  };

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, marginBottom:16}}>
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 26px'}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:14}}>
            <div>
              <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase'}}>Weight · last {range}</div>
              <div style={{display:'flex', alignItems:'baseline', gap:10, marginTop:4}}>
                <span style={{fontFamily:'Instrument Serif, serif', fontSize:48, lineHeight:1, color:M_FG, letterSpacing:'-0.02em'}}>{currentWt}</span>
                <span style={{fontSize:14, color:M_BONE_700}}>kg</span>
                <span style={{padding:'3px 9px', borderRadius:6, background: isLoss ? '#E8EFE5' : '#F1D8D4', color: isLoss ? M_SAGE_500 : '#A8413A', fontSize:12, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>
                  {delta > 0 ? '+' : ''}{delta} kg
                </span>
              </div>
            </div>
            <div style={{display:'flex', gap:6}}>
              {['30D','3M','6M','1Y'].map(p => (
                <button key={p} onClick={()=>setRange(p)}
                        style={{padding:'5px 11px', borderRadius:6, background:range===p?M_SAGE:'#fff', color:range===p?M_BONE_50:M_FG, border:`1px solid ${M_BONE_100}`, fontSize:11, fontWeight:600, cursor:'pointer', fontFamily:'Geist Mono, monospace'}}>{p}</button>
              ))}
            </div>
          </div>
          {(() => {
            if (pts.length < 2) return null;
            const max = Math.max(...pts), min = Math.min(...pts);
            const rangeSpan = Math.max(max - min, 0.6);  // avoid divide-by-zero on flat data
            const w = 800, h = 180;
            const path = pts.map((p,i)=>`${i===0?'M':'L'} ${(i/(pts.length-1))*w} ${h-((p-min)/rangeSpan)*h}`).join(' ');
            const firstDate = new Date(slice[0].date);
            const midDate = new Date(slice[Math.floor(slice.length/2)].date);
            const lastDate = new Date(slice[slice.length-1].date);
            const fmtShort = (d) => d.toLocaleString('en-GB', {day:'2-digit', month:'short'}).toUpperCase();
            return (
              <>
                <svg viewBox={`0 0 ${w} ${h+30}`} width="100%" height="220">
                  <defs><linearGradient id="mg" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor={M_SAGE} stopOpacity="0.22"/><stop offset="100%" stopColor={M_SAGE} stopOpacity="0"/></linearGradient></defs>
                  {[0.25, 0.5, 0.75].map(g => (<line key={g} x1="0" y1={h*g} x2={w} y2={h*g} stroke={M_BONE_200} strokeWidth="1"/>))}
                  <path d={`${path} L ${w} ${h} L 0 ${h} Z`} fill="url(#mg)"/>
                  <path d={path} stroke={M_SAGE} strokeWidth="2.5" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
                  <circle cx={w} cy={h-((pts[pts.length-1]-min)/rangeSpan)*h} r="6" fill={M_ACCENT}/>
                </svg>
                <div style={{display:'flex', justifyContent:'space-between', marginTop:4, fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em'}}>
                  <span>{fmtShort(firstDate)}</span><span>{fmtShort(midDate)}</span><span>{fmtShort(lastDate)}</span>
                </div>
              </>
            );
          })()}
        </div>

        <div style={{background:M_SAGE, color:'#D2E0CB', borderRadius:14, padding:'24px 26px', position:'relative', overflow:'hidden'}}>
          <div style={{position:'absolute', top:-30, right:-30, width:120, height:120, borderRadius:'50%', background:'rgba(74,122,79,0.16)'}}></div>
          <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:14, position:'relative'}}>
            <i data-lucide="sparkles" style={{width:14, height:14, color:M_SAGE_LIGHT}}></i>
            <span style={{fontSize:11, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', color:M_SAGE_LIGHT, fontWeight:600, textTransform:'uppercase'}}>Coach insight</span>
          </div>
          <p style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:22, lineHeight:1.3, color:M_BONE_50, margin:'0 0 14px', position:'relative'}}>Plateau detected at 78.4 kg for 9 days. Three suggestions ready.</p>
          <button onClick={()=>window.dispatchEvent(new CustomEvent('forge:nav', {detail:'coach'}))}
                  style={{padding:'10px 16px', borderRadius:8, background:M_ACCENT, color:'#fff', border:0, fontSize:12, fontWeight:600, cursor:'pointer', position:'relative', fontFamily:'inherit'}}>
            Open coach chat →
          </button>
        </div>
      </div>

      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:M_FG}}>Body composition</h3>
        <div style={{display:'grid', gridTemplateColumns:'repeat(5,1fr)', gap:12}}>
          {[['Weight','78.4','kg','−1.8'],['BMI','27.8','','−0.6'],['Body fat','22.4','%','−1.7'],['Waist','82','cm','−3'],['Chest','98','cm','+2']].map(([l,v,u,d],i)=>(
            <div key={i} style={{padding:'14px 16px', background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:10}}>
              <div style={{fontSize:11, color:M_BONE_700, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{l}</div>
              <div style={{display:'flex', alignItems:'baseline', gap:4, margin:'6px 0 4px'}}>
                <span style={{fontFamily:'Instrument Serif, serif', fontSize:28, lineHeight:1, color:M_FG}}>{v}</span>
                {u && <span style={{fontSize:11, color:M_BONE_700}}>{u}</span>}
              </div>
              <div style={{fontSize:11, color:d.startsWith('+')?'#A8413A':M_SAGE_500, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>{d}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Pending measurement requests */}
      {measReqs.length > 0 && (
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:20, margin:'0 0 12px', color:M_FG}}>Measurement requests</h3>
          {measReqs.map(r => (
            <div key={r.id} style={{display:'flex', alignItems:'center', gap:14, padding:'12px 14px', background:'#FFF8EA', border:'1px solid #F4E7CC', borderRadius:10, marginBottom:8}}>
              <div style={{width:32, height:32, borderRadius:8, background:'#B8893A', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                <i data-lucide="clock" style={{width:14, height:14, color:'#fff'}}></i>
              </div>
              <div style={{flex:1, minWidth:0}}>
                <div style={{fontSize:13, fontWeight:600, color:M_FG}}>Awaiting trainer · submitted {r.submitted}</div>
                {r.note && <div style={{fontSize:12, color:M_BONE_700, marginTop:2}}>"{r.note}"</div>}
              </div>
              <button onClick={()=>setMeasReqs(measReqs.filter(x => x.id !== r.id))}
                      style={{padding:'6px 10px', borderRadius:6, background:'transparent', border:`1px solid ${M_BONE_100}`, fontSize:11, color:M_BONE_700, cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
            </div>
          ))}
        </div>
      )}

      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:14}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:M_FG}}>Measurement history</h3>
          <button onClick={()=>setReqOpen(true)}
                  style={{padding:'7px 12px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>
            + Request measurement
          </button>
        </div>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead><tr style={{borderBottom:`1px solid ${M_BONE_100}`}}>
            {['Date','Weight','Height','BMI','Body fat','Waist','Chest'].map(h=><th key={h} style={{padding:'10px 12px', textAlign:'left', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase'}}>{h}</th>)}
          </tr></thead>
          <tbody>
            {MEASUREMENTS.map((m,i)=>(
              <tr key={i} style={{borderBottom:`1px solid ${M_BONE_200}`}}>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:12, color:M_BONE_700}}>{m.d}</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG, fontWeight:600}}>{m.wt} kg</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG}}>{m.ht} cm</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG}}>{m.bmi}</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG}}>{m.bf}%</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG}}>{m.waist} cm</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, color:M_FG}}>{m.chest} cm</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {reqOpen && (
        <div onClick={()=>setReqOpen(false)} style={{position:'fixed', inset:0, background:'rgba(14,26,17,0.55)', backdropFilter:'blur(4px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:100, padding:24}}>
          <div onClick={e=>e.stopPropagation()} style={{background:'#fff', borderRadius:16, padding:'24px 28px', maxWidth:460, width:'100%', boxShadow:'0 28px 60px rgba(14,26,17,0.32)'}}>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:28, lineHeight:1, margin:'0 0 6px', color:M_FG}}>Request a measurement</h3>
            <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 16px', lineHeight:1.55}}>Rea will record your body composition at your next session. You can leave a note (e.g. tighter waist measurement, body-fat caliper test).</p>
            <textarea value={reqNote} onChange={e=>setReqNote(e.target.value)} placeholder="e.g. Please measure waist tighter this time."
                      style={{width:'100%', minHeight:74, padding:'10px 12px', borderRadius:8, border:`1px solid ${M_BONE_100}`, fontSize:13, fontFamily:'inherit', resize:'vertical', boxSizing:'border-box', outline:'none', color:M_FG, background:M_BONE_25}}/>
            <div style={{display:'flex', gap:8, marginTop:16}}>
              <button onClick={()=>setReqOpen(false)} style={{padding:'10px 16px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:13, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
              <button onClick={submitMeasurementRequest} style={{flex:1, padding:'10px', borderRadius:8, background:M_SAGE, color:M_BONE_50, border:0, fontSize:13, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>Submit request →</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ============================================================
// MemberMembership — active plan, plans + PT add-ons (separated),
// pause/freeze flow, history. Replaces the old Payments page.
// ============================================================
const MONTHLY_PLANS = [
  { id:'basic',   name:'Basic',   price:25, period:'month', perks:['Gym access (Mon–Fri)','Locker room','Basic equipment'] },
  { id:'premium', name:'Premium', price:45, period:'month', perks:['Full gym + sauna','AI coach + food log','Group classes'], recommended:true },
  { id:'elite',   name:'Elite',   price:75, period:'month', perks:['Premium + unlimited PT','Body composition monthly','Nutrition plan'] },
];

const ADDONS = [
  { id:'pt-pack-4', name:'PT pack · 4 sessions', price:120, period:'4 sessions', perks:['Four 60-min PT sessions','Valid for 6 weeks','With your assigned trainer'] },
  { id:'pt-pack-8', name:'PT pack · 8 sessions', price:220, period:'8 sessions', perks:['Eight 60-min PT sessions','Valid for 12 weeks','Better per-session price'] },
  { id:'nutrition', name:'Nutrition plan', price:60, period:'one-off', perks:['1-hour consult with a dietitian','12-week meal plan tailored to you','Weekly check-in messages'] },
];

function MemberMembership() {
  const [requests, setRequests] = useLocalState('membershipReq', []);
  const [pauseReq, setPauseReq] = useLocalState('pauseReq', null);
  const [openModal, setOpenModal] = React.useState(null);
  const [method, setMethod] = React.useState('Cash at desk');
  const [note, setNote] = React.useState('');
  const [pauseOpen, setPauseOpen] = React.useState(false);
  const [pauseWeeks, setPauseWeeks] = React.useState(4);
  const [pauseReason, setPauseReason] = React.useState('');

  const submitRequest = () => {
    setRequests([{
      id: Date.now(),
      plan: openModal.name,
      amount: openModal.price,
      period: openModal.period,
      kind: openModal.kind,
      status: 'pending',
      submitted: fmtDate(today(), {withDay:false}),
      method, note,
    }, ...requests]);
    setOpenModal(null); setNote('');
  };
  const cancelRequest = (id) => setRequests(requests.filter(r => r.id !== id));

  const submitPause = () => {
    setPauseReq({ weeks: pauseWeeks, reason: pauseReason, submitted: fmtDate(today(), {withDay:false}), status:'pending' });
    setPauseOpen(false); setPauseReason('');
  };

  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'1.6fr 1fr', gap:16, marginBottom:20}}>
        <div style={{background:M_SAGE, color:M_BONE_50, borderRadius:14, padding:'32px 36px', position:'relative', overflow:'hidden'}}>
          <div style={{position:'absolute', top:-40, right:-40, width:200, height:200, borderRadius:'50%', background:'rgba(74,122,79,0.18)'}}></div>
          <div style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB', marginBottom:10, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>Premium · Member since Mar 2024</div>
          <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:56, lineHeight:1, margin:'0 0 24px', letterSpacing:'-0.02em', color:M_BONE_50}}>Membership <em style={{color:M_SAGE_LIGHT}}>active</em>.</h1>
          <div style={{display:'flex', gap:32, position:'relative'}}>
            <div>
              <div style={{fontSize:11, color:'#D2E0CB', marginBottom:4, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>NEXT RENEWAL</div>
              <div style={{fontFamily:'Instrument Serif, serif', fontSize:28, lineHeight:1}}>02 Jun 2026</div>
              <div style={{fontSize:12, color:'#8FB286', marginTop:4, fontFamily:'Geist Mono, monospace'}}>23 DAYS LEFT</div>
            </div>
            <div>
              <div style={{fontSize:11, color:'#D2E0CB', marginBottom:4, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>YOUR PIN</div>
              <div style={{fontFamily:'Geist Mono, monospace', fontSize:28, fontWeight:500, letterSpacing:'0.20em'}}>4 7 2 9</div>
              <div style={{fontSize:12, color:'#8FB286', marginTop:4, fontFamily:'Geist Mono, monospace'}}>USE AT THE DOOR</div>
            </div>
            <div>
              <div style={{fontSize:11, color:'#D2E0CB', marginBottom:4, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>RFID CARD</div>
              <div style={{fontFamily:'Geist Mono, monospace', fontSize:18, lineHeight:1.4}}>1042 7382 91</div>
              <div style={{fontSize:12, color:'#8FB286', marginTop:4, fontFamily:'Geist Mono, monospace'}}>ACTIVE</div>
            </div>
          </div>
        </div>
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', display:'flex', flexDirection:'column'}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:20, margin:'0 0 14px', color:M_FG}}>Plan details</h3>
          <div style={{display:'grid', gap:10, flex:1}}>
            {[['Plan','Premium'],['Price','€45 / month'],['Includes','Full gym + sauna + AI coach'],['Started','Mar 04, 2024'],['Renews','Manually · approved by admin']].map(([k,v])=>(
              <div key={k} style={{display:'flex', justifyContent:'space-between', alignItems:'center', padding:'6px 0', borderBottom:`1px solid ${M_BONE_200}`, fontSize:13}}>
                <span style={{color:M_BONE_700, fontFamily:'Geist Mono, monospace', fontSize:11, letterSpacing:'0.08em', textTransform:'uppercase'}}>{k}</span>
                <span style={{color:M_FG, fontWeight:500, textAlign:'right'}}>{v}</span>
              </div>
            ))}
          </div>
          <button onClick={()=>setPauseOpen(true)}
                  disabled={!!pauseReq}
                  style={{marginTop:14, padding:'9px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color: pauseReq ? M_BONE_500 : M_FG, cursor: pauseReq ? 'default' : 'pointer', fontFamily:'inherit'}}>
            {pauseReq ? `Pause requested · ${pauseReq.weeks} weeks (${pauseReq.status})` : 'Request pause / freeze'}
          </button>
        </div>
      </div>

      {/* Pending requests */}
      {requests.length > 0 && (
        <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:M_FG}}>Your requests</h3>
          {requests.map(r => (
            <div key={r.id} style={{display:'flex', alignItems:'center', gap:14, padding:'14px 16px', background:r.status==='pending'?'#FFF8EA':'#E8EFE5', border:`1px solid ${r.status==='pending'?'#F4E7CC':'#D2E0CB'}`, borderRadius:10, marginBottom:10}}>
              <div style={{width:36, height:36, borderRadius:8, background:r.status==='pending'?'#B8893A':M_SAGE_500, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                <i data-lucide={r.status==='pending'?'clock':'check'} style={{width:16, height:16, color:'#fff'}}></i>
              </div>
              <div style={{flex:1, minWidth:0}}>
                <div style={{fontSize:14, fontWeight:600, color:M_FG}}>{r.plan} · €{r.amount}{r.period?` / ${r.period}`:''}</div>
                <div style={{fontSize:12, color:M_BONE_700, marginTop:2}}>
                  Submitted {r.submitted} · {r.method}
                  {r.status==='pending' && ' · waiting for admin to confirm payment at the front desk'}
                </div>
              </div>
              <span style={{padding:'4px 10px', borderRadius:6, background:r.status==='pending'?'#B8893A':M_SAGE_500, color:'#fff', fontSize:10.5, fontWeight:600, fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase'}}>{r.status}</span>
              {r.status==='pending' && (
                <button onClick={()=>cancelRequest(r.id)}
                        style={{padding:'7px 11px', borderRadius:6, background:'transparent', border:`1px solid ${M_BONE_100}`, fontSize:11, fontWeight:500, color:M_BONE_700, cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Monthly plans */}
      <PlanGrid title="Request a renewal or change plan"
                subtitle="Submit your request here, then pay in cash or card at reception. Admin activates the plan once payment clears."
                plans={MONTHLY_PLANS.map(p => ({...p, kind:'plan'}))}
                onPick={setOpenModal}/>

      {/* PT packs + nutrition add-ons — clearly separated */}
      <PlanGrid title="Personal training & add-ons"
                subtitle="One-off purchases on top of your monthly plan. Same request → pay → activate flow."
                plans={ADDONS.map(p => ({...p, kind:'addon'}))}
                onPick={setOpenModal}/>

      {/* History */}
      <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:M_FG}}>Membership history</h3>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead><tr style={{borderBottom:`1px solid ${M_BONE_100}`}}>
            {['Date','Description','Amount','Method','Status','Receipt'].map(h=><th key={h} style={{padding:'10px 12px', textAlign:'left', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase'}}>{h}</th>)}
          </tr></thead>
          <tbody>
            {[
              ['01 May 2026','Premium · May renewal','€45','Cash'],
              ['01 Apr 2026','Premium · Apr renewal','€45','Card'],
              ['01 Mar 2026','Premium · Mar renewal','€45','Cash'],
              ['12 Feb 2026','PT pack · 4 sessions','€120','Card'],
              ['01 Feb 2026','Premium · Feb renewal','€45','Cash'],
            ].map((r,i)=>(
              <tr key={i} style={{borderBottom:`1px solid ${M_BONE_200}`}}>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:12, color:M_BONE_700}}>{r[0]}</td>
                <td style={{padding:'12px 12px', fontSize:13, color:M_FG}}>{r[1]}</td>
                <td style={{padding:'12px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, fontWeight:600, color:M_FG}}>{r[2]}</td>
                <td style={{padding:'12px 12px', fontSize:12, color:M_BONE_700}}>{r[3]}</td>
                <td style={{padding:'12px 12px'}}><span style={{padding:'3px 9px', borderRadius:999, background:'rgba(74,122,79,0.16)', color:M_SAGE_500, fontSize:10.5, fontWeight:600, fontFamily:'Geist Mono, monospace'}}>PAID</span></td>
                <td style={{padding:'12px 12px'}}>
                  <button onClick={()=>alert('Receipt PDF download (demo)')} style={{background:'transparent', border:0, fontSize:11, color:M_SAGE_700, cursor:'pointer', fontWeight:500, fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', padding:0}}>PDF →</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {openModal && (
        <RequestModal plan={openModal} method={method} setMethod={setMethod}
                      note={note} setNote={setNote}
                      onCancel={()=>setOpenModal(null)} onSubmit={submitRequest}/>
      )}

      {pauseOpen && (
        <div onClick={()=>setPauseOpen(false)} style={{position:'fixed', inset:0, background:'rgba(14,26,17,0.55)', backdropFilter:'blur(4px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:100, padding:24}}>
          <div onClick={e=>e.stopPropagation()} style={{background:'#fff', borderRadius:16, padding:'24px 28px', maxWidth:460, width:'100%', boxShadow:'0 28px 60px rgba(14,26,17,0.32)'}}>
            <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:28, lineHeight:1, margin:'0 0 6px', color:M_FG}}>Pause membership</h3>
            <p style={{fontSize:13, color:M_BONE_700, margin:'0 0 16px', lineHeight:1.55}}>Going on holiday, recovering from an injury, or just need a break? Pause adds extra days to your renewal date. Admin approves the same day.</p>
            <label style={{display:'block', fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8}}>For how long?</label>
            <div style={{display:'flex', gap:8, marginBottom:14}}>
              {[2,4,6,8].map(w => (
                <button key={w} onClick={()=>setPauseWeeks(w)}
                        style={{flex:1, padding:'10px', borderRadius:8, background:pauseWeeks===w?M_SAGE:'#fff', color:pauseWeeks===w?M_BONE_50:M_FG, border:`1px solid ${pauseWeeks===w?M_SAGE:M_BONE_100}`, fontSize:12, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>
                  {w} weeks
                </button>
              ))}
            </div>
            <label style={{display:'block', fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8}}>Reason (optional)</label>
            <textarea value={pauseReason} onChange={e=>setPauseReason(e.target.value)} placeholder="e.g. Travelling, post-surgery, exams…"
                      style={{width:'100%', minHeight:60, padding:'10px 12px', borderRadius:8, border:`1px solid ${M_BONE_100}`, fontSize:13, fontFamily:'inherit', resize:'vertical', boxSizing:'border-box', outline:'none', color:M_FG, background:M_BONE_25}}/>
            <div style={{display:'flex', gap:8, marginTop:16}}>
              <button onClick={()=>setPauseOpen(false)} style={{padding:'10px 16px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:13, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
              <button onClick={submitPause} style={{flex:1, padding:'10px', borderRadius:8, background:M_SAGE, color:M_BONE_50, border:0, fontSize:13, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>Submit pause →</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function PlanGrid({ title, subtitle, plans, onPick }) {
  return (
    <div style={{background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'22px 24px', marginBottom:16}}>
      <div style={{marginBottom:6}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:M_FG}}>{title}</h3>
        <p style={{fontSize:13, color:M_BONE_700, margin:'4px 0 16px', lineHeight:1.55}}>{subtitle}</p>
      </div>
      <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:12}}>
        {plans.map(p => (
          <div key={p.id} style={{padding:'18px 20px', background:M_BONE_25, border:`1px solid ${p.recommended?M_ACCENT:M_BONE_200}`, borderRadius:12, display:'flex', flexDirection:'column', position:'relative'}}>
            {p.recommended && <span style={{position:'absolute', top:-9, left:14, padding:'2px 8px', background:M_ACCENT, color:'#fff', fontSize:9.5, fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', borderRadius:4, fontWeight:600}}>RECOMMENDED</span>}
            <div style={{display:'flex', alignItems:'baseline', justifyContent:'space-between', marginBottom:4}}>
              <span style={{fontFamily:'Instrument Serif, serif', fontSize:22, color:M_FG, lineHeight:1.1}}>{p.name}</span>
              <span style={{fontFamily:'Geist Mono, monospace', fontSize:18, color:M_FG, fontWeight:500}}>€{p.price}</span>
            </div>
            <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em', textTransform:'uppercase', marginBottom:10}}>/ {p.period}</div>
            <ul style={{margin:0, padding:0, listStyle:'none', flex:1, marginBottom:14}}>
              {p.perks.map((perk,i)=>(
                <li key={i} style={{padding:'4px 0', fontSize:12, color:M_FG, display:'flex', alignItems:'center', gap:8}}>
                  <i data-lucide="check" style={{width:11, height:11, color:M_SAGE_500}}></i>{perk}
                </li>
              ))}
            </ul>
            <button onClick={()=>onPick(p)}
                    style={{padding:'10px', borderRadius:8, background:p.recommended?M_SAGE:'#fff', color:p.recommended?M_BONE_50:M_FG, border:p.recommended?0:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>
              Request this {p.kind === 'addon' ? 'add-on' : 'plan'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

function RequestModal({ plan, method, setMethod, note, setNote, onCancel, onSubmit }) {
  return (
    <div onClick={onCancel} style={{position:'fixed', inset:0, background:'rgba(14,26,17,0.55)', backdropFilter:'blur(4px)', display:'flex', alignItems:'center', justifyContent:'center', zIndex:100, padding:24}}>
      <div onClick={e=>e.stopPropagation()} style={{background:'#fff', borderRadius:16, padding:'28px 32px', maxWidth:520, width:'100%', boxShadow:'0 28px 60px rgba(14,26,17,0.32)'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:16}}>
          <div>
            <div style={{fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:4}}>New request</div>
            <h2 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:30, lineHeight:1.1, margin:0, color:M_FG, letterSpacing:'-0.02em'}}>{plan.name}</h2>
            <div style={{fontSize:13, color:M_BONE_700, marginTop:4}}>€{plan.price} / {plan.period}</div>
          </div>
          <button onClick={onCancel} style={{width:32, height:32, borderRadius:8, background:M_BONE_25, border:`1px solid ${M_BONE_100}`, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}>
            <i data-lucide="x" style={{width:14, height:14, color:M_FG}}></i>
          </button>
        </div>
        <div style={{padding:'12px 14px', background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:10, marginBottom:14, fontSize:12, color:M_BONE_700, lineHeight:1.55}}>
          <strong style={{color:M_FG}}>How it works.</strong> Submit this request, pay at the front desk, and the admin will activate it the same day. No card is charged online.
        </div>
        <label style={{display:'block', fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8}}>Payment method</label>
        <div style={{display:'flex', gap:8, marginBottom:14}}>
          {['Cash at desk','Card at desk','Bank transfer'].map(m=>(
            <button key={m} onClick={()=>setMethod(m)} style={{flex:1, padding:'9px', borderRadius:8, background:method===m?M_SAGE:'#fff', color:method===m?M_BONE_50:M_FG, border:`1px solid ${method===m?M_SAGE:M_BONE_100}`, fontSize:12, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>{m}</button>
          ))}
        </div>
        <label style={{display:'block', fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.10em', textTransform:'uppercase', marginBottom:8}}>Note for admin (optional)</label>
        <textarea value={note} onChange={e=>setNote(e.target.value)} placeholder="e.g. I'll pay tomorrow morning." style={{width:'100%', minHeight:70, padding:'10px 12px', borderRadius:8, border:`1px solid ${M_BONE_100}`, fontSize:13, fontFamily:'inherit', resize:'vertical', boxSizing:'border-box', outline:'none', color:M_FG, background:M_BONE_25}}/>
        <div style={{display:'flex', gap:8, marginTop:18}}>
          <button onClick={onCancel} style={{padding:'11px 16px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:13, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>Cancel</button>
          <button onClick={onSubmit} style={{flex:1, padding:'11px', borderRadius:8, background:M_SAGE, color:M_BONE_50, border:0, fontSize:13, fontWeight:600, cursor:'pointer', fontFamily:'inherit'}}>Submit request →</button>
        </div>
      </div>
    </div>
  );
}

// ============================================================
// MemberCoach — AI chat grounded in the member's actual logged data.
// Persists messages to localStorage. Builds a fresh context block
// from current meals + workout log on every send.
// ============================================================
function buildCoachContext() {
  const settings = readLocal('settings', { firstName:'Elira', goalKcal:1800 });
  const meals = readLocal('meals', []);
  const workouts = readLocal('workoutLog', {});
  const measReqs = readLocal('measurementReq', []);
  const membershipReqs = readLocal('membershipReq', []);
  const todayKey = ymd(today());
  const todaysMeals = meals.filter(m => m.date === todayKey);
  const totals = todaysMeals.reduce((a,m) => ({
    kcal: a.kcal + m.kcal, protein: a.protein + (m.protein||0), carbs: a.carbs + (m.carbs||0), fat: a.fat + (m.fat||0),
  }), { kcal:0, protein:0, carbs:0, fat:0 });

  // Workout days completed in the last 7 days
  const weekKeys = (() => {
    const ks = []; for (let i = 0; i < 7; i++) { const d = today(); d.setDate(d.getDate() - i); ks.push(ymd(d)); }
    return ks;
  })();
  const recentWorkouts = weekKeys.map(k => ({ date: k, items: (workouts[k] || []).filter(x => x.done) })).filter(x => x.items.length);

  const lines = [
    `Member: ${settings.firstName} ${(readLocal('settings',{}).lastName)||'Krasniqi'}.`,
    `Age 31, height 168 cm, current weight 78.4 kg, started at 82 kg in March 2024. Goal: lose 6 more kg, build leg strength.`,
    `Trainer: Rea Hoxha. Membership: Premium, 23 days remaining.`,
    `Today (${todayKey}): logged ${todaysMeals.length} meal(s) — ${totals.kcal} kcal / target ${settings.goalKcal || 1800}, ${totals.protein}P / ${totals.carbs}C / ${totals.fat}F.`,
    todaysMeals.length ? `Meals today: ${todaysMeals.map(m => `${m.label} — ${m.desc} (${m.kcal} kcal)`).join('; ')}.` : 'No meals logged yet today.',
    `Last 7 days of workouts completed: ${recentWorkouts.length ? recentWorkouts.map(w => `${w.date}: ${w.items.map(x=>x.name).join(', ')}`).join(' | ') : 'none recorded'}.`,
    measReqs.length ? `Pending measurement requests: ${measReqs.length}.` : '',
    membershipReqs.length ? `Pending membership requests: ${membershipReqs.map(r=>r.plan).join(', ')}.` : '',
  ].filter(Boolean).join('\n');
  return lines;
}

const COACH_RULES = `You are Forge Coach, a thoughtful AI fitness coach.
Tone: premium, grounded, warm. Short paragraphs. Sentence case. No emoji. Never ALL CAPS.
Name what you see in the member's data when relevant. Offer concrete next steps, not generic advice.
Keep replies under 120 words unless the user asks for detail.`;

function MemberCoach() {
  const [messages, setMessages] = useLocalState('coachChat', [
    { role:'assistant', content:"What's on your mind this morning?", ts: Date.now() }
  ]);
  const [, setLastSeen] = useLocalState('coachLastSeen', 0);
  const [input, setInput] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState(null);
  const scrollRef = React.useRef(null);

  // Mark coach as seen on mount and on every new message we render.
  React.useEffect(() => { setLastSeen(Date.now()); }, [messages.length]);
  React.useEffect(() => {
    if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    setTimeout(() => window.lucide && window.lucide.createIcons(), 30);
  }, [messages, busy]);

  const send = async (text) => {
    const content = (text ?? input).trim();
    if (!content || busy) return;
    setInput(''); setError(null);
    const ts = Date.now();
    const nextMsgs = [...messages, { role:'user', content, ts }];
    setMessages(nextMsgs);
    setBusy(true);
    try {
      const ctx = buildCoachContext();
      const history = nextMsgs.slice(-20).map(m => (m.role==='user'?'Member: ':'Coach: ') + m.content).join('\n');
      const prompt = `${COACH_RULES}\n\n— Member context (current, live) —\n${ctx}\n\n— Conversation so far —\n${history}\n\nReply as Coach to the latest member message. Just the reply, no role label.`;
      const reply = await window.claude.complete(prompt);
      setMessages([...nextMsgs, { role:'assistant', content: reply.trim(), ts: Date.now() }]);
    } catch (err) {
      setError('Coach is taking a breath — try again in a moment.');
    } finally {
      setBusy(false);
    }
  };

  const quick = [
    "Why have I stopped losing weight?",
    "Build me a 20-min home workout for tonight.",
    "What should I eat after legs day?",
    "Should I push the PT session this week?",
  ];

  return (
    <div style={{padding:'28px 32px 64px', display:'flex', justifyContent:'center'}}>
      <div style={{maxWidth:740, width:'100%', background:'#fff', border:`1px solid ${M_BONE_100}`, borderRadius:14, padding:'24px 28px', display:'flex', flexDirection:'column', height:'calc(100vh - 160px)'}}>
        <div style={{display:'flex', alignItems:'center', gap:14, marginBottom:18, paddingBottom:14, borderBottom:`1px solid ${M_BONE_200}`}}>
          <div style={{width:48, height:48, borderRadius:12, background:M_ACCENT, display:'flex', alignItems:'center', justifyContent:'center'}}>
            <i data-lucide="sparkles" style={{width:22, height:22, color:'#fff'}}></i>
          </div>
          <div style={{flex:1}}>
            <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:24, color:M_FG, lineHeight:1}}>Coach</div>
            <div style={{fontSize:12, color:M_SAGE_500, display:'flex', alignItems:'center', gap:6, marginTop:2}}>
              <span style={{width:6, height:6, borderRadius:'50%', background:M_SAGE_500}}></span>
              Online · sees your meals, workouts, measurements
            </div>
          </div>
          <button onClick={()=>{ if (confirm('Clear chat history?')) setMessages([{ role:'assistant', content:"Fresh start. What's on your mind?", ts: Date.now() }]); }}
                  style={{padding:'7px 12px', borderRadius:8, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, fontWeight:500, color:M_FG, cursor:'pointer', fontFamily:'inherit', display:'flex', alignItems:'center', gap:6}}>
            <i data-lucide="refresh-cw" style={{width:12, height:12}}></i>New chat
          </button>
        </div>

        <div ref={scrollRef} style={{flex:1, overflowY:'auto', paddingRight:6, marginBottom:14}}>
          {messages.map((m,i) => (
            m.role === 'assistant' ? (
              <div key={i} style={{display:'flex', gap:10, marginBottom:14, maxWidth:'90%'}}>
                <div style={{width:28, height:28, borderRadius:8, background:M_ACCENT, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}><i data-lucide="sparkles" style={{width:13, height:13, color:'#fff'}}></i></div>
                <div style={{background:M_BONE_25, border:`1px solid ${M_BONE_200}`, padding:'10px 14px', borderRadius:'4px 14px 14px 14px', fontSize:14, color:M_FG, lineHeight:1.55, whiteSpace:'pre-wrap'}}>{m.content}</div>
              </div>
            ) : (
              <div key={i} style={{display:'flex', justifyContent:'flex-end', marginBottom:14}}>
                <div style={{background:M_SAGE, color:M_BONE_50, padding:'10px 14px', borderRadius:'14px 14px 4px 14px', fontSize:14, maxWidth:'80%', lineHeight:1.5, whiteSpace:'pre-wrap'}}>{m.content}</div>
              </div>
            )
          ))}
          {busy && (
            <div style={{display:'flex', gap:10, marginBottom:14}}>
              <div style={{width:28, height:28, borderRadius:8, background:M_ACCENT, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}><i data-lucide="sparkles" style={{width:13, height:13, color:'#fff'}}></i></div>
              <div style={{background:M_BONE_25, border:`1px solid ${M_BONE_200}`, padding:'12px 14px', borderRadius:'4px 14px 14px 14px', display:'flex', gap:5}}>
                {[0,1,2].map(j=><span key={j} style={{width:6, height:6, borderRadius:'50%', background:M_SAGE_500, opacity:0.4, animation:`coachDot 1.2s ${j*0.15}s infinite`}}></span>)}
              </div>
            </div>
          )}
          {error && <div style={{padding:'10px 14px', background:'#F1D8D4', border:'1px solid #E5B8B3', borderRadius:8, fontSize:12, color:'#A8413A', marginBottom:14}}>{error}</div>}
          {messages.length <= 1 && !busy && (
            <div style={{display:'flex', flexWrap:'wrap', gap:8, paddingLeft:38, marginTop:4}}>
              {quick.map((q,i) => (
                <button key={i} onClick={()=>send(q)} style={{padding:'7px 12px', borderRadius:999, background:'#fff', border:`1px solid ${M_BONE_100}`, fontSize:12, color:M_FG, cursor:'pointer', fontFamily:'inherit'}}>{q}</button>
              ))}
            </div>
          )}
        </div>

        <form onSubmit={(e)=>{e.preventDefault(); send();}} style={{display:'flex', gap:8, background:M_BONE_25, border:`1px solid ${M_BONE_200}`, borderRadius:999, padding:'6px 6px 6px 16px', alignItems:'center'}}>
          <input value={input} onChange={e=>setInput(e.target.value)} disabled={busy} placeholder="Ask anything about your training, food, or progress…" style={{flex:1, fontSize:13, color:M_FG, background:'transparent', border:0, outline:'none', fontFamily:'inherit'}}/>
          <button type="submit" disabled={busy || !input.trim()} style={{width:34, height:34, borderRadius:'50%', background: busy||!input.trim()?M_BONE_100:M_ACCENT, border:0, color:'#fff', display:'flex', alignItems:'center', justifyContent:'center', cursor:busy||!input.trim()?'default':'pointer'}}>
            <i data-lucide={busy?'loader':'arrow-up'} style={{width:14, height:14}}></i>
          </button>
        </form>
        <style>{`@keyframes coachDot { 0%,100%{opacity:0.25; transform:translateY(0)} 50%{opacity:1; transform:translateY(-2px)} }`}</style>
      </div>
    </div>
  );
}

window.MemberProgress = MemberProgress;
window.MemberMembership = MemberMembership;
window.MemberCoach = MemberCoach;
