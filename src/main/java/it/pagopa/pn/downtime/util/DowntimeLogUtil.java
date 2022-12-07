package it.pagopa.pn.downtime.util;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DowntimeLogUtil {

	public static OffsetDateTime getGmtTimeFromOffsetDateTime(OffsetDateTime date) {

		OffsetDateTime gmtDate = null;

		log.info("OffsetDataTime: {}", date);
		if(date != null) {
			Timestamp timestampDate = Timestamp.valueOf(date.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
			Date newDate = new Date(timestampDate.getTime());
			gmtDate = newDate.toInstant().atOffset(ZoneOffset.UTC);

		}
		log.info("GmtDate: {}", gmtDate);
		return gmtDate;
	}


	public static OffsetDateTime getOffsetDateTimeFromGmtTime(OffsetDateTime gmtDate) {

		log.info("Current date: {}", OffsetDateTime.now());
		log.info("getOffsetDateTimeFromGmtTime: {}", gmtDate);
		ZoneId zone = ZoneId.of("Europe/Paris");
		OffsetDateTime newDate = gmtDate.toInstant().atOffset(zone.getRules().getOffset(gmtDate.toInstant()));
		log.info("OffsetDataTime - newDate: {}", newDate);
		return newDate;

	}


}




