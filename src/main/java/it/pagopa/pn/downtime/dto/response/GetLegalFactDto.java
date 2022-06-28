package it.pagopa.pn.downtime.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString

public class GetLegalFactDto {
      private String key;
      private String versionId;
      private String contentType;
      private BigDecimal contentLength;
      private String checksum;
      private String retentionUntil;
      private String documentType;
      private String lifecycleRule;
      private String status;
      private String resultCode;
      private DownloadLegalFactDto download;

}
