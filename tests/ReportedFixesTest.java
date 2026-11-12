package org.usbworkbench;
import java.io.*;import java.util.*;
public final class ReportedFixesTest{
 static int n;static void check(boolean x){n++;if(!x)throw new AssertionError("check "+n);}
 public static void main(String[] args)throws Exception{
 check(OutputNames.stem("samples.zip").equals("samples"));check(OutputNames.stem("عربي.tar.gz").equals("عربي"));check(OutputNames.stem("... ").equals("Archive"));check(!OutputNames.stem("../bad:name\\file.zip").contains("/"));check(!OutputNames.stem("../bad:name\\file.zip").contains("\\"));check(OutputNames.unique("samples",Arrays.asList("SAMPLES","samples (2)")).equals("samples (3)"));
 check(WimPaths.normalize(" /folder/hello.txt ",false).equals("/folder/hello.txt"));check(WimPaths.normalize("folder/عربي.txt",false).equals("/folder/عربي.txt"));check(WimPaths.normalize("/",true).equals("/"));
 for(String s:new String[]{"/ /folder/hello.txt","/../escape","/folder//file","","/"})try{WimPaths.normalize(s,false);throw new AssertionError("bad WIM path accepted");}catch(IOException e){check(e.getMessage().equals("WIM_FILE_PATH"));}
 final ByteArrayOutputStream data=new ByteArrayOutputStream();final int[] opened={0};FileBenchmark.File file=new FileBenchmark.File(){public OutputStream output(){opened[0]++;return data;}public InputStream input(){return new ByteArrayInputStream(data.toByteArray());}public void sync(){}};
 check(FileBenchmark.run(file,3,(a,b)->{}).contains("Size: 3 MiB"));check(data.size()==3*1048576);
 int before=opened[0];for(int size:new int[]{0,1025})try{FileBenchmark.run(file,size,(a,b)->{});throw new AssertionError("invalid size");}catch(IOException e){check(opened[0]==before);}
 String report=ReportText.render("EXTRACTED_NAMED\nsamples.zip\nsamples (2)\n10",false);check(report.contains("samples.zip")&&report.contains("samples (2)"));String arabic=ReportText.render("EXTRACTED_NAMED\nعربي.zip\nعربي\n10",true);check(arabic.contains("عربي.zip")&&!arabic.contains("Output folder"));
 System.out.println("PASS: "+n+" source-name/collision/Unicode/path-validation/custom-size/prewrite-rejection/translated-report checks");
 }
}
