#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_fsimg_root="$PWD"
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64};do
 export USB_NATIVE_ABI="$abi"
 out="$usb_fsimg_root/.downloads/native-build/$abi"
 python3 tools/native_cc.py -fPIE -pie -DHAVE_CONFIG_H -DUSBWB_NTFS -include native/android_compat.h -I"$out/ntfs" -I"$usb_fsimg_root/.downloads/native-src/ntfs-3g-2026.9.28/include" native/fsimage_cli.c "$out/ntfs/libntfs-3g/.libs/libntfs-3g.a" -Wl,--gc-sections -o "app/src/main/jniLibs/$abi/libusbwb_ntfsimage.so"
 python3 tools/native_cc.py -fPIE -pie -include native/android_compat.h -I"$out/ext/lib" -I"$usb_fsimg_root/.downloads/native-src/e2fsprogs-1.47.4/lib" native/fsimage_cli.c "$out/ext/lib/libext2fs.a" "$out/ext/lib/libcom_err.a" -Wl,--gc-sections -o "app/src/main/jniLibs/$abi/libusbwb_extimage.so"
 llvm-strip --strip-unneeded "app/src/main/jniLibs/$abi/libusbwb_ntfsimage.so" "app/src/main/jniLibs/$abi/libusbwb_extimage.so"
 echo "Built API23 readonly NTFS/ext image engines $abi"
done
