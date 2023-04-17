package it.pagopa.pn.downtime.util;

public class Constants {

	private Constants() {
	}

	public static final String GENERIC_MESSAGE = "Errore nell'elaborazione della richiesta";
	public static final String GENERIC_ENGLISH_MESSAGE = "An error occured during request elaboration";
	public static final String GENERIC_BAD_REQUEST_ERROR_MESSAGE = "Informazioni non valide";
	public static final String GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE = "Informations not valid";
	public static final String GENERIC_CONFLICT_ERROR_MESSAGE_TITLE = "Conflict in request. Requested resource in conflict with the current state of the server.";
	public static final String GENERIC_CONFLICT_ERROR_ENGLISH_MESSAGE = "Downtime of functionality %s already open, startDate(GMT/UTC): %s, endDate(GMT/UTC): %s. ";
	public static final String GENERIC_CREATING_EVENT_ERROR = "Error creating event, downtime for functionality %s has already been fixed";
	public static final String GENERIC_CREATING_FUTURE_EVENT_ERROR="An error occured during elaboration of PnStatusUpdateEvent's date";
	public static final String TRACE_ID_PLACEHOLDER = "trace_id";
}
