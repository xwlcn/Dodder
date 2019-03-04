package cc.dodder.torrent.download.client;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/***
 * Piped Stream 封装类
 *
 * @author Mr.Xu
 * @date 2019-03-01 19:08
 **/
public class PipedStream {

	private PipedOutputStream writeStream;
	private PipedInputStream readStream;

	public PipedStream() throws IOException {
		writeStream = new PipedOutputStream();
		readStream = new PipedInputStream(22 * 1024);
		readStream.connect(writeStream);
	}

	public int available() throws IOException {
		return readStream.available();
	}

	public void read(byte[] b, int off, int len) throws IOException {
		readStream.read(b, off, len);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		writeStream.write(b, off, len);
		writeStream.flush();
	}

	public void clear() throws IOException {
		writeStream.flush();
		readStream.skip(readStream.available());
	}
}
