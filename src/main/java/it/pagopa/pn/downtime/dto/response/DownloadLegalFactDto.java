package it.pagopa.pn.downtime.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Sub-Response Dto of the getLegalFact safeStorage request.It contains the downwolad url and the retry after.
 */
@Getter

/**
 * Sets the retry after.
 *
 * @param retryAfter the new retry after
 */
@Setter

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
