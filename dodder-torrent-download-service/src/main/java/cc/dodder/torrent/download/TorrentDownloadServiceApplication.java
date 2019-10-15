package cc.dodder.torrent.download;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.SystemClock;
import cc.dodder.torrent.download.client.Constants;
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
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private ExecutorService downloadTasks;

	public static void main(String[] args) {
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
	}

	@StreamListener("download-message-in")
	public void handleMessage(DownloadMsgInfo msgInfo) {
		long now = SystemClock.now();
		//延迟3分钟下载
		if (now - msgInfo.getTimestamp() < 3 * 60 * 1000) {
			try {
				Thread.sleep(3 * 60 * 1000 - (now - msgInfo.getTimestamp()));
			} catch (InterruptedException e) {
			}
		}

		//丢进线程池进行下载
		downloadTasks.execute(new DownloadTask(msgInfo));
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void autoFinalize() {
		//定时强制回收 Finalizer 队列里的 Socket 对象（有个抽象父类重写了 finalize 方法，
		//频繁创建 Socket 会导致 Socket 得不到及时回收频繁发生 FGC）
		System.runFinalization();
	}

	@PostConstruct
	public void init() {
		/*downloadTasks = new ThreadPoolExecutor(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(nThreads + nThreads / 2), new ThreadPoolExecutor.DiscardPolicy());*/
		downloadTasks = Executors.newFixedThreadPool(nThreads);
	}

	@PreDestroy
	public void destroy() {
		downloadTasks.shutdownNow();
	}

}

