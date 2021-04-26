package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.repository.customer.TorrentDao;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TorrentRepository extends MongoRepository<Torrent, String>, TorrentDao {
}
