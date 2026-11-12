package org.usbworkbench;
import java.io.IOException;
public interface BlockDevice extends AutoCloseable {
    int blockSize(); long blockCount();
    void read(long lba, byte[] data, int length) throws IOException;
    void write(long lba, byte[] data, int length) throws IOException;
    void sync() throws IOException;
    void close();
}
