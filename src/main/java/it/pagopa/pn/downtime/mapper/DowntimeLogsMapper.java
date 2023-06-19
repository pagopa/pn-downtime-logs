package it.pagopa.pn.downtime.mapper;

import org.mapstruct.Mapper;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnDowntimeEntry;
import it.pagopa.pn.downtime.model.DowntimeLogs;

@Mapper(componentModel = "spring")
public interface DowntimeLogsMapper {
	PnDowntimeEntry downtimeLogsToPnDowntimeEntry(DowntimeLogs downtimeLogs);
}
