package cc.dodder.api;

import cc.dodder.common.entity.Result;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;
import cc.dodder.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "torrent-store-service", configuration = FeignConfig.class)
public interface StoreFeignClient {

    /**
     * 根据 info_hash 判断数据库是否已经存在
     *
     * @param infoHash
     * @return org.springframework.http.ResponseEntity
     */
    @GetMapping("/exist/hash/{infoHash}")
    Result existHash(@PathVariable("infoHash") String infoHash);

    /**
    * 根据条件搜索 Torrents
    *
    * @param request
    * @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
    */
    @PostMapping("/torrents")
    Result<TorrentPageVO> torrents(@RequestBody SearchRequest request);

    /**
    * 根据 infoHash 查找 Torrent
    *
    * @param infoHash
    * @return cc.dodder.common.entity.Torrent
    */
    @RequestMapping("/torrent/{infoHash}")
    Result<TorrentVO> findById(@PathVariable("infoHash") String infoHash);
}
