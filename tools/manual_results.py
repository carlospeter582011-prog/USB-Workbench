"""Read the user's plain-text report without treating native [ERROR] lines as cases."""
import re,datetime
STATUS={'Worked':'pass','Did not work':'fail','Not sure':'unsure','Blocked / need help':'blocked','Not applicable':'na','Not tested':'todo'}
def read_report(text,sections):
 known={c['id'] for g in sections for c in g['cases']};names={g['name']:g['id'] for g in sections};state=dict(schema=1,updated=0,meta={},cases={},tools={});current=None;noting=False;notes=[];tool=None
 def flush():
  nonlocal noting,notes
  if noting:
   content='\n'.join(notes).strip()
   if current is not None:state['cases'][current]['notes']=content
   elif tool is not None:state['tools'][tool]=content
  noting=False;notes=[]
 for line in text.splitlines():
  if line.startswith('=== ') and line.endswith(' ==='):
   flush();current=None;tool=names.get(line[4:-4]);continue
  hit=re.match(r'^\[([A-Za-z0-9_-]+)\] (Worked|Did not work|Not sure|Blocked / need help|Not applicable|Not tested)(?: — RETEST REQUIRED)? \| ',line)
  if hit and hit[1] in known:
   flush();current=hit[1];state['cases'][current]=dict(status=STATUS[hit[2]],notes='',candidate='',testedAt=0);continue
  if line.startswith('TOOL/SECTION NOTES: '):flush();current=None;noting=True;notes=[line[20:]];continue
  if line.startswith('NOTES: '):flush();noting=True;notes=[line[7:]];continue
  if current is not None and line.startswith('Recorded: '):
   date,sep,candidate=line[10:].partition(' | Candidate: ')
   try:state['cases'][current]['testedAt']=int(datetime.datetime.fromisoformat(date.replace('Z','+00:00')).timestamp()*1000)
   except ValueError:pass
   state['cases'][current]['candidate']=candidate;continue
  if line.startswith('Expected: '):continue
  if noting:notes.append(line);continue
  for prefix,key in [('Candidate: ','candidate'),('Tester/date: ','tester'),('Android/device: ','device'),('USB/adapter/PC: ','hardware'),('General notes: ','notes')]:
   if line.startswith(prefix):state['meta'][key]=line[len(prefix):].replace('not recorded','').replace('(none)','')
  if line.startswith('Generated: '):
   try:state['updated']=int(datetime.datetime.fromisoformat(line[11:].replace('Z','+00:00')).timestamp()*1000)
   except ValueError:pass
 flush();return state
