package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class EnumAttribute {

  private String name;
  private String value;
  private Boolean isLast;
}
