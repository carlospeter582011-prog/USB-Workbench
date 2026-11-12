#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_f2fs_root="$PWD"
python3 - <<'PY'
from pathlib import Path
import tarfile,hashlib
archive=Path('native/sources/f2fs-tools.tar.gz');assert hashlib.sha256(archive.read_bytes()).hexdigest()=='772879c0a7970575327c03c973cec7a114633174c5804d7f76d2959bc17151b4'
root=Path('.downloads/native-src/f2fs-tools-1.17.0')
if not (root/'VERSION').is_file():
 root.mkdir(parents=True,exist_ok=True)
 with tarfile.open(archive) as t:t.extractall(root,filter='data')
assert (root/'VERSION').read_text().splitlines()[0]=='1.17.0'
PY
for abi in ${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64}; do
 export USB_NATIVE_ABI="$abi"
 usb_f2fs_out="$usb_f2fs_root/.downloads/native-build/$abi/f2fs"
 usb_f2fs_source="$usb_f2fs_root/.downloads/native-src/f2fs-tools-1.17.0"
 usb_f2fs_dest="$usb_f2fs_root/app/src/main/jniLibs/$abi"
 mkdir -p "$usb_f2fs_out" "$usb_f2fs_dest"
 cat > "$usb_f2fs_out/config.h" <<'CONFIG'
/* Original explicit API23 configuration; upstream sources remain unmodified. */
#define F2FS_TOOLS_VERSION "1.17.0"
#define F2FS_TOOLS_DATE "2026-09-24"
#define F2FS_MAJOR_VERSION 1
#define F2FS_MINOR_VERSION 17
#define HAVE_SCSI_SG_H 1
#define HAVE_LINUX_LOOP_H 1
#define HAVE_LIBUUID 1
#define HAVE_UUID_UUID_H 1
#define HAVE_LINUX_TYPES_H 1
#define HAVE_LINUX_BLKZONED_H 1
#define HAVE_BLK_ZONE_REP_V2 1
#define HAVE_LINUX_FALLOC_H 1
#define HAVE_LINUX_FIEMAP_H 1
#define HAVE_LINUX_FS_H 1
#define HAVE_LINUX_HDREG_H 1
#define HAVE_LINUX_MAJOR_H 1
#define HAVE_LINUX_XATTR_H 1
#define HAVE_BYTESWAP_H 1
#define HAVE_MNTENT_H 1
#define HAVE_SYS_IOCTL_H 1
#define HAVE_SYS_MOUNT_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_SYS_SYSCALL_H 1
#define HAVE_SYS_SYSMACROS_H 1
#define HAVE_SYS_UTSNAME_H 1
#define HAVE_SYS_XATTR_H 1
#define HAVE_GETUID 1
#define HAVE_GETGID 1
#define HAVE_GETMNTENT 1
#define HAVE_SETMNTENT 1
#define HAVE_FSTAT 1
#define HAVE_FSTAT64 1
#define HAVE_FSYNC 1
#define HAVE_PREAD 1
#define HAVE_PWRITE 1
#define HAVE_FSETXATTR 1
#define HAVE_CLOCK_GETTIME 1
#define HAVE_CLOCK_BOOTTIME 1
CONFIG
 usb_f2fs_flags=(-Os -fPIE -DHAVE_CONFIG_H -D_GNU_SOURCE -DWITH_BLKDISCARD -include "$usb_f2fs_root/native/android_compat.h" -I"$usb_f2fs_out" -I"$usb_f2fs_source/include" -I"$usb_f2fs_root/.downloads/native-build/$abi/ext/lib" -Wno-address-of-packed-member)
 usb_f2fs_libs=()
 for file in libf2fs libf2fs_io libf2fs_zoned nls_utf8; do
  python3 tools/native_cc.py "${usb_f2fs_flags[@]}" -c "$usb_f2fs_source/lib/$file.c" -o "$usb_f2fs_out/$file.o"
  usb_f2fs_libs+=("$usb_f2fs_out/$file.o")
 done
 for tool in mkfs fsck; do
  usb_f2fs_objects=()
  if [ "$tool" = mkfs ]; then usb_f2fs_files='f2fs_format_main f2fs_format f2fs_format_utils';else usb_f2fs_files='main fsck dump mount defrag resize node segment dir sload xattr compress dict mkquota quotaio quotaio_tree quotaio_v2 inject';fi
  for file in $usb_f2fs_files; do
   python3 tools/native_cc.py "${usb_f2fs_flags[@]}" -Dmain=usbwb_f2fs_main -I"$usb_f2fs_source/fsck" -c "$usb_f2fs_source/$tool/$file.c" -o "$usb_f2fs_out/$tool-$file.o"
   usb_f2fs_objects+=("$usb_f2fs_out/$tool-$file.o")
  done
  if [ "$tool" = fsck ];then usb_f2fs_entry_flag=-DUSBWB_F2FS_FSCK;else usb_f2fs_entry_flag=;fi
  python3 tools/native_cc.py -fPIE $usb_f2fs_entry_flag -c native/f2fs_entry.c -o "$usb_f2fs_out/entry.o"
  python3 tools/native_cc.py -pie "${usb_f2fs_objects[@]}" "${usb_f2fs_libs[@]}" "$usb_f2fs_out/entry.o" "$usb_f2fs_root/.downloads/native-build/$abi/ext/lib/libuuid.a" -lm -o "$usb_f2fs_dest/lib${tool}_f2fs.so"
  ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$usb_f2fs_dest/lib${tool}_f2fs.so"
 done
 echo "Built API23 F2FS engines: $abi"
done
