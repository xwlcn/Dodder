package cc.dodder.api;

import cc.dodder.common.entity.Result;
import cc.dodder.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
