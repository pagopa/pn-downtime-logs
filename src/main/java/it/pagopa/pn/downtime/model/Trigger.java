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
public class Trigger{

	@JsonProperty("MetricName")
	private String metricName;
	
	@JsonProperty("Namespace")
	private String namespace;
	
	@JsonProperty("StatisticType")
	private String statisticType;
	
	@JsonProperty("Statistic")
	private String statistic;
	
	@JsonProperty("Unit")
	private String unit;
	
	@JsonProperty("Dimensions")
	private List<Dimensions> dimensions;
	
	@JsonProperty("Period")
	private String period;
	
	@JsonProperty("EvaluationPeriods")
	private String evaluationPeriods;
	
	@JsonProperty("ComparisonOperator")
	private String comparisonOperator;
	
	@JsonProperty("Threshold")
	private String threshold;
	
	@JsonProperty("TreatMissingData")
	private String treatMissingData;
	
	@JsonProperty("EvaluateLowSampleCountPercentile")
	private String evaluateLowSampleCountPercentile;


}
