/* Original API23 allocation/legacy string compatibility for the HFS userspace port. */
#include <stdlib.h>
#include <string.h>
#include <strings.h>
static inline void *usbwb_pagealloc(size_t n){void *p=NULL;return posix_memalign(&p,4096,n)?NULL:p;}
#define valloc usbwb_pagealloc
#undef bzero
#define bzero(p,n) memset(p,0,n)
#undef bcopy
#define bcopy(s,d,n) memmove(d,s,n)

#define bcmp(a,b,n) memcmp(a,b,n)
