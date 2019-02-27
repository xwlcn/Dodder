package cc.dodder.torrent.store;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.service.TorrentService;
import cc.dodder.torrent.store.stream.MessageStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@Slf4j
@EnableBinding(MessageStreams.class)
@EnableDiscoveryClient
@SpringBootApplication
public class TorrentStoreServiceApplication {

	@Autowired
	private TorrentService torrentService;

	public static void main(String[] args) {
		SpringApplication.run(TorrentStoreServiceApplication.class, args);
	}

	@StreamListener("torrent-message-in")
	public void handleTorrent(Torrent torrent) {
		try {
			log.debug("Save torrent to elasticsearch, info hash is {}", torrent.getInfoHash());
			torrentService.upsert(torrent);
		} catch (Exception e) {
			log.error("Insert or update torrent error: {}", e);
		}
	}

}

