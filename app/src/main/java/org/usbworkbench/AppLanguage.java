package org.usbworkbench;
/** First-run fallback only; a saved choice always wins. No device settings are changed. */
public final class AppLanguage {
 private AppLanguage(){}
 public static boolean isArabic(String languageTag){return languageTag!=null&&languageTag.replace('_','-').split("-",2)[0].equalsIgnoreCase("ar");}
 public static String resolve(String saved,String deviceLanguage){return "ar".equals(saved)||"en".equals(saved)?saved:isArabic(deviceLanguage)?"ar":"en";}
}
