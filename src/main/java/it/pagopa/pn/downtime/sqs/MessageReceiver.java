package it.pagopa.pn.downtime.sqs;
import org.springframework.stereotype.Component;

import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageReceiver {

	@SqsListener()
	public void receiveStringMessage(final String message) {
		log.info("message received {}",message);
	}

}