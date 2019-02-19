package cc.dodder.common.util.bencode;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

public class BencodingOutputStream extends FilterOutputStream implements DataOutput {
	private final String encoding;

	public BencodingOutputStream(OutputStream var1) {
		this(var1, "UTF-8");
	}

	public BencodingOutputStream(OutputStream var1, String var2) {
		super(var1);
		if (var2 == null) {
			throw new NullPointerException("encoding");
		} else {
			this.encoding = var2;
		}
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void writeObject(Object var1) throws IOException {
		if (var1 == null) {
			this.writeNull();
		} else if (var1 instanceof byte[]) {
			this.writeBytes((byte[])((byte[])var1));
		} else if (var1 instanceof Boolean) {
			this.writeBoolean((Boolean)var1);
		} else if (var1 instanceof Character) {
			this.writeChar((Character)var1);
		} else if (var1 instanceof Number) {
			this.writeNumber((Number)var1);
		} else if (var1 instanceof String) {
			this.writeString((String)var1);
		} else if (var1 instanceof Collection) {
			this.writeCollection((Collection)var1);
		} else if (var1 instanceof Map) {
			this.writeMap((Map)var1);
		} else if (var1 instanceof Enum) {
			this.writeEnum((Enum)var1);
		} else if (var1.getClass().isArray()) {
			this.writeArray(var1);
		} else {
			this.writeCustom(var1);
		}

	}

	public void writeNull() throws IOException {
		throw new IOException("Null is not supported");
	}

	protected void writeCustom(Object var1) throws IOException {
		throw new IOException("Cannot bencode " + var1);
	}

	public void writeBytes(byte[] var1) throws IOException {
		this.writeBytes(var1, 0, var1.length);
	}

	public void writeBytes(byte[] var1, int var2, int var3) throws IOException {
		this.write(Integer.toString(var3).getBytes(this.encoding));
		this.write(58);
		this.write(var1, var2, var3);
	}

	public void writeBoolean(boolean var1) throws IOException {
		this.writeNumber(var1 ? BencodingUtils.TRUE : BencodingUtils.FALSE);
	}

	public void writeChar(int var1) throws IOException {
		this.writeString(Character.toString((char)var1));
	}

	public void writeByte(int var1) throws IOException {
		this.writeNumber((byte)var1);
	}

	public void writeShort(int var1) throws IOException {
		this.writeNumber((short)var1);
	}

	public void writeInt(int var1) throws IOException {
		this.writeNumber(var1);
	}

	public void writeLong(long var1) throws IOException {
		this.writeNumber(var1);
	}

	public void writeFloat(float var1) throws IOException {
		this.writeNumber(var1);
	}

	public void writeDouble(double var1) throws IOException {
		this.writeNumber(var1);
	}

	public void writeNumber(Number var1) throws IOException {
		String var2 = var1.toString();
		this.write(105);
		this.write(var2.getBytes(this.encoding));
		this.write(101);
	}

	public void writeString(String var1) throws IOException {
		this.writeBytes(var1.getBytes(this.encoding));
	}

	public void writeCollection(Collection<?> var1) throws IOException {
		this.write(108);
		Iterator var2 = var1.iterator();

		while(var2.hasNext()) {
			Object var3 = var2.next();
			this.writeObject(var3);
		}

		this.write(101);
	}

	public void writeMap(Map<?, ?> var1) throws IOException {
		if (!(var1 instanceof SortedMap)) {
			var1 = new TreeMap((Map)var1);
		}

		this.write(100);

		Object var5;
		for(Iterator var2 = ((Map)var1).entrySet().iterator(); var2.hasNext(); this.writeObject(var5)) {
			Map.Entry var3 = (Map.Entry)var2.next();
			Object var4 = var3.getKey();
			var5 = var3.getValue();
			if (var4 instanceof String) {
				this.writeString((String)var4);
			} else {
				this.writeBytes((byte[])((byte[])var4));
			}
		}

		this.write(101);
	}

	public void writeEnum(Enum<?> var1) throws IOException {
		this.writeString(var1.name());
	}

	public void writeArray(Object var1) throws IOException {
		this.write(108);
		int var2 = Array.getLength(var1);

		for(int var3 = 0; var3 < var2; ++var3) {
			this.writeObject(Array.get(var1, var3));
		}

		this.write(101);
	}

	public void writeBytes(String var1) throws IOException {
		this.writeString(var1);
	}

	public void writeChars(String var1) throws IOException {
		this.writeString(var1);
	}

	public void writeUTF(String var1) throws IOException {
		this.writeBytes(var1.getBytes("UTF-8"));
	}
}
