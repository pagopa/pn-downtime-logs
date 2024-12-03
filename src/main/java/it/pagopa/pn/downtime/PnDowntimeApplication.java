package it.pagopa.pn.downtime;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static it.pagopa.pn.downtime.util.SpringApplicationUtils.buildSpringApplicationWithListener;

@SpringBootApplication
@EnableScheduling
public class PnDowntimeApplication {

	public static void main(String[] args) {
		buildSpringApplicationWithListener().run(args);
	}

}
