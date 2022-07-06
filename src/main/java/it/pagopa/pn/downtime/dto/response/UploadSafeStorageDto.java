package it.pagopa.pn.downtime.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * Response Dto of the reserve on the safeStorage request.
 */
@Getter

/**
 * Sets the result code.
 *
 * @param resultCode the new result code
 */
@Setter

/**
 * Instantiates a new upload safe storage dto.
 */
@NoArgsConstructor

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString

public class UploadSafeStorageDto {

	/** The upload method. */
	private String uploadMethod;
	
	/** The upload url. */
	private String uploadUrl;
	
	/** The secret. */
	private String secret;
	
	/** The key. */
	private String key;
	
	/** The result code. */
	private String resultCode;
	
}
