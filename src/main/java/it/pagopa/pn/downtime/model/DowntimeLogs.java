package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityStatusConverter;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@DynamoDBTable(tableName = "DowntimeLogs")
@JsonIgnoreProperties
public class DowntimeLogs implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private DowntimeLogsId downtimeLogsId;
	private OffsetDateTime endDate;
	private PnFunctionality functionality;
	private PnFunctionalityStatus status;
	private String startEventUuid;
	private String endEventUuid;
	private String legalFactId;
	private String uuid;
	
	@DynamoDBHashKey
	public String getFunctionalityStartYear() {
		return downtimeLogsId != null ? downtimeLogsId.getFunctionalityStartYear() : null;
	}
	
	public void setFunctionalityStartYear(String functionalityStartYear) {
	 if (downtimeLogsId == null) {
		 downtimeLogsId = new DowntimeLogsId();
     }
	 downtimeLogsId.setFunctionalityStartYear(functionalityStartYear);
     }
	
	public void setStartDate(OffsetDateTime startDate) {
		 if (downtimeLogsId == null) {
			 downtimeLogsId = new DowntimeLogsId();
	     }
		 downtimeLogsId.setStartDate(startDate);
	     }
	
	@DynamoDBRangeKey
	@DynamoDBTypeConverted(converter=OffsetDateTimeConverter.Converter.class)
	public OffsetDateTime getStartDate() {
		return downtimeLogsId != null ? downtimeLogsId.getStartDate() : null;
	}

	@DynamoDBAttribute
	@DynamoDBTypeConverted(converter=OffsetDateTimeConverter.Converter.class)
	public OffsetDateTime getEndDate() {
		return endDate;
	}

	@DynamoDBAttribute
	@DynamoDBTypeConverted(converter=PnFunctionalityConverter.Converter.class)
	public PnFunctionality getFunctionality() {
		return functionality;
	}

	@DynamoDBAttribute
	@DynamoDBTypeConverted(converter=PnFunctionalityStatusConverter.Converter.class)
	public PnFunctionalityStatus getStatus() {
		return status;
	}

	@DynamoDBAttribute
	public String getStartEventUuid() {
		return startEventUuid;
	}

	@DynamoDBAttribute
	public String getEndEventUuid() {
		return endEventUuid;
	}

	@DynamoDBAttribute
	public String getLegalFactId() {
		return legalFactId;
	}

	@DynamoDBAttribute
	public String getUuid() {
		return uuid;
	}

	public void setEndDate(OffsetDateTime endDate) {
		this.endDate = endDate;
	}

	public void setFunctionality(PnFunctionality functionality) {
		this.functionality = functionality;
	}

	public void setStatus(PnFunctionalityStatus status) {
		this.status = status;
	}

	public void setStartEventUuid(String startEventUuid) {
		this.startEventUuid = startEventUuid;
	}

	public void setEndEventUuid(String endEventUuid) {
		this.endEventUuid = endEventUuid;
	}

	public void setLegalFactId(String legalFactId) {
		this.legalFactId = legalFactId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	


}
