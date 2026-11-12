package org.usbworkbench;

/** Fixed publisher landing pages, never direct OS downloads or arbitrary bridge URLs. */
final class OfficialDownloads {
    private OfficialDownloads() {}
    static String page(String id) {
        if ("windows".equals(id)) return "https://www.microsoft.com/software-download/windows11";
        if ("ubuntu".equals(id)) return "https://ubuntu.com/download/desktop";
        if ("fedora".equals(id)) return "https://fedoraproject.org/workstation/download/";
        if ("ventoy".equals(id)) return "https://www.ventoy.net/en/download.html";
        return null;
    }
}
