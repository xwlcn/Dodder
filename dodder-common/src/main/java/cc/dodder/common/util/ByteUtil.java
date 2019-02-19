package cc.dodder.common.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ByteUtil {

	/**
	 * byte数组转16进制字符串
	 * @param bytes	待转换byte数组
	 * @return		16进制字符串
	 */
	public static String byteArrayToHex(byte[] bytes) {
		return byteArrayToHex(bytes, false);
	}


	/**
	 * byte数组转16进制字符串
	 * @param bytes	待转换byte数组
	 * @param is
	 * @return		16进制字符串
	 */
	public static String byteArrayToHex(byte[] bytes, boolean is) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		StringBuffer sb = new StringBuffer(bytes.length * 2);
		String hexNumber;
		for (int x = 0; x < bytes.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & bytes[x]);

			if (is)
				sb.append("%");
			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return sb.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * int 转 byte 数组
	 * @param value	待转换 int
	 * @return		转换后的 byte 数组
	 */
	public static byte[] intToByteArray(int value) {
		return new byte[]{
				(byte) (value >>> 24),
				(byte) (value >>> 16),
				(byte) (value >>> 8),
				(byte) value};
	}

	/**
	 * byte[] 转 int
	 * @param bRefArr	待转换 byte[]
	 * @return			转换结果 int
	 */
	public static int byteArrayToInt(byte[] bRefArr) {
		int r = -1;
		try (ByteArrayInputStream bintput = new ByteArrayInputStream(bRefArr);
		     DataInputStream dintput = new DataInputStream(bintput)) {
			r = dintput.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}
}
