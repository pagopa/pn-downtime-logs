package it.pagopa.pn.downtime.mapper;

import org.mapstruct.Mapper;

import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.PnDowntimeEntry;

@Mapper
public interface DowntimeLogsMapper {
	PnDowntimeEntry downtimeLogsToPnDowntimeEntry(DowntimeLogs downtimeLogs);
	DowntimeLogs pnDowntimeEntryToDowntimeLogs(PnDowntimeEntry downtimeEntry);
}
