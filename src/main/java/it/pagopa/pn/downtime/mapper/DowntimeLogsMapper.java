package it.pagopa.pn.downtime.mapper;

import java.time.OffsetDateTime;
import java.util.Date;

import org.mapstruct.Mapper;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnDowntimeEntry;
import it.pagopa.pn.downtime.model.DowntimeLogs;

@Mapper(componentModel = "spring")
public interface DowntimeLogsMapper {
    PnDowntimeEntry downtimeLogsToPnDowntimeEntry(DowntimeLogs downtimeLogs);

    default Date map(OffsetDateTime value) {
        return value == null ? null : Date.from(value.toInstant());
    }
}