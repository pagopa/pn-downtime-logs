package it.pagopa.pn.downtime;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PnDowntimeApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PnDowntimeApplication.class);
		app.addListeners(new TaskIdApplicationListener());
		app.run(args);
	}

}
