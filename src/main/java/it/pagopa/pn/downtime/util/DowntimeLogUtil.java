package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DowntimeLogUtil {

	private DowntimeLogUtil() {
	}

	public static OffsetDateTime getGmtTimeFromOffsetDateTime(OffsetDateTime localDate) {
		log.info("Local date = {} " + localDate);
		if (localDate == null) {
			throw new IllegalArgumentException("The localDate parameter cannot be null.");
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		OffsetDateTime localDateFormatted = OffsetDateTime.parse(localDate.format(formatter));
		Timestamp timestampDate = Timestamp
				.valueOf(localDateFormatted.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date gmtDate = new Date(timestampDate.getTime());
		log.info("GMT/UTC date = {} " + gmtDate.toInstant().atOffset(ZoneOffset.UTC));
		return gmtDate.toInstant().atOffset(ZoneOffset.UTC);
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

		log.info("getGmtTimeNowFromOffsetDateTime - Current date {}= " + now);

		if (now == null || formatter == null) {
			throw new IllegalArgumentException("The parameter cannot be null.");
		}
		Timestamp timestampDate = Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
		Date newDate = new Date(timestampDate.getTime());
		log.info("getGmtTimeNowFromOffsetDateTime - Current date {}= " + newDate.toInstant().atOffset(ZoneOffset.UTC));
		return newDate.toInstant().atOffset(ZoneOffset.UTC);
	}

	public static OffsetDateTime getGmtTimeFromLocalDate(OffsetDateTime localDate) {

		LocalDateTime ldt = localDate.toLocalDateTime();

		log.info("getGmtTimeFromLocalDate - ldt: " + ldt);

		ZonedDateTime ldtZoned = ldt.atZone(ZoneId.systemDefault());
		log.info("getGmtTimeFromLocalDate - ldtZoned: " + ldtZoned);
		log.info("getGmtTimeFromLocalDate - ldtZoned local: " + ldtZoned.toLocalDate());

		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		ZonedDateTime utcZoned2 = ldtZoned.withZoneSameInstant(ZoneId.of("UTC+0"));
		ZonedDateTime utcZoned3 = ldtZoned.withZoneSameInstant(ZoneId.of("Europe/Belfast"));
		ZonedDateTime utcZoned4 = ldtZoned.withZoneSameInstant(ZoneId.of("Etc/Greenwich"));
		ZonedDateTime utcZoned5 = ldtZoned.withZoneSameInstant(ZoneId.of("Etc/UTC"));
		
		log.info("getGmtTimeFromLocalDate - utcZoned: " + utcZoned);
		log.info("getGmtTimeFromLocalDate - utcZoned local: " + utcZoned.toLocalTime());
		log.info("getGmtTimeFromLocalDate - utcZoned2: " + utcZoned2);
		log.info("getGmtTimeFromLocalDate - utcZoned2 local: " + utcZoned2.toLocalTime());
		log.info("getGmtTimeFromLocalDate - utcZoned3: " + utcZoned3);
		log.info("getGmtTimeFromLocalDate - utcZoned3 local: " + utcZoned3.toLocalTime());
		log.info("getGmtTimeFromLocalDate - utcZoned4: " + utcZoned4);
		log.info("getGmtTimeFromLocalDate - utcZoned4 local: " + utcZoned4.toLocalTime());
		log.info("getGmtTimeFromLocalDate - utcZoned5: " + utcZoned5);
		log.info("getGmtTimeFromLocalDate - utcZoned5 local: " + utcZoned5.toLocalTime());

		Calendar calendar = new GregorianCalendar();

		Date d = Date.from(localDate.toInstant());
		calendar.setTimeZone(TimeZone.getTimeZone("UTC-4"));
		calendar.setTime(d);
		log.info("getGmtTimeFromLocalDate - Calendar: " + calendar.getTime());
		log.info("getGmtTimeFromLocalDate - Date: " + d);
		return utcZoned.toOffsetDateTime();
	}
}
