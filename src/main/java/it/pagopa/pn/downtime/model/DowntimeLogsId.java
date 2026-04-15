package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.pagopa.pn.downtime.util.OffsetDateTimeSerializer;
import lombok.ToString;

@ToString
public class DowntimeLogsId implements Serializable {

	private static final long serialVersionUID = 1L;
	private String functionalityStartYear;
	@JsonSerialize(using = OffsetDateTimeSerializer.class)
	private OffsetDateTime startDate;

	public String getFunctionalityStartYear() {
		return functionalityStartYear;
	}
	public void setFunctionalityStartYear(String functionalityStartYear) {
		this.functionalityStartYear = functionalityStartYear;
	}

	public OffsetDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(OffsetDateTime startDate) {
		this.startDate = startDate;
	}
}
