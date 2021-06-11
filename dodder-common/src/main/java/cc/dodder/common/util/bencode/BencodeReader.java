package cc.dodder.common.util.bencode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BencodeReader implements Closeable {
    private PushbackInputStream input;

    /**
     * Creates a new <code>BencodeReader</code> out of <code>input</code>. The
     * <code>BencodeReader</code> will only read the exact value(s) one
     * requests, and will not buffer values.
     *
     * @since 0.1.0
     * @param input the <code>InputStream</code> to read from
     */
    public BencodeReader(InputStream input) {
        this.input = new PushbackInputStream(input, 1);
    }

    /**
     * Closes the underlying <code>InputStream</code>.
     *
     * @exception IOException if an IO exception occurs when closing
     */
    public void close() throws IOException {
        input.close();
    }

    private int forceRead() throws IOException {
        return input.read();
    }

    private int peek() throws IOException {
        int val = input.read();
        if (val == -1) {
            throw new EOFException();
        }
        input.unread(val);
        return val;
    }

    /**
     * Reads a bencoded long from the <code>InputStream</code>.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     */
    public long readLong() throws IOException {
        int initial = forceRead();
        if (initial != 'i') {
            throw new EOFException("Bencoded integer must start with 'i'");
        }
        long val = 0;
        boolean negative = false, readDigit = false;
        while (true) {
            int cur = forceRead();
            if (cur == '-' && !negative && !readDigit) {
                negative = true;
            }
            else if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == 'e') {
                if (readDigit) {
                    return negative ? -val : val;
                } else {
                    throw new EOFException("Bencoded integer must contain at least one digit");
                }
            }
            else {
                throw new EOFException();
            }
        }
    }

    // len is a positive ascii base-10 encoded integer, immediately followed by
    // a colon
    private int readLen() throws IOException {
        boolean readDigit = false;
        int val = 0;
        while (true) {
            int cur = forceRead();
            if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == ':') {
                if (readDigit) {
                    return val;
                } else {
                    throw new EOFException("Bencode-length must contain at least one digit");
                }
            }
            else {
                throw new EOFException();
            }
        }
    }

    /**
     * Reads a bencoded <code>String</code> from the <code>InputStream</code>.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     */
    public String readString() throws IOException {
        int len = readLen();
        // now read until we have the entire thing
        byte[] bs = new byte[len];
        if (len == 0) { // edge case where last value is an empty string
            return "";
        }
        int off = input.read(bs);
        if (off == -1) {
            throw new EOFException();
        }
        while (off != len) {
            int more = input.read(bs, off, len - off);
            if (more == -1) {
                throw new EOFException();
            }
            off += more;
        }
        return new String(bs, StandardCharsets.ISO_8859_1);
    }

    public byte[] readBytes() throws IOException {
        int len = readLen();
        // now read until we have the entire thing
        byte[] bs = new byte[len];
        if (len == 0) { // edge case where last value is an empty string
            return new byte[0];
        }
        int off = input.read(bs);
        if (off == -1) {
            throw new EOFException();
        }
        while (off != len) {
            int more = input.read(bs, off, len - off);
            if (more == -1) {
                throw new EOFException();
            }
            off += more;
        }
        return bs;
    }



    /**
     * Reads a bencoded <code>List</code> from the <code>InputStream</code>. The
     * <code>List</code> may contain lists and maps itself.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     */
    public List<Object> readList() throws IOException {
        int initial = forceRead();
        if (initial != 'l') {
            throw new EOFException("Bencoded list must start with 'l'");
        }
        ArrayList<Object> al = new ArrayList<Object>();
        while (peek() != 'e') {
            Object val = read();
            if (val == null) {
                throw new EOFException();
            }
            al.add(val);
        }
        forceRead(); // remove 'e' that we peeked
        return al;
    }

    /**
     * Reads a bencoded <code>Map</code> (dict in the specification) from the
     * <code>InputStream</code>. The <code>Map</code> may contain lists and maps
     * itself.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     */
    public Map<String, Object> readDict() throws IOException {
        int initial = forceRead();
        if (initial != 'd') {
            throw new EOFException("Bencoded dict must start with 'd'");
        }
        HashMap<String, Object> hm = new HashMap<String, Object>();
        while (peek() != 'e') {
            String key = readString();
            Object val = read();
            if (val == null) {
                throw new EOFException();
            }
            hm.put(key, val);
        }
        forceRead(); // read 'e' that we peeked
        return hm;
    }

    /**
     * Reads a bencoded value from the <code>InputStream</code>. If the stream
     * is empty, <code>null</code> is returned instead of an error.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     */
    public Object read() throws IOException {
        int t = input.read();
        if (t == -1) {
            return null;
        }
        input.unread(t);
        switch (t) {
            case 'i':
                return readLong();
            case 'l':
                return readList();
            case 'd':
                return readDict();
            default:
                return readBytes();
        }
    }
}
