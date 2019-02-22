package cc.dodder.dhtserver;

import cc.dodder.dhtserver.stream.MessageStreams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableBinding(MessageStreams.class)
@EnableScheduling
@SpringBootApplication
public class DhtServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DhtServerApplication.class, args);
	}

}

