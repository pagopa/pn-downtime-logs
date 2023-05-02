package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DowntimeLogUtil {

	private DowntimeLogUtil() {
	}

	public static OffsetDateTime getGmtTimeFromOffsetDateTimeOffsetDateTime(OffsetDateTime localDate) {
		log.info("Local date = {} " + localDate);
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		OffsetDateTime localDateFormatted = OffsetDateTime.parse(localDate.format(formatter));
		OffsetDateTime time = OffsetDateTime.of(localDateFormatted.toLocalDateTime(), ZoneId.systemDefault().getRules().getOffset(Instant.now()));
		log.info("GMT/UTC date {} case with OffsetDateTime " + time.toInstant().atOffset(ZoneOffset.UTC));
		log.info("System default ZoneId: ", ZoneId.systemDefault());
		log.info("System default ZoneId Rules: ", ZoneId.systemDefault().getRules());
		log.info("System default ZoneId Offset: ", ZoneId.systemDefault().getRules().getOffset(Instant.now()));
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

		log.info("getGmtTime - Local date {} ", localDate);
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
		Timestamp timestampDate = Timestamp
				.valueOf(localDate.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date gmtDate = new Date(timestampDate.getTime());
		
		OffsetDateTime localDateFormatted = OffsetDateTime.parse(gmtDate.toInstant().toString());
		log.info("getGmtTime - GMT/UTC date localDateFormatted with toInstant {}, toString {} " + localDateFormatted, gmtDate.toInstant(), gmtDate.toString());
		return gmtDate.toInstant().atOffset(ZoneOffset.UTC);
	}
}
