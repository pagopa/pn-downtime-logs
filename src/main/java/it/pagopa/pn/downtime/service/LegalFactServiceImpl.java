package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.Version;
import it.pagopa.pn.downtime.dto.request.ReserveSafeStorageDto;
import it.pagopa.pn.downtime.dto.response.GetLegalFactDto;
import it.pagopa.pn.downtime.dto.response.UploadSafeStorageDto;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.pn_downtime.model.LegalFactDownloadMetadataResponse;
import it.pagopa.pn.downtime.repository.DowntimeLogsRepository;
import it.pagopa.pn.downtime.util.DocumentComposition;
import it.pagopa.pn.downtime.util.LegalFactGenerator;
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
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;
    
   
    
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
	ResponseEntity<GetLegalFactDto> safeStorageResponse = restTemplate
			  .exchange(urlSafeStore.concat(urlReserveStore+":fileKey?metadataOnly=true"), HttpMethod.GET, safeStorageRequest, GetLegalFactDto.class,legalFactId);
	if (safeStorageResponse.getBody()!= null && safeStorageResponse.getBody().getContentLength() != null) {
		log.info("request for the legalFact made successfully");
		GetLegalFactDto safeStorageResponseBody = safeStorageResponse.getBody();
		response.setContentLength(safeStorageResponseBody.getContentLength());
		response.setUrl(safeStorageResponseBody.getDownload().getUrl());
		response.setRetryAfter(new BigDecimal(120));
	}
		return response;
	}
    
    
	@Override
	public DowntimeLogs reserveUploadFile(byte[] file,DowntimeLogs downtime) {
	log.info("reserveUploadFile");
	RestTemplate restTemplate = new RestTemplate();
	HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
    List<MediaType> acceptedTypes = new ArrayList<>();
    acceptedTypes.add(MediaType.APPLICATION_JSON);
    requestHeaders.setAccept(acceptedTypes);
    requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
    requestHeaders.add(PAGOPA_API_KEY_HEADER, PAGOPA_API_KEY_HEADER_VALUE);
    ReserveSafeStorageDto requestDto = new ReserveSafeStorageDto("application/pdf","PN_LEGAL_FACT_DOWNTIME","PRELOADED");
    String jsonInString = new Gson().toJson(requestDto);
    HttpEntity<String> safeStorageRequest = new HttpEntity<>(jsonInString, requestHeaders);
	ResponseEntity<UploadSafeStorageDto> safeStorageResponse = restTemplate
			  .exchange(urlSafeStore.concat(urlReserveStore), HttpMethod.POST, safeStorageRequest, UploadSafeStorageDto.class);
	if (safeStorageResponse.getBody().getKey()!=null) {
		log.info("Reservation made successfully");
		uploadFile(safeStorageResponse.getBody(),file);
		downtime.setLegalFactId(safeStorageResponse.getBody().getKey());	
	}
		return downtime;
	}
		
	public void uploadFile(UploadSafeStorageDto uploadDto,byte[] file) {
		log.info("uploadFile");
		//DA IMPLEMENTARE IN CASO DI SPECIFICHE
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    List<MediaType> acceptedTypes = new ArrayList<>();
	    acceptedTypes.add(MediaType.APPLICATION_JSON);
	    requestHeaders.setAccept(acceptedTypes);
	    requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
	    requestHeaders.add(PAGOPA_API_KEY_HEADER, PAGOPA_API_KEY_HEADER_VALUE);
	    String jsonInString = new Gson().toJson(file);
	    HttpEntity<String> safeStorageRequest = new HttpEntity<>(jsonInString, requestHeaders);
		ResponseEntity<Object> safeStorageResponse = restTemplate
				  .exchange(uploadDto.getUploadUrl().concat(uploadDto.getSecret()).concat(uploadDto.getKey()), HttpMethod.valueOf(uploadDto.getUploadMethod()), safeStorageRequest, Object.class);
		if (safeStorageResponse.getBody()!=null) {
			log.info("upload for the legalFact made successfully");
			
			
		}
	}


	@Override
	public DowntimeLogs generateLegalFact(DowntimeLogs downtime) throws IOException {
		Configuration freemarker = new Configuration(new Version(2,3,0)); //Version is a final class
		DocumentComposition documentComposition = new DocumentComposition(freemarker);
		 LegalFactGenerator legalFactGenerator= new LegalFactGenerator(documentComposition);
		 byte[] file = legalFactGenerator.generateLegalFact(downtime);
		 reserveUploadFile(file,downtime);
		return downtime;
		
	}
	
}



