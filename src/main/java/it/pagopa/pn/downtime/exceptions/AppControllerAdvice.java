package it.pagopa.pn.downtime.exceptions;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.amazonaws.SdkClientException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.dto.response.AwsSafeStorageErrorDto;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.Problem;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.ProblemError;
import it.pagopa.pn.downtime.util.Constants;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class AppControllerAdvice {

	@ExceptionHandler(value = { NoSuchElementException.class, RestClientException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> generalException(HttpServletRequest request, RuntimeException ex) {
		printLog(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, Constants.GENERIC_ENGLISH_MESSAGE,
				Constants.GENERIC_MESSAGE), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { NoSuchAlgorithmException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> noSuchAlgorithmException(HttpServletRequest request, GeneralSecurityException ex) {
		printLog(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, Constants.GENERIC_ENGLISH_MESSAGE,
				Constants.GENERIC_MESSAGE), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { IOException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> iOException(HttpServletRequest request, IOException ex) {
		printLog(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, Constants.GENERIC_ENGLISH_MESSAGE,
				Constants.GENERIC_MESSAGE), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { TemplateException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> templateException(HttpServletRequest request, TemplateException ex) {
		printLog(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, Constants.GENERIC_ENGLISH_MESSAGE,
				Constants.GENERIC_MESSAGE), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { SdkClientException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> sdkClientException(HttpServletRequest request, RuntimeException ex) {
		printLog(HttpStatus.BAD_REQUEST, ex, request);
		return new ResponseEntity<>(
				createProblem(HttpStatus.BAD_REQUEST, Constants.GENERIC_ENGLISH_MESSAGE, Constants.GENERIC_MESSAGE),
				new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { RuntimeException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> runTimeException(HttpServletRequest request, RuntimeException ex) {
		printLog(HttpStatus.BAD_REQUEST, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.BAD_REQUEST,
				Constants.GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE, Constants.GENERIC_BAD_REQUEST_ERROR_MESSAGE),
				new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { ServletException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> servletException(HttpServletRequest request, ServletException ex) {
		printLog(HttpStatus.BAD_REQUEST, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.BAD_REQUEST,
				Constants.GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE, Constants.GENERIC_BAD_REQUEST_ERROR_MESSAGE),
				new HttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { IllegalArgumentException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<Object> illegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
		printLog(HttpStatus.CONFLICT, ex, request);
		return new ResponseEntity<>(
				createProblem(HttpStatus.CONFLICT, Constants.GENERIC_CONFLICT_ERROR_MESSAGE_TITLE, ex.getMessage()),
				new HttpHeaders(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(value = { HttpClientErrorException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> httpClientErrorException(HttpServletRequest request, RuntimeException ex) {
		return new ResponseEntity<>(createProblemAmazonAws(HttpStatus.UNAUTHORIZED, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(value = { HttpRequestMethodNotSupportedException.class })
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public ResponseEntity<Object> unKnownException(HttpServletRequest request, ServletException ex) {
		printLog(HttpStatus.METHOD_NOT_ALLOWED, ex, request);
		return new ResponseEntity<>(createProblem(HttpStatus.METHOD_NOT_ALLOWED, Constants.GENERIC_ENGLISH_MESSAGE,
				Constants.GENERIC_MESSAGE), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
	}

	public Problem createProblem(HttpStatus status, String title, String detail) {
		Problem genericError = new Problem();
		genericError.setStatus(status.value());
		genericError.setTitle(title);
		genericError.setDetail(detail);
		genericError.setTraceId(MDC.get(Constants.TRACE_ID_PLACEHOLDER));
		ProblemError errorDetails = new ProblemError();
		errorDetails.setCode(status.toString());
		errorDetails.setDetail(detail);
		List<ProblemError> errorDetailsList = new ArrayList<>();
		errorDetailsList.add(errorDetails);
		genericError.setErrors(errorDetailsList);
		return genericError;
	}

	public Problem createProblemAmazonAws(HttpStatus status, Exception ex, String message, HttpServletRequest request) {
		Problem problem = new Problem();
		printLog(status, ex, request);
		problem.setTraceId(MDC.get(Constants.TRACE_ID_PLACEHOLDER));
		problem.setErrors(new ArrayList<>());
		if (message != null) {
			try {
				Type awsErrorsTypeList = new TypeToken<List<AwsSafeStorageErrorDto>>() {
				}.getType();
				List<AwsSafeStorageErrorDto> awsErrors = new Gson().fromJson(message, awsErrorsTypeList);
				for (AwsSafeStorageErrorDto errorAws : awsErrors) {
					for (String specificError : errorAws.getErrorList()) {
						ProblemError error = new ProblemError();
						error.setCode(errorAws.getResultCode());
						error.setDetail(specificError);
						problem.getErrors().add(error);
					}

				}
			} catch (Exception e) {
				return createProblem(HttpStatus.BAD_REQUEST, Constants.GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE,
						Constants.GENERIC_BAD_REQUEST_ERROR_MESSAGE);
			}
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			problem.setDetail(ExceptionUtils.getRootCauseMessage(ex.getCause()));
		}

		problem.setStatus(status.value());
		problem.setType(request.getRequestURI());
		problem.setTitle(ex.getClass().getName());
		return problem;
	}

	private void printLog(HttpStatus status, Exception ex, HttpServletRequest request) {
		log.error("ERROR CODE: {} {}", status.toString(), ex.getMessage());
		log.error("EXCEPTION TYPE: {}", ex.toString());
		log.error("ERROR TYPE: {}", status.getReasonPhrase());
		log.error("MESSAGE: {}", ex.getMessage());
		log.error("PATH: {}", request.getRequestURI());
		log.error("STACKTRACE: {}", ExceptionUtils.getStackTrace(ex));

	}

}
