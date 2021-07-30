package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction.GenericOhsrsFunction;

import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.collections.Lists;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OhsrsFunctionProvider {

  /** The bean factory. */
  @Autowired
  BeanFactory beanFactory;

  /** The agg map. */
  private Map<String, Class<? extends GenericOhsrsFunction>> processorMap =
      new HashMap<String, Class<? extends GenericOhsrsFunction>>();

  /**
   * Sets the generic ohsrs function processor.
   */
  @Autowired
  public void setGenericOhsrsFunction() {
    Reflections reflections = 
        new Reflections("com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction");

    Set<Class<? extends GenericOhsrsFunction>> subTypes =
        reflections.getSubTypesOf(GenericOhsrsFunction.class);

    Iterator<Class<? extends GenericOhsrsFunction>> processorIterator = subTypes.iterator();

    while (processorIterator.hasNext()) {
      Class<? extends GenericOhsrsFunction> processorClassName = processorIterator.next();
      Annotation[] annotations = processorClassName.getAnnotations();
      for (Annotation annotation : annotations) {
        if (!(annotation instanceof OhsrsFunctionProcessor)) {
          continue;
        }
        OhsrsFunctionProcessor processor = (OhsrsFunctionProcessor) annotation;
        processorMap.put(processor.supports(), processorClassName);
      }
    }
  }

  public GenericOhsrsFunction getProcessor(String ohsrsFunction)
      throws IllegalAccessException, InstantiationException {
    return ApplicationContextProvider.getApplicationContext()
        .getBean(processorMap.get(ohsrsFunction));
  }

  public List<String> getSupportedOhsrsFunctions() {
    return Lists.newArrayList(processorMap.keySet());
  }
}
