package it.pagopa.pn.downtime;

import it.pagopa.pn.downtime.config.AwsConfig;
import it.pagopa.pn.downtime.config.CloudWatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudWatchConfigTest {

    @Mock
    private AwsConfig props;

    @InjectMocks
    private CloudWatchConfig cloudWatchConfig;

    @BeforeEach
    void setUp() {
        when(props.getEndpointUrl()).thenReturn("");
    }

    @Test
    void testCreateCloudWatchAsyncClientWithCredentials() {
        // Act
        CloudWatchAsyncClient client = cloudWatchConfig.createCloudWatchAsyncClient();

        // Assert
        assertNotNull(client);
    }
}