package cc.dodder.torrent.download;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.torrent.download.client.StoreFeignClient;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.task.DownloadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableEurekaClient
@EnableFeignClients
@EnableBinding(MessageStreams.class)
@SpringBootApplication
public class TorrentDownloadServiceApplication {

	@Value("${download.num.thread}")
	private int threads;
	@Autowired
	private StoreFeignClient storeFeignClient;

	private ExecutorService downloadTasks;

	public static void main(String[] args) {
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
		//PeerWireClient client = new PeerWireClient();
		//client.downloadMetadata(new InetSocketAddress("160.19.2.249", 52881), ByteUtil.hexStringToBytes("68273319565c1230b67b3dd21731a15d9f766f89"));
	}

	@StreamListener("message-in")
	public void handleMessage(DownloadMsgInfo msgInfo) {
		ResponseEntity response = storeFeignClient.existHash(ByteUtil.byteArrayToHex(msgInfo.getInfoHash()));
		if (response.getStatusCode() == HttpStatus.NO_CONTENT)
			return;
		//丢进线程池进行下载
		downloadTasks.submit(new DownloadTask(msgInfo));
	}

	@PostConstruct
	public void init() {
		downloadTasks = Executors.newFixedThreadPool(threads);
	}

	@PreDestroy
	public void destroy() {
		downloadTasks.shutdownNow();
	}

}

