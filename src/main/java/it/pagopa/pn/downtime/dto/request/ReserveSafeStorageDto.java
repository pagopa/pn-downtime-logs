package it.pagopa.pn.downtime.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *  Dto used for the legal fact reservation on the safeStorage
 */
@Getter

/**
 * Instantiates a new reserve safe storage dto.
 *
 * @param contentType the content type
 * @param documentType the document type
 * @param status the status
 */
@AllArgsConstructor

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class ReserveSafeStorageDto {

	/** The content type. */
	private String contentType;
	
	/** The document type. */
	private String documentType;
	
	/** The status. */
	private String status;

}
