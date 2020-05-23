package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class ServiceQueryAttribute {

  private String type;
  private String name;
  private String defaultValue;
  private Boolean nullable;
  private Boolean isEnum;
  private String foreignModel;
}
