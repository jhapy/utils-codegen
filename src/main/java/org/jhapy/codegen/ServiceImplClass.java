package org.jhapy.codegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/05/2020
 */
@Data
public class ServiceImplClass {

  private Set<String> imports = new HashSet<>();
  private String packageName;
  private String className;
  private String interfaceName;
  private String modelClass;
  private String repositoryClass;

  private List<ServiceOperation> operations = new ArrayList<>();
}
