package cc.dodder.torrent.store.repository.customer;

import cc.dodder.common.entity.Torrent;

import java.io.IOException;

/***
 * 自定义扩展 Torrent Dao
 *
 * @author Mr.Xu
 * @since 2019-02-25 11:07
 **/
public interface TorrentDao {

	/**
	* 存在则更新，不存在则插入
	*
	* @param torrent
	* @return void
	*/
	void upsert(Torrent torrent) throws IOException;
}
