package it.pagopa.pn.downtime.dto.response;

import lombok.NoArgsConstructor;
import lombok.Setter;

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
