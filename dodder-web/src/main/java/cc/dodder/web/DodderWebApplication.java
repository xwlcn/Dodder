package cc.dodder.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"cc.dodder.api"})
@SpringBootApplication
public class DodderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(DodderWebApplication.class, args);
	}

}

