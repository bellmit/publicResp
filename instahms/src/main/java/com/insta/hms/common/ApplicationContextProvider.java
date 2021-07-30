package com.insta.hms.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The Class ApplicationContextProvider to get ApplicationContext throughout the application.
 * 
 * @author tanmay.k
 */
public class ApplicationContextProvider implements ApplicationContextAware {

  /** The context. */
  private static ApplicationContext context;

  /**
   * Gets the application context.
   *
   * @return the application context
   */
  public static ApplicationContext getApplicationContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext
   * (org.springframework.context.ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext ac) throws BeansException {
    context = ac;
  }

  /**
   * Gets the bean.
   *
   * @param           <T> the generic type
   * @param beanClass the bean class
   * @return the bean
   */
  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }

}