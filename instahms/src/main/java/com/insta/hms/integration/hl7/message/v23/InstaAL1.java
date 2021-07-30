package com.insta.hms.integration.hl7.message.v23;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.segment.AL1;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The Class InstaAL1.
 * 
 * @author yashwant
 */
@Component
public class InstaAL1 {

  /**
   * Creates the AL 1.
   *
   * @param al1
   *          the al 1
   * @param dataMap
   *          the data map
   * @throws DataTypeException
   *           the data type exception
   */
  public void createAL1(AL1 al1, Map<String, Object> dataMap) throws DataTypeException {
    al1.getAl12_AllergyType().setValue((String) dataMap.get("allergy_type"));
    al1.getAl13_AllergyCodeMnemonicDescription().getAlternateText()
        .setValue((String) dataMap.get("reaction"));
    al1.getAl14_AllergySeverity().setValue((String) dataMap.get("severity"));
    al1.getAl15_AllergyReaction().setValue((String) dataMap.get("allergy"));
  }
}
