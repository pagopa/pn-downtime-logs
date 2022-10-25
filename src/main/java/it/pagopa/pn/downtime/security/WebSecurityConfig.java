//package it.pagopa.pn.downtime.security;
//
//import java.util.Collections;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//	
//	   @Override
//	    protected void configure(HttpSecurity httpSecurity) throws Exception {
//	        httpSecurity
//	                .csrf().disable()
//	                .exceptionHandling()
//					.and()
//					.cors()
//	                .and()
//			            .headers()
//			            .contentSecurityPolicy("default-src 'none'; script-src 'self'; connect-src 'self'; img-src 'self'; style-src 'self'; frame-ancestors 'none'; form-action 'self'")
//			            .and()
//			            .httpStrictTransportSecurity()
//			            .includeSubDomains(true)
//			            .maxAgeInSeconds(10886400)
//			            .and()
//			            .contentTypeOptions()
//			            .and()
//			            .xssProtection()
//			            .disable()
//			            .frameOptions()
//			            .deny()
//			        .and()
//			            .sessionManagement()
//			            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//					.and()
//					.authorizeRequests()
//					.antMatchers("/").permitAll();
//	    }
//    
//    @Bean
//	CorsConfigurationSource corsConfigurationSource() {
//		CorsConfiguration configuration = new CorsConfiguration();
//		configuration.addAllowedOrigin("*");
//		configuration.setAllowedMethods(Collections.singletonList("POST, PUT, GET, OPTIONS, DELETE"));
//		configuration.addAllowedHeader("*");
//		configuration.addAllowedMethod("*");
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", configuration);
//		return source;
//	}
//}
