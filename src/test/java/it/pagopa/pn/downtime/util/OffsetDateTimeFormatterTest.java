package it.pagopa.pn.downtime.util;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;


class OffsetDateTimeFormatterTest {
    @Test
    void testGetDateFormatted() {
        // Arrange
        OffsetDateTime inputDate = OffsetDateTime.of(2024, 6, 12, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime adjustedDate = adjustOffsetByItaUTC(inputDate);

        // Act
        String formattedDate = OffsetDateTimeFormatter.getDateFormatted(inputDate);

        // Assert
        assertNotNull(formattedDate);
        assertEquals(adjustedDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), formattedDate);
    }

    @Test
    void testGetTimeFormatted() {
        // Arrange
        OffsetDateTime inputTime = OffsetDateTime.of(2024, 6, 12, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime adjustedTime = adjustOffsetByItaUTC(inputTime);

        // Act
        String formattedTime = OffsetDateTimeFormatter.getTimeFormatted(inputTime);

        // Assert
        assertNotNull(formattedTime);
        assertEquals(adjustedTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), formattedTime);
    }

    @Test
    void testGetDateFormattedWithNull() {
        // Act
        String formattedDate = OffsetDateTimeFormatter.getDateFormatted(null);

        // Assert
        assertNotNull(formattedDate);
        assertEquals("", formattedDate);
    }

    @Test
    void testGetTimeFormattedWithNull() {
        // Act
        String formattedTime = OffsetDateTimeFormatter.getTimeFormatted(null);

        // Assert
        assertNotNull(formattedTime);
        assertEquals("", formattedTime);
    }

    private OffsetDateTime adjustOffsetByItaUTC(OffsetDateTime input) {
        return input.withOffsetSameInstant(ZoneOffset.ofHours(2));
    }
}