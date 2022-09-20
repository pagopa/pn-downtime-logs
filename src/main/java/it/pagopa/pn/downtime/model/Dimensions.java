package it.pagopa.pn.downtime.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@JsonIgnoreProperties
public class Dimensions {

	@JsonProperty("value")
	private String value;

	public PnFunctionality getValue() {
		return PnFunctionality.fromValue(this.value);
	}

}
