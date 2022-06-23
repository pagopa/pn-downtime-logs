package it.pagopa.pn.downtime.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties
public class Alarm {

	@JsonProperty("AlarmName")
	private String alarmName;
	
	@JsonProperty("AlarmDescription")
	private String alarmDescription;
	
	@JsonProperty("AWSAccountId")
	private String awsAccountId;
	
	@JsonProperty("AlarmConfigurationUpdatedTimestamp")
	private String alarmConfigurationUpdatedTimestamp;
	
	@JsonProperty("NewStateValue")
	private String newStateValue;
	
	@JsonProperty("NewStateReason")
	private String newStateReason;
	
	@JsonProperty("StateChangeTime")
	private String stateChangeTime;
	
	@JsonProperty("Region")
	private String region;
	
	@JsonProperty("AlarmArn")
	private String alarmArn;
	
	@JsonProperty("OldStateValue")
	private String oldStateValue;
	
	@JsonProperty("OKActions")
	private List<String> okActions;
	
	@JsonProperty("AlarmActions")
	private List<String> alarmActions;
	
	@JsonProperty("InsufficientDataActions")
	private List<String> insufficientDataActions;
	
	@JsonProperty("Trigger")
	private Trigger trigger;

}
