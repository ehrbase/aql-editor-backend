package org.ehrbase.aqleditor.dto.aql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class QueryValidationResponse {

  private boolean valid;
  private String message;

}
