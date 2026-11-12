#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_check_root="$PWD"
python3 - <<'PY'
from pathlib import Path
import hashlib,tarfile
for name,folder,digest in [('dosfstools.tar.gz','dosfstools-4.2','64926eebf90092dca21b14259a5301b7b98e7b1943e8a201c7d726084809b527'),('exfatprogs.tar.xz','exfatprogs-1.3.0','11135e5f5320935cc0623b0d394417e667edb827d04b953680f1bbffc575045f')]:
 archive=Path('native/sources')/name
 expected=digest
 assert hashlib.sha256(archive.read_bytes()).hexdigest()==expected
 root=Path('.downloads/native-src')/folder
 if not (root/'configure').exists():
  with tarfile.open(archive) as t:t.extractall(root.parent,filter='data')
PY
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64};do
 export USB_NATIVE_ABI="$abi"
 case "$abi" in arm64-v8a)host=aarch64-linux-android;;armeabi-v7a)host=arm-linux-androideabi;;x86)host=i686-linux-android;;x86_64)host=x86_64-linux-android;;esac
 dest="$usb_check_root/app/src/main/jniLibs/$abi"
 for family in fat exfat;do
  if [ "$family" = fat ];then package=dosfstools-4.2;subdir=src;binary=fsck.fat;options=--without-iconv;else package=exfatprogs-1.3.0;subdir=fsck;binary=fsck.exfat;options=--disable-shared;fi
  out="$usb_check_root/.downloads/native-build/$abi/check-$family"
  mkdir -p "$out" "$dest"
  if [ ! -f "$out/Makefile" ];then
   (cd "$out" && CONFIG_SHELL=/bin/bash SHELL=/bin/bash CC="$usb_check_root/tools/native_cc.py" AR=${USB_LLVM_BIN}/llvm-ar RANLIB=${USB_LLVM_BIN}/llvm-ranlib CFLAGS="-Os -fPIE -std=gnu11" LDFLAGS=-pie bash "$usb_check_root/.downloads/native-src/$package/configure" --host="$host" --build=x86_64-pc-linux-gnu $options)
  fi
  if [ "$family" = exfat ];then make -C "$out/lib" -j1 SHELL=/bin/bash;fi
  make -C "$out/$subdir" -j1 SHELL=/bin/bash "$binary"
  cp "$out/$subdir/$binary" "$dest/libfsck_$family.so"
  ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$dest/libfsck_$family.so"
 done
 echo "Built API23 FAT/exFAT checkers: $abi"
done
