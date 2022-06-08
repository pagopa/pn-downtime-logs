package it.pagopa.pn.downtime.components.schemas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnDownTimeHistoryEntry {
	private PnFunctionality functionality;
	private PnFunctionalityStatus status;
	private String startDate;
	private String endDate;
	private String legalFactId;
	
}
