package it.pagopa.pn.downtime.config.msclient;

import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.ApiClient;
import it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.api.TemplateApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TemplatesClientConfig {

    @Value("${pn.downtime-logs.templates-engine-base-url}")
    private String templatesEngineBaseUrl;

    @Bean
    @Primary
    public TemplateApi templateApiConfig(@Qualifier("restTemplate") RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(templatesEngineBaseUrl);
        return new TemplateApi(apiClient);
    }

}
