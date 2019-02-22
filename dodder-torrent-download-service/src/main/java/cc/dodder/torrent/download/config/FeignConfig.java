package cc.dodder.torrent.download.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

/***
 * Feign 配置类
 *
 * @author Mr.Xu
 * @since 2019-02-22 11:06
 **/
@Configuration
public class FeignConfig {

	@Bean
	public Retryer feignRetryer() {
		return new Retryer.Default(100, SECONDS.toMillis(1), 3);
	}
}
