package it.pagopa.pn.downtime.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Sets the retry after.
 *
 * @param retryAfter the new retry after
 */
@Setter
@Getter
/**
 * Instantiates a new download legal fact dto.
 */
@NoArgsConstructor

public class DownloadLegalFactDto {

	/** The url. */
	private String url;
	
	/** The retry after. */
	private BigDecimal retryAfter;

}
