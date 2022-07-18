package it.pagopa.pn.downtime.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
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

/**
 * The Class LegalFactServiceImpl.
 */
@Service

/**
 * Instantiates a new legal fact service impl.
 */
@RequiredArgsConstructor

/** The Constant log. */
@Slf4j
public class LegalFactServiceImpl implements LegalFactService {

	/** The url safe store. */
	@Value("${amazon.safestore.baseurl}")
	private String urlSafeStore;
	
	/** The url reserve store. */
	@Value("${amazon.safestore.reservefile}")
	private String urlReserveStore;
	
	/** The enable api key. */
	@Value("${pagopa.header.enable-apikey}")
	private boolean enableApiKey;
	
	/** The api key header. */
	@Value("${pagopa.header.apikey}")
	private String apiKeyHeader;
	
	/** The api key header value. */
	@Value("${pagopa.headervalue.apikey}")
	private String apiKeyHeaderValue;
	
	/** The pago pa document type. */
	@Value("${pagopa.reservation.documenttype}")
	private String pagoPaDocumentType;
	
	/** The downtime logs repository. */
	@Autowired
	DowntimeLogsRepository downtimeLogsRepository;
	
	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;
	
	/** The Constant PAGOPA_SAFESTORAGE_HEADER. */
	private static final String PAGOPA_SAFESTORAGE_HEADER = "x-pagopa-safestorage-cx-id";
	
	/** The Constant PAGOPA_SAFESTORAGE_HEADER_VALUE. */
	private static final String PAGOPA_SAFESTORAGE_HEADER_VALUE = "pn-delivery-push";
	
	/** The Constant UPLOAD_SAFESTORAGE_HEADER_SECRET. */
	private static final String UPLOAD_SAFESTORAGE_HEADER_SECRET = "x-amz-meta-secret";
	
	/** The Constant UPLOAD_SAFESTORAGE_HEADER_CHECKSUM. */
	private static final String UPLOAD_SAFESTORAGE_HEADER_CHECKSUM = "x-amz-checksum-sha256";
	
	/** The Constant RESERVE_SAFESTORAGE_HEADER_CHECKSUM. */
	private static final String RESERVE_SAFESTORAGE_HEADER_CHECKSUM = "x-checksum";
	
	/** The Constant RESERVE_SAFESTORAGE_HEADER_CHECKSUM_VALUE. */
	private static final String RESERVE_SAFESTORAGE_HEADER_CHECKSUM_VALUE = "x-checksum-value";
	
	/** The Constant SHA256. */
	private static final String SHA256 = "SHA-256";

	/**
	 * Gets the legal fact by making the call to SafeStorage.
	 *
	 * @param legalFactId the legal fact id
	 * @return the link for the download of the legal fact or the retry after for retrying the request
	 */
	@Override
	public LegalFactDownloadMetadataResponse getLegalFact(String legalFactId) {
		log.info("getLegalFact - Input: " + legalFactId);
		LegalFactDownloadMetadataResponse response = new LegalFactDownloadMetadataResponse();
		HttpHeaders requestHeaders = new HttpHeaders();
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
		if (enableApiKey) {
			requestHeaders.add(apiKeyHeader, apiKeyHeaderValue);
		}
		UriComponents uriComponents = UriComponentsBuilder
				.fromHttpUrl(urlSafeStore.concat(urlReserveStore).concat("/{fileKey}?metadataOnly=false"))
				.buildAndExpand(legalFactId);
		HttpEntity<String> safeStorageRequest = new HttpEntity<>(null, requestHeaders);
		ResponseEntity<GetLegalFactDto> safeStorageResponse = restTemplate.exchange(uriComponents.toUri(),
				HttpMethod.GET, safeStorageRequest, GetLegalFactDto.class);
		if (safeStorageResponse.getBody() != null && safeStorageResponse.getBody().getContentLength() != null) {
			GetLegalFactDto safeStorageResponseBody = safeStorageResponse.getBody();
			log.info("Request for the legalFact made successfully: "+ safeStorageResponseBody.toString());
			response.setContentLength(safeStorageResponseBody.getContentLength());
			response.setUrl(safeStorageResponseBody.getDownload().getUrl());
			response.setRetryAfter(safeStorageResponseBody.getDownload().getRetryAfter());
		}
		log.info("Response: " + response.toString());
		return response;
	}

	/**
	 * Makes the request to SafeStorage for the reservation of the legal fact
	 *
	 * @param file the generated pdf of the legal fact
	 * @param downtime the downtime which is refered by the legal fact 
	 * @return the downtime log updated with the legal fact id
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public DowntimeLogs reserveUploadFile(byte[] file, DowntimeLogs downtime) throws NoSuchAlgorithmException {
		log.info("reserveUploadFile");
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		requestHeaders.add(PAGOPA_SAFESTORAGE_HEADER, PAGOPA_SAFESTORAGE_HEADER_VALUE);
		if (enableApiKey) {
			requestHeaders.add(apiKeyHeader, apiKeyHeaderValue);
		}

		MessageDigest digest = null;

			digest = MessageDigest.getInstance(SHA256);

		byte[] hash = digest != null ? digest.digest(file) : null;
		String checkSum = Base64.getEncoder().encodeToString(hash);
		requestHeaders.add(RESERVE_SAFESTORAGE_HEADER_CHECKSUM_VALUE, checkSum);
		requestHeaders.add(RESERVE_SAFESTORAGE_HEADER_CHECKSUM, SHA256);
		ReserveSafeStorageDto requestDto = new ReserveSafeStorageDto("application/pdf", pagoPaDocumentType, "PRELOADED");
		log.info(requestDto.toString());
		String jsonInString = new Gson().toJson(requestDto);
		HttpEntity<String> safeStorageRequest = new HttpEntity<>(jsonInString, requestHeaders);
		ResponseEntity<UploadSafeStorageDto> safeStorageResponse = restTemplate.exchange(
				urlSafeStore.concat(urlReserveStore), HttpMethod.POST, safeStorageRequest, UploadSafeStorageDto.class);
		if (safeStorageResponse.getBody().getResultCode() != null
				&& safeStorageResponse.getBody().getResultCode().equals("200.00")) {
			log.info("Reservation made successfully" + safeStorageResponse.getBody());
			uploadFile(safeStorageResponse.getBody(), checkSum, file);
			downtime.setLegalFactId(safeStorageResponse.getBody().getKey());
		} else {
			log.error("Error during the reservation request");
		}
		return downtime;
	}

	/**
	 * Uploads the legal fact on SafeStorage.
	 *
	 * @param uploadDto the response of the reserve file request
	 * @param checkSum the check sum of the file calculated during the reservation used to be sure that no data was lost during between the reservation and the upload
	 * @param file the generated pdf of the legal fact
	 */
	public void uploadFile(UploadSafeStorageDto uploadDto, String checkSum, byte[] file) {
		log.info("uploadFile");
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_PDF);
		List<MediaType> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(acceptedTypes);
		requestHeaders.add(UPLOAD_SAFESTORAGE_HEADER_CHECKSUM, checkSum);
		requestHeaders.add(UPLOAD_SAFESTORAGE_HEADER_SECRET, uploadDto.getSecret());
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(file, requestHeaders);
		UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(uploadDto.getUploadUrl()).build(true);
		ResponseEntity<Object> safeStorageResponse = restTemplate.exchange(uriComponents.toUri(),
				HttpMethod.valueOf(uploadDto.getUploadMethod()), requestEntity, Object.class);

		if (safeStorageResponse.getStatusCode().is2xxSuccessful()) {
			log.info("Upload Operation was successfull");
		}

	}

	/**
	 * Generates the pdf of the legal fact and makes the reservation and the upload of the file.
	 *
	 * @param downtime the downtime used for the legal fact generation
	 * @return the downtime log updated with the legal fact id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws TemplateException the template exception
	 */
	@Override
	public DowntimeLogs generateLegalFact(DowntimeLogs downtime) throws IOException, NoSuchAlgorithmException, TemplateException {
		Configuration freemarker = new Configuration(new Version(2, 3, 0)); // Version is a final class
		DocumentComposition documentComposition = new DocumentComposition(freemarker);
		LegalFactGenerator legalFactGenerator = new LegalFactGenerator(documentComposition);
		byte[] file = legalFactGenerator.generateLegalFact(downtime);
		reserveUploadFile(file, downtime);
		return downtime;

	}

}
