package org.jhapy.codegen;

import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class SearchServiceImplAttribute {

  private String name;
  private String getter;
  private Boolean isJoin;
  private String buildSpecification;
  private String metaModelClass;
  private String targetMetaModelClass;
}
