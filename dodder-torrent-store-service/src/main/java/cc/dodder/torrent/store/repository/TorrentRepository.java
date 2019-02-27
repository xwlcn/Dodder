package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.repository.customer.TorrentDao;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TorrentRepository extends ElasticsearchRepository<Torrent, String>, TorrentDao {
}
