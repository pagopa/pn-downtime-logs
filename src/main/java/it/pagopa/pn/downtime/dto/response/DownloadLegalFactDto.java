package it.pagopa.pn.downtime.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class DownloadLegalFactDto {

	private String url;
	
	private BigDecimal retryAfter;

}
