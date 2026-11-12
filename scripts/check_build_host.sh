#!/usr/bin/env bash
# User requires all Android builds/download caches to stay on the Lenovo tablet.
if command -v getprop >/dev/null 2>&1; then
    usb_build_model="$(getprop ro.product.model)"
    if [ "$usb_build_model" != 'Lenovo YT-J706X' ]; then
        echo 'Build stopped: this project is restricted to the Lenovo tablet. APK installation/testing is separate.' >&2
        return 1 2>/dev/null || exit 1
    fi
fi
