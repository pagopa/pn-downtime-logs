package it.pagopa.pn.downtime.util;

import java.util.UUID;

public class RandomUtils {
	
    /**
     * Generate a random string with Root=<random_UUID_string> format
     * @return The randomly generated string representing a trace id
     * */
    public String generateRandomTraceId() {
        return "Root=" + UUID.randomUUID().toString().toLowerCase();
    }

}
