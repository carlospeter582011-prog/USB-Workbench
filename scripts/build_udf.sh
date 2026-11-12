#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
python3 - <<'PY'
from pathlib import Path
import hashlib,tarfile
p=Path('native/sources/udftools.tar.gz');assert hashlib.sha256(p.read_bytes()).hexdigest()=='750dcf5c797765eb42265e0a56d1a99f97f94b7f6f4534263a5410503f0caf59'
if not Path('.downloads/native-src/udftools-2.3/mkudffs/main.c').exists():
 with tarfile.open(p) as t:t.extractall('.downloads/native-src',filter='data')
PY
usb_udf_src="$PWD/.downloads/native-src/udftools-2.3"
usb_udf_cfg="$PWD/.downloads/native-build/udf-config"
mkdir -p "$usb_udf_cfg"
cat > "$usb_udf_cfg/config.h" <<'CONFIG'
#define HAVE_DLFCN_H 1
#define HAVE_INTTYPES_H 1
#define HAVE_MEMORY_H 1
#define HAVE_STDINT_H 1
#define HAVE_STDLIB_H 1
#define HAVE_STRINGS_H 1
#define HAVE_STRING_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_UNISTD_H 1
#define STDC_HEADERS 1
#define PACKAGE "udftools"
#define PACKAGE_NAME "udftools"
#define PACKAGE_STRING "udftools 2.3"
#define PACKAGE_VERSION "2.3"
#define VERSION "2.3"
CONFIG
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64};do
 export USB_NATIVE_ABI="$abi"
 for tool in mkudffs udfinfo;do
  name="$tool";if [ "$tool" = mkudffs ];then name=mkfs_udf;fi
  python3 tools/native_cc.py -fPIE -pie -DHAVE_CONFIG_H -I"$usb_udf_cfg" -I"$usb_udf_src/include" "$usb_udf_src/$tool/"*.c "$usb_udf_src/libudffs/"*.c -o "app/src/main/jniLibs/$abi/lib$name.so"
  llvm-strip --strip-unneeded "app/src/main/jniLibs/$abi/lib$name.so"
 done
 echo "Built API23 UDF formatter/info $abi"
done
