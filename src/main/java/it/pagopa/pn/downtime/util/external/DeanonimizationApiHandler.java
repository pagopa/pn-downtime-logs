package it.pagopa.pn.downtime.util.external;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.pagopa.pn.downtime.exceptions.DowntimeException;


/**
 * Uility class for integrations with Piattaforma Notifiche de-anonymization service
 * */
@Component
public class DeanonimizationApiHandler {

	@Autowired
	RestTemplate client;
	
	@Value("${external.denomination.ensureRecipientByExternalId.url}")
	String getUniqueIdURL;


	/**
	 * Method that makes a request to Piattaforma Notifiche external service to
	 * retrieve the unique identifier of a person, given the recipient type and tax
	 * id of a person
	 * @param recipientType
	 * @param taxId
	 * @return
	 * @throws DowntimeException
	 */
	public String getUniqueIdentifierForPerson(String taxId) throws DowntimeException {
		HttpEntity<String> request =  new HttpEntity<>(taxId);
		String response = client.postForObject(getUniqueIdURL, request, String.class);
		if(StringUtils.isBlank(response) || "null".equalsIgnoreCase(response)) {
			throw new DowntimeException("Anonymized tax id is null");
		}
		return response;
	}


}
