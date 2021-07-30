package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospOptDischargesEV")
public class HospOptDischargesEvOhsrsFunction extends GenericOhsrsFunction {

  private static final String TOTAL_ER = "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND pr.visit_type='o'"
      + " AND pr.admitted_dept IN ";
  
  private static final String ADULTS = " AND"
      + " ((pr.reg_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25) >=19.0";

  private static final String PEDIA = " AND"
      + " ((pr.reg_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25) < 19.0";

  private static final String TRANS_OUT = " AND pr.discharge_type_id = 5"
      + " AND pr.transfer_destination is not null"
      + " AND pr.transfer_destination != ''";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(centerId);
    params.add(yearStart);
    params.add(yearEnd);
    Map<String, List<String>> deptMapping = getDeptMapping();
    List<String> erMapping = deptMapping.get("emergency");
    String baseQuery = TOTAL_ER + DatabaseHelper.getInOperatorPlaceholder(erMapping.size());
    for (String mapping : erMapping) {
      params.add(mapping);
    }
    Object[] paramsArr = params.toArray();
    Map<String, Object> map = new HashMap<>();
    map.put("emergencyvisits",DatabaseHelper.getInteger(baseQuery, paramsArr));
    map.put("emergencyvisitsadult",DatabaseHelper.getInteger(baseQuery + ADULTS, paramsArr));
    map.put("emergencyvisitspediatric",DatabaseHelper.getInteger(baseQuery + PEDIA, paramsArr));
    map.put("evfromfacilitytoanother",DatabaseHelper.getInteger(baseQuery + TRANS_OUT, paramsArr));
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
        responseXml = client.getTrainingPort().hospOptDischargesEV(
            settings.getHfhudCode(), 
            data.get("emergencyvisits"),
            data.get("emergencyvisitsadult"),
            data.get("emergencyvisitspediatric"),
            data.get("evfromfacilitytoanother"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesEV(
            settings.getHfhudCode(), 
            data.get("emergencyvisits"),
            data.get("emergencyvisitsadult"),
            data.get("emergencyvisitspediatric"),
            data.get("evfromfacilitytoanother"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
