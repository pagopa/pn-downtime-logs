package it.pagopa.pn.downtime.dto.response;

import it.pagopa.pn.downtime.components.schemas.PnDownTimeHistoryEntry;
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

public class PnDowntimeHistoryResponse {
	private PnDownTimeHistoryEntry[] result;
	private String nextPage;
	
}
