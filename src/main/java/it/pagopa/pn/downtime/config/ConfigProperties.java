package it.pagopa.pn.downtime.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigProperties {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${spring_cors_origin}")
    private String CORSOrigin;

    public boolean isActiveProfileDev() {
        return "dev".equals(getActiveProfile());
    }
}
