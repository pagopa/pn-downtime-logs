package it.pagopa.pn.downtime.exceptions;


import java.util.HashMap;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class AppControllerAdvice {

    @Autowired
    ObjectMapper mapper;

    @ExceptionHandler(value = { NoSuchElementException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> noSuchElementException(HttpServletRequest request, RuntimeException ex) {
        return new ResponseEntity<>(getBody(HttpStatus.INTERNAL_SERVER_ERROR, ex, ex.getMessage(), request),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { RuntimeException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> runTimeException(HttpServletRequest request, RuntimeException ex) {
        return new ResponseEntity<>(getBody(HttpStatus.BAD_REQUEST, ex, ex.getMessage(), request), new HttpHeaders(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { HttpClientErrorException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> httpClientErrorException(HttpServletRequest request, RuntimeException ex) {
        return new ResponseEntity<>(getBody(HttpStatus.UNAUTHORIZED, ex, ex.getMessage(), request), new HttpHeaders(),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = { HttpRequestMethodNotSupportedException.class })
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<Object> unKnownException(HttpServletRequest request, ServletException ex) {
        return new ResponseEntity<>(getBody(HttpStatus.METHOD_NOT_ALLOWED, ex, ex.getMessage(), request),
                new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Method for generic handling of exceptions
     *
     * @param status
     * @param ex
     * @param message
     * @param request
     * @return String with the details of the exception
     */
    public HashMap<String, String> getBody(HttpStatus status, Exception ex, String message,
            HttpServletRequest request) {
        printLog(status, ex, message, request);
        HashMap<String, String> ce = new HashMap<>();
        Throwable cause = ex.getCause();
        String exception = ex.toString();
        ce.put("message", message);
        ce.put("exception", exception);
        if (cause != null) {
            ce.put("exceptionCause", ex.getCause().toString());
            ce.put("detailMessage", ExceptionUtils.getRootCauseMessage(cause));
        }

        return ce;
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
