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
public class EndpointClass {

  private Set<String> imports = new HashSet<>();
  private String className;
  private String packageName;
  private String serviceName;
  private String searchServiceClass;
  private String serviceClass;
  private String modelClass;
  private String readingRole;
  private String writingRole;
  private String dtoClass;
  private String criteriaClass;

  private List<EndpointOperation> endpointOperations = new ArrayList<>();
}
