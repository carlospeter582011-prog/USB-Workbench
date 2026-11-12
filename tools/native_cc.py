#!/usr/bin/env python3
"""Official Linux NDK compiler, API23, four Android ABIs and16KiB ELF pages."""
import os,sys
from pathlib import Path
ndk=Path(os.environ['USB_NDK_ROOT']);bin=ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin'
targets={'arm64-v8a':'aarch64-linux-android23','armeabi-v7a':'armv7a-linux-androideabi23','x86':'i686-linux-android23','x86_64':'x86_64-linux-android23'}
compiler=bin/(targets[os.environ.get('USB_NATIVE_ABI','arm64-v8a')]+'-clang')
args=[str(compiler),'-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE','-U_FORTIFY_SOURCE','-D_FORTIFY_SOURCE=0','-Os','-Wno-unused-command-line-argument','-Wl,-z,max-page-size=16384','-Wl,-z,common-page-size=4096',*sys.argv[1:]]
os.execv(args[0],args)
