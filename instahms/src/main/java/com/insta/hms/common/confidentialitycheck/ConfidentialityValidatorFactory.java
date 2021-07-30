package com.insta.hms.common.confidentialitycheck;

import com.insta.hms.common.annotations.ConfidentialityValidator;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ConfidentialityValidatorFactory {

  Map<String, Set<Class<? extends ConfidentialityInterface>>> queryParamValidatorMap =
      new HashMap<>();
  Map<String, Class<? extends ConfidentialityInterface>> entityValidatorMap = new HashMap<>();
  Map<Class<? extends ConfidentialityInterface>, Set<String>> urlValidatorMap = new HashMap<>();

  /**
   * Sets the confidentiality validator factory.
   */
  @Autowired
  public void setConfidentialityValidatorFactory() {
    Reflections reflections = new Reflections("com.insta.hms.common.confidentialitycheck");
    Set<Class<? extends ConfidentialityInterface>> subTypes =
        reflections.getSubTypesOf(ConfidentialityInterface.class);
    for (Class<? extends ConfidentialityInterface> implementingClass : subTypes) {
      Annotation[] annotations = implementingClass.getAnnotations();
      for (Annotation annotation : annotations) {
        if (!(annotation instanceof ConfidentialityValidator)) {
          continue;
        }
        ConfidentialityValidator validatorAnnotation = (ConfidentialityValidator) annotation;
        for (String value : validatorAnnotation.queryParamNames()) {
          putInMap(value, implementingClass, queryParamValidatorMap);
        }
        for (String entityName : validatorAnnotation.urlEntityName()) {
          entityValidatorMap.put(entityName, implementingClass);
        }
        for (String url : validatorAnnotation.urlContains()) {
          if (!url.equals("")) {
            Set<String> handledUrls = new HashSet<>();
            if (urlValidatorMap.get(implementingClass) != null) {
              handledUrls = urlValidatorMap.get(implementingClass);
            }
            handledUrls.add(url);
            urlValidatorMap.put(implementingClass, handledUrls);
          }
        }
      }
    }
  }

  private void putInMap(String value, Class<? extends ConfidentialityInterface> implementingClass,
      Map<String, Set<Class<? extends ConfidentialityInterface>>> classMap) {
    Set<Class<? extends ConfidentialityInterface>> listClasses = new HashSet<>();
    if (queryParamValidatorMap.get(value) != null) {
      listClasses = queryParamValidatorMap.get(value);
    }
    listClasses.add(implementingClass);
    classMap.put(value, listClasses);
  }

  public Map<String, Set<Class<? extends ConfidentialityInterface>>> getQueryParamValidatoryMap() {
    return queryParamValidatorMap;
  }

  public Map<String, Class<? extends ConfidentialityInterface>> getEntityValidatorMap() {
    return entityValidatorMap;
  }

  public Map<Class<? extends ConfidentialityInterface>, Set<String>> getUrlValidatorMap() {
    return urlValidatorMap;
  }

}
