/* Original GPL-3.0-or-later WIM tree adapter; upstream wimlib sources unchanged. */
#define _GNU_SOURCE 1
#include <wimlib.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <locale.h>
#include <sys/stat.h>
#include <dlfcn.h>
#define MAX_PATHS 10000
#define MAX_FILES 1000
struct item {char *path;uint64_t size;unsigned type;};
struct plan {struct item *items;unsigned count,files;uint64_t total,limit;};
static int output_fd=-1;
/* Translate only this adapter's raw stdout extraction into a fixed virtual file.
 * All other writes retain the existing preload bridge and normal CLI behavior. */
ssize_t write(int fd,const void *data,size_t n){static ssize_t(*next)(int,const void*,size_t);if(!next)next=dlsym(RTLD_NEXT,"write");if(!next){errno=EIO;return -1;}return next(fd==1&&output_fd>=0?output_fd:fd,data,n);}
extern int usbwb_imagex_main(int,char **);
static int put(int fd,const void *data,size_t n){while(n){ssize_t r=write(fd,data,n);if(r<0&&errno==EINTR)continue;if(r<=0)return -1;data=(const char*)data+r;n-=r;}return 0;}
static void le(unsigned char *b,uint64_t n,int count){for(int i=0;i<count;i++)b[i]=n>>(8*i);}
static int collect(const struct wimlib_dir_entry *e,void *ctx){struct plan *p=ctx;if(!e->filename)return 0;const char *name=e->full_path;while(*name=='/')name++;size_t n=strlen(name);if(!n)return (e->attributes&WIMLIB_FILE_ATTRIBUTE_DIRECTORY)?0:-1;if(n>1024||e->depth>128||p->count>=MAX_PATHS)return -1;
 unsigned a=e->attributes;unsigned mode=e->unix_mode&S_IFMT;int special=(a&(WIMLIB_FILE_ATTRIBUTE_REPARSE_POINT|WIMLIB_FILE_ATTRIBUTE_ENCRYPTED|WIMLIB_FILE_ATTRIBUTE_DEVICE))||e->num_named_streams||(mode&&mode!=S_IFDIR&&mode!=S_IFREG);unsigned type=special?3:(a&WIMLIB_FILE_ATTRIBUTE_DIRECTORY)?1:2;uint64_t size=type==2?e->streams[0].resource.uncompressed_size:0;
 if(size>p->limit-p->total||type==2&&++p->files>MAX_FILES)return -1;char *copy=strdup(name);if(!copy)return -1;p->items[p->count++]=(struct item){copy,size,type};p->total+=size;return 0;}
static uint64_t number(const char *s){char *end;errno=0;unsigned long long n=strtoull(s,&end,10);return errno||!*s||*s=='-'||*end?UINT64_MAX:n;}
static int tree(int argc,char **argv){if(argc!=5)return 2;uint64_t index=number(argv[2]),limit=number(argv[3]);if(index<1||index>256||limit<1||limit>16ULL*1024*1024*1024||strlen(argv[4])>1025||argv[4][0]!='/')return 2;
 setlocale(LC_CTYPE,"C.UTF-8");wimlib_set_print_errors(true);WIMStruct *wim=NULL;struct plan p={.limit=limit};p.items=calloc(MAX_PATHS,sizeof(*p.items));int rc=3,fd=-1;int all=!strcmp(argv[1],"usbwb-tree-all");if(!p.items)goto done;
 if(wimlib_open_wim("/usbwb/source.wim",WIMLIB_OPEN_FLAG_CHECK_INTEGRITY,&wim))goto done;
 if(wimlib_iterate_dir_tree(wim,(int)index,argv[4],WIMLIB_ITERATE_DIR_TREE_FLAG_RECURSIVE|WIMLIB_ITERATE_DIR_TREE_FLAG_RESOURCES_NEEDED,collect,&p))goto done;
 fd=open("/usbwb/manifest.bin",O_CREAT|O_EXCL|O_RDWR,0600);if(fd<0||put(fd,"UWARCH01",8))goto done;
 for(unsigned i=0;i<p.count;i++){struct item *it=p.items+i;unsigned char record[13];record[0]=it->type;le(record+1,strlen(it->path),4);le(record+5,it->size,8);if(put(fd,record,13)||put(fd,it->path,strlen(it->path)))goto done;}if(fsync(fd)||close(fd)){fd=-1;goto done;}fd=-1;
 if(all)for(unsigned i=0;i<p.count;i++){struct item *it=p.items+i;if(it->type!=2)continue;char out[64];snprintf(out,sizeof(out),"/usbwb/entry%05u.bin",i);output_fd=open(out,O_CREAT|O_EXCL|O_RDWR,0600);if(output_fd<0)goto done;const char *paths[1]={it->path};int error=wimlib_extract_paths(wim,(int)index,".",paths,1,WIMLIB_EXTRACT_FLAG_TO_STDOUT);int sync=error?0:fsync(output_fd);int close_rc=close(output_fd);output_fd=-1;if(error||sync||close_rc)goto done;}
 rc=0;
 done:if(fd>=0)close(fd);if(output_fd>=0){close(output_fd);output_fd=-1;}if(wim)wimlib_free(wim);if(p.items){for(unsigned i=0;i<p.count;i++)free(p.items[i].path);free(p.items);}if(rc)fprintf(stderr,"WIM_TREE_FAILED paths/size/type/image/data constraints\n");return rc;
}
int main(int argc,char **argv){if(argc>1&&(!strcmp(argv[1],"usbwb-tree-list")||!strcmp(argv[1],"usbwb-tree-all")))return tree(argc,argv);return usbwb_imagex_main(argc,argv);}
