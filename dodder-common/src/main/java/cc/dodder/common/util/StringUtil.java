package cc.dodder.common.util;

import cc.dodder.common.entity.Node;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.entity.Tree;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/3/14.
 */
public class StringUtil {

	public static String getMiddleString(String text, String lStr, String rStr) {
		String result = "";
		int left = -1;
		int right = -1;
		left = text.indexOf(lStr);
		if (left >= 0) {
			right = text.indexOf(rStr, left);
		}

		if (left >= 0 && right > 0) {
			result = text.substring(left + lStr.length(), right);
		}
		return result;
	}

	public static String deleteCRLFOnce(String input) {
		return input.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
	}

	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();

		for (int i = 0; i < bs.length; i++) {
			int bit = (bs[i] & 0xF0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0xF;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			int n = str.indexOf(hexs[(2 * i)]) * 16;
			n += str.indexOf(hexs[(2 * i + 1)]);
			bytes[i] = ((byte) (n & 0xFF));
		}
		return new String(bytes);
	}

	public static String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	private static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1).byteValue();
		byte ret = (byte) (b0 | b1);
		return ret;
	}

	public static byte[] hexStr2Bytes(String src) {
		int m = 0;
		int n = 0;
		int l = src.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}
		return ret;
	}

	public static String str2Unicode(String strText) throws Exception {
		String strRet = "";

		for (int i = 0; i < strText.length(); i++) {
			char c = strText.charAt(i);
			int intAsc = c;
			String strHex = Integer.toHexString(intAsc);
			if (intAsc > 128) {
				strRet = strRet + "//u" + strHex;
			} else {
				strRet = strRet + "//u00" + strHex;
			}
		}
		return strRet;
	}

	public static String unicode2Str(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);

			String s1 = s.substring(2, 4) + "00";

			String s2 = s.substring(4);

			int n = Integer.valueOf(s1, 16).intValue() + Integer.valueOf(s2, 16).intValue();

			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}
	
	///清除html格式
	public static String delTagsFContent(String content){  
        return content.replaceAll("</?[^/?(br)|(p)|(div)][^><]*>","");
	} 
	
	public static String formatSize(double size) {

		int rank = 0;
		String rankchar = "Bytes";

		while (size > 1024) {
			size = size / 1024;
			rank++;
		}
		switch (rank) {
		case 1:
			rankchar = "KB";
			break;
		case 2:
			rankchar = "MB";
			break;
		case 3:
			rankchar = "GB";
			break;
		default:
			rankchar = "B";
		}
		return new DecimalFormat("0.##").format(size) + " " + rankchar;
	}

	/**
	* 获取子文件列表
	*
	* @param torrent
	* @return java.util.List<cc.dodder.common.entity.Node>
	*/
	public static List<Node> getFileList(Torrent torrent) {
		if (torrent.getFiles() == null) {   //单文件
			int pos = torrent.getFileName().lastIndexOf(".");
			String sname = torrent.getFileName();
			if (pos > 0)
				torrent.setFileName(sname.substring(0, pos));
			return Arrays.asList(new Node(1, 0, sname, torrent.getFileSize(), 1));
		}

		Tree tree = JSONUtil.parseObject(torrent.getFiles(), Tree.class);
		return tree.getLeafList();
	}

	public static String replaceSensitiveWords(String text) {
		return SensitiveWordsUtil.getInstance().replace(text);
	}

}
