/* Original MIT archive adapter. Never extracts real paths or runs external programs. */
#define _FILE_OFFSET_BITS 64
#include <archive.h>
#include <archive_entry.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <locale.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#define MAX_ENTRIES 10000
#define MAX_PATH 1024
#define MAX_SIZE (16ULL*1024*1024*1024)
static int put(int fd,const void *p,size_t n){while(n){ssize_t w=write(fd,p,n);if(w<0&&errno==EINTR)continue;if(w<=0)return -1;p=(const char*)p+w;n-=w;}return 0;}
static void le32(unsigned char *p,uint32_t v){for(int i=0;i<4;i++)p[i]=v>>(8*i);}
static void le64(unsigned char *p,uint64_t v){for(int i=0;i<8;i++)p[i]=v>>(8*i);}
static uint64_t number(const char *s){char *end;errno=0;unsigned long long n=strtoull(s,&end,10);if(errno||!*s||*s=='-'||*end)return UINT64_MAX;return n;}
int main(int argc,char **argv){
 setlocale(LC_CTYPE,"C.UTF-8");
 if(argc==2&&!strcmp(argv[1],"--version")){fprintf(stderr,"USBWB archive adapter / %s\n",archive_version_string());return 0;}
 int all=argc==3&&!strcmp(argv[1],"all");int listing=argc==3&&!strcmp(argv[1],"list"),extracting=argc==6&&!strcmp(argv[1],"extract");
 if(!listing&&!extracting&&!all){fprintf(stderr,"ARCHIVE_OPTIONS\n");return 2;}
 uint64_t limit=number(argv[2]),wanted=extracting?number(argv[3]):0,expected=extracting?number(argv[4]):0;
 if(!limit||limit>MAX_SIZE||wanted>=MAX_ENTRIES||expected>limit)return 2;
 struct archive *a=archive_read_new();if(!a)return 3;
 const char *password=getenv("USBWB_ARCHIVE_PASSWORD");if(password&&*password&&archive_read_add_passphrase(a,password)!=ARCHIVE_OK){archive_read_free(a);return 3;}
 /* Explicit in-process filters only: support_filter_all may spawn external helpers. */
 archive_read_support_filter_none(a);archive_read_support_filter_gzip(a);archive_read_support_filter_xz(a);archive_read_support_filter_lzma(a);
 archive_read_support_format_7zip(a);archive_read_support_format_rar(a);archive_read_support_format_rar5(a);archive_read_support_format_zip(a);archive_read_support_format_tar(a);archive_read_support_format_xar(a);archive_read_support_format_iso9660(a);archive_read_support_format_cab(a);archive_read_support_format_ar(a);archive_read_support_format_cpio(a);archive_read_support_format_lha(a);
 int fd=-1,rc=4,found=0;uint64_t total=0,index=0;struct archive_entry *entry;
 if(archive_read_open_filename(a,"/usbwb/source.wim",65536)!=ARCHIVE_OK)goto fail;
 if(listing){fd=open("/usbwb/manifest.bin",O_CREAT|O_EXCL|O_RDWR,0600);if(fd<0||put(fd,"UWARCH01",8))goto fail;}
 for(;;){int status=archive_read_next_header(a,&entry);if(status==ARCHIVE_EOF)break;if(status!=ARCHIVE_OK||index>=MAX_ENTRIES)goto fail;
  const char *path=archive_entry_pathname_utf8(entry);int type=archive_entry_filetype(entry);size_t len=path?strlen(path):0;
  if(!len||len>MAX_PATH||(archive_entry_is_encrypted(entry)>0&&(!password||!*password))||archive_entry_hardlink(entry)||archive_entry_symlink(entry)||(type!=AE_IFREG&&type!=AE_IFDIR)||(type==AE_IFREG&&!archive_entry_size_is_set(entry)))goto fail;
  int64_t size=archive_entry_size(entry);if(size<0||(uint64_t)size>limit-total||(type==AE_IFDIR&&size!=0))goto fail;total+=size;
  if(listing){unsigned char record[13];record[0]=type==AE_IFDIR?1:2;le32(record+1,len);le64(record+5,size);if(put(fd,record,sizeof(record))||put(fd,path,len))goto fail;}
  int selected=(extracting&&index==wanted)||(all&&type==AE_IFREG);
  if(selected){if(extracting&&(type!=AE_IFREG||(uint64_t)size!=expected||strcmp(path,argv[5])))goto fail;char output[64];if(all)snprintf(output,sizeof(output),"/usbwb/entry%05llu.bin",(unsigned long long)index);else strcpy(output,"/usbwb/export.bin");fd=open(output,O_CREAT|O_EXCL|O_RDWR,0600);if(fd<0)goto fail;found=1;}
  unsigned char buffer[65536];uint64_t readbytes=0;
  /* Consume every listed file, including solid streams, to validate sizes and CRCs. */
  for(;;){la_ssize_t n=archive_read_data(a,buffer,sizeof(buffer));if(n<0)goto fail;if(!n)break;if((uint64_t)n>(uint64_t)size-readbytes)goto fail;readbytes+=n;if(selected&&put(fd,buffer,n))goto fail;}
  if(readbytes!=(uint64_t)size)goto fail;
  if(selected){if(fsync(fd)||close(fd)){fd=-1;goto fail;}fd=-1;if(!all)break;}index++;
 }
 if(extracting&&!found)goto fail;
 if(fd>=0&&(fsync(fd)||close(fd))){fd=-1;goto fail;}fd=-1;rc=0;
fail:
 if(rc)fprintf(stderr,"ARCHIVE_ENGINE_FAILED: %s\n",archive_error_string(a)?archive_error_string(a):"unsupported/encrypted/unsafe entry, size limit or output I/O");
 if(fd>=0)close(fd);archive_read_free(a);return rc;
}
