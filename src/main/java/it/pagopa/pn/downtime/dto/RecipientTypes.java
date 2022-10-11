package it.pagopa.pn.downtime.dto;

import java.util.ArrayList;
import java.util.List;

public enum RecipientTypes {
PF, PG;
	
	/**
	 * Gets the enum values
	 * @return The enum values as a list of strings
	 * */
	public List<String> getValue(){
		List<String> valuesAsString = new ArrayList<>();
		for(RecipientTypes tempRep : RecipientTypes.values()) { 
	         valuesAsString.add(tempRep.toString());
	    }
		return valuesAsString;
	}
	
	/**
	 * Checks if the input values is present in the enum value list
	 * @param value The input value to check
	 * @return True if the value is present in the enum, false otherwise
	 * */
	public boolean isValid(String value) {
		return getValue().contains(value);
	}
	
}
