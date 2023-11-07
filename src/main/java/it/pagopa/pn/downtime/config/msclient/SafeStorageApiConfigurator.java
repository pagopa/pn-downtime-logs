package it.pagopa.pn.downtime.config.msclient;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.api.FileDownloadApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.api.FileMetadataUpdateApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.api.FileUploadApi;
import it.pagopa.pn.downtime.generated.openapi.msclient.safestorage.v1.client.ApiClient;

@Configuration
public class SafeStorageApiConfigurator {
	@Bean
	@Primary
	public FileUploadApi fileUploadApiReactive() {
		return new FileUploadApi(getNewApiClient());
	}

	@Bean
	@Primary
	public FileDownloadApi fileDownloadApiReactive() {
		return new FileDownloadApi(getNewApiClient());
	}

	@Bean
	@Primary
	public FileMetadataUpdateApi fileMetadataUpdateApiReactive() {
		return new FileMetadataUpdateApi(getNewApiClient());
	}

	@NotNull
	private ApiClient getNewApiClient() {
		return new ApiClient();
	}
}
