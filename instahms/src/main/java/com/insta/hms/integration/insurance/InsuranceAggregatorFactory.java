package com.insta.hms.integration.insurance;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.annotations.Aggregator;

import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A factory for creating InsuranceAggregator objects.
 */
@Component
public class InsuranceAggregatorFactory {

  /** The bean factory. */
  @Autowired
  BeanFactory beanFactory;

  /** The agg map. */
  private Map<String, Class<? extends GenericInsuranceAggregator>> aggMap =
      new HashMap<String, Class<? extends GenericInsuranceAggregator>>();
  
  /** The agg config map. */
  private Map<String, Object> aggConfigMap = new HashMap<String, Object>();

  /**
   * Sets the generic insurance aggregator.
   */
  @Autowired
  public void setGenericInsuranceAggregator() {

    Reflections reflections = new Reflections("com.insta.hms.integration.insurance");

    Set<Class<? extends GenericInsuranceAggregator>> subTypes =
        reflections.getSubTypesOf(GenericInsuranceAggregator.class);

    Iterator<Class<? extends GenericInsuranceAggregator>> aggItr = subTypes.iterator();

    while (aggItr.hasNext()) {
      Class<? extends GenericInsuranceAggregator> aggClassName = aggItr.next();
      Annotation[] annotations = aggClassName.getAnnotations();
      for (Annotation annotation : annotations) {
        if (!(annotation instanceof Aggregator)) {
          continue;
        }
        Aggregator agg = (Aggregator) annotation;
        aggMap.put(agg.value(), aggClassName);
        Map<String, Object> details = new HashMap<String, Object>();
        GenericInsuranceAggregator insAgg = beanFactory.getBean(aggClassName);
        details.put("configuration_required", insAgg.requiresConfiguration());
        details.put("center_configuration_schema", insAgg.getCenterConfigurationSchema());
        details.put("doctor_configuration_schema", insAgg.getDoctorConfigurationSchema());
        details.put("tpainsco_configuration_schema", insAgg.getTpaInsCoConfigurationSchema());
        details.put("store_configuration_schema", insAgg.getStoreConfigurationSchema());
        details.put("supported_services", insAgg.getSupportedServices());
        details.put("id", agg.value());
        details.put("name", aggClassName.getAnnotation(Aggregator.class).name());
        aggConfigMap.put(agg.value(), details);

      }
    }

  }

  /**
   * Gets the aggregators.
   *
   * @return the aggregators
   */
  public Map<String, Object> getAggregators() {
    return aggConfigMap;
  }

  /**
   * Gets the insurance aggregator instance.
   *
   * @param aggClassName the agg class name
   * @return the insurance aggregator instance
   * @throws IllegalAccessException the illegal access exception
   * @throws InstantiationException the instantiation exception
   */
  public GenericInsuranceAggregator getInsuranceAggregatorInstance(String aggClassName)
      throws IllegalAccessException, InstantiationException {
    return ApplicationContextProvider.getApplicationContext()
        .getBean(aggMap.get(aggClassName));
  }

}
