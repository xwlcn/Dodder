package cc.dodder.api;

import cc.dodder.common.entity.Result;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;

public interface TorrentApi {

    /**
     * 根据 info_hash 判断数据库是否已经存在
     *
     * @param infoHash
     * @return org.springframework.http.ResponseEntity
     */
    Result existHash(String infoHash);

    /**
    * 根据条件搜索 Torrents
    *
    * @param request
    * @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
    */
    Result<TorrentPageVO> torrents(SearchRequest request);

    /**
    * 根据 infoHash 查找 Torrent
    *
    * @param infoHash
    * @return cc.dodder.common.entity.Torrent
    */
    Result<TorrentVO> findById(String infoHash);
}
