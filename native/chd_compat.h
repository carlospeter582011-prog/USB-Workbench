/* Original MIT API23 profile. Actual app IO uses 64-bit read/lseek callbacks.
 * The library's unused stdio convenience entry points require API24 on 32-bit.
 * Reject them rather than silently narrowing offsets or raising minSdk. */
#ifndef USBWB_CHD_COMPAT_H
#define USBWB_CHD_COMPAT_H
#if defined(__ANDROID__) && __ANDROID_API__ < 24 && !defined(__LP64__)
#include <stdio.h>
#include <sys/types.h>
#include <errno.h>
static inline int usbwb_chd_stdio_seek(FILE *f,off64_t n,int whence){(void)f;(void)n;(void)whence;errno=ENOSYS;return -1;}
static inline off64_t usbwb_chd_stdio_tell(FILE *f){(void)f;errno=ENOSYS;return -1;}
#define fseeko usbwb_chd_stdio_seek
#define ftello usbwb_chd_stdio_tell
#endif
#endif
