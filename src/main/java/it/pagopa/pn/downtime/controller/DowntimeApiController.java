package it.pagopa.pn.downtime.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DowntimeApiController {
	
//	@Autowired
//	BuildProperties buildProperties;
	
	@RequestMapping(path = "/downtime/v1/status", produces = { "application/json" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<BodyBuilder> currentStatus() {
		return ResponseEntity.ok().allow(HttpMethod.GET, HttpMethod.OPTIONS).build();
	}

	@RequestMapping(path = "/downtime/v1/legal-facts/{legalFactId}", produces = {
			"application/json" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<BodyBuilder> getLegalFact() {
		return ResponseEntity.ok().allow(HttpMethod.GET, HttpMethod.OPTIONS).build();
	}

	@RequestMapping(path = "/downtime/v1/history", produces = { "application/json" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<BodyBuilder> statusHistory() {
		return ResponseEntity.ok().allow(HttpMethod.GET, HttpMethod.OPTIONS).build();
	}

	@RequestMapping(path = "/downtime-internal/v1/events", produces = { "application/json" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<BodyBuilder> addStatusChangeEvent() {
		return ResponseEntity.ok().allow(HttpMethod.POST, HttpMethod.OPTIONS).build();
	}

	@RequestMapping(path = "/status", produces = { "application/json" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<BodyBuilder> status() {
		return ResponseEntity.ok().allow(HttpMethod.GET, HttpMethod.OPTIONS).build();
	}
	
//	@GetMapping(path = "/version", produces = { "application/json" })
//	public ResponseEntity<Object> getVersion(){
//		Map<String, String> mapVersion = new HashMap<>();
//		mapVersion.put("version", buildProperties.getVersion());
//		return ResponseEntity.ok().body(mapVersion);
//	}
}
