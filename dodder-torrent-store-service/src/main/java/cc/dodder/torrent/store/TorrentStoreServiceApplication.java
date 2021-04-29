package cc.dodder.torrent.store;

import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.service.TorrentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class TorrentStoreServiceApplication {

	@Autowired
	private TorrentService torrentService;
	public static Boolean filterXxx;
	@Value("${dodder.filter-sensitive-torrent}")
	public void setFilterXxx(Boolean filterXxx) {
		TorrentStoreServiceApplication.filterXxx = filterXxx;
	}
	public static void main(String[] args) {
		SpringApplication.run(TorrentStoreServiceApplication.class, args);
	}


	@Bean
	public Consumer<Message<List<Torrent>>> store() {
		return message -> {
			Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
			List<Torrent> torrents = message.getPayload();
			//save to mongodb and index to es
			torrentService.upsertAndIndex(torrents);
			//no error, execute acknowledge
			if (acknowledgment != null) {
				acknowledgment.acknowledge();
			}
		};
	}
}
