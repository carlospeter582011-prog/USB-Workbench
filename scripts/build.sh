#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
mkdir -p artifacts
python3 tools/build_ui.py
python3 tools/package_native_sources.py
python3 tools/audit_native.py
# Force fresh APK containers: incremental ZIP updates can retain the previous large
# corresponding-source asset as unreachable bytes, making an otherwise small app huge.
python3 - <<'PYCOMPACT'
from pathlib import Path
for variant in ['debug','qa']:
 apk=Path('app/build/outputs/apk')/variant/('app-'+variant+'.apk')
 apk.unlink(missing_ok=True)
PYCOMPACT
# The local Maven endpoints identify budget-proxy downloads already in the Gradle cache.
# --offline guarantees this verification/package step makes no dependency network requests.
bash gradlew --offline --no-daemon --init-script tools/maven.init.gradle :app:assembleDebug :app:assembleQa :app:bundleRelease :app:validateReleaseBundle :app:lintDebug :app:lintRelease > artifacts/verification.log 2>&1
python3 tools/audit_payloads.py
cp app/build/outputs/apk/debug/app-debug.apk artifacts/USB-Workbench-0.16.1-debug.apk
cp app/build/outputs/apk/qa/app-qa.apk artifacts/USB-Workbench-0.16.1-QA.apk
cp app/build/outputs/bundle/release/app-release.aab artifacts/USB-Workbench-0.16.1-unsigned.aab
sha256sum artifacts/USB-Workbench-0.16.1-debug.apk artifacts/USB-Workbench-0.16.1-QA.apk artifacts/USB-Workbench-0.16.1-unsigned.aab > artifacts/SHA256SUMS
cat artifacts/SHA256SUMS
