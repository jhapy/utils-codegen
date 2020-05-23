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
public class EnumClass {

  private String className;
  private String packageName;
  private List<EnumAttribute> enumAttributeList = new ArrayList<>();
}
