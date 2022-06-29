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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import freemarker.template.TemplateException;
import it.pagopa.pn.downtime.dto.response.AwsSafeStorageErrorDto;
import it.pagopa.pn.downtime.pn_downtime.model.Problem;
import it.pagopa.pn.downtime.pn_downtime.model.ProblemError;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class AppControllerAdvice {

	@Autowired
	ObjectMapper mapper;

	@ExceptionHandler(value = { NoSuchElementException.class, RestClientException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> generalException(HttpServletRequest request, RuntimeException ex) {
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { NoSuchAlgorithmException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> noSuchAlgorithmException(HttpServletRequest request, GeneralSecurityException ex) {
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { IOException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> iOException(HttpServletRequest request, IOException ex) {
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { TemplateException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> templateException(HttpServletRequest request, TemplateException ex) {
		return new ResponseEntity<>(createProblem(HttpStatus.INTERNAL_SERVER_ERROR, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(value = { RuntimeException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> runTimeException(HttpServletRequest request, RuntimeException ex) {
		return new ResponseEntity<>(createProblem(HttpStatus.BAD_REQUEST, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.BAD_REQUEST);
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
		return new ResponseEntity<>(createProblem(HttpStatus.METHOD_NOT_ALLOWED, ex, ex.getMessage(), request),
				new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
	}

	public Problem createProblem(HttpStatus status, Exception ex, String message, HttpServletRequest request) {
		Problem problem = new Problem();
		printLog(status, ex, message, request);
		if (message != null) {
			String[] errorMessages = message.split(",");
			problem.setErrors(new ArrayList<>());
			for (String s : errorMessages) {
				ProblemError error = new ProblemError();
				error.setCode(status.toString());
				error.setDetail(s);
				problem.getErrors().add(error);
			}
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			problem.setDetail(ExceptionUtils.getRootCauseMessage(ex.getCause()));
		}

		problem.setStatus(status.value());
		problem.setType(request.getRequestURI());
		problem.setTitle(ex.getClass().getName());
		log.info(problem.toString());
		return problem;
	}

	public Problem createProblemAmazonAws(HttpStatus status, Exception ex, String message, HttpServletRequest request) {
		Problem problem = new Problem();
		printLog(status, ex, message, request);
		problem.setErrors(new ArrayList<>());
		if(message!=null) {
		String awsMessage = message.substring(message.indexOf("["));
		try {
			Type awsErrorsTypeList = new TypeToken<List<AwsSafeStorageErrorDto>>() {
			}.getType();
			List<AwsSafeStorageErrorDto> awsErrors = new Gson().fromJson(awsMessage, awsErrorsTypeList);
			for (AwsSafeStorageErrorDto errorAws : awsErrors) {
				for (String specificError : errorAws.getErrorList()) {
					ProblemError error = new ProblemError();
					error.setCode(errorAws.getResultCode());
					error.setDetail(specificError);
					problem.getErrors().add(error);
				}

			}
		} catch (Exception e) {
			return createProblem(HttpStatus.BAD_REQUEST, ex, ex.getMessage(), request);
		}
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			problem.setDetail(ExceptionUtils.getRootCauseMessage(ex.getCause()));
		}

		problem.setStatus(status.value());
		problem.setType(request.getRequestURI());
		problem.setTitle(ex.getClass().getName());
		log.info(problem.toString());
		return problem;
	}

	private void printLog(HttpStatus status, Exception ex, String message, HttpServletRequest request) {
		log.error("ERROR CODE: {} {}", status.toString(), message, ex.getMessage());
		log.error("EXCEPTION TYPE: {}", ex.toString());
		log.error("ERROR TYPE: {}", status.getReasonPhrase());
		log.error("MESSAGE: {}", message);
		log.error("PATH: {}", request.getRequestURI());
		log.error("STACKTRACE: {}", ExceptionUtils.getStackTrace(ex));

	}

}
