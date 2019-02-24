package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface TorrentRepository extends ElasticsearchCrudRepository<Torrent, String> {
}
