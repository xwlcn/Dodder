package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/***
 * 自定义扩展 Torrent Dao
 *
 * @author Mr.Xu
 * @date 2019-02-25 11:07
 **/
public interface TorrentDao {

	/**
	 * Elasticsearch 创建索引
	 *
	 * @param torrents
	 * @return void
	 */
	void index(List<Torrent> torrents);

	/**
	* 存在则更新，不存在则插入
	*
	* @param torrents
	* @return void
	*/
	void upsert(List<Torrent> torrents);

	/**
	 * 根据 id 查找
	 * @param id
	 * @return
	 */
	Optional<Torrent> findById(String id);

	/**
	* 分页搜索
	*
	* @param request, pageable
	* @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
	*/
	Page<Torrent> query(SearchRequest request, Pageable pageable);

	/**
	 * 相关推荐搜索
	 *
	 * @param torrent
	 * @param fields
	 * @param pageable
	 * @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
	 */
	Page<Torrent> searchSimilar(Torrent torrent, String[] fields, Pageable pageable);

	boolean existsById(String infoHash);

	Long countAll();
}
