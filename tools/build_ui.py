"""Emit ES5 plus concrete-color CSS for Android6's original Chromium44 WebView."""
from pathlib import Path
import os,re,subprocess
ROOT=Path(__file__).resolve().parent.parent
compiler=Path(os.environ.get('USB_TYPESCRIPT_COMPILER',str(ROOT/'.downloads/typescript/package/lib/tsc.js')))
if not compiler.is_file():raise SystemExit('TypeScript build tool missing: reuse local tools or fetch the pinned npm package with tools/quota_fetch.py. Packaged generated assets are already present for offline consumers.')
subprocess.run(['node',str(compiler),'--allowJs','--checkJs','false','--target','ES5','--module','none','--lib','ES2020,DOM','--skipLibCheck','--newLine','lf','--outDir',str(ROOT/'app/src/main/assets'),str(ROOT/'ui/catalog.js'),str(ROOT/'ui/app.js')],check=True,cwd=ROOT)
css=(ROOT/'ui/style.css').read_text()
variables=dict(re.findall(r'(--[\w-]+)\s*:\s*([^;}]+)',css))
css=re.sub(r'var\((--[\w-]+)\)',lambda m:variables[m[1]],css)
def rgba(m):
 s=m[1]
 if len(s)==4:s=''.join(c*2 for c in s)
 return 'rgba(%d,%d,%d,%.4f)'%(int(s[:2],16),int(s[2:4],16),int(s[4:6],16),int(s[6:8],16)/255)
css=re.sub(r'#([0-9a-fA-F]{8}|[0-9a-fA-F]{4})(?![0-9a-fA-F])',rgba,css)
css+='\n'+(ROOT/'ui/legacy.css').read_text()
(ROOT/'app/src/main/assets/style.css').write_text(css)
print('Generated ES5 UI and legacy-compatible CSS from ui/')
