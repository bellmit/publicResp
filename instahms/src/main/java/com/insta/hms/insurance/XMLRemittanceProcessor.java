package com.insta.hms.insurance;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Map;

/**
 * The Class XMLRemittanceProcessor.
 */
public abstract class XMLRemittanceProcessor extends RemittanceAdviceProcessor {

  /**
   * Process.
   *
   * @param remittanceBean the remittance bean
   * @param rform          the rform
   * @param errorMap       the error map
   * @return the remittance advice
   * @throws Exception the exception
   */
  public RemittanceAdvice process(BasicDynaBean remittanceBean, RemittanceForm rform, Map errorMap)
      throws Exception {
    XMLRemittanceProvider xmlRemittanceProvider = getXMLRemittanceProvider();
    RemittanceAdvice desc = xmlRemittanceProvider.getRemittanceAdvice(
        rform.getRemittance_metadata().getInputStream(), remittanceBean, errorMap);
    if (null == desc) {
      if (errorMap.get("error") == null || errorMap.get("error").equals("")) {
        errorMap.put("error", "XML parsing failed: Incorrectly formatted values supplied");
      }
    }
    return desc;
  }

  /**
   * Gets the XML remittance provider.
   *
   * @return the XML remittance provider
   */
  public abstract XMLRemittanceProvider getXMLRemittanceProvider();

}
