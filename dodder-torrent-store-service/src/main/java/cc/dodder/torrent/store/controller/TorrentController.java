package cc.dodder.torrent.store.controller;

import cc.dodder.common.entity.Result;
import cc.dodder.torrent.store.service.TorrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TorrentController {

    @Autowired
    private TorrentService torrentService;

    /**
     * 根据 info_hash 判断数据库是否已经存在
     *
     * @param infoHash
     * @return org.springframework.http.ResponseEntity
     */
    @GetMapping("/exist/hash/{infoHash}")
    public Result existHash(@PathVariable("infoHash") String infoHash) {
        if (torrentService.existsById(infoHash))
            return new Result(HttpStatus.NO_CONTENT.value());
        return new Result(HttpStatus.NOT_FOUND.value());
    }
}
