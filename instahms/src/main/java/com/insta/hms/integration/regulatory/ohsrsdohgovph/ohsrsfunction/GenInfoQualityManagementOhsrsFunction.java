package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsFunctionMeta;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "genInfoQualityManagement")
public class GenInfoQualityManagementOhsrsFunction extends GenericOhsrsFunction {

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    //Return Null as this section data is uploadable:always
    return null;
  }

  @Override
    protected boolean submit(int year, OhsrsdohgovphSettings settings,
      List<Map<String, String>> uploadData) {
    String responseXml = "";
    String reportYear = String.valueOf(year);
    boolean status = true;
    for (Map<String, String> data : uploadData) {
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().genInfoQualityManagement(
            settings.getHfhudCode(), 
            data.get("qualitymgmttype"),
            data.get("description"),
            data.get("certifyingbody"),
            data.get("philhealthaccreditation"),
            data.get("validityfrom"),
            data.get("validityto"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().genInfoQualityManagement(
            settings.getHfhudCode(), 
            data.get("qualitymgmttype"),
            data.get("description"),
            data.get("certifyingbody"),
            data.get("philhealthaccreditation"),
            data.get("validityfrom"),
            data.get("validityto"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }
}
