package it.pagopa.pn.downtime.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.model.Dimensions;
import it.pagopa.pn.downtime.pn_downtime.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime.model.PnStatusUpdateEvent;

@Mapper(componentModel = "spring")
public interface CloudwatchMapper {

	@Mapping(target = "status", source = "newStateValue")
	@Mapping(target = "functionality", source = "alarm.trigger.dimensions", qualifiedByName = "dimensions")
	@Mapping(target = "timestamp", source = "stateChangeTime")
	@Mapping(target = "sourceType", constant = "ALARM")
	@Mapping(target = "source", source = "alarmDescription")

	PnStatusUpdateEvent alarmToPnStatusUpdateEvent(Alarm alarm);

	@Named("dimensions")
	default List<PnFunctionality> dimensionsToPnFunctionality(List<Dimensions> dimension) {
		List<PnFunctionality> listFunctionality = new ArrayList<>();
		for (Dimensions d : dimension) {
			listFunctionality.add(d.getValue());
		}
		return listFunctionality;
	}
}
