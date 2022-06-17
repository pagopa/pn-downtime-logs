package it.pagopa.pn.downtime.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import it.pagopa.pn.downtime.dto.request.ReserveSafeStorageDto;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.dto.response.UploadSafeStorageDto;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalFactServiceImpl implements LegalFactService {
	
	
    @Value("${amazon.safestore.baseurl}")
    private String urlSafeStore;
    @Value("${amazon.safestore.reservefile}")
    private String urlReserveStore;
    
    private static final  String PAGOPA_SAFESTORAGE_HEADER = "x-pagopa-safestorage-cx-id";
    private static final  String PAGOPA_SAFESTORAGE_HEADER_VALUE = "pn-delivery-push";
    private static final  String PAGOPA_API_KEY_HEADER = "x-api-key";
    private static final  String PAGOPA_API_KEY_HEADER_VALUE = "apiKey";
	
    
	@Override
	public LegalFactDownloadMetadataResponse getLegalFact(String legalFactId) {
	log.info("getLegalFact");
	RestTemplate restTemplate = new RestTemplate();
	LegalFactDownloadMetadataResponse response = new LegalFactDownloadMetadataResponse();
	HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
    List<MediaType> acceptedTypes = new ArrayList<>();
    acceptedTypes.add(MediaType.APPLICATION_JSON);
    requestHeaders.setAccept(acceptedTypes);
    requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
    requestHeaders.add(PAGOPA_API_KEY_HEADER, PAGOPA_API_KEY_HEADER_VALUE);
    HttpEntity<String> safeStorageRequest = new HttpEntity<>(null, requestHeaders);
	ResponseEntity<Object> safeStorageResponse = restTemplate
			  .exchange(urlSafeStore.concat(urlReserveStore+":fileKey?metadataOnly=true"), HttpMethod.GET, safeStorageRequest, Object.class,legalFactId);
	if (safeStorageResponse.getBody() instanceof GetLegalFactDto) {
		log.info("request for the legalFact made successfully");
		//MAPPARE L''OUTPUT
		
	}
		return response;
	}
    
    
	@Override
	public Integer reserveUploadFile() {
	log.info("reserveUploadFile");
	RestTemplate restTemplate = new RestTemplate();
	HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
    List<MediaType> acceptedTypes = new ArrayList<>();
    acceptedTypes.add(MediaType.APPLICATION_JSON);
    requestHeaders.setAccept(acceptedTypes);
    requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
    requestHeaders.add(PAGOPA_API_KEY_HEADER, PAGOPA_API_KEY_HEADER_VALUE);
    ReserveSafeStorageDto requestDto = new ReserveSafeStorageDto("application/pdf","PN_NOTIFICATION_ATTACHMENTS","PRELOADED");
    String jsonInString = new Gson().toJson(requestDto);
    HttpEntity<String> safeStorageRequest = new HttpEntity<>(jsonInString, requestHeaders);
	ResponseEntity<Object> safeStorageResponse = restTemplate
			  .exchange(urlSafeStore.concat(urlReserveStore), HttpMethod.POST, safeStorageRequest, Object.class);
	if (safeStorageResponse.getBody() instanceof UploadSafeStorageDto) {
		log.info("Reservation made successfully");
		uploadFile((UploadSafeStorageDto) safeStorageResponse.getBody());
		
	}
		return null;
	}
		
	public void uploadFile(UploadSafeStorageDto uploadDto) {
		log.info("uploadFile");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    List<MediaType> acceptedTypes = new ArrayList<>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
	    requestHeaders.add(PAGOPA_API_KEY_HEADER, PAGOPA_API_KEY_HEADER_VALUE);
	    HttpEntity<String> safeStorageRequest = new HttpEntity<>(null, requestHeaders);
		ResponseEntity<Object> safeStorageResponse = restTemplate
				  .exchange(uploadDto.getUploadUrl().concat(uploadDto.getSecret()).concat(uploadDto.getKey()), HttpMethod.valueOf(uploadDto.getUploadMethod()), safeStorageRequest, Object.class);
		if (safeStorageResponse.getBody()!=null) {
			log.info("upload for the legalFact made successfully");
			
			
		}
	}
	
}



