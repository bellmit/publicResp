package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospitalOperationsMajorOpt")
public class HospitalOperationsMajorOptOhsrsFunction extends GenericOhsrsFunction {

  private static final String MAJOR_SURGERIES = "SELECT "
      + " om.operation_code, count(distinct bc.charge_ref) cn"
      + " FROM bill_charge bc"
      + " JOIN bill b on b.bill_no = bc.bill_no"
      + " JOIN patient_registration pr on pr.patient_id = b.visit_id"
      + " JOIN operation_master om on om.op_id = bc.op_id"
      + " WHERE bc.charge_head = 'ANATOPE'"
      + " AND bc.status != 'X'"
      + " AND pr.center_id = ?"
      + " AND pr.reg_date between ?::date and ?::date";

  private static final String EXCLUDE_OPE = " AND bc.op_id NOT IN ";
  
  private static final String EXCLUDE_ANAT = " AND bc.act_description_id NOT IN ";
  
  private static final String GROUP_BY = " GROUP BY om.operation_code";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(centerId);
    params.add(yearStart);
    params.add(yearEnd);
    List<String> excludeAnaesthesiaIds = getListMapping("minor_anesthesia_ohsrsdohgovph");
    List<String> excludeOperations = getListMapping("csection_operations_ohsrsdohgovph");
    String baseQuery = MAJOR_SURGERIES;
    if (excludeOperations != null && excludeOperations.size() > 0) {
      baseQuery = baseQuery + EXCLUDE_OPE
          + DatabaseHelper.getInOperatorPlaceholder(excludeOperations.size());
      for (String mapping : excludeOperations) {
        params.add(mapping);
      }
    }
    if (excludeAnaesthesiaIds != null && excludeAnaesthesiaIds.size() > 0) {
      baseQuery = baseQuery + EXCLUDE_ANAT
          + DatabaseHelper.getInOperatorPlaceholder(excludeAnaesthesiaIds.size()) + GROUP_BY;
      for (String mapping : excludeAnaesthesiaIds) {
        params.add(mapping);
      }
    } else {
      baseQuery = baseQuery + GROUP_BY;
    }
    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(baseQuery, params.toArray());
    if (beans == null) {
      return null;
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (BasicDynaBean bean : beans) {
      Map<String, Object> map = new HashMap<>();
      map.put("operationcode", (String) bean.get("operation_code"));
      map.put("number", bean.get("cn"));
      list.add(map);
    }
    return list;
  }

  @Override
  protected boolean submit(int year, OhsrsdohgovphSettings settings,
      List<Map<String, String>> uploadData) {
    String responseXml = "";
    String reportYear = String.valueOf(year);
    boolean status = true;
    if (uploadData.size() == 0) {
      Map<String, String> map = new HashMap<>();
      map.put("operationcode", (String) getReportMetaField("no_operations_ohsrsdohgovph"));
      map.put("number", "0");
      uploadData.add(map);
    }
    for (Map<String, String> data : uploadData) {
      updateSurgeryFields(data, "operationcode", "surgicaloperation");
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospitalOperationsMajorOpt(
            settings.getHfhudCode(), 
            data.get("operationcode"),
            data.get("surgicaloperation"),
            data.get("number"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospitalOperationsMajorOpt(
            settings.getHfhudCode(), 
            data.get("operationcode"),
            data.get("surgicaloperation"),
            data.get("number"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
