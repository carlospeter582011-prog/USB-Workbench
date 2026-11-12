/* Original MIT API23 adapter. Only literal --ref paths are accepted by the app. */
#ifndef USBWB_WIM_COMPAT_H
#define USBWB_WIM_COMPAT_H
#include <string.h>
#include <errno.h>
/* Bionic exposes GNU/POSIX signatures differently under per-ABI configure flags. */
static inline int usbwb_posix_strerror_r(int number,char *buffer,size_t length) {
    const char *message=strerror(number);size_t n=strlen(message);
    if(n>=length)return ERANGE;
    memcpy(buffer,message,n+1);return 0;
}
#define strerror_r usbwb_posix_strerror_r
#if defined(__ANDROID__) && __ANDROID_API__ < 26
#include <sys/time.h>
#include <sys/stat.h>
#include <fcntl.h>
static inline int usbwb_futimes(int fd,const struct timeval tv[2]) {
    if(!tv)return futimens(fd,NULL);
    struct timespec ts[2]={{tv[0].tv_sec,tv[0].tv_usec*1000},{tv[1].tv_sec,tv[1].tv_usec*1000}};
    return futimens(fd,ts);
}
static inline int usbwb_lutimes(const char *path,const struct timeval tv[2]) {
    if(!tv)return utimensat(AT_FDCWD,path,NULL,AT_SYMLINK_NOFOLLOW);
    struct timespec ts[2]={{tv[0].tv_sec,tv[0].tv_usec*1000},{tv[1].tv_sec,tv[1].tv_usec*1000}};
    return utimensat(AT_FDCWD,path,ts,AT_SYMLINK_NOFOLLOW);
}
#define futimes usbwb_futimes
#define lutimes usbwb_lutimes
#endif
#if defined(__ANDROID__) && __ANDROID_API__ < 28
#include <glob.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
static inline int usbwb_literal_glob(const char *path,int flags,
        int (*callback)(const char *,int),glob_t *result) {
    (void)callback;
    memset(result,0,sizeof(*result));
    if((flags & ~(GLOB_ERR|GLOB_NOSORT)) || strpbrk(path,"*?[]{}~\\")) {
        errno=EINVAL;return GLOB_ABORTED;
    }
    struct stat st;
    if(stat(path,&st))return errno==ENOENT?GLOB_NOMATCH:GLOB_ABORTED;
    result->gl_pathv=calloc(2,sizeof(char *));
    if(!result->gl_pathv)return GLOB_NOSPACE;
    result->gl_pathv[0]=strdup(path);
    if(!result->gl_pathv[0]){free(result->gl_pathv);result->gl_pathv=NULL;return GLOB_NOSPACE;}
    result->gl_pathc=1;result->gl_flags=flags;
    return 0;
}
static inline void usbwb_literal_globfree(glob_t *result) {
    if(result->gl_pathv){free(result->gl_pathv[0]);free(result->gl_pathv);}
    result->gl_pathv=NULL;result->gl_pathc=0;
}
#define glob usbwb_literal_glob
#define globfree usbwb_literal_globfree
#endif
#endif
