package it.pagopa.pn.downtime.util;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import it.pagopa.pn.downtime.PnDowntimeApplication;
import org.springframework.boot.SpringApplication;

public class SpringApplicationUtils {

    public static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(PnDowntimeApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }
}
