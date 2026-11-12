#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_ar_root="$PWD"
python3 - <<'PY'
from pathlib import Path
import hashlib,tarfile
for name,folder,digest in [('libarchive.tar.xz','libarchive-3.8.11','a3dc0872b1b39da34dc18ef1b944f0f11ffd37425d70ae04be679279f1cf43d2'),('xz-utils.tar.xz','xz-5.8.4','4ce24038fd4221e0d13bc1a2de7a4db56e90b92b3bf75321f6c14be73f65de4b')]:
 p=Path('native/sources')/name
 if name=='libarchive.tar.xz' and not p.exists():p=Path('native/sources/libarchive-runtime.tar.xz');digest='50aec2a25a90eb1d26d0d077d481db4e34e7537fcc3026527bf9459e87e87235'
 assert hashlib.sha256(p.read_bytes()).hexdigest()==digest
 if not (Path('.downloads/native-src')/folder/'CMakeLists.txt').exists():
  with tarfile.open(p) as t:t.extractall('.downloads/native-src',filter='data')
PY
# API23 POSIX/header presence is shared; keep size/CPU/compiler probes ABI-specific.
python3 - <<'PYCACHE'
from pathlib import Path
import re
p=Path('.downloads/native-build/arm64-v8a/archive/libarchive/CMakeCache.txt');out=[]
if p.exists():
 for line in p.read_text().splitlines():
  m=re.fullmatch(r'(HAVE_[A-Z0-9_]+):INTERNAL=(1|TRUE|FALSE|0|)',line)
  if m and not re.search(r'SIZEOF|_CODE|TRYRUN|ABI|BIGENDIAN|BUILTIN|ALIGN|ARM|SSE|CPUID|X86',m[1]):out.append('set('+m[1]+' "'+m[2]+'" CACHE INTERNAL "Shared API23 POSIX presence")')
Path('.downloads/native-build/archive-api23-probes.cmake').write_text('\n'.join(out)+'\n')
PYCACHE
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64};do
 export USB_NATIVE_ABI="$abi"
 case "$abi" in arm64-v8a)cpu=aarch64;triple=aarch64-linux-android;;armeabi-v7a)cpu=arm;triple=arm-linux-androideabi;;x86)cpu=i686;triple=i686-linux-android;;x86_64)cpu=x86_64;triple=x86_64-linux-android;;esac
 out="$usb_ar_root/.downloads/native-build/$abi/archive"
 sys="$usb_ar_root/.downloads/ndk-sysroot/sysroot"
 mkdir -p "$out" "$usb_ar_root/app/src/main/jniLibs/$abi"
 common=(-G Ninja -DCMAKE_SYSTEM_NAME=Linux -DCMAKE_SYSTEM_PROCESSOR="$cpu" -DCMAKE_C_COMPILER="$usb_ar_root/tools/native_cc.py" -DCMAKE_AR=${USB_LLVM_BIN}/llvm-ar -DCMAKE_RANLIB=${USB_LLVM_BIN}/llvm-ranlib -DCMAKE_BUILD_TYPE=MinSizeRel '-DCMAKE_C_FLAGS=-Os -std=gnu11 -fPIE -ffunction-sections -fdata-sections' -DBUILD_SHARED_LIBS=OFF)
 cmake -S "$usb_ar_root/.downloads/native-src/xz-5.8.4" -B "$out/xz" "${common[@]}" -DBUILD_TESTING=OFF -DXZ_TOOL_XZ=OFF -DXZ_TOOL_XZDEC=OFF -DXZ_TOOL_LZMADEC=OFF -DXZ_TOOL_LZMAINFO=OFF -DXZ_DOC=OFF -DXZ_NLS=OFF -DXZ_THREADS=no
 cmake --build "$out/xz" --target liblzma -j1
 probes=();if [ "$abi" != arm64-v8a ];then probes=(-C "$usb_ar_root/.downloads/native-build/archive-api23-probes.cmake");fi
 cmake "${probes[@]}" -S "$usb_ar_root/.downloads/native-src/libarchive-3.8.11" -B "$out/libarchive" "${common[@]}" "-DCMAKE_C_FLAGS=-Os -std=gnu11 -fPIE -ffunction-sections -fdata-sections -I$usb_ar_root/.downloads/native-src/libarchive-3.8.11/contrib/android/include" -DENABLE_TEST=OFF -DENABLE_TAR=OFF -DENABLE_CPIO=OFF -DENABLE_CAT=OFF -DENABLE_UNZIP=OFF -DENABLE_ACL=OFF -DENABLE_XATTR=OFF -DENABLE_ICONV=OFF -DENABLE_OPENSSL=OFF -DENABLE_MBEDTLS=OFF -DENABLE_NETTLE=OFF -DENABLE_CNG=OFF -DENABLE_MD=OFF -DENABLE_LIBB2=OFF -DENABLE_LZ4=OFF -DENABLE_LZO=OFF -DENABLE_ZSTD=OFF -DENABLE_BZip2=OFF -DENABLE_LIBXML2=OFF -DENABLE_EXPAT=OFF -DENABLE_BSDXML=OFF -DENABLE_WIN32_XMLLITE=OFF -DENABLE_PCREPOSIX=OFF -DENABLE_PCRE2POSIX=OFF -DENABLE_LIBGCC=OFF -DLIBLZMA_LIBRARY="$out/xz/liblzma.a" -DLIBLZMA_INCLUDE_DIR="$usb_ar_root/.downloads/native-src/xz-5.8.4/src/liblzma/api" -DZLIB_LIBRARY="$sys/usr/lib/$triple/23/libz.so" -DZLIB_INCLUDE_DIR="$sys/usr/include"
 cmake --build "$out/libarchive" --target archive_static -j1
 python3 tools/native_cc.py -fPIE -pie -I"$usb_ar_root/.downloads/native-src/libarchive-3.8.11/libarchive" -I"$usb_ar_root/.downloads/native-src/xz-5.8.4/src/liblzma/api" native/archive_cli.c native/archive_lzma_limits.c "$out/libarchive/libarchive/libarchive.a" "$out/xz/liblzma.a" -lz -Wl,--gc-sections -Wl,--wrap=lzma_raw_decoder,--wrap=lzma_stream_decoder,--wrap=lzma_alone_decoder -o "app/src/main/jniLibs/$abi/libusbwb_archive.so"
 llvm-strip --strip-unneeded "app/src/main/jniLibs/$abi/libusbwb_archive.so"
 echo "Built API23 archive engine $abi"
done
