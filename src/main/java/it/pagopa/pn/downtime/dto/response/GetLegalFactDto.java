package it.pagopa.pn.downtime.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Response Dto of the getLegalFact safeStorage request.
 */
@Getter

/**
 * Sets the download.
 *
 * @param download the new download
 */
@Setter

/**
 * Instantiates a new gets the legal fact dto.
 */
@NoArgsConstructor

public class GetLegalFactDto {
      
      /** The key. */
      private String key;
      
      /** The version id. */
      private String versionId;
      
      /** The content type. */
      private String contentType;
      
      /** The content length. */
      private BigDecimal contentLength;
      
      /** The checksum. */
      private String checksum;
      
      /** The retention until. */
      private String retentionUntil;
      
      /** The document type. */
      private String documentType;
      
      /** The lifecycle rule. */
      private String lifecycleRule;
      
      /** The status. */
      private String status;
      
      /** The result code. */
      private String resultCode;
      
      /** The download. */
      private DownloadLegalFactDto download;

}
