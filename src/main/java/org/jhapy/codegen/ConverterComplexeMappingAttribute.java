package org.jhapy.codegen;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class ConverterComplexeMappingAttribute {

  private String modelClass;
  private String dtoClass;
  private String attributesExclusionList;

  private List<ConverterStringListAttribute> stringListAttributes = new ArrayList<>();
  private List<ConverterForeignAttribute> attributes = new ArrayList<>();
}
