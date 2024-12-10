package it.pagopa.pn.downtime.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class OffsetDateTimeFormatter {
    private static final DateTimeFormatter DTF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTF_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private OffsetDateTimeFormatter() {}


    public static String getDateFormatted(OffsetDateTime date) {
        if (date == null) {
            return "";
        }
        OffsetDateTime newDate = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(date);
        return newDate.format(DTF_DATE);
    }

    public static String getTimeFormatted(OffsetDateTime time) {
        if (time == null) {
            return "";
        }
        OffsetDateTime newTime = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(time);
        return newTime.format(DTF_TIME);
    }
}
