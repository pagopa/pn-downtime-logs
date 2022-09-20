package it.pagopa.pn.downtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class PnDowntimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnDowntimeApplication.class, args);
	}



}
