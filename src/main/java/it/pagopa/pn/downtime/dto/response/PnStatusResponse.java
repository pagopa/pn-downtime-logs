package it.pagopa.pn.downtime.dto.response;

import it.pagopa.pn.downtime.components.schemas.PnDownTimeHistoryEntry;
import it.pagopa.pn.downtime.components.schemas.PnFunctionality;
import it.pagopa.pn.downtime.components.schemas.PnFunctionalityStatus;
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

public class PnStatusResponse {
	private PnFunctionality[] functionalities;
	private PnDownTimeHistoryEntry[] openIncidents;
	
}
