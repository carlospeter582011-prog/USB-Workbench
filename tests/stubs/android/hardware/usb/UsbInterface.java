package android.hardware.usb;
public class UsbInterface {public int protocol=80;public int getInterfaceClass(){return 8;}public int getInterfaceSubclass(){return 6;}public int getInterfaceProtocol(){return protocol;}public int getEndpointCount(){return 2;}public UsbEndpoint getEndpoint(int i){return new UsbEndpoint(i==0?128:0);}public int getId(){return 0;}}
