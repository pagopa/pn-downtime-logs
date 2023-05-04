package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DowntimeLogUtil {

	private DowntimeLogUtil() {}
	
	public static OffsetDateTime getGmtTimeFromOffsetDateTimeOffsetDateTime(OffsetDateTime localDate) {
		log.info("Local date = {} " + localDate);
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
		OffsetDateTime time = localDate.toLocalDateTime().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime();

		log.info("Date with offset (+02:00): " + time);
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		time = OffsetDateTime.parse(time.format(formatter));
		
		log.info("Date with offset with formatter (+02:00): " + time);
		
		log.info("UTC date: " + time.toInstant().atOffset(ZoneOffset.UTC));
		
		return time.toInstant().atOffset(ZoneOffset.UTC);
	}
	
	public static OffsetDateTime getOffsetDateTimeFromGmtTime(OffsetDateTime gmtDate) {
		if (gmtDate == null) {
			throw new IllegalArgumentException("The gmtDate parameter cannot be null.");
		}
		return gmtDate.toInstant().atOffset(ZoneId.of("Europe/Paris").getRules().getOffset(gmtDate.toInstant()));
	}

	public static OffsetDateTime getGmtTimeNowFromOffsetDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		OffsetDateTime now = OffsetDateTime.parse(OffsetDateTime.now().format(formatter));

		log.info("getGmtTimeNowFromOffsetDateTime - Current date {} " + now);

		if (now == null || formatter == null) {
			throw new IllegalArgumentException("The parameter cannot be null.");
		}
		Timestamp timestampDate = Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date newDate = new Date(timestampDate.getTime());
		log.info("getGmtTimeNowFromOffsetDateTime - Current date {} " + newDate.toInstant().atOffset(ZoneOffset.UTC));
		return newDate.toInstant().atOffset(ZoneOffset.UTC);
	}

	public static OffsetDateTime getGmtTime(OffsetDateTime localDate) {
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
        OffsetDateTime newdate = convertToGMT(localDate);
        log.info("getGmtTime - New date {} ", newdate);
        log.info("getGmtTime - Local date {} ", localDate);
        log.info("getGmtTime - Current date {}", OffsetDateTime.now());
        Timestamp timestampDate = Timestamp.valueOf(localDate.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date gmtDate = new Date(timestampDate.getTime());
		
		OffsetDateTime localDateFormatted = OffsetDateTime.parse(gmtDate.toInstant().toString());
        log.info("getGmtTime - GMT/UTC date localDateFormatted with toInstant {}, toString {} " + localDateFormatted,
                gmtDate.toInstant(), gmtDate.toString());
		return gmtDate.toInstant().atOffset(ZoneOffset.UTC);
	}
	
    public static OffsetDateTime convertToGMT(OffsetDateTime localDate) {
        log.info("convertToGMT - Local date {} ", localDate);
        ZoneOffset inputOffset = localDate.getOffset();
        OffsetDateTime gmtDateTime = localDate.withOffsetSameInstant(ZoneOffset.UTC);
        log.info("convertToGMT - GMT/UTC date localDateFormatted with toInstant {}, offset {} " + gmtDateTime,
                inputOffset.toString());
        return gmtDateTime;
    }
}
