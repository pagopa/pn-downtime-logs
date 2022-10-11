package it.pagopa.pn.downtime.filter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.pagopa.pn.downtime.exceptions.DowntimeException;
import it.pagopa.pn.downtime.util.RandomUtils;
import it.pagopa.pn.downtime.util.external.CognitoApiHandler;


/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Slf4j
@Component
public class MDCWebFilter extends OncePerRequestFilter {

	@Autowired
	private CognitoApiHandler cognitoApiHandler;
	
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
    	if(StringUtils.isBlank(request.getHeader("Auth"))) {
    		throw new RuntimeException("No Auth header found for current request: " + request.getRequestURI());
    	}
        try {
        	MDC.put("trace_id", new RandomUtils().generateRandomTraceId());
			log.info("Getting user identifier...");
			long serviceStartTime = System.currentTimeMillis();
        	MDC.put("user_identifier", cognitoApiHandler.getUserIdentifier(request.getHeader("Auth")));
			long performanceMillis = System.currentTimeMillis() - serviceStartTime;
			log.info("User identifier retrieved in {} ms", performanceMillis);
			MDC.put("validationTime", String.valueOf(performanceMillis));
            filterChain.doFilter(request, response);
        } catch (DowntimeException | IllegalArgumentException | JSONException e) {
        	throw new RuntimeException("Exception retrieving user identifier");
		} finally {
            MDC.remove("user_identifier");
			MDC.remove("trace_id");
			MDC.remove("validationTime");
        }
    }
    
//	@Override
//	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//		return "/health-check/status".equals(request.getRequestURI());
//	}
}