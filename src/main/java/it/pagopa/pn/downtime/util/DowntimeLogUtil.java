package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;


public class DowntimeLogUtil {

	public static OffsetDateTime getGmtTimeFromOffsetDateTime(OffsetDateTime date) {
		
		Timestamp timestampDate = Timestamp.valueOf(date.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date newDate = new Date(timestampDate.getTime());

	    return newDate.toInstant().atOffset(ZoneOffset.UTC);
	}
	
	
    public static OffsetDateTime getOffsetDateTimeFromGmtTime(OffsetDateTime gmtDate) {
    	
    	Timestamp timestampDate = Timestamp.valueOf(gmtDate.atZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime());
		Date newDate = new Date(timestampDate.getTime());
		
		OffsetDateTime offsetDateTime = newDate.toInstant().atOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())); 
		
        return offsetDateTime;

    }

	
}
