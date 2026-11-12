from pathlib import Path
import zipfile
root=Path(__file__).resolve().parents[1]
folders=['app/src','app/libs','third_party','ui','native','scripts','tools','tests','publishing','docs','LICENSES','gradle/wrapper']
files=['app/build.gradle','app/proguard-rules.pro','AGENTS.md','continue.md','DOWNLOAD_ESTIMATE.md','NEW_FEATURES.md','README.md','THIRD_PARTY.md','build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','quota.json']
paths=set()
for directory in folders:
 for path in (root/directory).rglob('*'):
  if path.is_file() and '__pycache__' not in path.parts and path.suffix!='.pyc':paths.add(path)
for file in files:
 path=root/file
 if path.is_file():paths.add(path)
with zipfile.ZipFile(root/'artifacts/USB-Workbench-source.zip','w',compression=zipfile.ZIP_DEFLATED) as out:
 for path in sorted(paths):out.write(path,path.relative_to(root))
print('Source archive:',(root/'artifacts/USB-Workbench-source.zip').stat().st_size,'bytes; no SDK, caches, signing keys or local.properties')
