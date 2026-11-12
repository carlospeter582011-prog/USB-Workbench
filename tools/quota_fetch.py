"""Budgeted HTTPS fetcher. All software network payloads are recorded, including failed reads."""
import fcntl, hashlib, json, os, sys, urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
LEDGER=ROOT/'quota.json'
def fetch(url,target,max_bytes=200_000_000,headers=None,require_range=False):
    target=Path(target);target.parent.mkdir(parents=True,exist_ok=True)
    with open(ROOT/'.downloads/quota.lock','a') as lock:
        fcntl.flock(lock,fcntl.LOCK_EX)
        q=json.loads(LEDGER.read_text());remaining=q['user_limit_bytes']-q['software_download_bytes']-q['conservative_research_and_overhead_reserve_bytes']
        # Bound this development session well below the user cap.
        remaining=min(remaining,650_000_000-q['software_download_bytes'],max_bytes)
        if remaining<=0:raise RuntimeError('Download budget reached')
        entry={'url':url,'bytes':0,'complete':False};q.setdefault('downloads',[]).append(entry)
        def save():
            q['remaining_software_allowance_bytes']=max(0,q['user_limit_bytes']-q['software_download_bytes']-q['conservative_research_and_overhead_reserve_bytes'])
            tmp=LEDGER.with_suffix('.tmp');tmp.write_text(json.dumps(q,indent=2)+'\n');os.replace(tmp,LEDGER)
        partial=target.with_suffix(target.suffix+'.part')
        try:
            with urllib.request.urlopen(urllib.request.Request(url,headers=dict({'User-Agent':'USBWorkbench-build/0.3'},**(headers or {}))),timeout=90) as response:
                if require_range and response.status!=206:raise RuntimeError('Server did not honor bounded range request')
                if int(response.headers.get('Content-Length',0))>remaining:raise RuntimeError('Artifact exceeds download allowance')
                with partial.open('wb') as out:
                    while True:
                        data=response.read(min(262144,remaining-entry['bytes']+1))
                        if not data:break
                        entry['bytes']+=len(data);q['software_download_bytes']+=len(data);save()
                        if entry['bytes']>remaining:raise RuntimeError('Download allowance reached')
                        out.write(data)
            entry['complete']=True;entry['sha256']=hashlib.sha256(partial.read_bytes()).hexdigest();os.replace(partial,target)
            return target
        finally:save()
if __name__=='__main__':
    path=fetch(sys.argv[1],sys.argv[2],int(sys.argv[3]) if len(sys.argv)>3 else 200_000_000)
    print(path, path.stat().st_size)
