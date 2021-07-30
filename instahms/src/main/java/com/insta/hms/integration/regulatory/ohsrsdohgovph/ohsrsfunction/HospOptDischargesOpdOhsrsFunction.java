package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospOptDischargesOPD")
public class HospOptDischargesOpdOhsrsFunction extends GenericOhsrsFunction {

  private static final String OPD_ICD_1 = "SELECT foo.icd_code, sum(foo.cn)::int cn FROM ("
      + "  SELECT md1.icd_code, count(md1.id) as cn "
      + "  FROM mrd_diagnosis md1 "
      + "  JOIN patient_registration pr1 on pr1.patient_id = md1.visit_id"
      + "  WHERE pr1.visit_type='o' "
      + "  AND pr1.reg_date between ?::date and ?::date"
      + "  AND pr1.center_id = ? AND md1.diag_type = 'V'"
      + "  AND md1.code_type like 'ICD%'";
  
  private static final String OPD_ICD_2 = " AND pr1.admitted_dept not in ";     
  
  private static final String OPD_ICD_3 = "  GROUP BY md1.icd_code"
      + "  UNION ALL"
      + "  SELECT md2.icd_code, count(md2.id) as cn "
      + "  FROM mrd_diagnosis md2 "
      + "  JOIN patient_registration pr2 on pr2.patient_id = md2.visit_id"
      + "  WHERE pr2.visit_type='o' "
      + "  AND pr2.reg_date between ?::date and ?::date"
      + "  AND pr2.center_id = ? AND md2.diag_type = 'P'"
      + "  AND md2.code_type like 'ICD%'";
  
  private static final String OPD_ICD_4 = " AND pr2.admitted_dept not in ";   
  
  private static final String OPD_ICD_5 = "  AND md2.visit_id not in ("
      + "    SELECT mdi.visit_id"
      + "    FROM mrd_diagnosis mdi "
      + "    JOIN patient_registration pri on pri.patient_id = mdi.visit_id"
      + "    WHERE pri.visit_type='o' "
      + "    AND pri.reg_date between ?::date and ?::date"
      + "    AND pri.center_id = ? AND mdi.diag_type = 'V'"
      + "    AND mdi.code_type like 'ICD%'";
  
  private static final String OPD_ICD_6 = " AND pri.admitted_dept not in "; 
  
  private static final String OPD_ICD_7 = "  ) GROUP BY md2.icd_code"
      + ") foo group by foo.icd_code;";
  
  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(yearStart);
    params.add(yearEnd);
    params.add(centerId);
    Map<String, List<String>> deptMapping = getDeptMapping();
    List<String> erMapping = deptMapping.get("emergency");
    String baseQuery;
    if (erMapping.size() > 0) {
      String placeHolders = DatabaseHelper.getInOperatorPlaceholder(erMapping.size());
      baseQuery = OPD_ICD_1 + OPD_ICD_2 + placeHolders + OPD_ICD_3 + OPD_ICD_4 + placeHolders
          + OPD_ICD_5 + OPD_ICD_6 + placeHolders + OPD_ICD_7;
      for (String mapping : erMapping) {
        params.add(mapping);
      }
    } else {
      baseQuery = OPD_ICD_1 + OPD_ICD_3 + OPD_ICD_5 + OPD_ICD_7;
    }
    List<Object> finalParams = new ArrayList<>();
    finalParams.addAll(params);
    finalParams.addAll(params);
    finalParams.addAll(params);
    Object[] paramsArr = finalParams.toArray();
    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(baseQuery, paramsArr);
    if (beans == null) {
      return null;
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (BasicDynaBean bean : beans) {
      Map<String, Object> map = new HashMap<>();
      map.put("icd10code", (String) bean.get("icd_code"));
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
      map.put("icd10code", (String) getReportMetaField("unknown_icdcode_ohsrsdohgovph"));
      map.put("number", "0");
      uploadData.add(map);
    }
    for (Map<String, String> data : uploadData) {
      updateIcdFields(data, "icd10code", "opdconsultations", "icd10category");
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospOptDischargesOPD(
            settings.getHfhudCode(), 
            data.get("opdconsultations"),
            data.get("number"),
            data.get("icd10code"),
            data.get("icd10category"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesOPD(
            settings.getHfhudCode(), 
            data.get("opdconsultations"),
            data.get("number"),
            data.get("icd10code"),
            data.get("icd10category"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
