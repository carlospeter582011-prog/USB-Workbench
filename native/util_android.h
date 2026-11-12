/* Original API23 shim for unused libcommon account-lookup paths. Fail closed. */
#include <grp.h>
#include <errno.h>
#if __ANDROID_API__ < 24
static inline int usbwb_getgrnam_r(const char *n,struct group *g,char *b,size_t l,struct group **r){(void)n;(void)g;(void)b;(void)l;*r=0;return ENOSYS;}
#define getgrnam_r usbwb_getgrnam_r
#endif
