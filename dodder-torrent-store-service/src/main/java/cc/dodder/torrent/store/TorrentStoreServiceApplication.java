package cc.dodder.torrent.store;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.service.TorrentService;
import cc.dodder.torrent.store.stream.MessageStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

@Slf4j
@EnableBinding(MessageStreams.class)
@SpringBootApplication
public class TorrentStoreServiceApplication {

	@Autowired
	private TorrentService torrentService;

	public static void main(String[] args) {
		SpringApplication.run(TorrentStoreServiceApplication.class, args);
	}

	@StreamListener("torrent-message-in")
	public void handleTorrent(Message<Torrent> message) {
		try {
			Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
			Torrent torrent = message.getPayload();
			log.debug("Save torrent to MongoDB, info hash is {}", torrent.getInfoHash());
			torrentService.upsert(torrent);
			//no error, execute acknowledge
			if (acknowledgment != null) {
				acknowledgment.acknowledge();
			}
		} catch (Exception e) {
			log.error("Insert or update torrent error: {}", e);
		}
	}

	@StreamListener("index-message-in")
	public void indexTorrent(Message<Torrent> message) {
		try {
			Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
			Torrent torrent = message.getPayload();
			log.debug("Index torrent to elasticsearch, info hash is {}", torrent.getInfoHash());
			torrentService.index(torrent);
			//no error, execute acknowledge
			if (acknowledgment != null) {
				acknowledgment.acknowledge();
			}
		} catch (Exception e) {
			log.error("Index torrent error: {}", e);
		}
	}

}

