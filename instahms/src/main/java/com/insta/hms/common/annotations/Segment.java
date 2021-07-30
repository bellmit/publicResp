package com.insta.hms.common.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Segment {
  /**
   * Segment().
   * 
   * @return string
   */
  String segment() default "";

  /**
   * Version().
   * 
   * @return string
   */
  String version() default "";
}