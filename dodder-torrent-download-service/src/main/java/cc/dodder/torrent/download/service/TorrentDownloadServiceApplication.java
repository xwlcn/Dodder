package cc.dodder.torrent.download.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class TorrentDownloadServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
	}

}

