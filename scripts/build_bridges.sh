#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64}; do
    export USB_NATIVE_ABI="$abi"
    dest="app/src/main/jniLibs/$abi/libusbwb_bridge.so"
    python3 tools/native_cc.py -U_FILE_OFFSET_BITS -shared -fPIC native/usb_bridge.c -o "$dest" -ldl
    ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$dest"
    file_dest="app/src/main/jniLibs/$abi/libfile_bridge.so"
    python3 tools/native_cc.py -U_FILE_OFFSET_BITS -shared -fPIC native/file_bridge.c -o "$file_dest" -ldl
    ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$file_dest"
done
