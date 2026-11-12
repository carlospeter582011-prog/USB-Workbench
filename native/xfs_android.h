/* Original MIT Android23 compatibility for userspace XFS tools. */
#include <endian.h>
#include "android_compat.h"
#include <stdlib.h>
#include <string.h>
#if defined(__ANDROID__) && __ANDROID_API__ < 26
static inline int usbwb_getsubopt(char **options,char *const *tokens,char **value){
 char *start=*options,*end=strchr(start,','),*equals;
 if(end){*end=0;*options=end+1;}else *options=start+strlen(start);
 equals=strchr(start,'=');if(equals){*equals=0;*value=equals+1;}else *value=NULL;
 for(int i=0;tokens[i];i++)if(!strcmp(start,tokens[i]))return i;
 if(equals)*equals='=';*value=start;return -1;
}
#define getsubopt usbwb_getsubopt
#endif

#if defined(__ANDROID__) && __ANDROID_API__ < 30
#include <errno.h>
struct statx;
static inline int usbwb_statx(int fd,const char *path,int flags,unsigned mask,struct statx *buf){(void)fd;(void)path;(void)flags;(void)mask;(void)buf;errno=ENOSYS;return -1;}
#define statx(fd,path,flags,mask,buf) usbwb_statx(fd,path,flags,mask,buf)
#endif
#if defined(__ANDROID__) && __ANDROID_API__ < 29
#include <stdint.h>
static inline void *usbwb_reallocarray(void *p,size_t n,size_t size){if(size&&n>SIZE_MAX/size){errno=ENOMEM;return NULL;}return realloc(p,n*size);}
#define reallocarray usbwb_reallocarray
#endif
