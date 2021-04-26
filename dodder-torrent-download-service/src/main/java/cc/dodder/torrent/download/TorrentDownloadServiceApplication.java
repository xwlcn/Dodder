package cc.dodder.torrent.download;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.SensitiveWordsUtil;
import cc.dodder.common.util.SystemClock;
import cc.dodder.torrent.download.client.Constants;
import cc.dodder.torrent.download.task.BlockingExecutor;
import cc.dodder.torrent.download.task.DownloadTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@EnableScheduling
@RestController
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"cc.dodder.api"})
@SpringBootApplication
public class TorrentDownloadServiceApplication {

	@Value("${download.num.thread}")
	private int nThreads;

	private BlockingExecutor blockingExecutor;
	public static Boolean filterSensitiveWords;

	@Value("${download.enable-filter-sensitive-words}")
	public void setFilterSensitiveWords(Boolean filterSensitiveWords) {
		TorrentDownloadServiceApplication.filterSensitiveWords = filterSensitiveWords;
	}

	public static void main(String[] args) {
		SensitiveWordsUtil.getInstance();	//init it
		SpringApplication.run(TorrentDownloadServiceApplication.class, args);
	}

	@Bean
	public Consumer<List<DownloadMsgInfo>> handle() {
		return list -> {
			//submit to blocking executor
			try {
				for(DownloadMsgInfo msgInfo: list) {
					//由于下载线程消费的速度总是比 dht server 生产的速度慢，所以要做一下时间限制，否则程序越跑越慢
					if (SystemClock.now() - msgInfo.getTimestamp() >= Constants.MAX_LOSS_TIME) {
						continue;
					}
					blockingExecutor.execute(new DownloadTask(msgInfo));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}

	/*@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void autoFinalize() {
		//定时强制回收 Finalizer 队列里的 Socket 对象（有个抽象父类重写了 finalize 方法，
		//频繁创建 Socket 会导致 Socket 得不到及时回收频繁发生 FGC）
		System.runFinalization();
	}*/

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