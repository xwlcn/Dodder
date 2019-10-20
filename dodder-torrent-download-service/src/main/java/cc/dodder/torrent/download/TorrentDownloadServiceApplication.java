package cc.dodder.torrent.download;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.task.BlockingExecutor;
import cc.dodder.torrent.download.task.DownloadTask;
import lombok.extern.slf4j.Slf4j;
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

	private BlockingExecutor blockingExecutor;

	public static void main(String[] args) {
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
	}

	@StreamListener("download-message-in")
	public void handleMessage(DownloadMsgInfo msgInfo) {
		//submit to blocking executor
		try {
			blockingExecutor.execute(new DownloadTask(msgInfo));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void autoFinalize() {
		//定时强制回收 Finalizer 队列里的 Socket 对象（有个抽象父类重写了 finalize 方法，
		//频繁创建 Socket 会导致 Socket 得不到及时回收频繁发生 FGC）
		System.runFinalization();
	}

	@PostConstruct
	public void init() {
		//max task bound 5000
		blockingExecutor = new BlockingExecutor(Executors.newFixedThreadPool(nThreads), 5000);
	}

	@PreDestroy
	public void destroy() {
		blockingExecutor.shutdownNow();
	}

}

