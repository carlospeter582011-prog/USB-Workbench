package org.usbworkbench;
import java.io.*;import java.nio.charset.StandardCharsets;import java.security.*;import java.util.*;import org.tukaani.xz.XZInputStream;

/** Imports data assets only from a user-selected official Linux package. Never runs package code. */
public final class VentoyBundle {
 public static final long EFI_BYTES=33554432,CORE_BYTES=1048064;
 public final File directory;public final String version;
 private VentoyBundle(File dir,String version){directory=dir;this.version=version;}
 public File file(String name){return new File(directory,name);}
 private static String hash(File file)throws IOException {try{MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)digest.update(b,0,n);}StringBuilder text=new StringBuilder();for(byte b:digest.digest())text.append(String.format(Locale.ROOT,"%02x",b&255));return text.toString();}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
 public static VentoyBundle importPackage(InputStream source,File staging,DiskTools.Progress progress)throws IOException {
  if(staging.exists()||!staging.mkdir())throw new IOException("VENTOY_CACHE");boolean ok=false;Set<String> found=new HashSet<>();String[] root={null},version={null};
  try{TarTools.scan(source,(entry,input)->{
   int slash=entry.path.indexOf('/');if(slash<0)return;String parent=entry.path.substring(0,slash),path=entry.path.substring(slash+1);
   if(!Arrays.asList("boot/boot.img","boot/core.img.xz","ventoy/ventoy.disk.img.xz","ventoy/version").contains(path))return;
   if(!parent.matches("ventoy-[0-9]+(\\.[0-9]+){1,3}")||!entry.regular()||!found.add(path)||root[0]!=null&&!root[0].equals(parent))throw new IOException("VENTOY_PACKAGE");root[0]=parent;
   if(path.equals("ventoy/version")){if(entry.size>32)throw new IOException("VENTOY_PACKAGE");byte[] text=new byte[(int)entry.size];DiskTools.readFully(input,text,text.length);version[0]=new String(text,StandardCharsets.US_ASCII).trim();return;}
   String output=path.equals("boot/boot.img")?"boot.img":path.equals("boot/core.img.xz")?"core.img":"efi.img";long expected=output.equals("boot.img")?512:output.equals("core.img")?CORE_BYTES:EFI_BYTES;
   if(entry.size>EFI_BYTES)throw new IOException("VENTOY_PACKAGE");InputStream decoded=path.endsWith(".xz")?new XZInputStream(input,32768):input;
   try(InputStream in=decoded;OutputStream out=new FileOutputStream(new File(staging,output))){byte[] b=new byte[65536];long done=0;while(done<expected){progress.update(done,expected);int n=in.read(b,0,(int)Math.min(b.length,expected-done));if(n<=0)throw new IOException("VENTOY_ASSET_SIZE");out.write(b,0,n);done+=n;}if(in.read()!=-1)throw new IOException("VENTOY_ASSET_SIZE");}
  },256L*1024*1024,10000,progress);
  if(found.size()!=4||version[0]==null||!root[0].equals("ventoy-"+version[0]))throw new IOException("VENTOY_PACKAGE");
  VentoyBundle bundle=new VentoyBundle(staging,version[0]);bundle.validate();Properties properties=new Properties();properties.setProperty("version",version[0]);for(String name:new String[]{"boot.img","core.img","efi.img"})properties.setProperty(name,hash(bundle.file(name)));try(OutputStream out=new FileOutputStream(new File(staging,"bundle.properties"))){properties.store(out,"User-imported data only; no executable package code");}ok=true;return bundle;
  }finally{if(!ok)removeOwned(staging);}
 }
 public static VentoyBundle load(File directory)throws IOException {Properties props=new Properties();try(InputStream in=new FileInputStream(new File(directory,"bundle.properties"))){props.load(in);}String version=props.getProperty("version","");if(!version.matches("[0-9]+(\\.[0-9]+){1,3}"))throw new IOException("VENTOY_PACKAGE");VentoyBundle bundle=new VentoyBundle(directory,version);for(String name:new String[]{"boot.img","core.img","efi.img"})if(!hash(bundle.file(name)).equals(props.getProperty(name)))throw new IOException("VENTOY_ASSET_CHANGED");bundle.validate();return bundle;}
 private void validate()throws IOException {
  if(file("boot.img").length()!=512||file("core.img").length()!=CORE_BYTES||file("efi.img").length()!=EFI_BYTES)throw new IOException("VENTOY_ASSET_SIZE");
  byte[] boot=new byte[512];try(InputStream in=new FileInputStream(file("boot.img"))){DiskTools.readFully(in,boot,512);}if((boot[510]&255)!=85||(boot[511]&255)!=170)throw new IOException("VENTOY_PACKAGE");
  try(RandomAccessFile efi=new RandomAccessFile(file("efi.img"),"r")){byte[] b=new byte[512];efi.readFully(b);if(OpticalImage.u16(b,11)!=512||b[13]!=1||b[16]!=2||!new String(b,43,11,StandardCharsets.US_ASCII).equals("VTOYEFI    ")||!new String(b,54,8,StandardCharsets.US_ASCII).equals("FAT16   ")||(b[510]&255)!=85||(b[511]&255)!=170)throw new IOException("VENTOY_EFI");long total=OpticalImage.u16(b,19);if(total==0)total=OpticalImage.u32(b,32);if(total!=65536)throw new IOException("VENTOY_EFI");int reserved=OpticalImage.u16(b,14),fat=OpticalImage.u16(b,22),roots=OpticalImage.u16(b,17);if(reserved<1||fat<1||roots<1||roots>4096)throw new IOException("VENTOY_EFI");long rootSector=reserved+2L*fat,dataSector=rootSector+(roots*32L+511)/512;if(dataSector>=65536)throw new IOException("VENTOY_EFI");
   byte[] root=new byte[roots*32];efi.seek(rootSector*512);efi.readFully(root);int dir=find(root,"EFI",true);byte[] directory=chain(efi,dir,reserved,dataSector);int bootDir=find(directory,"BOOT",true);byte[] loaders=chain(efi,bootDir,reserved,dataSector);find(loaders,"BOOTX64 EFI",false);find(loaders,"BOOTIA32EFI",false);
  }
 }
 private static int find(byte[] directory,String wanted,boolean dir)throws IOException{String target=wanted.length()==11?wanted:String.format(Locale.ROOT,"%-11s",wanted);for(int at=0;at+32<=directory.length;at+=32){if(directory[at]==0)break;if((directory[at]&255)==229||(directory[at+11]&15)==15)continue;if(new String(directory,at,11,StandardCharsets.US_ASCII).equals(target)){if(((directory[at+11]&16)!=0)!=dir)throw new IOException("VENTOY_EFI");if(!dir&&OpticalImage.u32(directory,at+28)==0)throw new IOException("VENTOY_EFI");return OpticalImage.u16(directory,at+26);}}throw new IOException("VENTOY_EFI");}
 private static byte[] chain(RandomAccessFile file,int cluster,int fatSector,long dataSector)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();Set<Integer> seen=new HashSet<>();while(cluster<65528){if(cluster<2||cluster==65527||!seen.add(cluster)||seen.size()>2048)throw new IOException("VENTOY_EFI");long sector=dataSector+cluster-2;if(sector>=65536)throw new IOException("VENTOY_EFI");byte[] b=new byte[512];file.seek(sector*512);file.readFully(b);out.write(b);file.seek(fatSector*512L+cluster*2L);cluster=file.readUnsignedByte()|(file.readUnsignedByte()<<8);}return out.toByteArray();}
 static void removeOwned(File dir){File[] files=dir.listFiles();if(files!=null)for(File file:files)if(file.isFile())file.delete();dir.delete();}
}
