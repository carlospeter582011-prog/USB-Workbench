"""Local Maven read-through cache. Uses existing Gradle artifacts before budgeted HTTPS fetches."""
import http.server, hashlib, json, sys, urllib.error
from pathlib import Path
from quota_fetch import fetch,ROOT
CACHE=ROOT/'.downloads/maven'
GRADLE=Path.home()/'.gradle/caches/modules-2/files-2.1'
class Handler(http.server.BaseHTTPRequestHandler):
    def log_message(self,*args):pass
    def do_HEAD(self):self.get(False)
    def do_GET(self):self.get(True)
    def get(self,body):
        route=self.path.split('?',1)[0].lstrip('/');parts=route.split('/',1)
        if len(parts)!=2 or parts[0] not in ('google','maven') or '..' in route:return self.send_error(400)
        repo,path=parts;target=CACHE/repo/path
        try:
            if not target.is_file():
                seg=path.split('/');name=seg[-1]
                if len(seg)>3:
                    group='.'.join(seg[:-3]);artifact,version=seg[-3:-1]
                    matches=list((GRADLE/group/artifact/version).glob('*/'+name))
                    if matches:target=matches[0]
                if not target.is_file():
                    base='https://dl.google.com/dl/android/maven2/' if repo=='google' else 'https://repo.maven.apache.org/maven2/'
                    target=fetch(base+path,target,100_000_000)
            self.send_response(200);self.send_header('Content-Length',str(target.stat().st_size));self.end_headers()
            if body:
                with target.open('rb') as f:
                    while data:=f.read(262144):self.wfile.write(data)
        except urllib.error.HTTPError as e:self.send_error(e.code)
        except (BrokenPipeError,ConnectionResetError):pass
        except Exception as e:
            print(type(e).__name__,str(e),flush=True);self.send_error(503)
if __name__=='__main__':
    server=http.server.HTTPServer(('127.0.0.1',18764),Handler);print('Budgeted Maven proxy 127.0.0.1:18764',flush=True);server.serve_forever()
