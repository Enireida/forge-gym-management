// Forge mobile — Progress, Check-in, AI Coach, Food tracker

// ============================================================
// 3. PROGRESS — weight chart + body measurements
// ============================================================
function MobileProgress() {
  const points = [80.2, 79.8, 79.5, 79.6, 79.1, 78.7, 78.4, 78.5, 78.4, 78.4];
  const max = Math.max(...points), min = Math.min(...points);
  const w = 280, h = 110;
  const path = points.map((p, i) => {
    const x = (i / (points.length-1)) * w;
    const y = h - ((p - min) / (max - min)) * h;
    return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
  }).join(' ');

  return (
    <div style={{height:'100%', background:F_BONE_50, overflowY:'auto', paddingBottom:100}}>
      <div style={{padding:'72px 24px 16px'}}>
        <div style={{fontSize:11, fontWeight:600, letterSpacing:'0.16em', textTransform:'uppercase', color:F_BONE_500, marginBottom:6}}>Progress</div>
        <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:38, lineHeight:1, letterSpacing:'-0.02em', margin:0, color:F_FG}}>
          You're <em style={{color:F_SAGE_700}}>down 1.8 kg</em><br/>since April.
        </h1>
      </div>

      <div style={{padding:'8px 24px 16px'}}>
        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:24, padding:24}}>
          <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:6}}>
            <div>
              <div style={{fontSize:12, color:F_BONE_700, marginBottom:4}}>Weight · last 30 days</div>
              <div style={{display:'flex', alignItems:'baseline', gap:8}}>
                <span style={{fontFamily:'Instrument Serif, serif', fontSize:42, lineHeight:1, color:F_FG}}>78.4</span>
                <span style={{fontSize:14, color:F_BONE_700}}>kg</span>
              </div>
            </div>
            <div style={{padding:'6px 10px', borderRadius:8, background:'#E8EFE5', color:'#4A7A4F', fontSize:12, fontWeight:600, display:'inline-flex', alignItems:'center', gap:4}}>
              <i data-lucide="trending-down" style={{width:12, height:12}}></i>−1.8 kg
            </div>
          </div>
          <svg viewBox={`0 0 ${w} ${h+20}`} width="100%" height="120" style={{marginTop:12}}>
            <defs>
              <linearGradient id="grad-w" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={F_SAGE_500} stopOpacity="0.25"/>
                <stop offset="100%" stopColor={F_SAGE_500} stopOpacity="0"/>
              </linearGradient>
            </defs>
            <path d={`${path} L ${w} ${h} L 0 ${h} Z`} fill="url(#grad-w)"/>
            <path d={path} stroke={F_SAGE} strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
            <circle cx={w} cy={h - ((points[points.length-1]-min)/(max-min))*h} r="5" fill={F_SAGE}/>
            <circle cx={w} cy={h - ((points[points.length-1]-min)/(max-min))*h} r="9" fill={F_SAGE} fillOpacity="0.18"/>
          </svg>
          <div style={{display:'flex', justifyContent:'space-between', fontSize:10, color:F_BONE_500, marginTop:4, fontFamily:'Geist Mono, monospace'}}>
            <span>APR 11</span><span>APR 26</span><span>MAY 11</span>
          </div>
        </div>
      </div>

      <div style={{padding:'8px 24px 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 12px', color:F_FG}}>Body</h3>
        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
          <BodyTile label="Body fat" val="22.4" unit="%" delta="−1.2" good/>
          <BodyTile label="Muscle mass" val="58.1" unit="kg" delta="+0.6" good/>
          <BodyTile label="Waist" val="82" unit="cm" delta="−2" good/>
          <BodyTile label="Chest" val="98" unit="cm" delta="+1" good/>
        </div>
      </div>

      <div style={{padding:'0 24px 16px'}}>
        <div style={{background:F_SAGE_50, border:`1px solid ${F_SAGE_50}`, borderRadius:18, padding:16, display:'flex', gap:12, alignItems:'flex-start'}}>
          <div style={{width:34, height:34, borderRadius:10, background:F_SAGE, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <i data-lucide="sparkles" style={{width:16, height:16, color:F_BONE_50}}></i>
          </div>
          <div>
            <div style={{fontSize:13, fontWeight:600, color:F_FG, marginBottom:2}}>Plateau check</div>
            <div style={{fontSize:13, color:F_BONE_700, lineHeight:1.5}}>Weight has held for 9 days. Tap to see what your coach suggests.</div>
          </div>
          <i data-lucide="chevron-right" style={{width:18, height:18, color:F_BONE_500, marginTop:8}}></i>
        </div>
      </div>
    </div>
  );
}

function BodyTile({label, val, unit, delta, good}) {
  return (
    <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:16, padding:16}}>
      <div style={{fontSize:12, color:F_BONE_700, marginBottom:8}}>{label}</div>
      <div style={{display:'flex', alignItems:'baseline', gap:4, marginBottom:8}}>
        <span style={{fontFamily:'Instrument Serif, serif', fontSize:28, color:F_FG, lineHeight:1}}>{val}</span>
        <span style={{fontSize:11, color:F_BONE_500}}>{unit}</span>
      </div>
      <div style={{fontSize:11, fontWeight:600, color: good ? '#4A7A4F' : '#A8413A', display:'inline-flex', alignItems:'center', gap:3}}>
        <i data-lucide={good ? 'arrow-down-right' : 'arrow-up-right'} style={{width:11, height:11}}></i>{delta}
      </div>
    </div>
  );
}

// ============================================================
// 4. CHECK-IN — kiosk-style PIN
// ============================================================
function MobileCheckin() {
  return (
    <div style={{height:'100%', background:F_SAGE, color:F_BONE_50, display:'flex', flexDirection:'column', padding:'72px 24px 32px'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:48}}>
        <button style={{width:38, height:38, borderRadius:'50%', background:'rgba(245,241,232,0.10)', border:0, color:F_BONE_50, cursor:'pointer', display:'flex', alignItems:'center', justifyContent:'center'}}><i data-lucide="x" style={{width:18, height:18}}></i></button>
        <div style={{fontSize:11, letterSpacing:'0.16em', textTransform:'uppercase', color:'#D2E0CB'}}>Check in</div>
        <div style={{width:38}}></div>
      </div>

      <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:36, lineHeight:1, letterSpacing:'-0.02em', margin:'0 0 8px'}}>Tap your card<br/><em style={{color:F_COPPER}}>or enter PIN</em>.</h1>
      <p style={{fontSize:14, color:'#D2E0CB', margin:'0 0 32px', maxWidth:280}}>Hold your RFID card to the reader, or type your PIN below.</p>

      <div style={{display:'flex', gap:12, justifyContent:'center', marginBottom:32}}>
        {['4','7','2','•'].map((d,i) => (
          <div key={i} style={{width:60, height:72, borderRadius:14, background: i<3 ? 'rgba(245,241,232,0.10)' : 'rgba(74,122,79,0.20)', border: i===3 ? `1.5px solid ${F_COPPER}`:'1px solid rgba(245,241,232,0.10)', display:'flex', alignItems:'center', justifyContent:'center', fontFamily:'Geist Mono, monospace', fontSize:32, fontWeight:500, color: i<3 ? F_BONE_50 : 'rgba(245,241,232,0.6)'}}>{d}</div>
        ))}
      </div>

      <div style={{flex:1}}></div>

      <div style={{background:'rgba(245,241,232,0.06)', borderRadius:24, padding:20, marginBottom:24}}>
        {[['1','2','3'],['4','5','6'],['7','8','9'],['','0','⌫']].map((row, ri) => (
          <div key={ri} style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:8, marginBottom: ri<3 ? 8 : 0}}>
            {row.map((k, ki) => (
              <button key={ki} disabled={!k} style={{height:54, borderRadius:14, background: k ? 'rgba(245,241,232,0.06)' : 'transparent', border:0, color:F_BONE_50, fontSize:22, fontWeight:500, fontFamily:'Geist, sans-serif', cursor:k?'pointer':'default'}}>{k}</button>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

// ============================================================
// 5. AI COACH — chat
// ============================================================
function MobileAIChat() {
  return (
    <div style={{height:'100%', background:F_BONE_50, display:'flex', flexDirection:'column'}}>
      <div style={{padding:'68px 20px 14px', borderBottom:'1px solid #EEEEEA', background:'rgba(245,241,232,0.92)', backdropFilter:'blur(12px)'}}>
        <div style={{display:'flex', alignItems:'center', gap:12}}>
          <div style={{width:38, height:38, borderRadius:10, background:F_SAGE, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <i data-lucide="sparkles" style={{width:18, height:18, color:F_COPPER}}></i>
          </div>
          <div style={{flex:1}}>
            <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:20, color:F_FG, lineHeight:1}}>Coach</div>
            <div style={{fontSize:11, color:F_SAGE_500, display:'flex', alignItems:'center', gap:5, marginTop:2}}>
              <span style={{width:6, height:6, borderRadius:'50%', background:F_SAGE_500, display:'inline-block'}}></span>Online · grounded in your data
            </div>
          </div>
          <button style={{width:36, height:36, borderRadius:'50%', background:'#fff', border:'1px solid #EEEEEA', cursor:'pointer'}}>
            <i data-lucide="more-horizontal" style={{width:16, height:16, color:F_BONE_700}}></i>
          </button>
        </div>
      </div>

      <div style={{flex:1, overflowY:'auto', padding:'20px'}}>
        <div style={{textAlign:'center', fontSize:11, color:F_BONE_500, margin:'8px 0 20px', fontFamily:'Geist Mono, monospace', letterSpacing:'0.08em'}}>TODAY · 09:14</div>

        <Bubble side="bot" copper>What's on your mind this morning, Elira?</Bubble>
        <Bubble side="user">Why have I stopped losing weight?</Bubble>
        <Bubble side="bot">
          You've held steady at <strong>78.4 kg</strong> since April 19. Looking at your data, two things stand out:
          <div style={{margin:'10px 0', padding:'10px 12px', background:'#F5F5F3', borderRadius:10, fontSize:13, color:F_FG}}>
            <div style={{display:'flex', gap:8, alignItems:'center', marginBottom:4}}><i data-lucide="moon" style={{width:14, height:14, color:F_SAGE_700}}></i><strong>Sleep</strong> · avg 6h12m (was 7h40m)</div>
            <div style={{display:'flex', gap:8, alignItems:'center'}}><i data-lucide="calendar-x" style={{width:14, height:14, color:F_SAGE_700}}></i><strong>Lower body</strong> · 11 days since last session</div>
          </div>
          Want me to swap Tuesday's chest day for legs and set a sleep reminder?
        </Bubble>

        <div style={{display:'flex', gap:8, padding:'4px 0 16px', flexWrap:'wrap'}}>
          <Suggest>Yes, update plan</Suggest>
          <Suggest>Show me the data</Suggest>
        </div>
      </div>

      <div style={{padding:'8px 16px 28px', background:F_BONE_50}}>
        <div style={{display:'flex', alignItems:'center', gap:8, background:'#fff', border:'1px solid #EEEEEA', borderRadius:999, padding:'6px 6px 6px 18px'}}>
          <i data-lucide="paperclip" style={{width:18, height:18, color:F_BONE_500}}></i>
          <span style={{flex:1, fontSize:14, color:F_BONE_500}}>Ask anything…</span>
          <button style={{width:38, height:38, borderRadius:'50%', background:F_SAGE, border:0, color:F_BONE_50, display:'flex', alignItems:'center', justifyContent:'center'}}><i data-lucide="mic" style={{width:16, height:16}}></i></button>
        </div>
      </div>
    </div>
  );
}

function Bubble({ side, children, copper }) {
  if (side === 'user') {
    return (
      <div style={{display:'flex', justifyContent:'flex-end', marginBottom:12}}>
        <div style={{background:F_SAGE, color:F_BONE_50, padding:'10px 14px', borderRadius:'18px 18px 4px 18px', maxWidth:'80%', fontSize:14, lineHeight:1.5}}>{children}</div>
      </div>
    );
  }
  return (
    <div style={{display:'flex', gap:10, marginBottom:12, maxWidth:'90%'}}>
      <div style={{width:30, height:30, borderRadius:8, background: copper ? F_COPPER : F_SAGE, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
        <i data-lucide="sparkles" style={{width:14, height:14, color:F_BONE_50}}></i>
      </div>
      <div style={{background:'#fff', border:'1px solid #EEEEEA', padding:'10px 14px', borderRadius:'4px 18px 18px 18px', fontSize:14, lineHeight:1.55, color:F_FG}}>{children}</div>
    </div>
  );
}

function Suggest({ children }) {
  return <button style={{padding:'8px 14px', background:'#fff', border:`1px solid ${F_SAGE_500}`, color:F_SAGE_700, borderRadius:999, fontSize:13, fontWeight:500, cursor:'pointer', fontFamily:'inherit'}}>{children}</button>;
}

// ============================================================
// 6. FOOD TRACKER — meal logging w/ AI photo
// ============================================================
function MobileFood() {
  return (
    <div style={{height:'100%', background:F_BONE_50, overflowY:'auto', paddingBottom:100}}>
      <div style={{padding:'72px 24px 16px'}}>
        <div style={{fontSize:11, fontWeight:600, letterSpacing:'0.16em', textTransform:'uppercase', color:F_BONE_500, marginBottom:6}}>Today · Sunday</div>
        <h1 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:38, lineHeight:1, letterSpacing:'-0.02em', margin:0, color:F_FG}}>
          <em style={{color:F_SAGE_700}}>1,420</em> kcal logged.
        </h1>
        <div style={{fontSize:14, color:F_BONE_700, marginTop:6}}>580 left until your goal</div>
      </div>

      {/* Macro rings */}
      <div style={{margin:'8px 24px 24px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:24, padding:24}}>
        <div style={{display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:8}}>
          <Ring label="Protein" cur={92} goal={140} unit="g" color={F_SAGE_500}/>
          <Ring label="Carbs" cur={184} goal={250} unit="g" color={F_COPPER}/>
          <Ring label="Fat" cur={48} goal={70} unit="g" color="#B8893A"/>
        </div>
        <div style={{height:1, background:'#F0F0EC', margin:'18px -8px 14px'}}></div>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', fontSize:13}}>
          <span style={{color:F_BONE_700}}>Daily goal</span>
          <span style={{fontFamily:'Geist Mono, monospace', color:F_FG}}>2,000 kcal</span>
        </div>
      </div>

      {/* Snap photo CTA */}
      <div style={{padding:'0 24px 24px'}}>
        <button style={{width:'100%', background:F_SAGE, color:F_BONE_50, border:0, borderRadius:18, padding:'16px 18px', display:'flex', alignItems:'center', gap:12, cursor:'pointer'}}>
          <div style={{width:40, height:40, borderRadius:12, background:F_COPPER, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <i data-lucide="camera" style={{width:18, height:18, color:F_BONE_50}}></i>
          </div>
          <div style={{textAlign:'left', flex:1}}>
            <div style={{fontSize:15, fontWeight:600}}>Snap a meal</div>
            <div style={{fontSize:12, color:'#D2E0CB'}}>AI estimates calories & macros</div>
          </div>
          <i data-lucide="chevron-right" style={{width:18, height:18, color:F_BONE_50}}></i>
        </button>
      </div>

      {/* Meals list */}
      <div style={{padding:'0 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 12px', color:F_FG}}>Today's meals</h3>
        <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:20, overflow:'hidden'}}>
          <Meal time="08:24" name="Greek yogurt + berries" kcal={320} icon="🥣" detected/>
          <div style={{height:1, background:'#F0F0EC'}}></div>
          <Meal time="13:10" name="Grilled chicken & quinoa" kcal={620} icon="🍗" detected/>
          <div style={{height:1, background:'#F0F0EC'}}></div>
          <Meal time="16:40" name="Espresso + dark chocolate" kcal={140} icon="☕"/>
          <div style={{height:1, background:'#F0F0EC'}}></div>
          <Meal time="19:50" name="Salmon, sweet potato, salad" kcal={540} icon="🐟" detected/>
        </div>
      </div>
    </div>
  );
}

function Ring({ label, cur, goal, unit, color }) {
  const pct = Math.min(cur/goal, 1);
  return (
    <div style={{textAlign:'center'}}>
      <div style={{position:'relative', width:74, height:74, margin:'0 auto'}}>
        <svg viewBox="0 0 80 80" width="74" height="74">
          <circle cx="40" cy="40" r="32" stroke="#F0F0EC" strokeWidth="6" fill="none"/>
          <circle cx="40" cy="40" r="32" stroke={color} strokeWidth="6" fill="none" strokeDasharray={`${201*pct} 201`} strokeLinecap="round" transform="rotate(-90 40 40)"/>
        </svg>
        <div style={{position:'absolute', inset:0, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center'}}>
          <span style={{fontFamily:'Geist Mono, monospace', fontSize:14, fontWeight:600, color:F_FG, lineHeight:1}}>{cur}</span>
          <span style={{fontSize:9, color:F_BONE_500}}>/{goal}{unit}</span>
        </div>
      </div>
      <div style={{fontSize:11, fontWeight:600, color:F_BONE_700, marginTop:8, letterSpacing:'0.04em'}}>{label}</div>
    </div>
  );
}

function Meal({ time, name, kcal, icon, detected }) {
  return (
    <div style={{padding:'14px 18px', display:'flex', alignItems:'center', gap:12}}>
      <div style={{width:38, height:38, borderRadius:10, background:F_SAGE_50, display:'flex', alignItems:'center', justifyContent:'center', fontSize:18, flexShrink:0}}>{icon}</div>
      <div style={{flex:1, minWidth:0}}>
        <div style={{fontSize:14, fontWeight:600, color:F_FG, marginBottom:2, whiteSpace:'nowrap', overflow:'hidden', textOverflow:'ellipsis'}}>{name}</div>
        <div style={{fontSize:11, color:F_BONE_500, fontFamily:'Geist Mono, monospace', display:'flex', gap:8, alignItems:'center'}}>
          <span>{time}</span>
          {detected && <span style={{padding:'1px 6px', background:F_SAGE_50, color:F_SAGE_700, borderRadius:4, fontSize:9, letterSpacing:'0.06em'}}>AI</span>}
        </div>
      </div>
      <div style={{fontFamily:'Geist Mono, monospace', fontSize:14, fontWeight:500, color:F_FG}}>{kcal}<span style={{fontSize:10, color:F_BONE_500, marginLeft:2}}>kcal</span></div>
    </div>
  );
}

window.MobileProgress = MobileProgress;
window.MobileCheckin = MobileCheckin;
window.MobileAIChat = MobileAIChat;
window.MobileFood = MobileFood;
