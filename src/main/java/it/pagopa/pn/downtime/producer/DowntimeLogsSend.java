package it.pagopa.pn.downtime.producer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import lombok.CustomLog;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@CustomLog
public class DowntimeLogsSend {

    @Autowired
    private SqsAsyncClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DowntimeLogsMapper mapperDowntimeLogsMapper;

    public void sendMessage(DowntimeLogs downtimeLogs, String url) throws JsonProcessingException {
        log.debug("Inserting data {} in SQS {}", downtimeLogs.toString(), StringUtils.substringAfterLast(url, "/"));
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(url)
                .messageBody(objectMapper.writeValueAsString(downtimeLogs))
                .build();
        sqsClient.sendMessage(sendMessageRequest).join();
        log.info("Inserted data in SQS {}", StringUtils.substringAfterLast(url, "/"));
    }
}
