package org.usbworkbench;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
/** Only streams opened by this app job, never files discovered by scanning. */
public final class IoRegistry {
    private final Set<Closeable> active=new HashSet<>();
    private synchronized void add(Closeable c){active.add(c);}private synchronized void remove(Closeable c){active.remove(c);}
    public Closeable track(Closeable source){Closeable tracked=new Closeable(){final AtomicBoolean closed=new AtomicBoolean();public void close()throws IOException{if(closed.compareAndSet(false,true)){remove(this);source.close();}}};add(tracked);return tracked;}
    public InputStream input(InputStream source){InputStream stream=new FilterInputStream(source){final AtomicBoolean closed=new AtomicBoolean();@Override public void close()throws IOException{if(closed.compareAndSet(false,true)){remove(this);in.close();}}};add(stream);return stream;}
    public OutputStream output(OutputStream target){OutputStream stream=new FilterOutputStream(target){final AtomicBoolean closed=new AtomicBoolean();@Override public void write(byte[] b,int off,int n)throws IOException{out.write(b,off,n);}@Override public void close()throws IOException{if(closed.compareAndSet(false,true)){remove(this);out.close();}}};add(stream);return stream;}
    public void cancelAsync(){final Closeable[] streams;synchronized(this){streams=active.toArray(new Closeable[0]);}if(streams.length==0)return;Thread closer=new Thread(()->{for(Closeable stream:streams)try{stream.close();}catch(IOException ignored){}},"cancel-streams");closer.setDaemon(true);closer.start();}
    public synchronized int size(){return active.size();}
}
