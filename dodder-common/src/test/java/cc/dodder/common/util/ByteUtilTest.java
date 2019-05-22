package cc.dodder.common.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ByteUtilTest {

    @Test
    public void byteArrayToHex() {
        assertNull(ByteUtil.byteArrayToHex(new byte[0], false));
        assertEquals("%3e", ByteUtil.byteArrayToHex(new byte[]{ 62 }, true));
        assertEquals("3e", ByteUtil.byteArrayToHex(new byte[]{ 62 }));
    }

    @Test
    public void hexStringToBytes() {
        assertNull(ByteUtil.hexStringToBytes(""));
        assertArrayEquals(new byte[]{ 62 }, ByteUtil.hexStringToBytes("3e"));
    }

    @Test
    public void byteArrayToInt() {
        assertEquals(33818640, ByteUtil.byteArrayToInt(
                new byte[] { 2, 4, 8, 16, 32, 64 }));
    }

    @Test
    public void intToByteArray() {
        assertArrayEquals(new byte[]{ 0, 4, 8, -16 },
                ByteUtil.intToByteArray(264432));
    }
}
