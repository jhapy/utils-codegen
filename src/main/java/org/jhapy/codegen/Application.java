package org.jhapy.codegen;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.internal.text.StringEscapeUtils;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jHapy Lead Dev
 * @version 1.0
 * @since 11/05/2020
 */
public class Application {

  public static final String CONFIG_PACKAGE = "configPackage";
  public static final String BASE_PACKAGE = "basePackage";
  public static final String DTO_PACKAGE = "dtoPackage";

  protected String moduleName = "test";
  protected String outputFolder = "./app-test-server";
  protected String sourceFolder = "src/main/java";
  protected String basePackage = "org.jhapy.test";
  protected String dtoPackage = "org.jhapy.test.generated.dto";
  protected String repositoryPackage = "org.jhapy.test.repository";
  protected String servicePackage = "org.jhapy.test.service";
  protected String converterPackage = "org.jhapy.test.converter";
  protected String searchServicePackage = "org.jhapy.test.searchService";
  protected String searchCriteriaPackage = "org.jhapy.test.searchCriteria";
  protected String endpointPackage = "org.jhapy.test.endpoint";
  protected String modelPackage = "org.jhapy.test.domain";
  protected String metaModelPackage = "org.jhapy.test.domain";
  protected String serviceQueryPackage = "org.jhapy.test.generated.serviceQuery";
  protected String configPackage = "org.wannagoframework.test.config";

  protected Map<String, String> typeMapping = new HashMap<String, String>();
  protected Map<String, String> importMapping = new HashMap<String, String>();

  public Application(String filePath) throws IOException {
    typeMapping = getTypeMappings();
    typeMapping.put("date", "LocalDate");
    typeMapping.put("date-time", "LocalDate");

    importMapping = getImportMappings();
    importMapping.put("LocalDate", "java.time.LocalDate");

    OpenAPIV3Parser openApiParser = new OpenAPIV3Parser();
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setFlatten(true);
    SwaggerParseResult parseResult = openApiParser.readLocation(filePath, null, options);

    OpenAPI openAPI = parseResult.getOpenAPI();

    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");

    Handlebars handlebars = new Handlebars(loader);
    handlebars.setPrettyPrint(true);

    Template modelTemplate = handlebars.compile("model");
    Template dtoTemplate = handlebars.compile("dto");
    Template criteriaTemplate = handlebars.compile("searchCriteria");
    Template enumTemplate = handlebars.compile("enum");
    Template repositoryTemplate = handlebars.compile("repository");
    Template searchServiceTemplate = handlebars.compile("searchService");
    Template searchServiceImpTemplate = handlebars.compile("searchServiceImpl");
    Template serviceTemplate = handlebars.compile("service");
    Template serviceImpTemplate = handlebars.compile("serviceImpl");
    Template endpointTemplate = handlebars.compile("endpoint");
    Template converterTemplate = handlebars.compile("converter");
    Template serviceQueryTemplate = handlebars.compile("serviceQueryObject");

    final ConverterClass converterClass = new ConverterClass();
    converterClass.setPackageName(converterPackage);
    converterClass.setClassName(StringUtils.capitalize(moduleName) + "Converter");
    converterClass.setConverterFunction(StringUtils.uncapitalize(moduleName) + "Converter");

    final Map<String, EnumClass> enumModelClasses = new HashMap<>();
    final Map<String, EnumClass> enumDtoClasses = new HashMap<>();
    final Map<String, EnumClass> enumQueryClasses = new HashMap<>();
    final Map<String, ModelClass> modelClasses = new HashMap<>();
    final Map<String, DtoClass> dtoClasses = new HashMap<>();
    final Map<String, RepositoryClass> repositoryClasses = new HashMap<>();
    final Map<String, SearchServiceClass> searchServiceClasses = new HashMap<>();
    final Map<String, SearchServiceImplClass> searchServiceImplClasses = new HashMap<>();
    final Map<String, ServiceClass> serviceClasses = new HashMap<>();
    final Map<String, ServiceImplClass> serviceImplClasses = new HashMap<>();
    final Map<String, EndpointClass> endpointClasses = new HashMap<>();
    final Map<String, CriteriaClass> criteriaClasses = new HashMap<>();
    final Map<String, ServiceQueryClass> serviceQueryClasses = new HashMap<>();

    openAPI.getComponents().getSchemas().forEach((s, schema) -> {
      if (schema.getExtensions() != null && schema.getExtensions().get("x-subType")
          .equals("model")) {
        String repositoryClassName = s + "Repository";
        String repositoryImport = repositoryPackage + "." + repositoryClassName;

        String modelClassName = s;
        String modelImport = modelPackage + "." + modelClassName;

        String dtoClassName = s;
        String dtoImport = dtoPackage + "." + dtoClassName;

        String searchServiceClassName = s + "SearchService";
        String searchServiceImport = searchServicePackage + "." + searchServiceClassName;

        String searchServiceImplClassName = s + "SearchServiceImpl";
        String searchServiceImplImport = searchServicePackage + "." + searchServiceImplClassName;

        String searchCriteriaClassName = s + "Criteria";
        String searchCriteriaImport = searchCriteriaPackage + "." + searchCriteriaClassName;

        String serviceClassName = s + "Service";
        String serviceImport = servicePackage + "." + serviceClassName;

        String serviceImplClassName = s + "ServiceImpl";
        String serviceImplImport = servicePackage + "." + serviceImplClassName;

        String endpointClassName = s + "ServiceEndpoint";
        String endpointImport = endpointPackage + "." + endpointClassName;

        String criteriaClassName = s + "Criteria";
        String criteriaImport = searchCriteriaPackage + "." + criteriaClassName;

        final ModelClass modelClass = new ModelClass();
        modelClass.setClassName(modelClassName);
        modelClass.setPackageName(modelPackage);

        if (!modelClasses.containsKey(modelClassName)) {
          modelClasses.put(modelClassName, modelClass);
        }

        final DtoClass dtoClass = new DtoClass();
        dtoClass.setClassName(dtoClassName);
        dtoClass.setPackageName(dtoPackage);

        if (!dtoClasses.containsKey(dtoClassName)) {
          dtoClasses.put(dtoClassName, dtoClass);
        }

        final RepositoryClass repositoryClass = new RepositoryClass();
        repositoryClass.setInterfaceName(repositoryClassName);
        repositoryClass.setModelClass(modelClassName);
        repositoryClass.getImports().add(modelImport);
        repositoryClass.setPackageName(repositoryPackage);

        if (!repositoryClasses.containsKey(repositoryClassName)) {
          repositoryClasses.put(repositoryClassName, repositoryClass);
        }

        final SearchServiceClass searchServiceClass = new SearchServiceClass();
        searchServiceClass.setInterfaceName(searchServiceClassName);
        searchServiceClass.setPackageName(searchServicePackage);
        searchServiceClass.setModelClass(modelClassName);
        searchServiceClass.getImports().add(modelImport);
        searchServiceClass.setSearchCriteriaClass(searchCriteriaClassName);
        searchServiceClass.getImports().add(searchCriteriaImport);

        if (!searchServiceClasses.containsKey(searchServiceClassName)) {
          searchServiceClasses.put(searchServiceClassName, searchServiceClass);
        }

        final SearchServiceImplClass searchServiceImplClass = new SearchServiceImplClass();
        searchServiceImplClass.setClassName(searchServiceImplClassName);
        searchServiceImplClass.setInterfaceName(searchServiceClassName);
        searchServiceImplClass.setPackageName(searchServicePackage);
        searchServiceImplClass.setRepositoryClass(repositoryClassName);
        searchServiceImplClass.getImports().add(repositoryImport);
        searchServiceImplClass.getImports().add(modelImport);
        searchServiceImplClass.setSearchCriteriaClass(searchCriteriaClassName);
        searchServiceImplClass.getImports().add(searchCriteriaImport);
        searchServiceImplClass.setModelClass(s);

        if (!searchServiceImplClasses.containsKey(searchServiceImplClassName)) {
          searchServiceImplClasses.put(searchServiceImplClassName, searchServiceImplClass);
        }

        final ServiceClass serviceClass = new ServiceClass();
        serviceClass.setPackageName(servicePackage);
        serviceClass.setInterfaceName(serviceClassName);
        serviceClass.setModelClass(s);
        serviceClass.getImports().add(modelImport);

        if (!serviceClasses.containsKey(serviceClassName)) {
          serviceClasses.put(serviceClassName, serviceClass);
        }

        final ServiceImplClass serviceImplClass = new ServiceImplClass();
        serviceImplClass.setPackageName(servicePackage);
        serviceImplClass.setClassName(serviceImplClassName);
        serviceImplClass.setInterfaceName(serviceClassName);
        serviceImplClass.setModelClass(s);
        serviceImplClass.getImports().add(modelImport);
        serviceImplClass.setRepositoryClass(repositoryClassName);
        serviceImplClass.getImports().add(repositoryImport);

        if (!serviceImplClasses.containsKey(serviceImplClassName)) {
          serviceImplClasses.put(serviceImplClassName, serviceImplClass);
        }

        final EndpointClass endpointClass = new EndpointClass();
        endpointClass.setPackageName(endpointPackage);
        endpointClass.setClassName(endpointClassName);
        endpointClass.setServiceClass(serviceClassName);
        endpointClass.getImports().add(serviceImport);
        endpointClass.setSearchServiceClass(searchServiceClassName);
        endpointClass.getImports().add(searchServiceImport);
        endpointClass.setDtoClass(dtoPackage + "." + dtoClassName);
        endpointClass.setCriteriaClass(searchCriteriaClassName);
        endpointClass.getImports().add(searchCriteriaImport);
        endpointClass.setModelClass(modelClassName);
        endpointClass.getImports().add(modelImport);
        endpointClass.setServiceName(StringUtils.uncapitalize(s) + "Service");
        endpointClass.setReadingRole("ROLE_" + moduleName.toUpperCase() + "_READ");
        endpointClass.setWritingRole("ROLE_" + moduleName.toUpperCase() + "_WRITE");

        if (!endpointClasses.containsKey(endpointClassName)) {
          endpointClasses.put(endpointClassName, endpointClass);
        }

        final CriteriaClass criteriaClass = new CriteriaClass();
        criteriaClass.setClassName(criteriaClassName);
        criteriaClass.setPackageName(searchCriteriaPackage);

        if (!criteriaClasses.containsKey(criteriaClassName)) {
          criteriaClasses.put(criteriaClassName, criteriaClass);
        }

        AtomicBoolean hasComplexeAttributes = new AtomicBoolean(false);

        ConverterComplexeMappingAttribute converterComplexeMappingAttribute = new ConverterComplexeMappingAttribute();
        converterComplexeMappingAttribute.setDtoClass(dtoPackage + "." + dtoClassName);
        converterComplexeMappingAttribute.setModelClass(modelClassName);
        converterClass.getImports().add(modelImport);

        ConverterSimpleMappingAttribute simpleMappingAttribute = new ConverterSimpleMappingAttribute();
        simpleMappingAttribute.setDtoClass(dtoPackage + "." + dtoClassName);
        simpleMappingAttribute.setModelClass(s);

        Map<String, Schema> properties = schema.getProperties();
        properties.keySet().forEach(s1 -> {
          Schema property = properties.get(s1);

          // Model Stuff
          ModelAttribute modelAttribute = new ModelAttribute();
          modelAttribute.setName(s1);
          modelAttribute
              .setNullable(property.getNullable() == null ? true : property.getNullable());

          ConverterRepository converterRepository = new ConverterRepository();

          if (StringUtils.isNotBlank(property.get$ref())) {
            String joinColumn = getSchemaType(property);
            joinColumn = StringUtils.uncapitalize(joinColumn);
            joinColumn = joinColumn.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase() + "_ID";
            modelAttribute.setIsJoin(true);
            modelAttribute.setType(s);
            modelAttribute.setJoinType("@ManyToOne");
            modelAttribute.setJoinColumn(joinColumn);
            modelClass.getImports().add("javax.persistence.JoinColumn");
            modelClass.getImports().add("javax.persistence.ManyToOne");

            converterRepository.setInterfaceName(repositoryClassName);
            converterRepository.setInterfaceAttribute(StringUtils.uncapitalize(s) + "Repository");
            converterClass.getImports().add(repositoryImport);
            converterClass.getRepositories().add(converterRepository);

            ConverterForeignAttribute converterForeignAttribute = new ConverterForeignAttribute();
            converterForeignAttribute.setAttributeName(StringUtils.capitalize(s1));
            converterForeignAttribute
                .setRepositoryInterface(StringUtils.uncapitalize(s) + "Repository");
            converterComplexeMappingAttribute.getAttributes().add(converterForeignAttribute);
            hasComplexeAttributes.set(true);

          } else if (property.getEnum() != null && property.getEnum().size() > 0) {
            EnumClass enumDtoClass = new EnumClass();
            enumDtoClass.setPackageName(dtoPackage);
            enumDtoClass.setClassName(StringUtils.capitalize(s1) + "Enum");

            EnumClass enumModelClass = new EnumClass();
            enumModelClass.setPackageName(modelPackage);
            enumModelClass.setClassName(StringUtils.capitalize(s1) + "Enum");

            property.getEnum().forEach(o -> {
              EnumAttribute enumAttribute = new EnumAttribute();
              enumAttribute.setName(o.toString());
              enumAttribute.setIsLast(false);
              enumDtoClass.getEnumAttributeList().add(enumAttribute);
              enumModelClass.getEnumAttributeList().add(enumAttribute);
            });
            enumDtoClass.getEnumAttributeList().get(enumDtoClass.getEnumAttributeList().size() - 2)
                .setIsLast(true);
            enumModelClass.getEnumAttributeList()
                .get(enumModelClass.getEnumAttributeList().size() - 2).setIsLast(true);

            if (!enumDtoClasses.containsKey(enumDtoClass.getClassName())) {
              enumDtoClasses.put(enumDtoClass.getClassName(), enumDtoClass);
            }
            if (!enumModelClasses.containsKey(enumModelClass.getClassName())) {
              enumModelClasses.put(enumModelClass.getClassName(), enumModelClass);
            }

            modelAttribute.setType(StringUtils.capitalize(s1) + "Enum");
            modelAttribute.setIsEnum(true);
            modelClass.getImports().add("javax.persistence.Enumerated");
            modelClass.getImports().add("javax.persistence.EnumType");
          } else {
            modelAttribute.setType(getSchemaType(property));
            if (importMapping.containsKey(modelAttribute.getType())) {
              modelClass.getImports().add(importMapping.get(modelAttribute.getType()));
            }
            if (property.getType() != null && property.getType().equalsIgnoreCase("string")
                && StringUtils.isNotBlank(property.getFormat()) && property.getFormat()
                .equalsIgnoreCase("long-string")) {
              modelAttribute.setIsLob(true);
              modelClass.getImports().add("javax.persistence.Lob");
            }
          }
          if (property.getExtensions() != null && property.getExtensions()
              .containsKey("x-isEncrypted")) {
            if ((Boolean) property.getExtensions().get("x-isEncrypted")) {
              modelAttribute.setConverter("TransitConverterFor" + getSchemaType(property));
              modelClass.getImports().add("javax.persistence.Convert");
              modelClass.getImports().add(
                  "org.jhapy.test.dbAttributeConverter." + "TransitConverterFor" + getSchemaType(
                      property));
            }
          }
          modelClass.getAttributeList().add(modelAttribute);

          // Repository Stuff

          // DTO Stuff
          DtoAttribute dtoAttribute = new DtoAttribute();
          dtoAttribute.setNullable(property.getNullable() == null ? true : property.getNullable());
          if (importMapping.containsKey(modelAttribute.getType())) {
            dtoClass.getImports().add(importMapping.get(modelAttribute.getType()));
          }

          if (StringUtils.isNotBlank(property.get$ref())) {
            dtoAttribute.setName(s1 + "Id");
            dtoAttribute.setType("Long");
          } else {
            dtoAttribute.setName(s1);
            if (property.getEnum() != null && property.getEnum().size() > 0) {
              dtoAttribute.setType(StringUtils.capitalize(s1) + "Enum");
            } else {
              dtoAttribute.setType(getSchemaType(property));
            }
          }
          dtoClass.getAttributeList().add(dtoAttribute);

          // Search Criteria Stuff
          CriteriaAttribute criteriaAttribute = new CriteriaAttribute();
          SearchServiceImplAttribute searchServiceImplAttribute = new SearchServiceImplAttribute();
          searchServiceImplAttribute.setGetter("get" + StringUtils.capitalize(s1));
          searchServiceImplAttribute.setMetaModelClass(StringUtils.capitalize(s) + "_");
          searchServiceImplClass.getImports()
              .add(metaModelPackage + "." + StringUtils.capitalize(s) + "_");
          searchServiceImplAttribute.setName(s1);
          if (StringUtils.isNotBlank(property.get$ref())) {
            criteriaAttribute.setName(s1 + "Id");
            criteriaAttribute.setType("LongFilter");
            searchServiceImplAttribute.setGetter("get" + StringUtils.capitalize(s1) + "Id");
            searchServiceImplAttribute.setIsJoin(true);
            searchServiceImplAttribute.setTargetMetaModelClass(getSchemaType(property) + "_");
            searchServiceImplClass.getImports()
                .add(metaModelPackage + "." + getSchemaType(property) + "_");
            searchServiceImplAttribute.setBuildSpecification("buildSpecification");
          } else {
            if (property.getEnum() != null && property.getEnum().size() > 0) {
              CriteriaFilterAttribute criteriaFilterAttribute = new CriteriaFilterAttribute();
              criteriaFilterAttribute.setName(StringUtils.capitalize(s1) + "EnumFilter");
              criteriaFilterAttribute.setRelatedEnum(StringUtils.capitalize(s1) + "Enum");
              criteriaClass.getFilterAttributeList().add(criteriaFilterAttribute);
              criteriaAttribute.setName(s1);
              criteriaAttribute.setType(StringUtils.capitalize(s1) + "EnumFilter");
              searchServiceImplAttribute.setBuildSpecification("buildSpecification");
              criteriaClass.getImports()
                  .add(modelPackage + "." + StringUtils.capitalize(s1) + "Enum");
            } else {
              criteriaAttribute.setName(s1);
              criteriaAttribute.setType(getSchemaType(property) + "Filter");

              if (getSchemaType(property).equalsIgnoreCase("String")) {
                searchServiceImplAttribute.setBuildSpecification("buildStringSpecification");
              } else {
                searchServiceImplAttribute.setBuildSpecification("buildRangeSpecification");
              }
            }
          }
          searchServiceImplClass.getAttributeList().add(searchServiceImplAttribute);
          criteriaClass.getAttributeList().add(criteriaAttribute);
        });

        if (hasComplexeAttributes.get()) {
          converterClass.getComplexeMapping().add(converterComplexeMappingAttribute);
        } else {
          converterClass.getSimpleMapping().add(simpleMappingAttribute);
        }
      } else if (schema.getExtensions() != null && schema.getExtensions().get("x-subType")
          .equals("serviceQuery")) {
        String serviceQueryClassName = s;
        String serviceQueryImport = serviceQueryPackage + "." + serviceQueryClassName;

        ServiceQueryClass serviceQueryClass = new ServiceQueryClass();
        serviceQueryClass.setPackageName(serviceQueryPackage);
        serviceQueryClass.setClassName(serviceQueryClassName);

        if (!serviceQueryClasses.containsKey(serviceQueryClassName)) {
          serviceQueryClasses.put(serviceQueryClassName, serviceQueryClass);
        }

        Map<String, Schema> properties = schema.getProperties();
        properties.keySet().forEach(s1 -> {
          Schema property = properties.get(s1);

          // Model Stuff
          ServiceQueryAttribute modelAttribute = new ServiceQueryAttribute();
          modelAttribute.setName(s1);
          modelAttribute
              .setNullable(property.getNullable() == null ? true : property.getNullable());

          if (property.getExtensions() != null
              && property.getExtensions().get("x-foreignModel") != null) {
            Map<String, String> map = (Map<String, String>) property.getExtensions()
                .get("x-foreignModel");
            String foreignModel = map.get("$ref");
            modelAttribute.setType(getSchemaType(property));
            modelAttribute
                .setForeignModel(foreignModel.substring("#/components/schemas/".length()));
          } else if (property.getEnum() != null && property.getEnum().size() > 0) {
            EnumClass enumQueryClass = new EnumClass();
            enumQueryClass.setPackageName(serviceQueryPackage);
            enumQueryClass.setClassName(StringUtils.capitalize(s1) + "Enum");

            property.getEnum().forEach(o -> {
              EnumAttribute enumAttribute = new EnumAttribute();
              enumAttribute.setName(o.toString());
              enumAttribute.setIsLast(false);
              enumQueryClass.getEnumAttributeList().add(enumAttribute);
            });
            enumQueryClass.getEnumAttributeList()
                .get(enumQueryClass.getEnumAttributeList().size() - 2).setIsLast(true);

            if (!enumQueryClasses.containsKey(enumQueryClass.getClassName())) {
              enumQueryClasses.put(enumQueryClass.getClassName(), enumQueryClass);
            }

            modelAttribute.setType(StringUtils.capitalize(s1) + "Enum");
            modelAttribute.setIsEnum(true);
          } else {

            modelAttribute.setType(getSchemaType(property));
            if ( property instanceof ArraySchema) {
              Schema items = ((ArraySchema) property).getItems();
              String obj = getSchemaType( items );
              if ( dtoClasses.containsKey( obj ))
              serviceQueryClass.getImports().add( dtoPackage+"."+obj);
              serviceQueryClass.getImports().add(importMapping.get("List"));
            }

            if (importMapping.containsKey(modelAttribute.getType())) {
              serviceQueryClass.getImports().add(importMapping.get(modelAttribute.getType()));
            }
          }
          serviceQueryClass.getAttributeList().add(modelAttribute);
        });
      }
    });

    openAPI.getPaths().forEach((s, path) -> {
      String servicePath = s;
      path.readOperations().forEach(operation -> {
        String serviceName = StringUtils
            .capitalize(servicePath.substring(1, servicePath.indexOf("/", 1)));
        String endpointClassName = serviceName + "Endpoint";

        EndpointClass endpointClass;

        if (endpointClasses.containsKey(endpointClassName)) {
          endpointClass = endpointClasses.get(endpointClassName);
        } else {
          endpointClass = new EndpointClass();
          endpointClass.setPackageName(endpointPackage);
          endpointClass.setServiceName(StringUtils.uncapitalize(serviceName));
          endpointClass.setServiceClass(serviceName);
          endpointClass.setClassName(endpointClassName);
          endpointClass.getImports().add(servicePackage+"."+serviceName);
          endpointClasses.put(endpointClass.getClassName(), endpointClass);
        }
        ServiceClass serviceClass;
        ServiceImplClass serviceImplClass;
        if (serviceClasses.containsKey(serviceName)) {
          serviceClass = serviceClasses.get(serviceName);
          serviceImplClass = serviceImplClasses.get(serviceName + "Impl");
        } else {
          serviceClass = new ServiceClass();
          serviceClass.setPackageName(servicePackage);
          serviceClass.setInterfaceName(serviceName);
          serviceClasses.put(serviceName, serviceClass);

          serviceImplClass = new ServiceImplClass();
          serviceImplClass.setClassName(serviceName + "Impl");
          serviceImplClass.setPackageName(servicePackage);
          serviceImplClass.setInterfaceName(serviceName );
          serviceImplClasses.put(serviceName + "Impl", serviceImplClass);
        }

        EndpointOperation endpointOperation = new EndpointOperation();
        endpointOperation.setName(operation.getOperationId());
        endpointOperation.setOperationPath(operation.getOperationId());

        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setName(operation.getOperationId());

        String returnType;
        String endpointReturn;
        String serviceReturn;
        if (operation.getResponses().getExtensions() != null
            && operation.getResponses().getExtensions().get("x-value") != null) {
          Map<String, Object> returnTypeSchema = (Map<String, Object>) operation.getResponses()
              .getExtensions().get("x-value");
          Schema schema;
          if ( returnTypeSchema.get("type") != null && returnTypeSchema.get("type").toString().equalsIgnoreCase("array") ) {
            schema = new ArraySchema();
            Schema x = new Schema();
            x.set$ref( ( ( Map<String,String> )returnTypeSchema.get( "items") ).get("$ref") );
            ( ( ArraySchema )schema ).setItems( x );
          } else {
            schema = new Schema();
          }
          schema.setType(returnTypeSchema.get("type") != null ? returnTypeSchema.get("type").toString() : null );
          schema.setFormat(returnTypeSchema.get("format") != null ? returnTypeSchema.get("format").toString() : null );
          schema.set$ref(returnTypeSchema.get("$ref") != null ? returnTypeSchema.get("$ref").toString() : null );
          returnType = getSchemaType(schema);
          serviceOperation.setHasReturn(true);

          if ( returnTypeSchema.get("$ref") != null ) {
            serviceReturn = returnType + " result";
            endpointReturn = "handleResult(loggerPrefix, mapperFacade.map( result, " + dtoPackage + "." + returnType + ".class, getOrikaContext(query)) )";
          } else {
            if ( schema.getType().equalsIgnoreCase("array") ) {
              ArraySchema a = ( ArraySchema )schema;
              String subType = a.getItems().get$ref();
              if ( subType != null ) {
                serviceReturn = "List<"+subType.substring("#/components/schemas/".length()) + "> result";
              endpointReturn = "handleResult(loggerPrefix, mapperFacade.mapAsList( result, " + dtoPackage + "." + subType.substring("#/components/schemas/".length())  + ".class, getOrikaContext(query)) )";
              }  else {
                serviceReturn = "List<"+returnType + "> result";
                endpointReturn = "handleResult(loggerPrefix, mapperFacade.mapAsList( result, " + dtoPackage + "." + returnType + ".class, getOrikaContext(query)) )";
              }
            } else {
              serviceReturn = returnType + " result";
              endpointReturn = "handleResult(loggerPrefix, result)";
            }
          }
        } else {
          returnType = "void";
          endpointReturn = "handleResult(loggerPrefix)";
          serviceReturn = null;
        }
        endpointOperation.setEndpointReturn( endpointReturn );
        endpointOperation.setServiceReturn( serviceReturn );
        serviceOperation.setReturnType(returnType);

        Content content = operation.getRequestBody().getContent();
        MediaType mediaType = content.get("application/json");
        String ref = getSchemaType(mediaType.getSchema());
        endpointOperation.setOperationQuery(ref);
        endpointClass.getImports().add(serviceQueryPackage + "." + ref);
        endpointClass.getEndpointOperations().add(endpointOperation);

        ServiceQueryClass serviceQueryClass = serviceQueryClasses.get(ref);

        List<ServiceQueryAttribute> serviceQueryAttributes = serviceQueryClass.getAttributeList();
        StringBuilder serviceQueryParameters = new StringBuilder();
        StringBuilder serviceParameters = new StringBuilder();
        serviceQueryAttributes.forEach(serviceQueryAttribute -> {
          if (serviceQueryAttribute.getForeignModel() != null) {
            serviceQueryParameters.append("mapperFacade.map( query.get")
                .append(StringUtils.capitalize(serviceQueryAttribute.getName())).append("(), ")
                .append(serviceQueryAttribute.getForeignModel())
                .append(".class, getOrikaContext(query)), ");
            endpointClass.getImports()
                .add(modelPackage + "." + serviceQueryAttribute.getForeignModel());
            // TODO: Ajouter le test de savoir si termine par Id....
            serviceParameters.append(serviceQueryAttribute.getForeignModel()).append(" ")
                .append(serviceQueryAttribute.getName().substring( 0, serviceQueryAttribute.getName().length() - 2)).append(", ");
            serviceClass.getImports().add( modelPackage+"."+serviceQueryAttribute.getForeignModel());
            serviceImplClass.getImports().add( modelPackage+"."+serviceQueryAttribute.getForeignModel());
          } else if (serviceQueryAttribute.getType().startsWith("List")) {
            String type = serviceQueryAttribute.getType().substring(5, serviceQueryAttribute.getType().lastIndexOf(">"));
            if ( dtoClasses.containsKey( type )) {
              serviceQueryParameters.append("mapperFacade.mapAsList( query.get")
                  .append(StringUtils.capitalize(serviceQueryAttribute.getName())).append("(), ")
                  .append(type)
                  .append(".class, getOrikaContext(query)), ");
              endpointClass.getImports()
                  .add(modelPackage + "." + type);
              serviceParameters.append(serviceQueryAttribute.getType()).append(" ")
                  .append(serviceQueryAttribute.getName()).append(", ");
              serviceClass.getImports().add( modelPackage+"."+type);
              serviceImplClass.getImports().add( modelPackage+"."+type);
            } else {
              serviceQueryParameters.append("query.get")
                  .append(StringUtils.capitalize(serviceQueryAttribute.getName())).append("(), ");
            }
          } else {
            serviceQueryParameters.append("query.get")
                .append(StringUtils.capitalize(serviceQueryAttribute.getName())).append("(), ");
            serviceParameters.append(serviceQueryAttribute.getType()).append(" ")
                .append(serviceQueryAttribute.getName()).append(", ");
          }

        });
        if (serviceQueryParameters.length() > 0) {
          endpointOperation.setServiceParameters(
              serviceQueryParameters.substring(0, serviceQueryParameters.length() - 2).trim());
          serviceOperation
              .setParameters(serviceParameters.substring(0, serviceParameters.length() - 2).trim());
        }
        //serviceClass.getImports().addAll(serviceQueryClass.getImports());
        serviceClass.getOperations().add(serviceOperation);
        //serviceImplClass.getImports().addAll(serviceQueryClass.getImports());
        serviceImplClass.getOperations().add(serviceOperation);
      });

    });
    try {
      //
      for (ModelClass modelClass : modelClasses.values()) {
        writeToFile((outputFolder + File.separator + sourceFolder + File.separator + modelPackage)
            .replace(".", java.io.File.separator) + File.separator + modelClass.getClassName()
            + ".java", modelTemplate.apply(modelClass));
      }

      for (DtoClass dtoClass : dtoClasses.values()) {
        writeToFile((outputFolder + File.separator + sourceFolder + File.separator + dtoPackage)
            .replace(".", java.io.File.separator) + File.separator + dtoClass.getClassName()
            + ".java", dtoTemplate.apply(dtoClass));
      }

      for (CriteriaClass criteriaClass : criteriaClasses.values()) {
        writeToFile((outputFolder + File.separator + sourceFolder + File.separator
                + searchCriteriaPackage).replace(".", java.io.File.separator) + File.separator
                + criteriaClass.getClassName() + ".java",
            criteriaTemplate.apply(criteriaClass));
      }

      for (RepositoryClass repositoryClass : repositoryClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + repositoryPackage)
                .replace(".", java.io.File.separator) + File.separator + repositoryClass
                .getInterfaceName() + ".java", repositoryTemplate.apply(repositoryClass));
      }

      for (SearchServiceClass searchServiceClass : searchServiceClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + searchServicePackage)
                .replace(".", java.io.File.separator) + File.separator + searchServiceClass
                .getInterfaceName() + ".java", searchServiceTemplate.apply(searchServiceClass));
      }

      for (SearchServiceImplClass searchServiceImplClass : searchServiceImplClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + searchServicePackage)
                .replace(".", java.io.File.separator) + File.separator + searchServiceImplClass
                .getClassName() + ".java",
            searchServiceImpTemplate.apply(searchServiceImplClass));
      }

      for (ServiceClass serviceClass : serviceClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + servicePackage)
                .replace(".", java.io.File.separator) + File.separator + serviceClass
                .getInterfaceName() + ".java", serviceTemplate.apply(serviceClass));
      }

      for (ServiceImplClass serviceImplClass : serviceImplClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + servicePackage)
                .replace(".", java.io.File.separator) + File.separator + serviceImplClass
                .getClassName() + ".java", serviceImpTemplate.apply(serviceImplClass));
      }

      for (EndpointClass endpointClass : endpointClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + endpointPackage)
                .replace(".", java.io.File.separator) + File.separator + endpointClass
                .getClassName() + ".java", endpointTemplate.apply(endpointClass));
      }

      for (EnumClass enumDtoClass : enumDtoClasses.values()) {
        writeToFile((outputFolder + File.separator + sourceFolder + File.separator + dtoPackage)
                .replace(".", File.separator) + File.separator + enumDtoClass.getClassName()
                + ".java",
            enumTemplate.apply(enumDtoClass));
      }

      for (EnumClass enumClass : enumModelClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + modelPackage)
                .replace(".", File.separator) + File.separator + enumClass.getClassName()
                + ".java", enumTemplate.apply(enumClass));
      }

      for (ServiceQueryClass serviceQueryClass : serviceQueryClasses.values()) {
        writeToFile(
            (outputFolder + File.separator + sourceFolder + File.separator + serviceQueryPackage)
                .replace(".", java.io.File.separator) + File.separator + serviceQueryClass
                .getClassName() + ".java", serviceQueryTemplate.apply(serviceQueryClass));
      }

      writeToFile((outputFolder + File.separator + sourceFolder + File.separator + converterPackage)
          .replace(".", java.io.File.separator) + File.separator + converterClass.getClassName()
          + ".java", converterTemplate.apply(converterClass));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String escapeText(String input) {
    if (input == null) {
      return input;
    }

    // remove \t, \n, \r
    // replace \ with \\
    // replace " with \"
    // outter unescape to retain the original multi-byte characters
    // finally escalate characters avoiding code injection
    return
        StringEscapeUtils.unescapeJava(
            StringEscapeUtils.escapeJava(input)
                .replace("\\/", "/"))
            .replaceAll("[\\t\\n\\r]", " ")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
  }

  public String getSchemaType(Schema schema) {
    String schemaType = null;

    if (StringUtils.isNotBlank(schema.get$ref())) {
      try {
        schemaType = schema.get$ref();
        if (schemaType.indexOf("#/components/schemas/") == 0) {
          schemaType = schemaType.substring("#/components/schemas/".length());
        }
      } catch (Exception e) {
        System.err.println(
            "Error obtaining the datatype from ref:" + schema + ". Datatype default to Object");
        schemaType = "Object";
        System.err.println(e.getMessage());
      }
    } else {
      schemaType = getTypeOfSchema(schema);
    }
    // don't apply renaming on types from the typeMapping
    if (typeMapping.containsKey(schemaType)) {
      String result = typeMapping.get(schemaType);
      if (result.equalsIgnoreCase("List")) {
        Schema items = ((ArraySchema) schema).getItems();
        return "List<" + getSchemaType(items) + ">";
      }
      return typeMapping.get(schemaType);
    }

    if (null == schemaType) {
      if (schema.getName() != null) {
        System.err.println("No Type defined for Property " + schema.getName());
      } else {
        // System.err.println("No Type defined.", new Exception());
      }
    }
    return camelize(schemaType, false);
  }

  public File writeToFile(String filename, String contents) throws IOException {
    File output = new File(filename);

    if (output.getParent() != null && !new File(output.getParent()).exists()) {
      File parent = new File(output.getParent());
      parent.mkdirs();
    }
    Writer out = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(output), StandardCharsets.UTF_8));

    out.write(contents);
    out.close();
    return output;
  }

  public static String camelize(String word, boolean lowercaseFirstLetter) {
    // Replace all slashes with dots (package separator)
    String originalWord = word;
    System.out.println("camelize start - " + originalWord);
    Pattern p = Pattern.compile("\\/(.?)");
    Matcher m = p.matcher(word);
    int i = 0;
    int MAX = 100;
    while (m.find()) {
      if (i > MAX) {
        //System.err.println("camelize reached find limit - {} / {}", originalWord, word);
        break;
      }
      i++;
      word = m.replaceFirst("." + m.group(
          1)/*.toUpperCase()*/); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.
      m = p.matcher(word);
    }
    i = 0;
    // case out dots
    String[] parts = word.split("\\.");
    StringBuilder f = new StringBuilder();
    for (String z : parts) {
      if (z.length() > 0) {
        f.append(Character.toUpperCase(z.charAt(0))).append(z.substring(1));
      }
    }
    word = f.toString();

    m = p.matcher(word);
    while (m.find()) {
      if (i > MAX) {
        //System.err.println("camelize reached find limit - {} / {}", originalWord, word);
        break;
      }
      i++;
      word = m.replaceFirst("" + Character.toUpperCase(m.group(1).charAt(0)) + m.group(1)
          .substring(1)/*.toUpperCase()*/);
      m = p.matcher(word);
    }
    i = 0;
    // Uppercase the class name.
    p = Pattern.compile("(\\.?)(\\w)([^\\.]*)$");
    m = p.matcher(word);
    if (m.find()) {
      String rep = m.group(1) + m.group(2).toUpperCase() + m.group(3);
      rep = rep.replaceAll("\\$", "\\\\\\$");
      word = m.replaceAll(rep);
    }

    // Remove all underscores (underscore_case to camelCase)
    p = Pattern.compile("(_)(.)");
    m = p.matcher(word);
    while (m.find()) {
      if (i > MAX) {
        //System.err.println("camelize reached find limit - {} / {}", originalWord, word);
        break;
      }
      i++;
      String original = m.group(2);
      String upperCase = original.toUpperCase();
      if (original.equals(upperCase)) {
        word = word.replaceFirst("_", "");
      } else {
        word = m.replaceFirst(upperCase);
      }
      m = p.matcher(word);
    }

    // Remove all hyphens (hyphen-case to camelCase)
    p = Pattern.compile("(-)(.)");
    m = p.matcher(word);
    i = 0;
    while (m.find()) {
      if (i > MAX) {
        //System.err.println("camelize reached find limit - {} / {}", originalWord, word);
        break;
      }
      i++;
      word = m.replaceFirst(m.group(2).toUpperCase());
      m = p.matcher(word);
    }

    if (lowercaseFirstLetter && word.length() > 0) {
      word = word.substring(0, 1).toLowerCase() + word.substring(1);
    }

    return word;
  }

  static Map<String, String> getTypeMappings() {
    final Map<String, String> typeMapping = new HashMap<>();
    typeMapping.put("array", "List");
    typeMapping.put("map", "Map");
    typeMapping.put("List", "List");
    typeMapping.put("boolean", "Boolean");
    typeMapping.put("string", "String");
    typeMapping.put("int", "Integer");
    typeMapping.put("float", "Float");
    typeMapping.put("number", "BigDecimal");
    typeMapping.put("DateTime", "Date");
    typeMapping.put("long", "Long");
    typeMapping.put("short", "Short");
    typeMapping.put("char", "String");
    typeMapping.put("double", "Double");
    typeMapping.put("object", "Object");
    typeMapping.put("integer", "Integer");
    typeMapping.put("ByteArray", "byte[]");
    typeMapping.put("binary", "byte[]");
    typeMapping.put("file", "File");
    typeMapping.put("UUID", "UUID");
    typeMapping.put("BigDecimal", "BigDecimal");
    return typeMapping;
  }

  private static String getTypeOfSchema(Schema schema) {
    if (schema instanceof StringSchema && "number".equals(schema.getFormat())) {
      return "BigDecimal";
    } else if (schema instanceof ByteArraySchema) {
      return "ByteArray";
    } else if (schema instanceof BinarySchema) {
      return SchemaTypeUtil.BINARY_FORMAT;
    } else if (schema instanceof FileSchema) {
      return SchemaTypeUtil.BINARY_FORMAT;
    } else if (schema instanceof BooleanSchema) {
      return SchemaTypeUtil.BOOLEAN_TYPE;
    } else if (schema instanceof DateSchema) {
      return SchemaTypeUtil.DATE_FORMAT;
    } else if (schema instanceof DateTimeSchema) {
      return "DateTime";
    } else if (schema instanceof NumberSchema) {
      if (SchemaTypeUtil.FLOAT_FORMAT.equals(schema.getFormat())) {
        return SchemaTypeUtil.FLOAT_FORMAT;
      } else if (SchemaTypeUtil.DOUBLE_FORMAT.equals(schema.getFormat())) {
        return SchemaTypeUtil.DOUBLE_FORMAT;
      } else {
        return "BigDecimal";
      }
    } else if (schema instanceof IntegerSchema) {
      if (SchemaTypeUtil.INTEGER64_FORMAT.equals(schema.getFormat())) {
        return "long";
      } else {
        return "integer";
      }
    } else if (schema instanceof MapSchema) {
      return "map";
    } else if (schema instanceof ObjectSchema) {
      return "object";
    } else if (schema instanceof UUIDSchema) {
      return "UUID";
    } else if (schema instanceof StringSchema) {
      return "string";
    } else if (schema instanceof ComposedSchema && schema.getExtensions() != null && schema
        .getExtensions().containsKey("x-model-name")) {
      return schema.getExtensions().get("x-model-name").toString();

    } else {
      if (schema != null) {
        if (SchemaTypeUtil.OBJECT_TYPE.equals(schema.getType()) && (hasSchemaProperties(schema)
            || hasTrueAdditionalProperties(schema))) {
          return "map";
        } else {
          if (schema.getType() == null && schema.getProperties() != null && !schema.getProperties()
              .isEmpty()) {
            return "object";
          }
          return schema.getType();
        }
      }
    }
    return null;
  }

  protected static boolean hasSchemaProperties(Schema schema) {
    final Object additionalProperties = schema.getAdditionalProperties();
    return additionalProperties != null && additionalProperties instanceof Schema;
  }

  protected static boolean hasTrueAdditionalProperties(Schema schema) {
    final Object additionalProperties = schema.getAdditionalProperties();
    return additionalProperties != null && Boolean.TRUE.equals(additionalProperties);
  }

  static Map<String, String> getImportMappings() {
    final Map<String, String> importMapping = new HashMap<>();
    importMapping.put("BigDecimal", "java.math.BigDecimal");
    importMapping.put("UUID", "java.util.UUID");
    importMapping.put("File", "java.io.File");
    importMapping.put("Date", "java.util.Date");
    importMapping.put("Timestamp", "java.sql.Timestamp");
    importMapping.put("Map", "java.util.Map");
    importMapping.put("HashMap", "java.util.HashMap");
    importMapping.put("Array", "java.util.List");
    importMapping.put("ArrayList", "java.util.ArrayList");
    importMapping.put("List", "java.util.*");
    importMapping.put("Set", "java.util.*");
    importMapping.put("DateTime", "org.joda.time.*");
    importMapping.put("LocalDateTime", "org.joda.time.*");
    importMapping.put("LocalDate", "org.joda.time.*");
    importMapping.put("LocalTime", "org.joda.time.*");
    return importMapping;
  }

  public static void main(String[] args) {
    try {
      new Application(
          "/Users/clavaud/Development/DevArea/ilem-resource-mgt/swagger/MyGenerator/test.yml");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
