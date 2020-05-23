package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class EndpointOperation {

  private String name;
  private String operationPath;
  private String operationQuery;
  private String serviceParameters;
 private String endpointReturn;
  private String serviceReturn;
}
