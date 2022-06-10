package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;


@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDBTable(tableName = "DowntimeLogs")
public class DowntimeLogs implements Serializable {
	private static final long serialVersionUID = 1L;

	private String functionalityStartYear;
	private OffsetDateTime startDate;
	private OffsetDateTime endDate;
	private PnFunctionality functionality;
	private PnFunctionalityStatus status;
	private String startEventUuid;
	private String endEventUuid;
	private String legalFactId;
	private String uuid;


	@DynamoDBHashKey
	public String getFunctionalityStartYear() {
		return functionalityStartYear;
	}

	@DynamoDbSortKey
	public OffsetDateTime getStartDate() {
		return startDate;
	}

	@DynamoDBAttribute
	public OffsetDateTime getEndDate() {
		return endDate;
	}

	@DynamoDBAttribute
	public PnFunctionality getFunctionality() {
		return functionality;
	}

	@DynamoDBAttribute
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





}
