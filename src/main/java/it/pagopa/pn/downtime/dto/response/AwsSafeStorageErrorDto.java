package it.pagopa.pn.downtime.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response Dto of SafeStorage in case of errors in the request.
 */
@Getter

/**
 * Instantiates a new aws safe storage error dto.
 */
@NoArgsConstructor
public class AwsSafeStorageErrorDto {
      
      /** The result description. */
      private String resultDescription;
      
      /** The error list. */
      private List<String> errorList;
      
      /** The result code. */
      private String resultCode;
}
