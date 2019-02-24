package cc.dodder.torrent.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TorrentStoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TorrentStoreServiceApplication.class, args);
	}

}

