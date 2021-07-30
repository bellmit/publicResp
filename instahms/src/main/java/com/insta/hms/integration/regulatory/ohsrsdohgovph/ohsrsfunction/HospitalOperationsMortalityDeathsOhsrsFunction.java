package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@OhsrsFunctionProcessor(supports = "hospitalOperationsMortalityDeaths")
public class HospitalOperationsMortalityDeathsOhsrsFunction extends GenericOhsrsFunction {


  private static final String AGE_DIST_MORBIDITY_1 = "SELECT"
      + " foo.icd_code, "
      + " CASE"
      + "   WHEN foo.pat_age < 1 THEN 0"
      + "   WHEN foo.pat_age BETWEEN 1 AND 4 THEN 1"
      + "   WHEN foo.pat_age BETWEEN 5 AND 9 THEN 5"
      + "   WHEN foo.pat_age BETWEEN 10 AND 14 THEN 10"
      + "   WHEN foo.pat_age BETWEEN 15 AND 19 THEN 15"
      + "   WHEN foo.pat_age BETWEEN 20 AND 24 THEN 20"
      + "   WHEN foo.pat_age BETWEEN 25 AND 29 THEN 25"
      + "   WHEN foo.pat_age BETWEEN 30 AND 34 THEN 30"
      + "   WHEN foo.pat_age BETWEEN 35 AND 39 THEN 35"
      + "   WHEN foo.pat_age BETWEEN 40 AND 44 THEN 40"
      + "   WHEN foo.pat_age BETWEEN 45 AND 49 THEN 45"
      + "   WHEN foo.pat_age BETWEEN 50 AND 54 THEN 50"
      + "   WHEN foo.pat_age BETWEEN 55 AND 59 THEN 55"
      + "   WHEN foo.pat_age BETWEEN 60 AND 64 THEN 60"
      + "   WHEN foo.pat_age BETWEEN 65 AND 69 THEN 65"
      + "   ELSE 70 END as age_group,"
      + " foo.patient_gender,"
      + " sum(foo.cn)::int cn FROM ("
      + "   SELECT "
      + "     pd.cause_of_death_icdcode as icd_code,"
      + "     pd.patient_gender,"
      + "     FLOOR((pr.discharge_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)"
      + "      as pat_age, "
      + "     count(pd.mr_no) as cn"
      + "   FROM patient_registration pr"
      + "   JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + "   WHERE pr.visit_type='i'"
      + "   AND pr.discharge_date between ?::date and ?::date"
      + "   AND pr.center_id = ?"
      + "   AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + "   AND (pr.discharge_type_id = 3)";

  private static final String AGE_DIST_FILTER = "   AND pd.cause_of_death_icdcode NOT IN ";

  private static final String AGE_DIST_MORBIDITY_2 = "   GROUP BY pd.cause_of_death_icdcode,"
      + "     pd.patient_gender,"
      + "     FLOOR((pr.discharge_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25)"
      + " ) foo"
      + " GROUP BY" 
      + "   foo.icd_code,"
      + "   foo.patient_gender,"
      + "   CASE"
      + "     WHEN foo.pat_age < 1 THEN 0"
      + "     WHEN foo.pat_age BETWEEN 1 AND 4 THEN 1"
      + "     WHEN foo.pat_age BETWEEN 5 AND 9 THEN 5"
      + "     WHEN foo.pat_age BETWEEN 10 AND 14 THEN 10"
      + "     WHEN foo.pat_age BETWEEN 15 AND 19 THEN 15"
      + "     WHEN foo.pat_age BETWEEN 20 AND 24 THEN 20"
      + "     WHEN foo.pat_age BETWEEN 25 AND 29 THEN 25"
      + "     WHEN foo.pat_age BETWEEN 30 AND 34 THEN 30"
      + "     WHEN foo.pat_age BETWEEN 35 AND 39 THEN 35"
      + "     WHEN foo.pat_age BETWEEN 40 AND 44 THEN 40"
      + "     WHEN foo.pat_age BETWEEN 45 AND 49 THEN 45"
      + "     WHEN foo.pat_age BETWEEN 50 AND 54 THEN 50"
      + "     WHEN foo.pat_age BETWEEN 55 AND 59 THEN 55"
      + "     WHEN foo.pat_age BETWEEN 60 AND 64 THEN 60"
      + "     WHEN foo.pat_age BETWEEN 65 AND 69 THEN 65"
      + "     ELSE 70 END";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    Map<String, List<String>> icdMappings = getIcdMapping();
    List<String> exclusionIcds = new ArrayList<>();
    if (icdMappings.get("maternaldeath") != null) {
      exclusionIcds.addAll(icdMappings.get("maternaldeath"));
    }
    if (icdMappings.get("cardiacarrest") != null) {
      exclusionIcds.addAll(icdMappings.get("cardiacarrest"));
    }

    List<Object> params = new ArrayList<>();
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    params.add(yearStart);
    params.add(yearEnd);
    params.add(centerId);
    String query = AGE_DIST_MORBIDITY_1 + AGE_DIST_MORBIDITY_2; 
    if (exclusionIcds.size() > 0) {
      query = AGE_DIST_MORBIDITY_1 + AGE_DIST_FILTER
        + DatabaseHelper.getInOperatorPlaceholder(exclusionIcds.size()) + AGE_DIST_MORBIDITY_2;
      for (String icd : exclusionIcds) {
        params.add(icd);
      }
    }

    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(query, params.toArray());
    if (beans == null) {
      return null;
    }
    Map<String, Map<String, Object>> icdWiseMap = new HashMap<>();
    for (BasicDynaBean bean : beans) {
      String icdCode = (String) bean.get("icd_code");
      if (!icdWiseMap.containsKey(icdCode)) {
        Map<String,Object> map = new HashMap<>();
        icdWiseMap.put(icdCode, map);
        map.put("munder1", 0);
        map.put("m1to4", 0);
        map.put("m5to9", 0);
        map.put("m10to14", 0);
        map.put("m15to19", 0);
        map.put("m20to24", 0);
        map.put("m25to29", 0);
        map.put("m30to34", 0);
        map.put("m35to39", 0);
        map.put("m40to44", 0);
        map.put("m45to49", 0);
        map.put("m50to54", 0);
        map.put("m55to59", 0);
        map.put("m60to64", 0);
        map.put("m65to69", 0);
        map.put("m70over", 0);
        map.put("funder1", 0);
        map.put("f1to4", 0);
        map.put("f5to9", 0);
        map.put("f10to14", 0);
        map.put("f15to19", 0);
        map.put("f20to24", 0);
        map.put("f25to29", 0);
        map.put("f30to34", 0);
        map.put("f35to39", 0);
        map.put("f40to44", 0);
        map.put("f45to49", 0);
        map.put("f50to54", 0);
        map.put("f55to59", 0);
        map.put("f60to64", 0);
        map.put("f65to69", 0);
        map.put("f70over", 0);
        map.put("icd10code", (String) bean.get("icd_code"));
      }
      Map<String, Object> row = icdWiseMap.get(icdCode);
      switch ((int) bean.get("age_group")) {
        case 0: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "under1", bean.get("cn"));
          break;
        case 1: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "1to4", bean.get("cn"));
          break;
        case 5: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "5to9", bean.get("cn"));
          break;
        case 10: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "10to14", bean.get("cn"));
          break;
        case 15: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "15to19", bean.get("cn"));
          break;
        case 20: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "20to24", bean.get("cn"));
          break;
        case 25: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "25to29", bean.get("cn"));
          break;
        case 30: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "30to34", bean.get("cn"));
          break;
        case 35: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "35to39", bean.get("cn"));
          break;
        case 40: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "40to44", bean.get("cn"));
          break;
        case 45: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "45to49", bean.get("cn"));
          break;
        case 50: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "50to54", bean.get("cn"));
          break;
        case 55: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "55to59", bean.get("cn"));
          break;
        case 60: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "60to64", bean.get("cn"));
          break;
        case 65: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "65to69", bean.get("cn"));
          break;
        case 70: row.put(
            ((String) bean.get("patient_gender")).toLowerCase() + "70over", bean.get("cn"));
          break;
        default:
      }
    }
    List<Map<String, Object>> list = new ArrayList<>();
    for (Entry<String, Map<String,Object>> entry : icdWiseMap.entrySet()) {
      list.add(entry.getValue());
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
      map.put("icd10code", (String) getReportMetaField("unknown_death_icdcode_ohsrsdohgovph"));
      map.put("munder1", "0");
      map.put("m1to4", "0");
      map.put("m5to9", "0");
      map.put("m10to14", "0");
      map.put("m15to19", "0");
      map.put("m20to24", "0");
      map.put("m25to29", "0");
      map.put("m30to34", "0");
      map.put("m35to39", "0");
      map.put("m40to44", "0");
      map.put("m45to49", "0");
      map.put("m50to54", "0");
      map.put("m55to59", "0");
      map.put("m60to64", "0");
      map.put("m65to69", "0");
      map.put("m70over", "0");
      map.put("funder1", "0");
      map.put("f1to4", "0");
      map.put("f5to9", "0");
      map.put("f10to14", "0");
      map.put("f15to19", "0");
      map.put("f20to24", "0");
      map.put("f25to29", "0");
      map.put("f30to34", "0");
      map.put("f35to39", "0");
      map.put("f40to44", "0");
      map.put("f45to49", "0");
      map.put("f50to54", "0");
      map.put("f55to59", "0");
      map.put("f60to64", "0");
      map.put("f65to69", "0");
      map.put("f70over", "0");
      uploadData.add(map);
    }
    for (Map<String, String> data : uploadData) {
      updateIcdFields(data, "icd10code", "icd10desc", "icd10category");
      data.put("msubtotal", String.valueOf(Integer.parseInt(data.get("munder1")) 
          + Integer.parseInt(data.get("m1to4")) + Integer.parseInt(data.get("m5to9")) 
          + Integer.parseInt(data.get("m10to14")) + Integer.parseInt(data.get("m15to19")) 
          + Integer.parseInt(data.get("m20to24")) + Integer.parseInt(data.get("m25to29")) 
          + Integer.parseInt(data.get("m30to34")) + Integer.parseInt(data.get("m35to39")) 
          + Integer.parseInt(data.get("m40to44")) + Integer.parseInt(data.get("m45to49")) 
          + Integer.parseInt(data.get("m50to54")) + Integer.parseInt(data.get("m55to59")) 
          + Integer.parseInt(data.get("m60to64")) + Integer.parseInt(data.get("m65to69")) 
          + Integer.parseInt(data.get("m70over"))));
      data.put("fsubtotal", String.valueOf(Integer.parseInt(data.get("funder1")) 
          + Integer.parseInt(data.get("f1to4")) + Integer.parseInt(data.get("f5to9")) 
          + Integer.parseInt(data.get("f10to14")) + Integer.parseInt(data.get("f15to19")) 
          + Integer.parseInt(data.get("f20to24")) + Integer.parseInt(data.get("f25to29")) 
          + Integer.parseInt(data.get("f30to34")) + Integer.parseInt(data.get("f35to39")) 
          + Integer.parseInt(data.get("f40to44")) + Integer.parseInt(data.get("f45to49")) 
          + Integer.parseInt(data.get("f50to54")) + Integer.parseInt(data.get("f55to59")) 
          + Integer.parseInt(data.get("f60to64")) + Integer.parseInt(data.get("f65to69")) 
          + Integer.parseInt(data.get("f70over"))));
      data.put("grandtotal", String.valueOf(Integer.parseInt(data.get("msubtotal")) 
          + Integer.parseInt(data.get("fsubtotal"))));
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospitalOperationsMortalityDeaths(
            settings.getHfhudCode(), 
            data.get("icd10desc"),
            data.get("munder1"),
            data.get("funder1"),
            data.get("m1to4"),
            data.get("f1to4"),
            data.get("m5to9"),
            data.get("f5to9"),
            data.get("m10to14"),
            data.get("f10to14"),
            data.get("m15to19"),
            data.get("f15to19"),
            data.get("m20to24"),
            data.get("f20to24"),
            data.get("m25to29"),
            data.get("f25to29"),
            data.get("m30to34"),
            data.get("f30to34"),
            data.get("m35to39"),
            data.get("f35to39"),
            data.get("m40to44"),
            data.get("f40to44"),
            data.get("m45to49"),
            data.get("f45to49"),
            data.get("m50to54"),
            data.get("f50to54"),
            data.get("m55to59"),
            data.get("f55to59"),
            data.get("m60to64"),
            data.get("f60to64"),
            data.get("m65to69"),
            data.get("f65to69"),
            data.get("m70over"),
            data.get("f70over"),
            data.get("msubtotal"),
            data.get("fsubtotal"),
            data.get("grandtotal"),
            data.get("icd10code"),
            data.get("icd10category"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospitalOperationsMortalityDeaths(
            settings.getHfhudCode(), 
            data.get("icd10desc"),
            data.get("munder1"),
            data.get("funder1"),
            data.get("m1to4"),
            data.get("f1to4"),
            data.get("m5to9"),
            data.get("f5to9"),
            data.get("m10to14"),
            data.get("f10to14"),
            data.get("m15to19"),
            data.get("f15to19"),
            data.get("m20to24"),
            data.get("f20to24"),
            data.get("m25to29"),
            data.get("f25to29"),
            data.get("m30to34"),
            data.get("f30to34"),
            data.get("m35to39"),
            data.get("f35to39"),
            data.get("m40to44"),
            data.get("f40to44"),
            data.get("m45to49"),
            data.get("f45to49"),
            data.get("m50to54"),
            data.get("f50to54"),
            data.get("m55to59"),
            data.get("f55to59"),
            data.get("m60to64"),
            data.get("f60to64"),
            data.get("m65to69"),
            data.get("f65to69"),
            data.get("m70over"),
            data.get("f70over"),
            data.get("msubtotal"),
            data.get("fsubtotal"),
            data.get("grandtotal"),
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
