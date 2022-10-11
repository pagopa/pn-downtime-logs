package it.pagopa.pn.downtime.exceptions;

public class DowntimeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DowntimeException(String errorMessage) {
        super(errorMessage);
    }
}
