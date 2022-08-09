package it.pagopa.pn.downtime.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties

public class MessageCloudwatch {
	
	@JsonProperty("Message")
	private String message;
	
}
