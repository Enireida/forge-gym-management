// Forge Admin — additional screens: Members, Payments, Plans, Equipment, Staff, Announcements, Reports

function MembersScreen() {
  const members = [
    { id:1, n:'Elira Krasniqi', em:'elira@gmail.com', ph:'+355 69 212 4001', pin:'4729', rfid:'1042738291', plan:'Premium', status:'Active', exp:'02 Jun 2026' },
    { id:2, n:'Driton Mehmeti', em:'driton.m@gmail.com', ph:'+355 68 145 7322', pin:'8104', rfid:'1042738455', plan:'Premium', status:'Active', exp:'18 Jun 2026' },
    { id:3, n:'Besnik Hoxha', em:'besnik@hotmail.com', ph:'+355 69 882 4519', pin:'5630', rfid:'1042738711', plan:'Standard', status:'Active', exp:'24 May 2026' },
    { id:4, n:'Mirjeta Lleshi', em:'mirjeta.lleshi@gmail.com', ph:'+355 69 552 1980', pin:'2117', rfid:'—', plan:'Standard', status:'Expiring', exp:'20 May 2026' },
    { id:5, n:'Arben Selmani', em:'a.selmani@yahoo.com', ph:'+355 68 720 4411', pin:'9402', rfid:'1042738912', plan:'Premium', status:'Active', exp:'14 Jul 2026' },
    { id:6, n:'Anila Shehu', em:'anila.s@gmail.com', ph:'+355 69 304 8821', pin:'6815', rfid:'1042739044', plan:'Family', status:'Active', exp:'08 Sep 2026' },
    { id:7, n:'Ardit Vata', em:'ardit.vata@gmail.com', ph:'+355 68 991 0273', pin:'3057', rfid:'—', plan:'Standard', status:'Expired', exp:'01 May 2026' },
    { id:8, n:'Rezarta Çela', em:'rezarta@gmail.com', ph:'+355 69 412 7745', pin:'7392', rfid:'1042739182', plan:'Premium', status:'Active', exp:'30 Jun 2026' },
  ];
  const statusColor = (s) => s === 'Active' ? {bg:'rgba(90,125,79,0.18)', fg:'#4A7A4F'} : s === 'Expiring' ? {bg:'rgba(74,122,79,0.16)', fg:'#1A2E1A'} : {bg:'rgba(168,65,58,0.14)', fg:'#7A2E29'};
  return (
    <div style={{padding:'28px 32px 64px'}}>

      {/* Add member form */}
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px', marginBottom:20}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>Add new member</h3>
          <span style={{fontSize:11, color:'#8FB286', letterSpacing:'0.10em', fontFamily:'Geist Mono, monospace'}}>PIN AUTO-GENERATED</span>
        </div>
        <div style={{display:'grid', gridTemplateColumns:'repeat(4, 1fr)', gap:12, marginBottom:14}}>
          <input placeholder="Full name" style={inputStyle}/>
          <input placeholder="Email" type="email" style={inputStyle}/>
          <input placeholder="Phone · +355 ..." style={inputStyle}/>
          <select style={inputStyle}><option>Premium · €45/mo</option><option>Standard · €30/mo</option><option>Family · €70/mo</option></select>
        </div>
        <div style={{display:'flex', gap:12, alignItems:'center'}}>
          <input placeholder="Scan RFID card or type 10 digits" style={{...inputStyle, flex:1}}/>
          <button style={{padding:'10px 20px', background:'#0E1A11', color:'#FAFAFA', border:0, borderRadius:8, fontSize:13, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="plus" style={{width:14, height:14}}></i>Add member
          </button>
        </div>
      </div>

      {/* Members table */}
      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>All members · 247</h3>
          <div style={{display:'flex', gap:10, alignItems:'center'}}>
            <div style={{position:'relative'}}>
              <i data-lucide="search" style={{width:14, height:14, position:'absolute', left:12, top:10, color:'#8A8A85'}}></i>
              <input placeholder="Search..." style={{height:34, padding:'0 14px 0 34px', border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, width:200, fontFamily:'inherit'}}/>
            </div>
            <button style={{padding:'8px 14px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
              <i data-lucide="filter" style={{width:13, height:13}}></i>Filter
            </button>
          </div>
        </div>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead>
            <tr style={{borderBottom:'1px solid #EEEEEA'}}>
              {['Member','Phone','PIN','RFID','Plan','Status','Expiry',''].map((h,i)=>(
                <th key={i} style={{padding:'10px 12px', textAlign:'left', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase'}}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {members.map(m => {
              const sc = statusColor(m.status);
              return (
                <tr key={m.id} style={{borderBottom:'1px solid #F0F0EC'}}>
                  <td style={{padding:'14px 12px'}}>
                    <div style={{display:'flex', alignItems:'center', gap:10}}>
                      <div style={{width:28, height:28, borderRadius:'50%', background:'#E8EFE5', color:'#1A2E1A', display:'flex', alignItems:'center', justifyContent:'center', fontSize:11, fontWeight:600}}>{m.n.split(' ').map(s=>s[0]).join('')}</div>
                      <div>
                        <div style={{fontSize:13, fontWeight:600, color:'#0E1A11'}}>{m.n}</div>
                        <div style={{fontSize:11, color:'#6A6A65'}}>{m.em}</div>
                      </div>
                    </div>
                  </td>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:12, color:'#4A4A45'}}>{m.ph}</td>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:13, fontWeight:600, color:'#0E1A11', letterSpacing:'0.06em'}}>{m.pin}</td>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:11, color:'#6A6A65'}}>{m.rfid}</td>
                  <td style={{padding:'14px 12px', fontSize:12, color:'#1A2E1A'}}>{m.plan}</td>
                  <td style={{padding:'14px 12px'}}>
                    <span style={{padding:'3px 9px', borderRadius:999, background:sc.bg, color:sc.fg, fontSize:10.5, fontWeight:600, letterSpacing:'0.04em'}}>{m.status}</span>
                  </td>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:11, color:'#4A4A45'}}>{m.exp}</td>
                  <td style={{padding:'14px 12px', textAlign:'right'}}>
                    <button style={{padding:'5px 10px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:6, fontSize:11, cursor:'pointer', color:'#1A2E1A'}}>···</button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
const inputStyle = { height:38, padding:'0 12px', border:'1px solid #EEEEEA', borderRadius:8, fontSize:13, fontFamily:'inherit', color:'#0E1A11', background:'#F5F5F3', outline:'none' };

function PaymentsScreen() {
  const payments = [
    { d:'14 May 2026', m:'Elira Krasniqi', amt:45, method:'Card', status:'Paid', desc:'Premium · May renewal' },
    { d:'14 May 2026', m:'Driton Mehmeti', amt:90, method:'Cash', status:'Paid', desc:'Premium · 2-month' },
    { d:'13 May 2026', m:'Mirjeta Lleshi', amt:30, method:'Bank transfer', status:'Pending', desc:'Standard · May renewal' },
    { d:'12 May 2026', m:'Anila Shehu', amt:70, method:'Card', status:'Paid', desc:'Family · May' },
    { d:'12 May 2026', m:'Besnik Hoxha', amt:30, method:'Cash', status:'Paid', desc:'Standard · May' },
    { d:'11 May 2026', m:'Arben Selmani', amt:45, method:'Card', status:'Paid', desc:'Premium · May' },
    { d:'10 May 2026', m:'Rezarta Çela', amt:135, method:'Card', status:'Paid', desc:'Premium · 3-month' },
    { d:'09 May 2026', m:'Ardit Vata', amt:30, method:'Cash', status:'Refunded', desc:'Refund · cancel' },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:16, marginBottom:20}}>
        {[
          { l:'Revenue · this month', v:'€20,640', d:'+7.3%', ico:'banknote' },
          { l:'Paid · this week', v:'€4,820', d:'+12%', ico:'check-circle' },
          { l:'Pending', v:'€420', d:'3 invoices', ico:'clock' },
          { l:'Avg. ticket', v:'€51.20', d:'+€2.10', ico:'tag' },
        ].map((s, i) => (
          <div key={i} style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'18px 20px'}}>
            <div style={{display:'flex', alignItems:'center', gap:10, marginBottom:10}}>
              <div style={{width:28, height:28, borderRadius:8, background:'#E8EFE5', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <i data-lucide={s.ico} style={{width:14, height:14, color:'#1A2E1A'}}></i>
              </div>
              <span style={{fontSize:12, color:'#4A4A45'}}>{s.l}</span>
            </div>
            <div style={{fontFamily:'Instrument Serif, serif', fontSize:36, lineHeight:1, color:'#0E1A11', letterSpacing:'-0.02em', marginBottom:6}}>{s.v}</div>
            <div style={{fontSize:11, color:'#4A7A4F', fontWeight:600, fontFamily:'Geist Mono, monospace'}}>{s.d}</div>
          </div>
        ))}
      </div>

      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16}}>
          <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:0, color:'#0E1A11'}}>All payments</h3>
          <button style={{padding:'8px 14px', background:'#fff', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, fontWeight:500, cursor:'pointer', display:'flex', alignItems:'center', gap:6, fontFamily:'inherit'}}>
            <i data-lucide="download" style={{width:13, height:13}}></i>Export
          </button>
        </div>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead>
            <tr style={{borderBottom:'1px solid #EEEEEA'}}>
              {['Date','Member','Amount','Method','Status','Description'].map((h,i)=>(
                <th key={i} style={{padding:'10px 12px', textAlign:'left', fontSize:10, fontWeight:600, letterSpacing:'0.10em', color:'#8FB286', textTransform:'uppercase'}}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {payments.map((p,i)=>{
              const sc = p.status==='Paid' ? {bg:'rgba(90,125,79,0.18)', fg:'#4A7A4F'} : p.status==='Pending' ? {bg:'rgba(74,122,79,0.16)', fg:'#1A2E1A'} : {bg:'rgba(168,65,58,0.14)', fg:'#7A2E29'};
              return (
                <tr key={i} style={{borderBottom:'1px solid #F0F0EC'}}>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:12, color:'#4A4A45'}}>{p.d}</td>
                  <td style={{padding:'14px 12px', fontSize:13, fontWeight:600, color:'#0E1A11'}}>{p.m}</td>
                  <td style={{padding:'14px 12px', fontFamily:'Geist Mono, monospace', fontSize:14, fontWeight:600, color:'#0E1A11'}}>€{p.amt}</td>
                  <td style={{padding:'14px 12px', fontSize:12, color:'#4A4A45'}}>{p.method}</td>
                  <td style={{padding:'14px 12px'}}><span style={{padding:'3px 9px', borderRadius:999, background:sc.bg, color:sc.fg, fontSize:10.5, fontWeight:600}}>{p.status}</span></td>
                  <td style={{padding:'14px 12px', fontSize:12, color:'#4A4A45'}}>{p.desc}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function PlansScreen() {
  const plans = [
    { name:'Standard', price:30, dur:1, desc:'Full gym access · group classes · open hours', members:142, color:'#1A2E1A' },
    { name:'Premium', price:45, dur:1, desc:'All Standard + sauna + PT 2 sessions/mo', members:78, color:'#4A7A4F', featured:true },
    { name:'Family', price:70, dur:1, desc:'Up to 4 family members on one plan', members:23, color:'#4A7A4F' },
    { name:'Student', price:20, dur:1, desc:'Discounted plan for students under 26', members:4, color:'#D2E0CB', tone:'soft' },
  ];
  return (
    <div style={{padding:'28px 32px 64px'}}>
      <div style={{display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:16, marginBottom:24}}>
        {plans.map((p,i)=>(
          <div key={i} style={{background:'#fff', border:`1px solid ${p.featured?p.color:'#EEEEEA'}`, borderRadius:14, padding:'24px 22px', position:'relative'}}>
            {p.featured && <span style={{position:'absolute', top:-10, right:18, padding:'3px 10px', background:p.color, color:'#fff', fontSize:10, fontWeight:600, borderRadius:999, letterSpacing:'0.10em'}}>POPULAR</span>}
            <div style={{height:4, width:32, background:p.color, borderRadius:2, marginBottom:14}}></div>
            <div style={{fontFamily:'Instrument Serif, serif', fontStyle:'italic', fontSize:24, color:'#0E1A11', marginBottom:8}}>{p.name}</div>
            <p style={{fontSize:12, color:'#4A4A45', lineHeight:1.5, margin:'0 0 16px', minHeight:60}}>{p.desc}</p>
            <div style={{display:'flex', alignItems:'baseline', gap:6, marginBottom:8}}>
              <span style={{fontFamily:'Instrument Serif, serif', fontSize:42, color:'#0E1A11', lineHeight:1}}>€{p.price}</span>
              <span style={{fontSize:11, color:'#6A6A65'}}>/ {p.dur} month</span>
            </div>
            <div style={{padding:'10px 0', borderTop:'1px solid #F0F0EC', marginTop:14, fontSize:11, color:'#8FB286', fontFamily:'Geist Mono, monospace', letterSpacing:'0.06em', display:'flex', justifyContent:'space-between'}}>
              <span>ACTIVE MEMBERS</span>
              <span style={{color:'#0E1A11', fontWeight:600}}>{p.members}</span>
            </div>
            <button style={{width:'100%', padding:'8px 0', marginTop:10, background:'#F5F5F3', border:'1px solid #EEEEEA', borderRadius:8, fontSize:12, fontWeight:500, color:'#1A2E1A', cursor:'pointer', fontFamily:'inherit'}}>Edit plan</button>
          </div>
        ))}
      </div>

      <div style={{background:'#fff', border:'1px solid #EEEEEA', borderRadius:14, padding:'22px 24px'}}>
        <h3 style={{fontFamily:'Instrument Serif, serif', fontWeight:400, fontSize:22, margin:'0 0 14px', color:'#0E1A11'}}>Plan distribution</h3>
        <div style={{display:'flex', gap:2, height:36, borderRadius:8, overflow:'hidden'}}>
          <div style={{flex:142, background:'#1A2E1A', display:'flex', alignItems:'center', justifyContent:'center', color:'#E8EFE5', fontFamily:'Geist Mono, monospace', fontSize:12, fontWeight:600}}>Standard · 142</div>
          <div style={{flex:78, background:'#4A7A4F', display:'flex', alignItems:'center', justifyContent:'center', color:'#fff', fontFamily:'Geist Mono, monospace', fontSize:12, fontWeight:600}}>Premium · 78</div>
          <div style={{flex:23, background:'#4A7A4F', display:'flex', alignItems:'center', justifyContent:'center', color:'#E8EFE5', fontFamily:'Geist Mono, monospace', fontSize:11, fontWeight:600}}>Family · 23</div>
          <div style={{flex:4, background:'#D2E0CB', display:'flex', alignItems:'center', justifyContent:'center', color:'#1A2E1A', fontFamily:'Geist Mono, monospace', fontSize:9, fontWeight:600}}>Student</div>
        </div>
      </div>
    </div>
  );
}

window.MembersScreen = MembersScreen;
window.PaymentsScreen = PaymentsScreen;
window.PlansScreen = PlansScreen;
