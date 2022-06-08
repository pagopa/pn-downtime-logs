package it.pagopa.pn.downtime.components.schemas;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public enum PnFunctionalityStatus {
	KO("KO"), OK("OK");

	private String status;

	private PnFunctionalityStatus(String status) {
		this.status = status;
	}

}
