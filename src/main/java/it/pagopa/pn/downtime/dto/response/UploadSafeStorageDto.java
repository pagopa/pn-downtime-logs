package it.pagopa.pn.downtime.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UploadSafeStorageDto {

	private String uploadMethod;
	private String uploadUrl;
	private String secret;
	private String key;
	private String resultCode;
	
}
