/* Original MIT bounded CHDv5 data-image decoder; read-only callback IO, no CD track guessing. */
#include <libchdr/chd.h>
#include <libchdr/coretypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/stat.h>
struct input{int fd;uint64_t size;};
static uint64_t length(void *p){return ((struct input*)p)->size;}
static size_t input_read(void *b,size_t size,size_t count,void *p){if(!size||count>SIZE_MAX/size)return 0;size_t n=size*count,done=0;while(done<n){ssize_t r=read(((struct input*)p)->fd,(char*)b+done,n-done);if(r<0&&errno==EINTR)continue;if(r<=0)break;done+=r;}return done/size;}
static int input_seek(void *p,int64_t at,int whence){return lseek(((struct input*)p)->fd,at,whence)<0?-1:0;}
static int input_close(void *p){(void)p;return 0;}
static const core_file_callbacks callbacks={length,input_read,input_close,input_seek};
static int put(int fd,void *b,size_t n){while(n){ssize_t w=write(fd,b,n);if(w<0&&errno==EINTR)continue;if(w<=0)return -1;b=(char*)b+w;n-=w;}return 0;}
int main(int argc,char **argv){
 if(argc==2&&!strcmp(argv[1],"--version")){fprintf(stderr,"USBWB libchdr0.3.0 decoder\n");return 0;}
 if(argc!=3||(strcmp(argv[1],"check")&&strcmp(argv[1],"convert")))return 2;char *end;errno=0;uint64_t max=strtoull(argv[2],&end,10);if(errno||!*argv[2]||*argv[2]=='-'||*end||max<1||max>16ULL*1024*1024*1024)return 2;
 struct input in={-1,0};chd_header header;chd_file *chd=NULL;unsigned char *buffer=NULL;int output=-1,rc=4;struct stat st;
 in.fd=open("/usbwb/source.wim",O_RDONLY);if(in.fd<0||fstat(in.fd,&st)||st.st_size<124)goto done;in.size=st.st_size;
 chd_error error=chd_read_header_core_file_callbacks(&callbacks,&in,&header);if(error!=CHDERR_NONE)goto done;
 if(header.version!=5||!header.logicalbytes||header.logicalbytes>max||(header.unitbytes!=512&&header.unitbytes!=2048)||header.logicalbytes%header.unitbytes||header.hunkbytes<header.unitbytes||header.hunkbytes>1048576||header.hunkbytes%header.unitbytes||header.hunkcount>4194304||header.hunkcount!=(header.logicalbytes+header.hunkbytes-1)/header.hunkbytes){fprintf(stderr,"CHD_UNSUPPORTED\n");goto done;}
 for(int i=0;i<20;i++)if(header.parentsha1[i]){fprintf(stderr,"CHD_PARENT\n");goto done;}
 error=chd_open_core_file_callbacks(&callbacks,&in,CHD_OPEN_READ,NULL,&chd);if(error!=CHDERR_NONE)goto done;
 buffer=malloc(header.hunkbytes);if(!buffer)goto done;
 if(!strcmp(argv[1],"convert")){output=open("/usbwb/export.bin",O_CREAT|O_EXCL|O_RDWR,0600);if(output<0)goto done;}
 uint64_t at=0;for(uint32_t i=0;i<header.hunkcount;i++){error=chd_read(chd,i,buffer);if(error!=CHDERR_NONE)goto done;size_t n=header.logicalbytes-at<header.hunkbytes?(size_t)(header.logicalbytes-at):header.hunkbytes;if(output>=0&&put(output,buffer,n))goto done;at+=n;}
 if(output>=0&&fsync(output))goto done;
 fprintf(stderr,"CHD_INFO %llu %u ",(unsigned long long)header.logicalbytes,header.unitbytes);for(int i=0;i<20;i++)fprintf(stderr,"%02x",header.rawsha1[i]);fprintf(stderr,"\n");rc=0;
 done:if(rc)fprintf(stderr,"CHD_ENGINE_FAILED\n");if(output>=0)close(output);if(chd)chd_close(chd);if(in.fd>=0)close(in.fd);free(buffer);return rc;
}
