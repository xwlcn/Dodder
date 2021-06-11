package cc.dodder.common.util.bencode;

import cc.dodder.common.util.JSONUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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

	public static Map<String, ?> decode1(byte[] bytes) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		BencodingInputStream bencode = new BencodingInputStream(stream)) {
			return bencode.readMap();
		} catch (Exception e) {
			return null;
		}
	}

	public static Map<String, ?> decode(byte[] bytes) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			 BencodeReader bencode = new BencodeReader(stream)) {
			return bencode.readDict();
		} catch (Exception e) {
			return null;
		}
	}

	public static Map<String, ?> decode(byte[] bytes, int offset, int length) {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes, offset, length);
			 BencodeReader bencode = new BencodeReader(stream)) {
			return bencode.readDict();
		} catch (Exception e) {
			return null;
		}
	}


	public static void main(String[] args) {

		String s = "d1:ei0e4:ipv44:aaaa12:complete_agoi1729e1:md11:upload_onlyi3e11:lt_donthavei7e12:ut_holepunchi4e11:ut_metadatai2e6:ut_pexi1e10:ut_commenti6e6:ut_bidi9e15:ut_bid_responsei10e17:ut_channel_state2i11e18:ut_payment_addressi12ee13:metadata_sizei14987e1:pi44454e4:reqqi255e1:v17:BitTorrent 7.10.52:ypi52166e6:yourip4:Y���e";

		Map map = decode1(s.getBytes(StandardCharsets.ISO_8859_1));
		System.out.println(JSONUtil.toJSONString(map));
		map = decode(s.getBytes());
		System.out.println(JSONUtil.toJSONString(map));
		/*try (FileInputStream stream = new FileInputStream(new File("E:\\web\\img1\\53.torrent"));
			 BencodingInputStream bencode = new BencodingInputStream(stream, "UTF-8", true)) {
			Map map = bencode.readMap();
			System.out.println();
		} catch (Exception e) {
		}*/
	}
}