package org.usbworkbench;
public final class IsoPersistenceReportTest {
 public static void main(String[] args){int n=0;
 for(boolean ar:new boolean[]{false,true}){
  String result=ReportText.render("ISO_PERSISTENCE_OK\n3\n768\npersistence",ar);
  if(!result.contains("768")||!result.contains("persistence")||!result.contains(ar?"القسم":"Partition")||!result.contains(ar?"تختلف":"differs"))throw new AssertionError("result language");n++;
  if(result.contains(ar?"Partition:":"القسم:"))throw new AssertionError("mixed result");n++;
  for(String code:new String[]{"ISO_PERSISTENCE_LAYOUT","ISO_PERSISTENCE_GPT","ISO_PERSISTENCE_SPACE","ISO_PERSISTENCE_GEOMETRY","ISO_PERSISTENCE_INCOMPLETE owned_error"}){String e=ReportText.error(code,ar);if(!e.contains(ar?"تعذر إكمال":"Direct persistence could not finish")||!e.contains(code))throw new AssertionError("error language");n++;}
  for(String phase:new String[]{"iso_persistence_check","iso_write","iso_verify"}){String value=ReportText.phase(phase,ar);if(value.equals(phase)||ar&&!value.matches("(?s).*\\p{IsArabic}.*"))throw new AssertionError("phase language");n++;}
  if(ReportText.job("writePersistent",ar).equals("writePersistent"))throw new AssertionError("job name");n++;
 }
 if(!ReportText.render("ISO_PERSISTENCE_OK\n",false).equals("ISO_PERSISTENCE_OK\n"))throw new AssertionError("short historic record");n++;
 System.out.println("PASS: "+n+" direct persistence selected-language result/error/phase/job and short-record checks");
 }
}
