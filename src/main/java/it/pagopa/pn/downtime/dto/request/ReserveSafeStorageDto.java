package it.pagopa.pn.downtime.dto.request;

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

public class ReserveSafeStorageDto {

	private String contentType;
	private String documentType;
	private String status;

}
