package cc.dodder.torrent.store.repository.customer;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

/***
 * 自定义扩展 Torrent Dao
 *
 * @author Mr.Xu
 * @date 2019-02-25 11:07
 **/
public interface TorrentDao {

	/**
	* 存在则更新，不存在则插入
	*
	* @param torrent
	* @return void
	*/
	void upsert(Torrent torrent) throws IOException;

	/**
	* 分页搜索
	*
	* @param request, pageable
	* @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
	*/
	Page<Torrent> query(SearchRequest request, Pageable pageable);
}
