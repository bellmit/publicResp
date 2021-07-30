package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "staffingPatternOthers")
public class StaffingPatternOthersOhsrsFunction extends GenericOhsrsFunction {

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
        responseXml = client.getTrainingPort().staffingPatternOthers(
            settings.getHfhudCode(), 
            data.get("parent"),
            data.get("professiondesignation"),
            data.get("specialtyboardcertified"),
            data.get("fulltime40permanent"),
            data.get("fulltime40contractual"),
            data.get("parttimepermanent"),
            data.get("parttimecontractual"),
            data.get("activerotatingaffiliate"),
            data.get("outsourced"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().staffingPatternOthers(
            settings.getHfhudCode(), 
            data.get("parent"),
            data.get("professiondesignation"),
            data.get("specialtyboardcertified"),
            data.get("fulltime40permanent"),
            data.get("fulltime40contractual"),
            data.get("parttimepermanent"),
            data.get("parttimecontractual"),
            data.get("activerotatingaffiliate"),
            data.get("outsourced"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
