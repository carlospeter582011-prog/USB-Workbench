/* Original IPC disk adapter, MIT licensed. Loaded only by bundled formatter utilities.
 * Actual disk I/O stays in Android's permission-checked Java USB host API.
 */
#define _GNU_SOURCE 1
#define _LARGEFILE64_SOURCE 1
#undef _FORTIFY_SOURCE
#define _FORTIFY_SOURCE 0
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/fs.h>
#include <dlfcn.h>
#include <errno.h>
#include <string.h>
#include <stdarg.h>
#include <pthread.h>
#include <limits.h>

#define MAX_IO 1048576
static int enabled,rpc_out=-1,rpc_in=0,read_only=1;
static uint64_t disk_size;
static int buffered_block_size=512;
#define MAX_HANDLES 32
static struct {int fd,flags;uint64_t position;} handles[MAX_HANDLES];
static int handle_count;
static pthread_mutex_t position_mutex=PTHREAD_MUTEX_INITIALIZER;
static int slot(int fd){if(fd<0)return -1;for(int i=0;i<handle_count;i++)if(handles[i].fd==fd)return i;return -1;}
static int virtual_handle(int fd){return enabled&&slot(fd)>=0;}
static pthread_mutex_t mutex=PTHREAD_MUTEX_INITIALIZER;
static ssize_t (*real_read)(int,void*,size_t);
static ssize_t (*real_write)(int,const void*,size_t);
static int (*real_open)(const char*,int,...);
static int (*real_close)(int);
static void init(void){
 if(real_read)return;
 real_read=dlsym(RTLD_NEXT,"read");real_write=dlsym(RTLD_NEXT,"write");real_open=dlsym(RTLD_NEXT,"open");real_close=dlsym(RTLD_NEXT,"close");
}
__attribute__((constructor)) static void setup(void){
 init();const char *size=getenv("USBWB_BYTES");if(!size)return;
 char *end=0;disk_size=strtoull(size,&end,10);if(!disk_size||*end)return;
 read_only=strcmp(getenv("USBWB_WRITE")?getenv("USBWB_WRITE"):"0","1")!=0;
 rpc_out=dup(STDOUT_FILENO);if(rpc_out<0)return;dup2(STDERR_FILENO,STDOUT_FILENO);enabled=1;
}
static void put64(unsigned char *p,uint64_t n){for(int i=0;i<8;i++)p[i]=(unsigned char)(n>>(i*8));}
static uint32_t get32(const unsigned char *p){return (uint32_t)p[0]|((uint32_t)p[1]<<8)|((uint32_t)p[2]<<16)|((uint32_t)p[3]<<24);}
static int write_all(int fd,const void *data,size_t n){const unsigned char *p=data;while(n){ssize_t r=real_write(fd,p,n);if(r<=0){if(errno==EINTR)continue;return -1;}p+=r;n-=r;}return 0;}
static int read_all(int fd,void *data,size_t n){unsigned char *p=data;while(n){ssize_t r=real_read(fd,p,n);if(r<=0){if(errno==EINTR)continue;return -1;}p+=r;n-=r;}return 0;}
static ssize_t request(int op,uint64_t offset,void *data,size_t size){
 if(offset>disk_size||size>disk_size-offset){errno=EINVAL;return -1;}if(op==2&&read_only){errno=EROFS;return -1;}
 size_t complete=0;pthread_mutex_lock(&mutex);
 do{
  uint32_t chunk=size-complete>MAX_IO?MAX_IO:(uint32_t)(size-complete);unsigned char header[17]={0x55,0x42,0x57,0x31,(unsigned char)op};put64(header+5,offset+complete);header[13]=chunk;header[14]=chunk>>8;header[15]=chunk>>16;header[16]=chunk>>24;
  if(write_all(rpc_out,header,17)<0||(op==2&&write_all(rpc_out,(char*)data+complete,chunk)<0))goto fail;
  unsigned char response[8];if(read_all(rpc_in,response,8)<0)goto fail;uint32_t err=get32(response),count=get32(response+4);if(err||count!=chunk){errno=err?err:EIO;goto fail;}
  if(op==1&&read_all(rpc_in,(char*)data+complete,chunk)<0)goto fail;
  complete+=chunk;
 }while(complete<size);
 pthread_mutex_unlock(&mutex);return complete;
 fail:pthread_mutex_unlock(&mutex);if(!errno)errno=EIO;return -1;
}
static int is_virtual(const char *path){return enabled&&path&&!strcmp(path,"/dev/usbworkbench");}
int open(const char *path,int flags,...){init();mode_t mode=0;if(flags&O_CREAT){va_list ap;va_start(ap,flags);mode=va_arg(ap,int);va_end(ap);}if(is_virtual(path)){pthread_mutex_lock(&position_mutex);int at=-1;for(int i=0;i<handle_count;i++)if(handles[i].fd<0){at=i;break;}if(at<0&&handle_count<MAX_HANDLES)at=handle_count++;if(at<0){pthread_mutex_unlock(&position_mutex);errno=EMFILE;return -1;}int fd=real_open("/dev/null",O_RDWR|O_CLOEXEC);handles[at].fd=fd;handles[at].flags=flags;handles[at].position=0;pthread_mutex_unlock(&position_mutex);return fd;}return real_open(path,flags,mode);}
int open64(const char *path,int flags,...){mode_t mode=0;if(flags&O_CREAT){va_list ap;va_start(ap,flags);mode=va_arg(ap,int);va_end(ap);}return open(path,flags,mode);}
int __open_2(const char *path,int flags){return open(path,flags);}
int __open64_2(const char *path,int flags){return open(path,flags);}
int close(int fd){init();pthread_mutex_lock(&position_mutex);int at=enabled?slot(fd):-1;if(at>=0)handles[at].fd=-1;int result=real_close(fd);pthread_mutex_unlock(&position_mutex);return result;}
ssize_t pread64(int fd,void *buf,size_t count,off64_t offset){init();if(virtual_handle(fd)){if((handles[slot(fd)].flags&O_ACCMODE)==O_WRONLY){errno=EBADF;return -1;}if(offset<0){errno=EINVAL;return -1;}return request(1,offset,buf,count);}ssize_t(*fn)(int,void*,size_t,off64_t)=dlsym(RTLD_NEXT,"pread64");return fn(fd,buf,count,offset);}
ssize_t pwrite64(int fd,const void *buf,size_t count,off64_t offset){init();if(virtual_handle(fd)){if((handles[slot(fd)].flags&O_ACCMODE)==O_RDONLY){errno=EBADF;return -1;}if(offset<0){errno=EINVAL;return -1;}return request(2,offset,(void*)buf,count);}ssize_t(*fn)(int,const void*,size_t,off64_t)=dlsym(RTLD_NEXT,"pwrite64");return fn(fd,buf,count,offset);}
ssize_t pread(int fd,void *buf,size_t count,off_t offset){return pread64(fd,buf,count,offset);}
ssize_t pwrite(int fd,const void *buf,size_t count,off_t offset){return pwrite64(fd,buf,count,offset);}
ssize_t read(int fd,void *buf,size_t count){init();if(virtual_handle(fd)){pthread_mutex_lock(&position_mutex);int at=slot(fd);if((handles[at].flags&O_ACCMODE)==O_WRONLY){pthread_mutex_unlock(&position_mutex);errno=EBADF;return -1;}uint64_t position=handles[at].position;if(position>=disk_size){pthread_mutex_unlock(&position_mutex);return 0;}size_t n=count>disk_size-position?disk_size-position:count;ssize_t r=request(1,position,buf,n);if(r>0)handles[at].position+=r;pthread_mutex_unlock(&position_mutex);return r;}return real_read(fd,buf,count);}
ssize_t write(int fd,const void *buf,size_t count){init();if(virtual_handle(fd)){pthread_mutex_lock(&position_mutex);int at=slot(fd);if((handles[at].flags&O_ACCMODE)==O_RDONLY){pthread_mutex_unlock(&position_mutex);errno=EBADF;return -1;}ssize_t r=request(2,handles[at].position,(void*)buf,count);if(r>0)handles[at].position+=r;pthread_mutex_unlock(&position_mutex);return r;}return real_write(fd,buf,count);}
off64_t lseek64(int fd,off64_t offset,int whence){init();if(virtual_handle(fd)){pthread_mutex_lock(&position_mutex);int at=slot(fd);int64_t base=whence==SEEK_SET?0:whence==SEEK_CUR?(int64_t)handles[at].position:whence==SEEK_END?(int64_t)disk_size:-1;if(base<0||(offset<0&&offset<-base)||offset>INT64_MAX-base){pthread_mutex_unlock(&position_mutex);errno=EINVAL;return -1;}int64_t next=base+offset;if(next<0||(uint64_t)next>disk_size){pthread_mutex_unlock(&position_mutex);errno=EINVAL;return -1;}handles[at].position=next;pthread_mutex_unlock(&position_mutex);return next;}off64_t(*fn)(int,off64_t,int)=dlsym(RTLD_NEXT,"lseek64");return fn(fd,offset,whence);}
off_t lseek(int fd,off_t offset,int whence){return (off_t)lseek64(fd,offset,whence);}
static void fake_stat(struct stat *s){memset(s,0,sizeof(*s));s->st_mode=S_IFBLK|0600;s->st_nlink=1;s->st_size=disk_size;s->st_blksize=4096;s->st_blocks=disk_size/512;}
int fstat(int fd,struct stat *s){init();if(virtual_handle(fd)){fake_stat(s);return 0;}int(*fn)(int,struct stat*)=dlsym(RTLD_NEXT,"fstat");return fn(fd,s);}
int stat(const char *path,struct stat *s){init();if(is_virtual(path)){fake_stat(s);return 0;}int(*fn)(const char*,struct stat*)=dlsym(RTLD_NEXT,"stat");return fn(path,s);}
int lstat(const char *path,struct stat *s){init();if(is_virtual(path)){fake_stat(s);return 0;}int(*fn)(const char*,struct stat*)=dlsym(RTLD_NEXT,"lstat");return fn(path,s);}
int fsync(int fd){init();if(virtual_handle(fd))return request(3,0,NULL,0)<0?-1:0;int(*fn)(int)=dlsym(RTLD_NEXT,"fsync");return fn(fd);}
int fdatasync(int fd){return fsync(fd);}
int ioctl(int fd,int command,...){va_list ap;va_start(ap,command);void *arg=va_arg(ap,void*);va_end(ap);init();if(virtual_handle(fd)){switch((unsigned int)command){case BLKGETSIZE64:*(uint64_t*)arg=disk_size;return 0;case BLKGETSIZE:*(unsigned long*)arg=disk_size/512;return 0;case BLKSSZGET:*(int*)arg=512;return 0;case BLKBSZGET:*(int*)arg=buffered_block_size;return 0;case BLKBSZSET:if(*(int*)arg!=512&&*(int*)arg!=4096){errno=EINVAL;return -1;}buffered_block_size=*(int*)arg;return 0;case BLKPBSZGET:*(int*)arg=512;return 0;case BLKROGET:*(int*)arg=read_only;return 0;case BLKFLSBUF:return fsync(fd);default:errno=ENOTTY;return -1;}}int(*fn)(int,int,...)=dlsym(RTLD_NEXT,"ioctl");return fn(fd,command,arg);}
static void fake_stat64(struct stat64 *s){memset(s,0,sizeof(*s));s->st_mode=S_IFBLK|0600;s->st_nlink=1;s->st_size=disk_size;s->st_blksize=4096;s->st_blocks=disk_size/512;}
int fstat64(int fd,struct stat64 *s){init();if(virtual_handle(fd)){fake_stat64(s);return 0;}int(*fn)(int,struct stat64*)=dlsym(RTLD_NEXT,"fstat64");return fn(fd,s);}
int stat64(const char *p,struct stat64 *s){init();if(is_virtual(p)){fake_stat64(s);return 0;}int(*fn)(const char*,struct stat64*)=dlsym(RTLD_NEXT,"stat64");return fn(p,s);}
int lstat64(const char *p,struct stat64 *s){init();if(is_virtual(p)){fake_stat64(s);return 0;}int(*fn)(const char*,struct stat64*)=dlsym(RTLD_NEXT,"lstat64");return fn(p,s);}
