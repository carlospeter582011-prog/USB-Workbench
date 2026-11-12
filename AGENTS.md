# Repository instructions

Keep this repository text-only. Do not commit SDKs, caches, binaries, upstream archives, signing material or personal test reports. Dependency downloads happen on GitHub runners via ci/dependencies.json with checksums. User explicitly authorizes GitHub builds and automatic APK/AAB releases.

Edit ui/ then run tools/build_ui.py; packaged JS must remain ES5. Keep minSdk23/target36, English/Arabic and RTL. Preserve real native engines, licenses/corresponding sources and documented experimental limits. Never replace missing engines with placeholder success. Workflow success is not hardware/boot proof.

Native build changes must invalidate the per-ABI cache. Never print Actions signing secrets. Release only after complete signed APK/AAB validation; mark successful main builds Latest. Use immutable commit targets and monotonically increasing version codes.
