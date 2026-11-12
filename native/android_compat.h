/* Original API23 compatibility shim, MIT license. */
#ifndef USBWB_ANDROID_COMPAT_H
#define USBWB_ANDROID_COMPAT_H
#include <mntent.h>
#include <string.h>
#if defined(__ANDROID__) && __ANDROID_API__ < 26
static inline char *usbwb_hasmntopt(const struct mntent *entry,const char *option){
    const char *at=entry->mnt_opts;size_t n=strlen(option);if(!at||!n)return 0;
    while(*at){const char *end=strchr(at,',');size_t length=end?(size_t)(end-at):strlen(at);if(length==n&&!memcmp(at,option,n))return (char*)at;if(!end)break;at=end+1;}return 0;
}
#define hasmntopt usbwb_hasmntopt
#endif
#endif
