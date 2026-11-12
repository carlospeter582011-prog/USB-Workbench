#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_wim_root="$PWD"
python3 - <<'PY'
from pathlib import Path
import hashlib,tarfile
archive=Path('native/sources/wimlib.tar.gz')
assert hashlib.sha256(archive.read_bytes()).hexdigest()=='84221a3abd5b91228f15f8e6065c335a336237b5738197b75bf419eea561a194','WIM source digest mismatch'
destination=Path('.downloads/checkers')
if not (destination/'wimlib-1.14.5/configure').is_file():
 destination.mkdir(parents=True,exist_ok=True)
 with tarfile.open(archive) as source:source.extractall(destination,filter='data')
PY
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64}; do
 export USB_NATIVE_ABI="$abi"
 case "$abi" in arm64-v8a) host=aarch64-linux-android;; armeabi-v7a) host=arm-linux-androideabi;; x86) host=i686-linux-android;; x86_64) host=x86_64-linux-android;; esac
 out="$usb_wim_root/.downloads/native-build/$abi/wim"
 dest="$usb_wim_root/app/src/main/jniLibs/$abi"
 mkdir -p "$out" "$dest"
 if [ ! -f "$out/Makefile" ]; then
  (cd "$out" && PKG_CONFIG_LIBDIR=/nonexistent PKG_CONFIG_PATH= CONFIG_SHELL=/bin/bash SHELL=/bin/bash CC="$usb_wim_root/tools/native_cc.py" AR=${USB_LLVM_BIN}/llvm-ar RANLIB=${USB_LLVM_BIN}/llvm-ranlib CFLAGS='-Os -fPIE' LDFLAGS='-pie' bash "$usb_wim_root/.downloads/checkers/wimlib-1.14.5/configure" --host="$host" --build=x86_64-pc-linux-gnu --disable-shared --enable-static --without-fuse --without-ntfs-3g --disable-dependency-tracking)
 fi
 make -C "$out" -j1 SHELL=/bin/bash CFLAGS="-Os -fPIE -include $usb_wim_root/native/wim_compat.h" wimlib-imagex
 ${USB_LLVM_BIN}/llvm-objcopy --redefine-sym main=usbwb_imagex_main "$out/programs/wimlib_imagex-imagex.o" "$out/usbwb-imagex.o"
 python3 tools/native_cc.py -fPIE -pie -Wl,--export-dynamic -I"$usb_wim_root/.downloads/checkers/wimlib-1.14.5/include" native/wim_tree.c "$out/usbwb-imagex.o" "$out/.libs/libwim.a" -lm -ldl -o "$dest/libwimlib_imagex.so"
 ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$dest/libwimlib_imagex.so"
done
