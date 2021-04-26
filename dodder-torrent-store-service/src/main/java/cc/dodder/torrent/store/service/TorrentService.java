package cc.dodder.torrent.store.service;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.torrent.store.repository.TorrentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TorrentService {

	@Autowired
	private TorrentDao torrentDao;

	public boolean existsById(String infoHash) {
		return torrentDao.existsById(infoHash);
	}

	public void index(List<Torrent> torrents) {
		torrentDao.index(torrents);
	}

	public void upsert(List<Torrent> torrents) {
		torrentDao.upsert(torrents);
	}

	public Page<Torrent> query(SearchRequest request, Pageable pageable) {
		return torrentDao.query(request, pageable);
	}

	public Optional<Torrent> findById(String infoHash) {
		return torrentDao.findById(infoHash);
	}

	public Page<Torrent> findSimilar(Torrent torrent, Pageable pageable) {
		return torrentDao.searchSimilar(torrent, new String[] {"fileName"}, pageable);
	}
	@Transactional
	public void upsertAndIndex(List<Torrent> torrents) {
		torrentDao.upsert(torrents);
		torrentDao.index(torrents);
	}
}
