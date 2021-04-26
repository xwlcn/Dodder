package cc.dodder.torrent.store.repository.customer;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

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
	 * @param torrent
	 * @return void
	 */
	void index(Torrent torrent) throws IOException;

	/**
	* 存在则更新，不存在则插入
	*
	* @param torrents
	* @return void
	*/
	void upsert(Torrent torrents);

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
}
