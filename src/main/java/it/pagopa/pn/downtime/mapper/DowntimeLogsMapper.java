package it.pagopa.pn.downtime.mapper;

import org.mapstruct.Mapper;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeEntry;

@Mapper(componentModel = "spring")
public interface DowntimeLogsMapper {
	PnDowntimeEntry downtimeLogsToPnDowntimeEntry(DowntimeLogs downtimeLogs);
}
