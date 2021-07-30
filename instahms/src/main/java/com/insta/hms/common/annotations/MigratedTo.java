package com.insta.hms.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface MigratedTo {
  
  /**
   * Value of migratedto class.
   *
   * @return the class
   */
  Class value();

  /**
   * Method name of migratedto.
   *
   * @return the string
   */
  String method() default "";
}