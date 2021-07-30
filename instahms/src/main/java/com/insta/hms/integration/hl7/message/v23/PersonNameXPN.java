package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.datatype.XPN;

import java.util.Map;

/**
 * The Class PersonNameXPN.
 * 
 * @author yashwant
 */
public class PersonNameXPN {

  /**
   * Instantiates a new person name XPN.
   */
  private PersonNameXPN() {

  }

  /**
   * Prepare name.
   *
   * @param xpn
   *          the xpn
   * @param patientMap
   *          the patient map
   * @throws DataTypeException
   *           the data type exception
   */
  public static void prepareName(XPN xpn, Map<String, Object> patientMap) throws DataTypeException {
    xpn.getFamilyName().setValue((String) patientMap.get("last_name"));
    // PID-6.2
    xpn.getGivenName().setValue((String) patientMap.get("patient_name"));
    // PID-6.3
    xpn.getMiddleInitialOrName().setValue((String) patientMap.get("middle_name"));
    // PID-6.5
    xpn.getPrefixEgDR().setValue((String) patientMap.get("salutation"));
  }
}
