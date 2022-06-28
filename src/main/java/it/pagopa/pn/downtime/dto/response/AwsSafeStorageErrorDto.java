package it.pagopa.pn.downtime.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AwsSafeStorageErrorDto {
      private String resultDescription;
      private List<String> errorList;
      private String resultCode;


}
