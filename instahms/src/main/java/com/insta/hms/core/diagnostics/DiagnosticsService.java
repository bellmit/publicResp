package com.insta.hms.core.diagnostics;

import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemRepository;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemRepository;
import com.insta.hms.master.ServiceMaster.ServiceMasterAction;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagnosticsService.
 */
@Service("diagnosticsService")
public class DiagnosticsService {

  /** The diagnostics repository. */
  static Logger logger = LoggerFactory.getLogger(ServiceMasterAction.class);

  /** The tests prescribed repository. */
  @Autowired
  private TestOrderItemRepository testsPrescribedRepository;

  /** The services prescribed repository. */
  @Autowired
  private ServiceOrderItemRepository servicesPrescribedRepository;

  /**
   * Gets the associated mr no for report id.
   *
   * @param reportId
   *          the report id
   * @return the associated mr no for report id
   */
  public List<String> getAssociatedMrNoForReportId(List<String> reportId) {
    return testsPrescribedRepository.getMrNoForReportId(reportId);
  }

  /**
   * Gets the associated mr no for prescribed id.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the associated mr no for prescribed id
   */
  public List<String> getAssociatedMrNoForTestsPrescribedId(List<String> prescribedId) {
    return testsPrescribedRepository.getMrNoForPrescribedId(prescribedId);
  }

  /**
   * Gets the associated mr no for service prescribed id.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the associated mr no for service prescribed id
   */
  public List<String> getAssociatedMrNoForServicePrescribedId(List<String> prescribedId) {
    return servicesPrescribedRepository.getMrNoForServicePrescribedId(prescribedId);
  }

  /**
   * Parses the int.
   *
   * @param str
   *          the str
   * @return the integer
   */
  private Integer parseInt(String str) {
    Integer intParameter = null;
    try {
      intParameter = Integer.parseInt(str);
    } catch (NumberFormatException exception) {
      logger.warn("Invalid Parameter Id :" + str);
    }
    return intParameter;
  }

  /**
   * Checks if is report id valid.
   *
   * @param parameter
   *          the parameter
   * @return the boolean
   */
  public Boolean isReportIdValid(String parameter) {

    Integer intParameter = parseInt(parameter);
    if (intParameter == null) {
      logger.warn("Invalid report Id :" + parameter);
      return false;
    }

    return testsPrescribedRepository.exist("report_id", intParameter);
  }

  /**
   * Checks if is tests prescription id valid.
   *
   * @param parameter
   *          the parameter
   * @return the boolean
   */
  public Boolean isTestsPrescriptionIdValid(String parameter) {
    Integer intParameter = parseInt(parameter);
    if (intParameter == null) {
      logger.warn("Invalid test prescribed Id :" + parameter);
      return false;
    }
    return testsPrescribedRepository.exist("prescribed_id", intParameter);
  }

  /**
   * Checks if is service prescribed id valid.
   *
   * @param parameter
   *          the parameter
   * @return the boolean
   */
  public Boolean isServicePrescribedIdValid(String parameter) {
    Integer intParameter = parseInt(parameter);
    if (intParameter == null) {
      logger.warn("Invalid service prescribed Id :" + parameter);
      return false;
    }
    return servicesPrescribedRepository.exist("prescription_id", intParameter);
  }
  
  /**
   * Status change.
   *
   * @param dataMap the data map
   * @return the boolean
   */
  public Boolean statusChange(Map<String, Object> dataMap) {
    if (dataMap.isEmpty()) {
      return false;
    }
    Map keys = new HashMap<String, Object>();
    keys.put("prescribed_id", Integer.parseInt((String)dataMap.get("prescribed_id")));
    BasicDynaBean bean = testsPrescribedRepository.getBean();
    bean.set("conducted", dataMap.get("conducted"));

    return testsPrescribedRepository.update(bean, keys) > 0;
  }
}
