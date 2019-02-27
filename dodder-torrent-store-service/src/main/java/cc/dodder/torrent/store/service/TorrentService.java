package cc.dodder.torrent.store.service;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.repository.TorrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TorrentService {

	@Autowired
	private TorrentRepository torrentRepository;

	public boolean existsById(String infoHash) {
		return torrentRepository.existsById(infoHash);
	}

	public void upsert(Torrent torrent) throws IOException {
		torrentRepository.upsert(torrent);
	}
}
