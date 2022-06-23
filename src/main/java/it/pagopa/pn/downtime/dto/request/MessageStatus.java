package it.pagopa.pn.downtime.dto.request;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.serializer.OffsetDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties
public class MessageStatus {
	
	private String functionalityStartYear;
	@JsonSerialize(using = OffsetDateTimeSerializer.class)
	private OffsetDateTime startDate;
	@JsonSerialize(using = OffsetDateTimeSerializer.class)
	private OffsetDateTime endDate;
	private PnFunctionality functionality;
	private PnFunctionalityStatus status;
	private String startEventUuid;
	private String endEventUuid;
	private String legalFactId;
	private String uuid;

}
