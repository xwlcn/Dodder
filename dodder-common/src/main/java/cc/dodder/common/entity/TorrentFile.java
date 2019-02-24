package cc.dodder.common.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.eclipse.ecf.protocol.bittorrent.internal.encode.BEncodedDictionary;
import org.eclipse.ecf.protocol.bittorrent.internal.encode.Decode;

/***
 * Torrent 种子文件信息，改自 eclipse ecf torrent 协议 TorrentFile 类
 *
 * @author Mr.Xu
 * @since 2019-02-24 20:23
 **/
public class TorrentFile {
	static MessageDigest shaDigest;
	private final String[] filenames;
	private final String[] pieces;
	private final long[] lengths;
	private final byte[] torrentData;
	private final ByteBuffer buffer;
	private final BEncodedDictionary dictionary;
	private final String tracker;
	private final String infoHash;
	private final String hexHash;
	private File file;
	private String name;
	private long total;
	private final int pieceLength;
	private final int numPieces;

	/**
	 * Wild China 2xBD50/DISC_1/CERTIFICATE
	 * Wild China 2xBD50/DISC_1/disc.inf
	 */
	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException var1) {
			throw new RuntimeException(var1);
		}
	}

	public TorrentFile(String metadata) throws IllegalArgumentException, IOException {

		this.dictionary = Decode.bDecode(metadata);
		this.torrentData = this.dictionary.toString().getBytes(StandardCharsets.ISO_8859_1);
		this.tracker = (String)this.dictionary.get("announce");
		BEncodedDictionary info = (BEncodedDictionary)this.dictionary.get("info");
		List list = (List)info.get("files");
		if (list != null) {
			this.filenames = new String[list.size()];
			this.lengths = new long[this.filenames.length];
			this.total = 0L;

			for(int i = 0; i < this.filenames.length; ++i) {
				BEncodedDictionary aDictionary = (BEncodedDictionary)list.get(i);
				this.lengths[i] = (Long)aDictionary.get("length");
				this.total += this.lengths[i];
				List aList = (List)aDictionary.get("path");
				StringBuffer buffer = new StringBuffer();
				synchronized(buffer) {
					int j = 0;

					while(true) {
						if (j >= aList.size()) {
							break;
						}

						buffer.append(aList.get(j)).append(File.separator);
						++j;
					}
				}

				this.filenames[i] = buffer.toString();
			}
		} else {
			this.lengths = new long[]{(Long)info.get("length")};
			this.total = this.lengths[0];
			this.filenames = new String[]{(String)info.get("name")};
		}
		this.name = this.filenames[0].split("/")[0];
		this.pieceLength = ((Long)info.get("piece length")).intValue();
		this.buffer = ByteBuffer.allocate(this.pieceLength);
		String shaPieces = (String)info.get("pieces");
		this.pieces = new String[shaPieces.length() / 20];

		for(int i = 0; i < this.pieces.length; ++i) {
			this.pieces[i] = shaPieces.substring(i * 20, i * 20 + 20);
		}

		this.numPieces = this.pieces.length;
		this.infoHash = new String(shaDigest.digest(info.toString().getBytes(StandardCharsets.ISO_8859_1)), StandardCharsets.ISO_8859_1);
		byte[] bytes = this.infoHash.getBytes(StandardCharsets.ISO_8859_1);
		StringBuffer hash = new StringBuffer(40);

		for(int i = 0; i < bytes.length; ++i) {
			if (-1 < bytes[i] && bytes[i] < 16) {
				hash.append('0');
			}

			hash.append(Integer.toHexString(255 & bytes[i]));
		}

		this.hexHash = hash.toString();
	}

	private boolean hashCheckFile() throws FileNotFoundException, IOException {
		int remainder = (int)(this.file.length() % (long)this.pieceLength);
		int count = 0;

		for(FileChannel channel = (new FileInputStream(this.file)).getChannel(); channel.read(this.buffer) == this.pieceLength; ++count) {
			this.buffer.rewind();
			if (!this.pieces[count].equals(new String(shaDigest.digest(this.buffer.array()), StandardCharsets.ISO_8859_1))) {
				return false;
			}
		}

		this.buffer.rewind();
		shaDigest.update(this.buffer.array(), 0, remainder);
		return this.pieces[this.pieces.length - 1].equals(new String(shaDigest.digest(), StandardCharsets.ISO_8859_1));
	}

	private boolean hashCheckFolder() throws FileNotFoundException, IOException {
		int read = 0;
		int count = 0;

		for(int i = 0; i < this.filenames.length; ++i) {
			File download = new File(this.file.getAbsolutePath(), this.filenames[i]);

			for(FileChannel channel = (new FileInputStream(download)).getChannel(); (read += channel.read(this.buffer)) == this.pieceLength; read = 0) {
				this.buffer.rewind();
				if (!this.pieces[count].equals(new String(shaDigest.digest(this.buffer.array()), StandardCharsets.ISO_8859_1))) {
					return false;
				}

				++count;
			}
		}

		this.buffer.rewind();
		shaDigest.update(this.buffer.array(), 0, read);
		return this.pieces[this.pieces.length - 1].equals(new String(shaDigest.digest(), StandardCharsets.ISO_8859_1));
	}

	public boolean validate() throws IllegalStateException, IOException {
		if (this.file == null) {
			throw new IllegalStateException("The target file for this torrent has not yet been set");
		} else {
			return this.file.isDirectory() ? this.hashCheckFolder() : this.hashCheckFile();
		}
	}

	public void setTargetFile(File file) throws IllegalArgumentException {
		if (file == null) {
			throw new IllegalArgumentException("The file cannot be null");
		} else if (this.filenames.length == 1 && file.isDirectory()) {
			throw new IllegalArgumentException("This torrent is downloading a file, the actual file should be set here and not a directory");
		} else {
			this.file = file;
		}
	}

	public String getInfoHash() {
		return this.infoHash;
	}

	public String getHexHash() {
		return this.hexHash;
	}

	public long[] getLengths() {
		return this.lengths;
	}

	public int getPieceLength() {
		return this.pieceLength;
	}

	public String getTracker() {
		return this.tracker;
	}

	public String[] getPieces() {
		return this.pieces;
	}

	public int getNumPieces() {
		return this.numPieces;
	}

	public String[] getFilenames() {
		return this.filenames;
	}

	public String getName() {
		return this.name;
	}

	public File getTargetFile() {
		return this.file;
	}

	public boolean isMultiFile() {
		return this.lengths.length != 1;
	}

	public long getTotalLength() {
		return this.total;
	}

	public void save(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(this.torrentData);
		fos.flush();
		fos.close();
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else {
			return other instanceof TorrentFile ? this.infoHash.equals(((TorrentFile)other).infoHash) : false;
		}
	}

	public int hashCode() {
		return this.infoHash.hashCode();
	}

	public Optional<Torrent> toEntity() {
		Torrent torrent = Torrent.builder()
				.infoHash(infoHash)
				.build();
		return Optional.ofNullable(torrent);
	}
}
