/* Original filesystem image adapter, GPL-2.0-or-later.
 * Only private read-only library contexts and virtual file RPC. Never an OS mount. */
#define _GNU_SOURCE 1
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <locale.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#ifdef USBWB_NTFS
#include <ntfs-3g/volume.h>
#include <ntfs-3g/inode.h>
#include <ntfs-3g/attrib.h>
#include <ntfs-3g/dir.h>
#include <ntfs-3g/unistr.h>
static ntfs_volume *fs;
#else
#include <ext2fs/ext2fs.h>
static ext2_filsys fs;
#endif
#define MAX_ENTRIES 10000
struct item{char *path;uint64_t inode,size;int dir;};
static struct item entries[MAX_ENTRIES];static uint64_t visited[MAX_ENTRIES],total,maximum;static unsigned used,visits;static int failed;
static int put(int fd,const void *p,size_t n){while(n){ssize_t w=write(fd,p,n);if(w<0&&errno==EINTR)continue;if(w<=0)return -1;p=(const char*)p+w;n-=w;}return 0;}
static void putle32(unsigned char *p,uint32_t n){for(int i=0;i<4;i++)p[i]=n>>(i*8);}static void putle64(unsigned char *p,uint64_t n){for(int i=0;i<8;i++)p[i]=n>>(i*8);}
static int walk(uint64_t inode,const char *prefix,unsigned depth);
static int add(uint64_t inode,const char *name,const char *parent,unsigned depth){
 if(failed)return -1;if(!strcmp(name,".")||!strcmp(name,".."))return 0;
 if(used>=MAX_ENTRIES||depth>=32||strlen(parent)+strlen(name)+(!!*parent)>1024)return failed=1,-1;
 int dir;uint64_t size=0;
#ifdef USBWB_NTFS
 ntfs_inode *ni=ntfs_inode_open(fs,inode);if(!ni)return failed=1,-1;dir=(ni->flags&(FILE_ATTR_REPARSE_POINT|FILE_ATTR_ENCRYPTED))?2:((ni->mrec->flags&MFT_RECORD_IS_DIRECTORY)!=0);
 if(!dir){ntfs_attr *attr=ntfs_attr_open(ni,AT_DATA,AT_UNNAMED,0);if(!attr||attr->data_size<0){if(attr)ntfs_attr_close(attr);ntfs_inode_close(ni);return failed=1,-1;}size=attr->data_size;ntfs_attr_close(attr);}ntfs_inode_close(ni);
#else
 struct ext2_inode ni;if(ext2fs_read_inode(fs,(ext2_ino_t)inode,&ni))return failed=1,-1;dir=LINUX_S_ISDIR(ni.i_mode);if((!dir&&!LINUX_S_ISREG(ni.i_mode))||(ni.i_flags&EXT4_ENCRYPT_FL))dir=2;if(!dir)size=EXT2_I_SIZE(&ni);
#endif
 if(size>maximum-total)return failed=1,-1;total+=size;
 struct item *e=&entries[used++];e->path=malloc(strlen(parent)+strlen(name)+2);if(!e->path)return failed=1,-1;sprintf(e->path,"%s%s%s",parent,*parent?"/":"",name);e->inode=inode;e->size=size;e->dir=dir;
 if(dir==1&&walk(inode,e->path,depth+1))return failed=1,-1;return 0;
}
struct context{const char *parent;unsigned depth;};
#ifdef USBWB_NTFS
static int found(void *p,const ntfschar *name,int n,int type,s64 pos,MFT_REF ref,unsigned dt){(void)pos;(void)dt;if(type==FILE_NAME_DOS||MREF(ref)<16)return 0;char *utf8=NULL;int bytes=ntfs_ucstombs(name,n,&utf8,0);if(bytes<0)return failed=1,-1;if((int)strlen(utf8)!=bytes||strchr(utf8,'/')){free(utf8);return failed=1,-1;}struct context *c=p;int rc=add(ref,utf8,c->parent,c->depth);free(utf8);return rc;}
#else
static int found(ext2_ino_t ino,int entry,struct ext2_dir_entry *e,int offset,int blocksize,char *buf,void *p){(void)ino;(void)entry;(void)offset;(void)blocksize;(void)buf;if(!e->inode)return 0;int n=ext2fs_dirent_name_len(e);if(n<1||n>255)return failed=1,DIRENT_ABORT;char name[256];if(memchr(e->name,0,n)||memchr(e->name,'/',n))return failed=1,DIRENT_ABORT;memcpy(name,e->name,n);name[n]=0;struct context *c=p;if(add(e->inode,name,c->parent,c->depth))return DIRENT_ABORT;return 0;}
#endif
static int walk(uint64_t inode,const char *prefix,unsigned depth){
 if(visits>=MAX_ENTRIES)return -1;for(unsigned i=0;i<visits;i++)if(visited[i]==inode)return -1;visited[visits++]=inode;struct context c={prefix,depth};
#ifdef USBWB_NTFS
 ntfs_inode *ni=ntfs_inode_open(fs,inode);if(!ni)return -1;s64 pos=0;int rc=ntfs_readdir(ni,&pos,&c,found);ntfs_inode_close(ni);return rc||failed?-1:0;
#else
 return ext2fs_dir_iterate2(fs,(ext2_ino_t)inode,0,NULL,found,&c)||failed?-1:0;
#endif
}
static int copy(struct item *e,int fd){unsigned char b[65536];uint64_t at=0;int rc=-1;
#ifdef USBWB_NTFS
 ntfs_inode *ni=ntfs_inode_open(fs,e->inode);if(!ni)return -1;ntfs_attr *file=ntfs_attr_open(ni,AT_DATA,AT_UNNAMED,0);if(!file){ntfs_inode_close(ni);return -1;}if(file->data_size<0||(uint64_t)file->data_size!=e->size)goto end;
#else
 ext2_file_t file;if(ext2fs_file_open(fs,(ext2_ino_t)e->inode,0,&file))return -1;__u64 size;if(ext2fs_file_get_lsize(file,&size)||size!=e->size)goto end;
#endif
 while(at<e->size){unsigned n=e->size-at<sizeof(b)?(unsigned)(e->size-at):sizeof(b);
#ifdef USBWB_NTFS
 s64 got=ntfs_attr_pread(file,at,n,b);if(got!=(s64)n)goto end;
#else
 unsigned got;if(ext2fs_file_read(file,b,n,&got)||got!=n)goto end;
#endif
 if(fd>=0&&put(fd,b,n))goto end;at+=n;
 }rc=0;
 end:
#ifdef USBWB_NTFS
 ntfs_attr_close(file);ntfs_inode_close(ni);
#else
 ext2fs_file_close(file);
#endif
 return rc;
}
static int stats(void){
 uint64_t blocks,free_blocks,block_size;
#ifdef USBWB_NTFS
 if(ntfs_volume_get_free_space(fs)||fs->nr_clusters<1||fs->free_clusters<0||fs->free_clusters>fs->nr_clusters)return -1;
 blocks=fs->nr_clusters;free_blocks=fs->free_clusters;block_size=fs->cluster_size;
#else
 blocks=ext2fs_blocks_count(fs->super);free_blocks=ext2fs_free_blocks_count(fs->super);block_size=fs->blocksize;
#endif
 if(!block_size||free_blocks>blocks||blocks>UINT64_MAX/block_size)return -1;
 unsigned char data[40];memset(data,0,sizeof(data));memcpy(data,"UWFSTAT2",8);putle64(data+8,blocks);putle64(data+16,free_blocks);putle64(data+24,block_size);
 const char *label="";
#ifdef USBWB_NTFS
 label=fs->vol_name?fs->vol_name:"";putle32(data+36,le16_to_cpu(fs->flags));
#endif
 size_t length=strlen(label);if(length>1024)return -1;putle32(data+32,length);
 int fd=open("/usbwb/manifest.bin",O_CREAT|O_EXCL|O_RDWR,0600);if(fd<0)return -1;int rc=put(fd,data,sizeof(data));if(!rc&&put(fd,label,length))rc=-1;if(fsync(fd))rc=-1;if(close(fd))rc=-1;return rc;
}
int main(int argc,char **argv){setlocale(LC_CTYPE,"C.UTF-8");if(argc==2&&!strcmp(argv[1],"--version")){fprintf(stderr,"USBWB readonly filesystem image reader\n");return 0;}if(argc!=3||(strcmp(argv[1],"list")&&strcmp(argv[1],"all")&&strcmp(argv[1],"stats")))return 2;char *end;maximum=strtoull(argv[2],&end,10);if(!*argv[2]||*argv[2]=='-'||*end||!maximum||maximum>16ULL*1024*1024*1024)return 2;int rc=4,fd=-1,all=!strcmp(argv[1],"all");
#ifdef USBWB_NTFS
 fs=ntfs_mount("/usbwb/source.wim",NTFS_MNT_RDONLY|NTFS_MNT_FORENSIC);if(!fs)goto done;if(!strcmp(argv[1],"stats")){rc=stats()?4:0;goto done;}if(walk(FILE_root,"",0))goto done;
#else
 if(ext2fs_open("/usbwb/source.wim",EXT2_FLAG_64BITS,0,0,unix_io_manager,&fs))goto done;if(fs->super->s_feature_incompat&EXT3_FEATURE_INCOMPAT_RECOVER)goto done;if(!strcmp(argv[1],"stats")){rc=stats()?4:0;goto done;}if(walk(EXT2_ROOT_INO,"",0))goto done;
#endif
 fd=open("/usbwb/manifest.bin",O_CREAT|O_EXCL|O_RDWR,0600);if(fd<0||put(fd,"UWARCH01",8))goto done;
 for(unsigned i=0;i<used;i++){struct item *e=&entries[i];{unsigned char h[13];h[0]=e->dir==2?3:e->dir?1:2;putle32(h+1,strlen(e->path));putle64(h+5,e->size);if(put(fd,h,13)||put(fd,e->path,strlen(e->path)))goto done;}
  if(all&&!e->dir){int output=-1;if(all){char name[64];snprintf(name,sizeof(name),"/usbwb/entry%05u.bin",i);output=open(name,O_CREAT|O_EXCL|O_RDWR,0600);if(output<0)goto done;}int error=copy(e,output);if(output>=0){if(fsync(output))error=-1;if(close(output))error=-1;}if(error)goto done;}
 }
 if(fd>=0&&(fsync(fd)||close(fd))){fd=-1;goto done;}fd=-1;rc=0;
 done:if(rc)fprintf(stderr,"FSIMAGE_ENGINE_FAILED: unsupported/dirty/encrypted/linked/damaged filesystem, limits or I/O\n");if(fd>=0)close(fd);
#ifdef USBWB_NTFS
 if(fs)ntfs_umount(fs,FALSE);
#else
 if(fs)ext2fs_close(fs);
#endif
 for(unsigned i=0;i<used;i++)free(entries[i].path);return rc;
}
