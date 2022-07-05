package it.pagopa.pn.downtime.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties
public class Alarm {

	@JsonProperty("AlarmDescription")
	private String alarmDescription;

	@JsonProperty("NewStateValue")
	private String newStateValue;

	@JsonProperty("StateChangeTime")
	private OffsetDateTime stateChangeTime;

	@JsonProperty("Trigger")
	private Trigger trigger;

}
