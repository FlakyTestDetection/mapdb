package org.mapdb;

import org.mapdb.util.DataIO;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Output of serialization
 */
public class DataOutput2 extends OutputStream implements DataOutput{

    public byte[] buf;
    public int pos;
    public int sizeMask;


    public DataOutput2(){
        pos = 0;
        buf = new byte[128]; //PERF take hint from serializer for initial size
        sizeMask = 0xFFFFFFFF-(buf.length-1);
    }


    public byte[] copyBytes(){
        return Arrays.copyOf(buf, pos);
    }

    /**
     * make sure there will be enough space in buffer to write N bytes
     * @param n number of bytes which can be safely written after this method returns
     */
    public void ensureAvail(int n) {
        //$DELAY$
        n+=pos;
        if ((n&sizeMask)!=0) {
            grow(n);
        }
    }

    private void grow(int n) {
        //$DELAY$
        int newSize = Math.max(DataIO.nextPowTwo(n),buf.length);
        sizeMask = 0xFFFFFFFF-(newSize-1);
        buf = Arrays.copyOf(buf, newSize);
    }


    @Override
    public void write(final int b) throws IOException {
        ensureAvail(1);
        //$DELAY$
        buf[pos++] = (byte) b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b,0,b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        ensureAvail(len);
        //$DELAY$
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        ensureAvail(1);
        //$DELAY$
        buf[pos++] = (byte) (v ? 1 : 0);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        ensureAvail(1);
        //$DELAY$
        buf[pos++] = (byte) (v);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        ensureAvail(2);
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v >> 8));
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v));
    }

    @Override
    public void writeChar(final int v) throws IOException {
        ensureAvail(2);
        buf[pos++] = (byte) (v>>>8);
        buf[pos++] = (byte) (v);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        ensureAvail(4);
        buf[pos++] = (byte) (0xff & (v >> 24));
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v >> 16));
        buf[pos++] = (byte) (0xff & (v >> 8));
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v));
    }

    @Override
    public void writeLong(final long v) throws IOException {
        ensureAvail(8);
        buf[pos++] = (byte) (0xff & (v >> 56));
        buf[pos++] = (byte) (0xff & (v >> 48));
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v >> 40));
        buf[pos++] = (byte) (0xff & (v >> 32));
        buf[pos++] = (byte) (0xff & (v >> 24));
        //$DELAY$
        buf[pos++] = (byte) (0xff & (v >> 16));
        buf[pos++] = (byte) (0xff & (v >> 8));
        buf[pos++] = (byte) (0xff & (v));
        //$DELAY$
    }

    @Override
    public void writeFloat(final float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        writeUTF(s);
    }

    @Override
    public void writeChars(final String s) throws IOException {
        writeUTF(s);
    }

    @Override
    public void writeUTF(final String s) throws IOException {
        final int len = s.length();
        packInt(len);
        for (int i = 0; i < len; i++) {
            //$DELAY$
            int c = (int) s.charAt(i);
            packInt(c);
        }
    }

    public void packInt(int value) throws IOException {
        ensureAvail(5); //ensure worst case bytes

        // Optimize for the common case where value is small. This is particular important where our caller
        // is SerializerBase.SER_STRING.serialize because most chars will be ASCII characters and hence in this range.
        // credit Max Bolingbroke https://github.com/jankotek/MapDB/pull/489
        int shift = (value & ~0x7F); //reuse variable
        if (shift != 0) {
            shift = 31 - Integer.numberOfLeadingZeros(value);
            shift -= shift % 7; // round down to nearest multiple of 7
            while (shift != 0) {
                buf[pos++] = (byte) ((value >>> shift) & 0x7F);
                shift -= 7;
            }
        }
        buf[pos++] = (byte) ((value & 0x7F)| 0x80);
    }

    public void packIntBigger(int value) throws IOException {
        ensureAvail(5); //ensure worst case bytes
        int shift = 31-Integer.numberOfLeadingZeros(value);
        shift -= shift%7; // round down to nearest multiple of 7
        while(shift!=0){
            buf[pos++] = (byte) ((value>>>shift) & 0x7F);
            shift-=7;
        }
        buf[pos++] = (byte) ((value & 0x7F)|0x80);
    }

    public void packLong(long value) {
        ensureAvail(10); //ensure worst case bytes
        int shift = 63-Long.numberOfLeadingZeros(value);
        shift -= shift%7; // round down to nearest multiple of 7
        while(shift!=0){
            buf[pos++] = (byte) ((value>>>shift) & 0x7F);
            shift-=7;
        }
        buf[pos++] = (byte) ((value & 0x7F) | 0x80);
    }


    public void packLongArray(long[] array, int fromIndex, int toIndex  ) {
        for(int i=fromIndex;i<toIndex;i++){
            long value = array[i];
            ensureAvail(10); //ensure worst case bytes
            int shift = 63-Long.numberOfLeadingZeros(value);
            shift -= shift%7; // round down to nearest multiple of 7
            while(shift!=0){
                buf[pos++] = (byte) ((value>>>shift) & 0x7F);
                shift-=7;
            }
            buf[pos++] = (byte) ((value & 0x7F) | 0x80);
        }
    }
}
