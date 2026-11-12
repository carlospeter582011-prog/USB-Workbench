/* Original MIT launcher: preserve the upstream utility's argv[0]-based mode selection. */
extern int usbwb_f2fs_main(int,char **);
int main(int argc,char **argv){
#ifdef USBWB_F2FS_FSCK
 argv[0]="fsck.f2fs";
#else
 argv[0]="mkfs.f2fs";
#endif
 return usbwb_f2fs_main(argc,argv);
}
