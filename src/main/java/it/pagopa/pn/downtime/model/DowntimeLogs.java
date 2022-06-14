package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityStatusConverter;
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
	@DynamoDBTypeConverted(converter=OffsetDateTimeConverter.Converter.class)
	public OffsetDateTime getStartDate() {
		return startDate;
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





}
