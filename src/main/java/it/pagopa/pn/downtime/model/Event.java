package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent.SourceTypeEnum;
import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityStatusConverter;
import it.pagopa.pn.downtime.model.converter.PnSourceTypeConverter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Setter
@NoArgsConstructor
@DynamoDbBean
@ToString
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;
	private String idEvent;
	private OffsetDateTime timestamp;
	private String yearMonth;
	private PnFunctionality functionality;
	private PnFunctionalityStatus status;
	private SourceTypeEnum sourceType;
	private String source;
	private String uuid;

	@DynamoDbPartitionKey
	public String getIdEvent() {
		return idEvent;
	}

	@DynamoDbConvertedBy(OffsetDateTimeConverter.Converter.class)
	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public String getYearMonth() {
		return yearMonth;
	}

	@DynamoDbConvertedBy(PnFunctionalityConverter.Converter.class)
	public PnFunctionality getFunctionality() {
		return functionality;
	}

	@DynamoDbConvertedBy(PnFunctionalityStatusConverter.Converter.class)
	public PnFunctionalityStatus getStatus() {
		return status;
	}

	@DynamoDbConvertedBy(PnSourceTypeConverter.Converter.class)
	public SourceTypeEnum getSourceType() {
		return sourceType;
	}

	public String getSource() {
		return source;
	}

	public String getUuid() {
		return uuid;
	}
}
