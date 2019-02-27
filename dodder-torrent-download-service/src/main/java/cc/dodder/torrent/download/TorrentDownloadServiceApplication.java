package cc.dodder.torrent.download;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.task.DownloadTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableScheduling
@RestController
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"cc.dodder.api"})
@EnableBinding(MessageStreams.class)
@SpringBootApplication
public class TorrentDownloadServiceApplication {

	@Value("${download.num.thread}")
	private int nThreads;
	@Autowired
	private StoreFeignClient storeFeignClient;

	private ExecutorService downloadTasks;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
		//PeerWireClient client = new PeerWireClient();
		//client.downloadMetadata(new InetSocketAddress("178.166.3.249", 23139), ByteUtil.hexStringToBytes("10d40d4b047a91c813e8c6ffbfb6adf45c2df5f6"));
	}

	@StreamListener("download-message-in")
	public void handleMessage(DownloadMsgInfo msgInfo) {
		//丢进线程池进行下载
		downloadTasks.execute(new DownloadTask(msgInfo));
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void autoFinalize() {
		System.runFinalization();
	}

	@PostConstruct
	public void init() {
		downloadTasks = new ThreadPoolExecutor(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(nThreads + 100), new ThreadPoolExecutor.DiscardPolicy());
	}

	@PreDestroy
	public void destroy() {
		downloadTasks.shutdownNow();
	}

}

