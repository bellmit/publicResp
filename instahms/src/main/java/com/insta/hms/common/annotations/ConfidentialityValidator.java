package com.insta.hms.common.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Confidentiality Validator annotation used for restricting access to data of confidential
 * patients.
 * 
 * @author aditya
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ConfidentialityValidator {

  /**
   * QueryParamName : Name/Aliases of query parameters handled by the validator.
   *
   * @return the string[]
   */
  String[] queryParamNames();

  /**
   * urlEntityName : Entity name used to parse rest urls; if url is /consultation/{id} then
   * "consultation" is the urlEntityName. Used for rest urls
   * 
   * @return the string[]
   */
  String[] urlEntityName();

  /**
   * Url contains: This parameter is used to restrict the usage of a confidentiality filter to a
   * particular set of urls.
   *
   * @return the string[]
   */
  String[] urlContains() default "";
}
