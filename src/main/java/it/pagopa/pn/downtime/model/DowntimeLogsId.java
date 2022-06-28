package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;

public class DowntimeLogsId implements Serializable{

	private static final long serialVersionUID = 1L;
	private String functionalityStartYear;
	private OffsetDateTime startDate;
	
	
//	public DowntimeLogsId(String functionalityStartYear, OffsetDateTime startDate) {
//		super();
//		this.functionalityStartYear = functionalityStartYear;
//		this.startDate = startDate;
//	}
	
	@DynamoDBHashKey
	public String getFunctionalityStartYear() {
		return functionalityStartYear;
	}
	public void setFunctionalityStartYear(String functionalityStartYear) {
		this.functionalityStartYear = functionalityStartYear;
	}

	@DynamoDBRangeKey
	@DynamoDBTypeConverted(converter=OffsetDateTimeConverter.Converter.class)
	public OffsetDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(OffsetDateTime startDate) {
		this.startDate = startDate;
	}
}
