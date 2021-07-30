package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "revenues")
public class RevenuesOhsrsFunction extends GenericOhsrsFunction {

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
      data.put("grandtotal", String.valueOf(Double.parseDouble(data.get("amountfromdoh")) 
          + Double.parseDouble(data.get("amountfromlgu")) 
          + Double.parseDouble(data.get("amountfromdonor")) 
          + Double.parseDouble(data.get("amountfromprivateorg")) 
          + Double.parseDouble(data.get("amountfromphilhealth")) 
          + Double.parseDouble(data.get("amountfrompatient")) 
          + Double.parseDouble(data.get("amountfromreimbursement")) 
          + Double.parseDouble(data.get("amountfromothersources"))));
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().revenues(
            settings.getHfhudCode(), 
            data.get("amountfromdoh"),
            data.get("amountfromlgu"),
            data.get("amountfromdonor"),
            data.get("amountfromprivateorg"),
            data.get("amountfromphilhealth"),
            data.get("amountfrompatient"),
            data.get("amountfromreimbursement"),
            data.get("amountfromothersources"),
            data.get("grandtotal"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().revenues(
            settings.getHfhudCode(), 
            data.get("amountfromdoh"),
            data.get("amountfromlgu"),
            data.get("amountfromdonor"),
            data.get("amountfromprivateorg"),
            data.get("amountfromphilhealth"),
            data.get("amountfrompatient"),
            data.get("amountfromreimbursement"),
            data.get("amountfromothersources"),
            data.get("grandtotal"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
