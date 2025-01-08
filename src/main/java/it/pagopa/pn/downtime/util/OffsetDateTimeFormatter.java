package it.pagopa.pn.downtime.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class OffsetDateTimeFormatter {
    private static final DateTimeFormatter DTF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTF_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private OffsetDateTimeFormatter() {}

    /**
     * Get the date formatted base on Italian's date format.
     * <p>
     * This method invokes the right utils to get correct date.
     * </p>
     *
     * @param date is a representation of a date-time with an offset.
     * @return the string of the formatted date.
     */
    public static String getDateFormatted(OffsetDateTime date) {
        if (date == null) {
            return "";
        }
        OffsetDateTime newDate = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(date);
        return newDate.format(DTF_DATE);
    }

    /**
     * Get the time formatted base on Italian's time format.
     * <p>
     * This method invokes the right utils to get correct time.
     * </p>
     *
     * @param time is a representation of a date-time with an offset.
     * @return the string of the formatted time.
     */
    public static String getTimeFormatted(OffsetDateTime time) {
        if (time == null) {
            return "";
        }
        OffsetDateTime newTime = DowntimeLogUtil.getOffsetDateTimeFromGmtTime(time);
        return newTime.format(DTF_TIME);
    }
}
