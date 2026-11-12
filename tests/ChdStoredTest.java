package org.usbworkbench;
import java.io.*;import java.nio.file.*;import java.util.*;
public class ChdStoredTest{
 public static void main(String[] args)throws Exception{File root=new File("artifacts/chd-stored-fixtures");root.mkdirs();int checks=0;for(int unit:new int[]{512,2048})for(int size:new int[]{unit,65536,65536+unit}){File input=new File(root,"own-"+unit+"-"+size+".img"),chd=new File(root,input.getName()+".chd");byte[] b=new byte[size];new Random(33).nextBytes(b);Files.write(input.toPath(),b);chd.delete();try(OpticalImage.Source source=ArchiveTest.source(input);WimFiles.RandomFile output=ArchiveTest.output(chd)){String hash=ChdStored.create(source,output,unit,1048576,(a,n)->{},()->false);try(OpticalImage.Source saved=ArchiveTest.source(chd)){if(!ChdStored.verify(new File("app/src/main/jniLibs/arm64-v8a"),saved,hash,1048576,(a,n)->{},()->false).equals(hash))throw new AssertionError("roundtrip");checks++;}}
 System.out.println("PASS stored CHD roundtrip "+unit+" / "+size);}
 for(long n:new long[]{0,1,513,1048577})try{ChdStored.outputSize(n,512,1048576);throw new AssertionError("accepted");}catch(IOException expected){checks++;}
 System.out.println("PASS: "+checks+" stored CHD actual native decode/hash/size/boundary checks");}}
