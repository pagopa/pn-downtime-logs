package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;


public class DowntimeLogUtil {

	public static OffsetDateTime getGmtTimeFromOffsetDateTime(OffsetDateTime date) {

		OffsetDateTime gmtDate = null;

		if(date != null) {
			Timestamp timestampDate = Timestamp.valueOf(date.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
			Date newDate = new Date(timestampDate.getTime());
			gmtDate = newDate.toInstant().atOffset(ZoneOffset.UTC);

		}
		return gmtDate;
	}


	public static OffsetDateTime getOffsetDateTimeFromGmtTime(OffsetDateTime gmtDate) {

		OffsetDateTime date = null;
		if(gmtDate != null) {
			Timestamp timestampDate = Timestamp.valueOf(gmtDate.atZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime());
			Date newDate = new Date(timestampDate.getTime());
			date = newDate.toInstant().atOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())); 
		} 
		return date;
	}


}
