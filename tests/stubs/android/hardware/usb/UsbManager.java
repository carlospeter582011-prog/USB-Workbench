package android.hardware.usb;
public class UsbManager {public final UsbDeviceConnection connection;public UsbManager(UsbDeviceConnection c){connection=c;}public UsbDeviceConnection openDevice(UsbDevice d){return connection;}}
