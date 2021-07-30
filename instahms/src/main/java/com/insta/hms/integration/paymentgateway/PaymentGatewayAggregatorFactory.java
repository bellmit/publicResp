package com.insta.hms.integration.paymentgateway;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.annotations.EdcMachine;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A factory for creating PaymentGatewayAggregator objects.
 */
@Component
public class PaymentGatewayAggregatorFactory {

  static Logger log = LoggerFactory.getLogger(PaymentGatewayAggregatorFactory.class);

  /** The agg map. */
  private Map<String, Class<? extends GenericPaymentsAggregator>> aggMap =
      new HashMap<String, Class<? extends GenericPaymentsAggregator>>();
  
  /** The agg config map. */
  private Map<String, Object> aggConfigMap = new HashMap<String, Object>();

  /**
   * Instantiates a new payment gateway aggregator factory.
   */
  public PaymentGatewayAggregatorFactory() {

    Reflections reflections = new Reflections("com.insta.hms.integration.paymentgateway");

    Set<Class<? extends GenericPaymentsAggregator>> subTypes = reflections
        .getSubTypesOf(GenericPaymentsAggregator.class);

    Iterator<Class<? extends GenericPaymentsAggregator>> aggItr = subTypes.iterator();

    while (aggItr.hasNext()) {
      Class<? extends GenericPaymentsAggregator> aggClassName = aggItr.next();
      Annotation[] annotations = aggClassName.getAnnotations();
      for (Annotation annotation : annotations) {
        if (!(annotation instanceof EdcMachine)) {
          continue;
        }
        EdcMachine agg = (EdcMachine) annotation;
        aggMap.put(agg.value(), aggClassName);
        Map<String, Object> details = new HashMap<String, Object>();
        try {
          GenericPaymentsAggregator payAgg = aggClassName.newInstance();
          details.put("configuration_required", payAgg.requiresConfiguration());
          details.put("configuration_schema", payAgg.getConfigurationSchema());
          details.put("supported_services", payAgg.getSupportedServices());
          details.put("id", agg.value());
          details.put("name", aggClassName.getAnnotation(EdcMachine.class).name());
          aggConfigMap.put(agg.value(), details);
        } catch (InstantiationException ie) {
          log.error("", ie);
        } catch (IllegalAccessException iae) {
          log.error("", iae);
        }

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
   * Gets the payment gateway aggregator instance.
   *
   * @param aggName the agg name
   * @return the payment gateway aggregator instance
   * @throws IllegalAccessException the illegal access exception
   * @throws InstantiationException the instantiation exception
   */
  public GenericPaymentsAggregator getPaymentGatewayAggregatorInstance(String aggName)
      throws IllegalAccessException, InstantiationException {
    return ApplicationContextProvider.getApplicationContext().getBean(aggMap.get(aggName));
  }

}
