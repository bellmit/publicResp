package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospOptDischargesNumberDeliveries")
public class HospOptDischargesNumberDeliveriesOhsrsFunction extends GenericOhsrsFunction {

  private static final String TOTAL_NEWBORN_GROUPED = "SELECT pd.delivery_type, count(*) cn"
      + " FROM admission ad"
      + " JOIN patient_registration pr on pr.patient_id = ad.parent_id and pr.visit_type='i' "
      + "   AND center_id = ? "
      + " JOIN patient_details pd on pd.mr_no = ad.mr_no"
      + " WHERE ad.isbaby = 'Y'"
      + " AND (admit_date BETWEEN ?::date AND ?::date) "
      + " AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + " GROUP BY pd.delivery_type";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    String nextYearStart = String.valueOf(year + 1) + "-01-01";
    List<BasicDynaBean> rows = DatabaseHelper.queryToDynaList(TOTAL_NEWBORN_GROUPED, 
        new Object[] {centerId, yearStart, yearEnd});
    int nsd = 0;
    int ceasarean = 0;
    int others = 0;
    if (rows != null) {
      for (BasicDynaBean row : rows) {
        String deliveryType = (String) row.get("delivery_type");
        if (deliveryType != null && deliveryType.equals("N")) {
          nsd += Integer.parseInt(String.valueOf(row.get("cn")));
        } else if (deliveryType != null && deliveryType.equals("C")) {
          ceasarean += Integer.parseInt(String.valueOf(row.get("cn")));
        } else {
          others += Integer.parseInt(String.valueOf(row.get("cn")));
        }
      }
    }
    Map<String, Object> map = new HashMap<>();
    map.put("totalifdelivery", nsd + ceasarean + others);
    map.put("totallbvdelivery", nsd);
    map.put("totallbcdelivery", ceasarean);
    map.put("totalotherdelivery", others);
    List<Map<String, Object>> list = new ArrayList<>();
    list.add(map);
    return list;
  }

  @Override
    protected boolean submit(int year, OhsrsdohgovphSettings settings,
      List<Map<String, String>> uploadData) {
    String responseXml = "";
    String reportYear = String.valueOf(year);
    boolean status = true;
    for (Map<String, String> data : uploadData) {
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospOptDischargesNumberDeliveries(
            settings.getHfhudCode(), 
            data.get("totalifdelivery"),
            data.get("totallbvdelivery"),
            data.get("totallbcdelivery"),
            data.get("totalotherdelivery"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesNumberDeliveries(
            settings.getHfhudCode(), 
            data.get("totalifdelivery"),
            data.get("totallbvdelivery"),
            data.get("totallbcdelivery"),
            data.get("totalotherdelivery"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
