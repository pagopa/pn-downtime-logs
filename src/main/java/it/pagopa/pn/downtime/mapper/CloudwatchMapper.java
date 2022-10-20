package it.pagopa.pn.downtime.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.pagopa.pn.downtime.model.Alarm;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionality;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;
import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent;

@Mapper(componentModel = "spring")
public interface CloudwatchMapper {

	@Mapping(target = "status", source = "newStateValue", qualifiedByName = "stateValue")
	@Mapping(target = "functionality", source = "alarmName", qualifiedByName = "functionalityToAlarmName")
	@Mapping(target = "timestamp", source = "stateChangeTime")
	@Mapping(target = "sourceType", constant = "ALARM")
	@Mapping(target = "source", source = "alarmDescription")
	//@Mapping(target = "functionality", source = "alarm.trigger.dimensions", qualifiedByName = "dimensions")
	
	PnStatusUpdateEvent alarmToPnStatusUpdateEvent(Alarm alarm);

	@Named("functionalityToAlarmName")
	default List<PnFunctionality> stringToPnFunctionality(String alarmName) {
		List<PnFunctionality> listFunctionality = new ArrayList<>();
		for (PnFunctionality pn : PnFunctionality.values()) {
			if(alarmName.equals(pn.getValue()) || alarmName.contains(pn.getValue())) {
				listFunctionality.add(pn);
			}
		}
		return listFunctionality;
	}
	
	@Named("stateValue")
	default PnFunctionalityStatus stringToPnFunctionalityStatus(String stateValue) {
		PnFunctionalityStatus pnFunctionalityStatus = null;
		if (stateValue.equals(PnFunctionalityStatus.OK.toString())) {
			pnFunctionalityStatus = PnFunctionalityStatus.OK;
		} else {
			pnFunctionalityStatus = PnFunctionalityStatus.KO;
		}
		return pnFunctionalityStatus;
	}
	
}
