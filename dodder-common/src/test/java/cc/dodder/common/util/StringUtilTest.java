package cc.dodder.common.util;

import cc.dodder.common.entity.Node;
import cc.dodder.common.entity.Torrent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringUtilTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testByte2HexStr() {
        assertEquals("060200", StringUtil.byte2HexStr(new byte[]{ (byte)6, (byte)2, (byte)0 }));
    }

    @Test
    public void testStr2Unicode() throws Exception {
        assertEquals("//u005c", StringUtil.str2Unicode("\\"));
        assertEquals("//u0066//u006f//u006f", StringUtil.str2Unicode("foo"));
    }

    @Test
    public void testFormatSize() {
        assertEquals("512 GB", StringUtil.formatSize(0x1p+39));
        assertEquals("16 MB", StringUtil.formatSize(0x1p+24));
        assertEquals("1024 KB", StringUtil.formatSize(0x1p+20));
        assertEquals("4 B", StringUtil.formatSize(0x1p+72));
    }

    @Test
    public void testHexStr2Str() {
        assertEquals("foo", StringUtil.hexStr2Str("666F6F"));
    }

    @Test
    public void testGetMiddleString() {
        assertEquals("", StringUtil.getMiddleString(">>", ">", ">>???????"));

        thrown.expect(StringIndexOutOfBoundsException.class);
        StringUtil.getMiddleString("??>>>>>>?", ">>>>>>", "");
    }

    @Test
    public void testHexStr2Bytes() {
        assertArrayEquals(new byte[] {40, 74, 127}, StringUtil.hexStr2Bytes("284A7F"));
    }

    @Test
    public void testDeleteCRLFOnce() {
        assertEquals("fooBar", StringUtil.deleteCRLFOnce("fooBar"));
        assertEquals("foo\nBar", StringUtil.deleteCRLFOnce("foo\n\nBar"));
    }

    @Test
    public void testStr2HexStr() {
        assertEquals("666F6F", StringUtil.str2HexStr("foo"));
    }

    @Test
    public void testUnicode2Str() {
        assertEquals("foo", StringUtil.unicode2Str("//0066//006F//006F"));
    }

    @Test
    public void testGetEncoding() {
        assertEquals("US-ASCII", StringUtil.getEncoding(new byte[] {40, 74, 127}));
    }

    @Test
    public void testGetFileList() {
        assertEquals(new Node(1, 0, "foo.bar", 0L, 1), StringUtil.getFileList(
                new Torrent(null, null, "foo.bar", 0L, 0L, null)).get(0));
    }
}
