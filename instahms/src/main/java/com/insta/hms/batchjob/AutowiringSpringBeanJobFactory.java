package com.insta.hms.batchjob;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * A factory for creating Quartz Jobs with auto-wiring capability.
 * 
 * @author tanmay.k
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
    implements ApplicationContextAware {

  /** The bean factory. */
  private transient AutowireCapableBeanFactory beanFactory;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.
   * context.ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext context) {
    beanFactory = context.getAutowireCapableBeanFactory();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.scheduling.quartz.SpringBeanJobFactory#createJobInstance(org.quartz.spi.
   * TriggerFiredBundle)
   */
  @Override
  public Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
    final Object job = super.createJobInstance(bundle);
    beanFactory.autowireBean(job);
    return job;
  }

}
