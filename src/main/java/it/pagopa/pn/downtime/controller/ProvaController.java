package it.pagopa.pn.downtime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.downtime.sqs.MessageReceiver;

@Validated
@RestController
@RequestMapping(value = "prova")
public class ProvaController{


	@Autowired
	QueueMessagingTemplate queueMessagingTemplate;
	@Autowired
	MessageReceiver messageReceiver;

	@PostMapping(value = "/send")
	public ResponseEntity<Object> sendMessage(@RequestBody String message) {

		String url = "http://localhost:9324/queue/default";
		queueMessagingTemplate.send(url, MessageBuilder.withPayload(message).build());
		return ResponseEntity.ok("Messaggio inviato correttamente");
	}



//	@GetMapping(value = "/get")
//	public ResponseEntity<Object> getMessage() {
//		String message="";
//		messageReceiver.receiveStringMessage(message);
//		return ResponseEntity.ok("Messaggio inviato correttamente");
//	}


}
