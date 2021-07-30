package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.datatype.XAD;

import java.util.Map;

/**
 * The Class AddressXAD.
 * 
 * @author yashwant
 */
public class AddressXAD {

  /**
   * Instantiates a new address XAD.
   */
  private AddressXAD() {

  }

  /**
   * Prepare address.
   *
   * @param xad
   *          the xad
   * @param patientMap
   *          the patient map
   * @throws DataTypeException
   *           the data type exception
   */
  public static void prepareAddress(XAD xad, Map<String, Object> patientMap)
      throws DataTypeException {
    xad.getXad1_StreetAddress().setValue((String) patientMap.get("patient_address"));
    xad.getXad3_City().setValue((String) patientMap.get("city_name"));
    xad.getXad4_StateOrProvince().setValue((String) patientMap.get("state_name"));
    xad.getXad6_Country().setValue((String) patientMap.get("country_name"));
    xad.getXad9_CountyParishCode().setValue((String) patientMap.get("country_code"));
  }
}
