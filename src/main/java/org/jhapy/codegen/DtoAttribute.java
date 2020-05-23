package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class DtoAttribute {

  private String type;
  private String name;
  private String defaultValue;
  private Boolean nullable;
}
