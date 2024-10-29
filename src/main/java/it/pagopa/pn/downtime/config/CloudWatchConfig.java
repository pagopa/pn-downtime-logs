package it.pagopa.pn.downtime.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.net.URI;

@Configuration
public class CloudWatchConfig {

    public CloudWatchConfig(AwsConfig props) {
        this.props = props;
    }

    private final AwsConfig props;

    @Value("${amazon.cloudwatch.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.cloudwatch.secretkey}")
    private String amazonAWSSecretKey;

    @Bean
    public CloudWatchAsyncClient createCloudWatchAsyncClient() {
        if (StringUtils.isNotBlank(props.getEndpointUrl()) && StringUtils.isNotBlank(props.getRegionCode())) {
            return CloudWatchAsyncClient.builder()
                    .region(Region.of(props.getRegionCode()))
                    .endpointOverride(URI.create(props.getEndpointUrl()))
                    .build();
        } else if (StringUtils.isNotBlank(amazonAWSAccessKey) && StringUtils.isNotBlank(amazonAWSSecretKey)) {
            return CloudWatchAsyncClient.builder()
                    .region(Region.of(props.getRegionCode()))
                    .endpointOverride(URI.create(props.getEndpointUrl()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.amazonAWSAccessKey, this.amazonAWSSecretKey)))
                    .build();
        } else {
            return CloudWatchAsyncClient.create();
        }
    }
}


