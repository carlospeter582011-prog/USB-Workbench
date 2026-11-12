#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_chd_root="$PWD"
python3 - <<'PY'
from pathlib import Path
import hashlib,tarfile
p=Path('native/sources/libchdr.tar.gz');assert hashlib.sha256(p.read_bytes()).hexdigest()=='f38fb1b3e907c8927271a57ad6e3c02347eaa2d5803f910880b4b3c3021b3657'
if not Path('.downloads/native-src/libchdr-93d8c239ff0d4e8d7722985992649fce12d2463b/CMakeLists.txt').exists():
 with tarfile.open(p) as t:t.extractall('.downloads/native-src',filter='data')
PY
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64};do
 export USB_NATIVE_ABI="$abi"
 case "$abi" in arm64-v8a)cpu=aarch64;;armeabi-v7a)cpu=arm;;x86)cpu=i686;;x86_64)cpu=x86_64;;esac
 out="$usb_chd_root/.downloads/native-build/$abi/chd";src="$usb_chd_root/.downloads/native-src/libchdr-93d8c239ff0d4e8d7722985992649fce12d2463b"
 cmake -S "$src" -B "$out" -G Ninja -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR="$cpu" -DCMAKE_C_COMPILER="$usb_chd_root/tools/native_cc.py" -DCMAKE_AR=${USB_LLVM_BIN}/llvm-ar -DCMAKE_RANLIB=${USB_LLVM_BIN}/llvm-ranlib -DCMAKE_BUILD_TYPE=MinSizeRel "-DCMAKE_C_FLAGS=-Os -std=gnu11 -fPIE -ffunction-sections -fdata-sections -include $usb_chd_root/native/chd_compat.h" -DBUILD_SHARED_LIBS=OFF -DBUILD_FUZZER=OFF -DCHDR_VERIFY_BLOCK_CRC=ON
 cmake --build "$out" --target chdr-static -j1
 python3 tools/native_cc.py -fPIE -pie -I"$src/include" native/chd_cli.c "$out/libchdr-static.a" "$out/deps/lzma-25.01/libchdr-lzma.a" "$out/deps/miniz-3.1.1/libminiz.a" "$out/deps/zstd-1.5.7/libzstd.a" -Wl,--gc-sections -o "app/src/main/jniLibs/$abi/libusbwb_chd.so"
 llvm-strip --strip-unneeded "app/src/main/jniLibs/$abi/libusbwb_chd.so"
 echo "Built API23 CHD decoder $abi"
done
