package cc.dodder.torrent.store.service;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.torrent.store.repository.TorrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class TorrentService {

	@Autowired
	private TorrentRepository torrentRepository;

	public boolean existsById(String infoHash) {
		return torrentRepository.existsById(infoHash);
	}

	public void index(Torrent torrent) throws IOException {
		torrentRepository.index(torrent);
	}

	public void upsert(Torrent torrent) {
		torrentRepository.upsert(torrent);
	}

	public Page<Torrent> query(SearchRequest request, Pageable pageable) {
		return torrentRepository.query(request, pageable);
	}

	public Optional<Torrent> findById(String infoHash) {
		return torrentRepository.findById(infoHash);
	}

	public Page<Torrent> findSimilar(Torrent torrent, Pageable pageable) {
		return torrentRepository.searchSimilar(torrent, new String[] {"fileName"}, pageable);
	}
}
