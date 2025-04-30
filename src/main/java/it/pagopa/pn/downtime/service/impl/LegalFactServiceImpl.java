package it.pagopa.pn.downtime.service.impl;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.api.FileUploadApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.client.ApiClient;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.dto.FileCreationRequest;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.dto.FileCreationResponse;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.dto.FileDownloadResponse;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.LegalFactDownloadMetadataResponse;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.MalfunctionLegalFact;
import it.pagopa.pn.downtime.mapper.DowntimeLogsMapper;
import it.pagopa.pn.downtime.middleware.legalfactgenerator.LegalFactGenerator;
import it.pagopa.pn.downtime.model.DowntimeLogs;
import it.pagopa.pn.downtime.service.LegalFactService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@CustomLog
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

	/** The rest template. */
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private FileDownloadApi fileDownloadApi;

	@Autowired
	private FileUploadApi fileUploadApi;
	
	@Autowired
	private ApiClient api;

	@Autowired
	private DowntimeLogsMapper downtimeLogsMapper;

	private final LegalFactGenerator legalFactGenerator;

	/** The Constant PAGOPA_SAFESTORAGE_HEADER. */
	private static final String PAGOPA_SAFESTORAGE_HEADER = "x-pagopa-safestorage-cx-id";

	/** The Constant PAGOPA_SAFESTORAGE_HEADER_VALUE. */
	private static final String PAGOPA_SAFESTORAGE_HEADER_VALUE = "pn-downtime-logs";

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
	 * @return the link for the download of the legal fact or the retry after for
	 *         retrying the request
	 */
	@Override
	public LegalFactDownloadMetadataResponse getLegalFact(String legalFactId) {
		log.info("getLegalFact - Input: " + legalFactId);
		api.setBasePath(urlSafeStore);
		if (enableApiKey) {
			api.setApiKey(apiKeyHeaderValue);
		}
		fileDownloadApi.setApiClient(api);
		FileDownloadResponse response = fileDownloadApi.getFile(legalFactId, PAGOPA_SAFESTORAGE_HEADER_VALUE, false);
		LegalFactDownloadMetadataResponse legalFactResponse = new LegalFactDownloadMetadataResponse();
		legalFactResponse.setContentLength(
				Optional.ofNullable(response.getContentLength()).map(BigDecimal::intValue).orElse(null));
		legalFactResponse.setUrl(response.getDownload().getUrl());
		legalFactResponse.setRetryAfter(
				Optional.ofNullable(response.getDownload().getRetryAfter()).map(BigDecimal::intValue).orElse(null));
		log.info("Response: " + legalFactResponse.toString());
		return legalFactResponse;
	}

	/**
	 * Makes the request to SafeStorage for the reservation of the legal fact
	 *
	 * @param file     the generated pdf of the legal fact
	 * @param downtime the downtime which is refered by the legal fact
	 * @return the downtime log updated with the legal fact id
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public DowntimeLogs reserveUploadFile(byte[] file, DowntimeLogs downtime) throws NoSuchAlgorithmException {
		log.info("reserveUploadFile");
		api.setBasePath(urlSafeStore);
		if (enableApiKey) {
			api.setApiKey(apiKeyHeaderValue);
		}
		fileUploadApi.setApiClient(api);

		FileCreationRequest fileCreationRequest = new FileCreationRequest();
		fileCreationRequest.setContentType("application/pdf");
		fileCreationRequest.setDocumentType(pagoPaDocumentType);
		fileCreationRequest.setStatus("PRELOADED");
		MessageDigest digest = null;
		digest = MessageDigest.getInstance(SHA256);
		byte[] hash = digest != null ? digest.digest(file) : null;
		String checkSum = Base64.getEncoder().encodeToString(hash);
		ResponseEntity<FileCreationResponse> response = fileUploadApi
				.createFileWithHttpInfo(PAGOPA_SAFESTORAGE_HEADER_VALUE, checkSum, SHA256, fileCreationRequest);

		if (response != null && response.getStatusCodeValue() == 200) {
			FileCreationResponse fileCreationResponse = response.getBody();
			if (fileCreationResponse != null) {
				log.info("Reservation made successfully" + fileCreationResponse);
				downtime.setLegalFactId(fileCreationResponse.getKey());
				uploadFile(fileCreationResponse, checkSum, file);
			}
		} else {
			log.fatal("Error during the reservation request");
		}
		return downtime;
	}

	/**
	 * Uploads the legal fact on SafeStorage.
	 *
	 * @param uploadDto the response of the reserve file request
	 * @param checkSum  the check sum of the file calculated during the reservation
	 *                  used to be sure that no data was lost during between the
	 *                  reservation and the upload
	 * @param file      the generated pdf of the legal fact
	 */
	public void uploadFile(FileCreationResponse uploadDto, String checkSum, byte[] file) {
		log.info("uploadFile for LegalFactId = {}", uploadDto.getKey());
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
				HttpMethod.valueOf(uploadDto.getUploadMethod().getValue()), requestEntity, Object.class);

		if (safeStorageResponse.getStatusCode().is2xxSuccessful()) {
			log.info("Upload Operation was successfull");
		}

	}

	/**
	 * Generates the pdf of the legal fact and makes the reservation and the upload
	 * of the file.
	 *
	 * @param downtime the downtime used for the legal fact generation
	 * @return the downtime log updated with the legal fact id
	 * @throws IOException              Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws TemplateException        the template exception
	 */
	@Override
	public DowntimeLogs generateLegalFact(DowntimeLogs downtime)
			throws IOException, NoSuchAlgorithmException, TemplateException {
		byte[] file = legalFactGenerator.generateMalfunctionLegalFact(downtime);
		reserveUploadFile(file, downtime);
		return downtime;
	}

    @Override
    public byte[] previewLegalFact(MalfunctionLegalFact malfunctionLegalFact) throws IOException, TemplateException {
        it.pagopa.pn.downtime.generated.openapi.msclient.templatesengine.model.MalfunctionLegalFact entry = downtimeLogsMapper.malfunctionLegalFactDtoToModelClient(malfunctionLegalFact);
        return legalFactGenerator.generateMalfunctionLegalFact(entry);
    }

}