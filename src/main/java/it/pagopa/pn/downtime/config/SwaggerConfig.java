package it.pagopa.pn.downtime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.classmate.TypeResolver;

import it.pagopa.pn.downtime.PnDowntimeApplication;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

	private ApiInfo getApiInfo() {
		return new ApiInfoBuilder().title("Pn-downtime-logs")
				.description("This application manages the downtimes of the pagoPa Services").version("1.0.0").build();
	}

	@Bean
	public Docket api(TypeResolver typeResolver) {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage(PnDowntimeApplication.class.getPackageName()))
				.paths(PathSelectors.any()).build().apiInfo(getApiInfo());
	}

}
