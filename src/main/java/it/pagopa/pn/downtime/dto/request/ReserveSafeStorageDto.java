package it.pagopa.pn.downtime.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ReserveSafeStorageDto {

	private String contentType;
	private String documentType;
	private String status;

}
