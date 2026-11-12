#!/usr/bin/env bash
set -euo pipefail
export USB_BUILD_ABIS="$1"
export USB_NATIVE_JOBS=1
bash scripts/build_native.sh
bash scripts/build_bridges.sh
bash scripts/build_wim.sh
bash scripts/build_f2fs.sh
bash scripts/build_fscheck.sh
bash scripts/build_archive.sh
bash scripts/build_chd.sh
bash scripts/build_fsimage.sh
bash scripts/build_udf.sh
python3 scripts/build_hfs.py
python3 scripts/build_apfs.py
python3 scripts/build_extra_dependencies.py
python3 scripts/build_xfs.py
