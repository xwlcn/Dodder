package cc.dodder.dhtserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DhtServerApplication {


	public static void main(String[] args) {
		SpringApplication.run(DhtServerApplication.class, args);
	}

}

