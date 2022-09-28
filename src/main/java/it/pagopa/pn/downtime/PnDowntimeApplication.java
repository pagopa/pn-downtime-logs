package it.pagopa.pn.downtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableConfigurationProperties
@EnableSwagger2
public class PnDowntimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnDowntimeApplication.class, args);
	}



}
