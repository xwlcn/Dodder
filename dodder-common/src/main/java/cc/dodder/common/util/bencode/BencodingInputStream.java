package cc.dodder.common.util.bencode;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BencodingInputStream extends FilterInputStream implements DataInput {
	private final String encoding;
	private final boolean decodeAsString;

	public BencodingInputStream(InputStream var1) {
		this(var1, "UTF-8", false);
	}

	public BencodingInputStream(InputStream var1, String var2) {
		this(var1, var2, false);
	}

	public BencodingInputStream(InputStream var1, boolean var2) {
		this(var1, "UTF-8", var2);
	}

	public BencodingInputStream(InputStream var1, String var2, boolean var3) {
		super(var1);
		if (var2 == null) {
			throw new NullPointerException("encoding");
		} else {
			this.encoding = var2;
			this.decodeAsString = var3;
		}
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean isDecodeAsString() {
		return this.decodeAsString;
	}

	public Object readObject() throws IOException {
		int var1 = this.read();
		if (var1 == -1) {
			throw new EOFException();
		} else {
			return this.readObject(var1);
		}
	}

	protected Object readObject(int var1) throws IOException {
		if (var1 == 100) {
			return this.readMap0();
		} else if (var1 == 108) {
			return this.readList0();
		} else if (var1 == 105) {
			return this.readNumber0();
		} else if (isDigit(var1)) {
			byte[] var2 = this.readBytes(var1);
			return this.decodeAsString ? new String(var2, this.encoding) : var2;
		} else {
			return this.readCustom(var1);
		}
	}

	protected Object readCustom(int var1) throws IOException {
		throw new IOException("Not implemented: " + var1);
	}

	public byte[] readBytes() throws IOException {
		int var1 = this.read();
		if (var1 == -1) {
			throw new EOFException();
		} else {
			return this.readBytes(var1);
		}
	}

	private byte[] readBytes(int var1) throws IOException {
		StringBuilder var2 = new StringBuilder();
		var2.append((char)var1);

		while((var1 = this.read()) != 58) {
			if (var1 == -1) {
				throw new EOFException();
			}

			var2.append((char)var1);
		}

		int var3 = Integer.parseInt(var2.toString());
		byte[] var4 = new byte[var3];
		this.readFully(var4);
		return var4;
	}

	public String readString() throws IOException {
		return this.readString(this.encoding);
	}

	private String readString(String var1) throws IOException {
		return new String(this.readBytes(), var1);
	}

	public <T extends Enum<T>> T readEnum(Class<T> var1) throws IOException {
		return Enum.valueOf(var1, this.readString());
	}

	public char readChar() throws IOException {
		return this.readString().charAt(0);
	}

	public boolean readBoolean() throws IOException {
		return this.readInt() != 0;
	}

	public byte readByte() throws IOException {
		return this.readNumber().byteValue();
	}

	public short readShort() throws IOException {
		return this.readNumber().shortValue();
	}

	public int readInt() throws IOException {
		return this.readNumber().intValue();
	}

	public float readFloat() throws IOException {
		return this.readNumber().floatValue();
	}

	public long readLong() throws IOException {
		return this.readNumber().longValue();
	}

	public double readDouble() throws IOException {
		return this.readNumber().doubleValue();
	}

	public Number readNumber() throws IOException {
		int var1 = this.read();
		if (var1 == -1) {
			throw new EOFException();
		} else if (var1 != 105) {
			throw new IOException();
		} else {
			return this.readNumber0();
		}
	}

	private Number readNumber0() throws IOException {
		StringBuilder var1 = new StringBuilder();
		boolean var2 = false;

		int var6;
		for(boolean var3 = true; (var6 = this.read()) != 101; var1.append((char)var6)) {
			if (var6 == -1) {
				throw new EOFException();
			}

			if (var6 == 46) {
				var2 = true;
			}
		}

		try {
			if (var2) {
				return new BigDecimal(var1.toString());
			} else {
				return new BigInteger(var1.toString());
			}
		} catch (NumberFormatException var5) {
			throw new IOException("NumberFormatException", var5);
		}
	}

	public List<?> readList() throws IOException {
		int var1 = this.read();
		if (var1 == -1) {
			throw new EOFException();
		} else if (var1 != 108) {
			throw new IOException();
		} else {
			return this.readList0();
		}
	}

	private List<?> readList0() throws IOException {
		ArrayList var1 = new ArrayList();
		boolean var2 = true;

		int var3;
		while((var3 = this.read()) != 101) {
			if (var3 == -1) {
				throw new EOFException();
			}

			var1.add(this.readObject(var3));
		}

		return var1;
	}

	public Map<String, ?> readMap() throws IOException {
		int var1 = this.read();
		if (var1 == -1) {
			throw new EOFException();
		} else if (var1 != 100) {
			throw new IOException();
		} else {
			return this.readMap0();
		}
	}

	private Map<String, ?> readMap0() throws IOException {
		TreeMap var1 = new TreeMap();
		boolean var2 = true;

		int var5;
		while((var5 = this.read()) != 101) {
			if (var5 == -1) {
				throw new EOFException();
			}

			String var3 = new String(this.readBytes(var5), this.encoding);
			Object var4 = this.readObject();
			var1.put(var3, var4);
		}

		return var1;
	}

	public void readFully(byte[] var1) throws IOException {
		this.readFully(var1, 0, var1.length);
	}

	public void readFully(byte[] var1, int var2, int var3) throws IOException {
		int var5;
		for(int var4 = 0; var4 < var3; var4 += var5) {
			var5 = this.read(var1, var4, var3 - var4);
			if (var5 == -1) {
				throw new EOFException();
			}
		}

	}

	public String readLine() throws IOException {
		return this.readString();
	}

	public int readUnsignedByte() throws IOException {
		return this.readByte() & 255;
	}

	public int readUnsignedShort() throws IOException {
		return this.readShort() & '\uffff';
	}

	public String readUTF() throws IOException {
		return this.readString("UTF-8");
	}

	public int skipBytes(int var1) throws IOException {
		return (int)this.skip((long)var1);
	}

	private static boolean isDigit(int var0) {
		return 48 <= var0 && var0 <= 57;
	}
}
