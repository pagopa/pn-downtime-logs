package it.pagopa.pn.downtime.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;

public class DowntimeLogUtil {

	private DowntimeLogUtil() {}
	
	public static final ZoneId italianZoneId = ZoneId.of("Europe/Rome");

    @NotNull
	public static OffsetDateTime getGmtTimeFromOffsetDateTime(OffsetDateTime localDate) {
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
		OffsetDateTime gmtDate = OffsetDateTime.parse(localDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		return gmtDate.toInstant().atOffset(ZoneOffset.UTC);
	}
    
    @NotNull
	public static OffsetDateTime getOffsetDateTimeFromGmtTime(OffsetDateTime gmtDate) {
		if (gmtDate == null) {
			throw new IllegalArgumentException("The gmtDate parameter cannot be null.");
		}
		return gmtDate.toInstant().atOffset(italianZoneId.getRules().getOffset(gmtDate.toInstant()));
	}
    
    @NotNull
	public static OffsetDateTime getOffsetDateTimeNowFormatted() {
		return OffsetDateTime.parse(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
	}
}
