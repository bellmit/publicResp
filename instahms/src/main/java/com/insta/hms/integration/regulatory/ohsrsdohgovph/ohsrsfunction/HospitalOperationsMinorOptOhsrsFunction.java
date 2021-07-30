package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospitalOperationsMinorOpt")
public class HospitalOperationsMinorOptOhsrsFunction extends GenericOhsrsFunction {

  private static final String MINOR_SURGERIES = "SELECT "
      + " om.operation_code, count(distinct bc.charge_id) cn"
      + " FROM bill_charge bc"
      + " JOIN bill b on b.bill_no = bc.bill_no"
      + " JOIN patient_registration pr on pr.patient_id = b.visit_id"
      + " JOIN operation_master om on om.op_id = bc.op_id"
      + " WHERE bc.charge_head = 'SACOPE'"
      + " AND bc.status != 'X'"
      + " AND pr.center_id = ?"
      + " AND pr.reg_date between ?::date and ?::date"
      + " AND bc.charge_id not in (";

  private static final String MAJOR_SURGERIES = "SELECT "
      + " bci.charge_ref "
      + " FROM bill_charge bci"
      + " JOIN bill bi on bi.bill_no = bci.bill_no"
      + " JOIN patient_registration pri on pri.patient_id = bi.visit_id"
      + " WHERE bci.charge_head = 'ANATOPE'"
      + " AND bci.status != 'X'"
      + " AND pri.center_id = ?"
      + " AND pri.reg_date between ?::date and ?::date";

  private static final String MAJOR_EXCLUDE_ANAT = " AND bci.act_description_id NOT IN ";
  
  private static final String MINOR_CHARGE_REF_EXCLUDE = ") AND (bc.charge_ref IS NULL OR"
      + " bc.charge_ref NOT IN (";

  private static final String MINOR_GROUP_BY = ")) GROUP BY om.operation_code";
 
  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(centerId);
    params.add(yearStart);
    params.add(yearEnd);
    List<Object> majorParams = new ArrayList<>();
    majorParams.addAll(params);
    List<String> excludeAnaesthesiaIds = getListMapping("minor_anesthesia_ohsrsdohgovph");
    String majorQuery = MAJOR_SURGERIES;
    for (String mapping : excludeAnaesthesiaIds) {
      majorParams.add(mapping);
    }
    if (excludeAnaesthesiaIds.size() > 0) {
      majorQuery = MAJOR_SURGERIES + MAJOR_EXCLUDE_ANAT
          + DatabaseHelper.getInOperatorPlaceholder(excludeAnaesthesiaIds.size());
    }
    params.addAll(majorParams);
    params.addAll(majorParams);
    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(
        MINOR_SURGERIES + majorQuery + MINOR_CHARGE_REF_EXCLUDE + majorQuery + MINOR_GROUP_BY,
        params.toArray());
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
        responseXml = client.getTrainingPort().hospitalOperationsMinorOpt(
            settings.getHfhudCode(), 
            data.get("operationcode"),
            data.get("surgicaloperation"),
            data.get("number"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospitalOperationsMinorOpt(
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
