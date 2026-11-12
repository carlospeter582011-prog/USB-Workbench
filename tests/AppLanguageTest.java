package org.usbworkbench;
public class AppLanguageTest{
 public static void main(String[] args){int n=0;String[][] cases={{"", "ar","ar"},{"","ar-EG","ar"},{"","ar_SA","ar"},{"","AR-ae","ar"},{"","en-US","en"},{"","fr-FR","en"},{"", "", "en"},{"en","ar-EG","en"},{"ar","en-US","ar"},{"bad","ar","ar"},{null,null,"en"},{null,"arz","en"}};for(String[] c:cases){if(!AppLanguage.resolve(c[0],c[1]).equals(c[2]))throw new AssertionError(java.util.Arrays.toString(c));n++;}System.out.println("PASS: "+n+" first-run language/tag/fallback/saved-choice cases");}
}
