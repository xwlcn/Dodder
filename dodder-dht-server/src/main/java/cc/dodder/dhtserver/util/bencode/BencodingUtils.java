package cc.dodder.dhtserver.util.bencode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class BencodingUtils {

	public static final String UTF_8 = "UTF-8";
	public static final int LENGTH_DELIMITER = 58;
	public static final int DICTIONARY = 100;
	public static final int LIST = 108;
	public static final int NUMBER = 105;
	public static final int EOF = 101;
	public static final Integer TRUE = 1;
	public static final Integer FALSE = 0;

	public static byte[] encode(Map map) {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
		     BencodingOutputStream bencode = new BencodingOutputStream(stream)) {
			bencode.writeMap(map);
			return stream.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}
	}

	public static Map<String, ?> decode(byte[] bytes) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		BencodingInputStream bencode = new BencodingInputStream(stream)) {
			return bencode.readMap();
		} catch (Exception e) {
			return null;
		}
	}
}