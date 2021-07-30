package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "expenses")
public class ExpensesOhsrsFunction extends GenericOhsrsFunction {

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
      data.put("totalps", String.valueOf(Double.parseDouble(data.get("salarieswages")) 
          + Double.parseDouble(data.get("employeebenefits")) 
          + Double.parseDouble(data.get("allowances"))));
      data.put("totalmooe", String.valueOf(Double.parseDouble(data.get("totalamountmedicine")) 
          + Double.parseDouble(data.get("totalamountmedicalsupplies")) 
          + Double.parseDouble(data.get("totalamountutilities")) 
          + Double.parseDouble(data.get("totalamountnonmedicalservice"))));
      data.put("totalco", String.valueOf(Double.parseDouble(data.get("amountinfrastructure"))
          + Double.parseDouble(data.get("amountequipment"))));
      data.put("grandtotal", String.valueOf(Double.parseDouble(data.get("totalps")) 
          + Double.parseDouble(data.get("totalmooe")) + Double.parseDouble(data.get("totalco"))));
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().expenses(
            settings.getHfhudCode(), 
            data.get("salarieswages"),
            data.get("employeebenefits"),
            data.get("allowances"),
            data.get("totalps"),
            data.get("totalamountmedicine"),
            data.get("totalamountmedicalsupplies"),
            data.get("totalamountutilities"),
            data.get("totalamountnonmedicalservice"),
            data.get("totalmooe"),
            data.get("amountinfrastructure"),
            data.get("amountequipment"),
            data.get("totalco"),
            data.get("grandtotal"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().expenses(
            settings.getHfhudCode(), 
            data.get("salarieswages"),
            data.get("employeebenefits"),
            data.get("allowances"),
            data.get("totalps"),
            data.get("totalamountmedicine"),
            data.get("totalamountmedicalsupplies"),
            data.get("totalamountutilities"),
            data.get("totalamountnonmedicalservice"),
            data.get("totalmooe"),
            data.get("amountinfrastructure"),
            data.get("amountequipment"),
            data.get("totalco"),
            data.get("grandtotal"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml);
    }
    return status;
  }
}
