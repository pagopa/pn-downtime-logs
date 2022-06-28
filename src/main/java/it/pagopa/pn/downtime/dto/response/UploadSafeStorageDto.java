package it.pagopa.pn.downtime.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class UploadSafeStorageDto {

	private String uploadMethod;
	private String uploadUrl;
	private String secret;
	private String key;
	private String resultCode;
	
}
