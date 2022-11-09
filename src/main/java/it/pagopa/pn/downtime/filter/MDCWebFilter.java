package it.pagopa.pn.downtime.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.pagopa.pn.downtime.util.Constants;
import it.pagopa.pn.downtime.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * WebFilter that puts in the MDC log map a unique identifier for incoming requests.
 */
@Slf4j
@Component
public class MDCWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
			MDC.put(Constants.TRACE_ID_PLACEHOLDER, new RandomUtils().generateRandomTraceId());
			filterChain.doFilter(request, response);
        } catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		} finally {
			MDC.remove(Constants.TRACE_ID_PLACEHOLDER);

        }
    }

}