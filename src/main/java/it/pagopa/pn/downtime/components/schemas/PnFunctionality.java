package it.pagopa.pn.downtime.components.schemas;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public enum PnFunctionality {
	NOTIFICATION_CREATE("NOTIFICATION_CREATE"), NOTIFICATION_VISUALIZZATION("NOTIFICATION_VISUALIZZATION"),
	NOTIFICATION_WORKFLOW("NOTIFICATION_WORKFLOW");

	private String functionality;

	private PnFunctionality(String functionality) {
		this.functionality = functionality;
	}
}
