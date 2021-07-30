package com.insta.hms.common.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component

public @interface EdcMachine {

  /**
   * Value of the edcmachine vendor.
   *
   * @return the string
   */
  String value() default "";

  /**
   * Name of edcmachince vendor.
   *
   * @return the string
   */
  String name() default "";
}
