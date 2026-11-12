# USB Workbench

Native Android USB workshop: Java app, offline JavaScript/HTML/CSS interface and C/C++ filesystem/archive engines. English/Arabic, RTL, Android6/API23 through target36. Users supply their own OS/images; none are bundled.

## Automatic builds

Every push to main (including text source/config updates) and manual workflow dispatch builds all four native ABIs, validates them, regenerates the ES5 UI, and produces a signed APK and AAB. Successful builds publish a new release and mark it **Latest**. Failed builds never replace the latest working release. View **Actions → Build APK and AAB · Latest release**.

[Latest release](https://github.com/carlospeter582011-prog/USB-Workbench/releases/latest)

Only UTF-8 source/config/license text is committed. SDKs, caches, third-party archives, native binaries, APK/AAB files, signing keys, personal testing reports and tablet-specific configuration are excluded. GitHub downloads checksum-verified dependencies itself and caches native engines; those large downloads do not use the tablet’s internet quota. Source archives and patches accompany native binaries inside the app for license compliance.

## Signing and versioning

The repository uses a persistent upload key from encrypted Actions secrets ANDROID_KEYSTORE_BASE64 and ANDROID_KEYSTORE_PASSWORD. The private key is never committed or released. Keep the separate local backup. Cloud version codes increase with workflow run numbers (100000 + run). These builds have a different certificate from earlier debug-signed QA APKs, so they cannot update those APKs in place. Future cloud builds share the same signing key.

Automated releases do not certify physical USB/controller, operating-system mount or PC/console boot compatibility. Experimental engines and unavailable features keep their documented limits. See THIRD_PARTY.md and LICENSES for upstream notices.
