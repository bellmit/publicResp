package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospitalOperationsHAI")
public class HospitalOperationsHaiOhsrsFunction extends GenericOhsrsFunction {

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
      if (data.get("numdischarges").equals("0")) {
        data.put("infectionrate", "0");
      } else {
        data.put("infectionrate", String.valueOf(Double.parseDouble(
            data.get("numhai")) * 100 / Double.parseDouble(data.get("numdischarges"))));
      }
      if (data.get("totalventilatordays").equals("0")) {
        data.put("resultvap", "0");
      } else {
        data.put("resultvap", String.valueOf(Double.parseDouble(
            data.get("patientnumvap")) * 1000 / Double.parseDouble(
                data.get("totalventilatordays"))));
      }
      if (data.get("totalnumcentralline").equals("0")) {
        data.put("resultbsi", "0");
      } else {
        data.put("resultbsi", String.valueOf(Double.parseDouble(
            data.get("patientnumbsi")) * 1000 / Double.parseDouble(
                data.get("totalnumcentralline"))));
      }
      if (data.get("totalcatheterdays").equals("0")) {
        data.put("resultuti", "0");
      } else {
        data.put("resultuti", String.valueOf(Double.parseDouble(
            data.get("patientnumuti")) * 1000 / Double.parseDouble(
                data.get("totalcatheterdays"))));
      }
      if (data.get("totalproceduresdone").equals("0")) {
        data.put("resultssi", "0");
      } else {
        data.put("resultssi", String.valueOf(Double.parseDouble(
            data.get("numssi")) * 100 / Double.parseDouble(
                data.get("totalproceduresdone"))));
      }
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospitalOperationsHAI(
            settings.getHfhudCode(), 
            data.get("numhai"),
            data.get("numdischarges"),
            data.get("infectionrate"),
            data.get("patientnumvap"),
            data.get("totalventilatordays"),
            data.get("resultvap"),
            data.get("patientnumbsi"),
            data.get("totalnumcentralline"),
            data.get("resultbsi"),
            data.get("patientnumuti"),
            data.get("totalcatheterdays"),
            data.get("resultuti"),
            data.get("numssi"),
            data.get("totalproceduresdone"),
            data.get("resultssi"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospitalOperationsHAI(
            settings.getHfhudCode(), 
            data.get("numhai"),
            data.get("numdischarges"),
            data.get("infectionrate"),
            data.get("patientnumvap"),
            data.get("totalventilatordays"),
            data.get("resultvap"),
            data.get("patientnumbsi"),
            data.get("totalnumcentralline"),
            data.get("resultbsi"),
            data.get("patientnumuti"),
            data.get("totalcatheterdays"),
            data.get("resultuti"),
            data.get("numssi"),
            data.get("totalproceduresdone"),
            data.get("resultssi"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
