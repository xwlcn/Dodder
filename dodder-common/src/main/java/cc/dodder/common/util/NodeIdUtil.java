package cc.dodder.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

/***
 * NodeId 生成工具
 *
 * @author Mr.Xu
 * @simce 2019-02-15 17:36
 **/
public class NodeIdUtil {

	private static MessageDigest messageDigest;
	private static Random random;
	final static char[] digits = {
			'0' , '1' , '2' , '3' , '4' , '5' ,
			'6' , '7' , '8' , '9' , 'a' , 'b' ,
			'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
			'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
			'o' , 'p' , 'q' , 'r' , 's' , 't' ,
			'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};
	static
	{
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		random = new Random(new Date().getTime());
	}

	public static byte[] createRandomNodeId() {
		byte[] bytes = new byte[20];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte)random.nextInt(256);
		}
		messageDigest.update(bytes);
		return messageDigest.digest();
	}

	public static byte[] getNeighbor(byte[] nodeId, byte[] info_hash) {
		byte[] bytes = new byte[20];
		System.arraycopy(info_hash, 0, bytes, 0, 10);
		System.arraycopy(nodeId, 10, bytes, 10, 10);
		return bytes;
	}

}
