#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/check_build_host.sh
usb_native_root="$PWD"
usb_native_abis="${USB_BUILD_ABIS:-arm64-v8a armeabi-v7a x86 x86_64}"
for abi in $usb_native_abis; do
    export USB_NATIVE_ABI="$abi"
    case "$abi" in arm64-v8a) host=aarch64-linux-android;; armeabi-v7a) host=arm-linux-androideabi;; x86) host=i686-linux-android;; x86_64) host=x86_64-linux-android;; esac
    out="$usb_native_root/.downloads/native-build/$abi"
    dest="$usb_native_root/app/src/main/jniLibs/$abi"
    mkdir -p "$out/ntfs" "$out/ext" "$dest"
    echo "Building API23 native engines: $abi"
    python3 tools/native_cc.py -U_FILE_OFFSET_BITS -shared -fPIC native/usb_bridge.c -o "$dest/libusbwb_bridge.so" -ldl
    if [ ! -f "$out/ntfs/Makefile" ]; then
        (cd "$out/ntfs" && CONFIG_SHELL=/bin/bash SHELL=/bin/bash CC="$usb_native_root/tools/native_cc.py" AR=${USB_LLVM_BIN}/llvm-ar RANLIB=${USB_LLVM_BIN}/llvm-ranlib CFLAGS='-Os -fPIE' LDFLAGS='-pie' bash "$usb_native_root/.downloads/native-src/ntfs-3g-2026.9.28/configure" --host="$host" --build=x86_64-pc-linux-gnu --disable-shared --enable-static --disable-ntfs-3g --disable-crypto --without-uuid --disable-ldconfig)
    fi
    make -C "$out/ntfs/libntfs-3g" -j1 SHELL=/bin/bash
    for tool in mkntfs ntfsfix ntfsls ntfscat ntfscp ntfsinfo ntfsclone; do rm -f "$out/ntfs/ntfsprogs/$tool"; done
    make -C "$out/ntfs/ntfsprogs" -j1 SHELL=/bin/bash mkntfs ntfsfix ntfsls ntfscat ntfscp ntfsinfo ntfsclone
    for tool in mkntfs ntfsfix ntfsls ntfscat ntfscp ntfsinfo ntfsclone; do cp "$out/ntfs/ntfsprogs/$tool" "$dest/lib${tool}.so"; done
    if [ ! -f "$out/ext/Makefile" ]; then
        (cd "$out/ext" && CONFIG_SHELL=/bin/bash SHELL=/bin/bash CC="$usb_native_root/tools/native_cc.py" BUILD_CC=/usr/bin/cc AR=${USB_LLVM_BIN}/llvm-ar RANLIB=${USB_LLVM_BIN}/llvm-ranlib CFLAGS='-Os -fPIE' LDFLAGS='-pie' bash "$usb_native_root/.downloads/native-src/e2fsprogs-1.47.4/configure" --host="$host" --build=x86_64-pc-linux-gnu --disable-elf-shlibs --enable-libuuid --enable-libblkid --disable-nls --disable-fuse2fs --disable-uuidd --disable-e2initrd-helper --without-libarchive)
    fi
    usb_ext_flags="-Os -fPIE -DCONFIG_DISABLE_LIBARCHIVE -include $usb_native_root/native/android_compat.h"
    make -C "$out/ext" -j1 SHELL=/bin/bash LN=/bin/cp CFLAGS="$usb_ext_flags" CFLAGS_STLIB="$usb_ext_flags" libs
    for usb_archive in "$out"/ext/lib/*/*.a; do cp "$usb_archive" "$out/ext/lib/$(basename "$usb_archive")"; done
    rm -f "$out/ext/misc/mke2fs" "$out/ext/e2fsck/e2fsck"
    make -C "$out/ext/misc" -j1 SHELL=/bin/bash LN=/bin/cp CFLAGS="$usb_ext_flags" CFLAGS_STLIB="$usb_ext_flags" mke2fs
    make -C "$out/ext/e2fsck" -j1 SHELL=/bin/bash LN=/bin/cp CFLAGS="$usb_ext_flags" CFLAGS_STLIB="$usb_ext_flags" e2fsck
    cp "$out/ext/misc/mke2fs" "$dest/libmke2fs.so"
    cp "$out/ext/e2fsck/e2fsck" "$dest/libe2fsck.so"
    ${USB_LLVM_BIN}/llvm-strip --strip-unneeded "$dest"/*.so
done
