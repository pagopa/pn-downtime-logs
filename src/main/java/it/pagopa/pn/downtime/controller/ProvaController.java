package it.pagopa.pn.downtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.downtime.consumer.MessageReceiver;

@Validated
@RestController
@RequestMapping(value = "prova")
public class ProvaController{


	@Autowired
	QueueMessagingTemplate queueMessagingTemplate;
	@Autowired
	MessageReceiver messageReceiver;
	
	@Value("${cloud.aws.fila.test}")
	private String url;

	@PostMapping(value = "/send")
	public ResponseEntity<Object> sendMessage(@RequestBody String message) {

		queueMessagingTemplate.send(url, MessageBuilder.withPayload(message).build());
		return ResponseEntity.ok("Messaggio inviato correttamente");
	}



}
