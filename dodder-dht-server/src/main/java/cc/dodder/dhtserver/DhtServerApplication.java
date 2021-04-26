package cc.dodder.dhtserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class DhtServerApplication {

	@Autowired
	KafkaTemplate kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication.run(DhtServerApplication.class, args);
	}

}

