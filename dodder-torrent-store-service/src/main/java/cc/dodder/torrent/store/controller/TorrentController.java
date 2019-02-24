package cc.dodder.torrent.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TorrentController {

    /**
     * 根据 info_hash 判断数据库是否已经存在
     *
     * @param infoHash
     * @return org.springframework.http.ResponseEntity
     */
    @GetMapping("/exist/hash/{infoHash}")
    public ResponseEntity existHash(@PathVariable("infoHash") String infoHash) {
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}
