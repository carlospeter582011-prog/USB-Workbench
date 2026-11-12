#!/usr/bin/env bash
set -euo pipefail
python3 ci/bootstrap.py
python3 tools/build_ui.py
node tests/ui.test.cjs
node tests/learning.test.cjs
node tests/es5.test.cjs
python3 tools/package_archive_source.py
python3 tools/package_native_sources.py
mkdir -p .downloads/ci-bin
printf '#!/bin/sh\nexec readelf "$@"\n' > .downloads/ci-bin/llvm-readelf
chmod +x .downloads/ci-bin/llvm-readelf
PATH="$PWD/.downloads/ci-bin:$PATH" python3 tools/audit_native.py
sdk_manager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$sdk_manager" ]; then
  sdk_manager="$(find "$ANDROID_HOME/cmdline-tools" -name sdkmanager -type f | sort -V | tail -n 1)"
fi
test -x "$sdk_manager"
"$sdk_manager" "platforms;android-37.2" "build-tools;37.0.0" >/dev/null
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
# Download the pinned Gradle wrapper bootstrap on GitHub, not in the source upload.
curl --fail --location --retry 3 --silent --show-error https://raw.githubusercontent.com/gradle/gradle/v9.6.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
printf '%s  %s\n' '497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7' gradle/wrapper/gradle-wrapper.jar | sha256sum --check --status
bash gradlew --no-daemon :app:assembleRelease :app:bundleRelease :app:validateReleaseBundle :app:lintRelease
mkdir -p artifacts/release
cp app/build/outputs/apk/release/app-release.apk artifacts/release/USB-Workbench.apk
cp app/build/outputs/bundle/release/app-release.aab artifacts/release/USB-Workbench.aab
"$ANDROID_HOME/build-tools/37.0.0/apksigner" verify --verbose --min-sdk-version 23 artifacts/release/USB-Workbench.apk
jarsigner -verify artifacts/release/USB-Workbench.aab
(cd artifacts/release && sha256sum USB-Workbench.apk USB-Workbench.aab > SHA256SUMS)
python3 - <<'PY'
import os,json,zipfile
from pathlib import Path
p=Path('artifacts/release');metadata={'commit':os.environ['GITHUB_SHA'],'versionCode':int(os.environ['CI_VERSION_CODE']),'versionName':os.environ['CI_VERSION_NAME'],'compileSdk':'37.2','minSdk':23,'targetSdk':36,'signing':'Persistent GitHub repository upload key; differs from old locally signed QA builds','releaseQuality':'Automated build, not hardware/boot certification'}
with zipfile.ZipFile(p/'USB-Workbench.apk') as z:
 for abi in ['arm64-v8a','armeabi-v7a','x86','x86_64']:
  assert sum(n.startswith('lib/'+abi+'/') and n.endswith('.so') for n in z.namelist())==30,abi
 assert 'assets/native-corresponding-source.zip' in z.namelist()
for f in [p/'USB-Workbench.apk',p/'USB-Workbench.aab']:assert f.stat().st_size<=70000000,(f,f.stat().st_size)
(p/'BUILD_INFO.json').write_text(json.dumps(metadata,indent=2)+'\n')
PY
