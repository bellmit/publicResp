package com.insta.hms.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation is used to determine what all order objects can be possible. Specific object can
 * be accessed using it's key.
 * 
 * @author ritolia
 *
 */

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

  /**
   * @return
   */
  String key() default "";

  /**
   * @return
   */
  String prefix() default "";

  /**
   * @return
   */
  String[] value() default "";
}
