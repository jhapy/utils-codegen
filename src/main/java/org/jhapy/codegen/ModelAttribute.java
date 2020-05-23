package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class ModelAttribute {

  private String type;
  private String name;
  private String defaultValue;
  private Boolean nullable;
  private Boolean isJoin;
  private String joinType;
  private String joinColumn;
  private Boolean isLob = Boolean.FALSE;
  private String converter;
  private Boolean isEnum;
}
