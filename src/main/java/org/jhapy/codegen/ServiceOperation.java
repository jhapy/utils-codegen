package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class ServiceOperation {

  private String name;
  private String isTransactional;
  private String parameters;
  private String returnType;
  private Boolean hasReturn = Boolean.FALSE;
}
