/* Owned synthetic RPC probe. Not shipped in the APK. */
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/fs.h>
int main(int argc,char **argv){
 int ro=argc>1;int a=open("/dev/usbworkbench",O_RDWR),b=open("/dev/usbworkbench",O_RDONLY);if(a<0||b<0||a==b)return 10;int bs=0,want=4096;if(ioctl(a,BLKBSZGET,&bs)||bs!=512||ioctl(a,BLKBSZSET,&want)||ioctl(a,BLKBSZGET,&bs)||bs!=4096)return 23;want=8192;if(ioctl(a,BLKBSZSET,&want)!=-1)return 24;char data[512],out[512];memset(data,0x5a,512);
 if(ro){if(pwrite(a,data,512,0)!=-1||errno!=EROFS)return 11;}else{if(pwrite(a,data,512,0)!=512)return 12;if(pwrite(b,data,512,0)!=-1||errno!=EBADF)return 13;}
 if(lseek(a,1024,SEEK_SET)!=1024||lseek(b,0,SEEK_CUR)!=0)return 14;
 if(read(b,out,512)!=512||lseek(b,0,SEEK_CUR)!=512||lseek(a,0,SEEK_CUR)!=1024)return 15;
 if(!ro&&memcmp(out,data,512))return 16;
 if(pread(a,out,512,64LL*1048576)!=-1)return 17;
 if(close(a)||read(b,out,512)!=512||close(b))return 18;
 int fds[32];for(int i=0;i<32;i++)if((fds[i]=open("/dev/usbworkbench",O_RDONLY))<0)return 19;
 if(open("/dev/usbworkbench",O_RDONLY)!=-1||errno!=EMFILE)return 20;
 for(int i=0;i<32;i++)if(close(fds[i]))return 21;
 if(read(-1,out,512)!=-1||errno!=EBADF)return 22;
 return 0;
}
