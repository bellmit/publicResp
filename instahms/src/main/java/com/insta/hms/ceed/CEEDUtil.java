package com.insta.hms.ceed;

import com.insta.hms.ceed.generated_test.Gateway;
import com.insta.hms.ceed.generated_test.GatewaySoap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is a util class for ceed integration.
 */
public class CEEDUtil {
  private static Logger logger = LoggerFactory.getLogger(CEEDUtil.class);
  private static Gateway ceedgtwy = null;
  private static GatewaySoap ceedgtwysoap = null;

  private CEEDUtil() {

  }

  /**
   * Get CEED gateway soap.
   * @return CEED Gateway Soap object
   * @throws CEEDInternetConnectionException Exception establishing connection to CEED
   */
  public static GatewaySoap getGateWaySoap() throws CEEDInternetConnectionException {
    try {
      if (ceedgtwy == null) {
        ceedgtwy = new Gateway();
      }
      if (ceedgtwysoap == null) {
        ceedgtwysoap = ceedgtwy.getGatewaySoap();
      }
    } catch (Throwable ex) {
      logger.error(ex.getMessage());
      throw new CEEDInternetConnectionException(ex.getMessage());
    }
    return ceedgtwysoap;
  }
}
