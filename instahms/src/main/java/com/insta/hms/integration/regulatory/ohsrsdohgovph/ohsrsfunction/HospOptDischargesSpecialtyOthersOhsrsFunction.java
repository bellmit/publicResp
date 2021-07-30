package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@OhsrsFunctionProcessor(supports = "hospOptDischargesSpecialtyOthers")
public class HospOptDischargesSpecialtyOthersOhsrsFunction extends GenericOhsrsFunction {

  private static final String ADMDEPT_STAY_LENGTH =  "SELECT "
      + " d.dept_name as service,"
      + " count(*) cn, "
      + " sum(EXTRACT(EPOCH FROM "
      + "   ((discharge_date + discharge_time) - (reg_date + reg_time))/86400))::int service_days"
      + " FROM patient_registration pr"
      + " JOIN department d on d.dept_id = pr.admitted_dept"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND pr.admitted_dept NOT IN #ADMDEPT#"
      + " AND (pr.discharge_type_id IS NOT NULL AND pr.discharge_type_id != 6)"
      + " GROUP BY d.dept_name";

  private static final String ADMDEPT_ACC_SPLIT =  "SELECT "
      + " d.dept_name as service,"
      + " CASE"
      + "   WHEN pr.ward_id in #WARD# THEN 'service' ELSE 'pay' END ward_type,"
      + " CASE "
      + "   WHEN pi.sponsor_id = #PH# OR si.sponsor_id = #PH# THEN 'ph'"
      + "   WHEN pi.sponsor_id = #OWWA# or si.sponsor_id = #OWWA# THEN 'owwa'"
      + "   WHEN pi.sponsor_id IN #HMO# OR si.sponsor_id IN #HMO# THEN 'hmo'"
      + "   ELSE 'np' END payor,"
      + " count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN department d on d.dept_id = pr.admitted_dept"
      + " LEFT JOIN patient_insurance_plans pi on pi.patient_id = pr.patient_id and pi.priority = 1"
      + " LEFT JOIN patient_insurance_plans si on si.patient_id = pr.patient_id and pi.priority = 2"
      + " WHERE pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (discharge_date between ?::date AND ?::date)"
      + " AND pr.discharge_type_id IS NOT NULL"
      + " AND pr.discharge_type_id != 6"
      + " AND pr.admitted_dept NOT IN #ADMDEPT#"
      + " GROUP BY"
      + "   d.dept_name,"
      + "   CASE WHEN pr.ward_id in #WARD# THEN 'service' ELSE 'pay' END,"
      + "   CASE "
      + "     WHEN pi.sponsor_id = #PH# OR si.sponsor_id = #PH# THEN 'ph'"
      + "     WHEN pi.sponsor_id = #OWWA# or si.sponsor_id = #OWWA# THEN 'owwa'"
      + "     WHEN pi.sponsor_id IN #HMO# OR si.sponsor_id IN #HMO# THEN 'hmo'"
      + "     ELSE 'np' END";

  private static final String ADMDEPT_DISCHARGE_SPLIT =  "SELECT "
      + " d.dept_name as service,"
      + " pr.discharge_type_id,"
      + " count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN department d on d.dept_id = pr.admitted_dept"
      + " WHERE pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (discharge_date between ?::date AND ?::date)"
      + " AND pr.discharge_type_id IS NOT NULL"
      + " AND pr.discharge_type_id not in (6,3)"
      + " AND pr.admitted_dept NOT IN #ADMDEPT#"
      + " GROUP BY"
      + " d.dept_name,"
      + "   pr.discharge_type_id";

  private static final String ADMDEPT_DEATH_SPLIT =  "SELECT "
      + " d.dept_name as service,"
      + " CASE WHEN (EXTRACT(EPOCH FROM "
      + "   ((pd.death_date + pd.death_time) - (pr.reg_date + pr.reg_time)))/3600) < 48"
      + "   THEN 'U48' ELSE 'A48' END deathsplit,"
      + " count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " JOIN department d on d.dept_id = pr.admitted_dept"
      + " WHERE pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (discharge_date between ?::date AND ?::date)"
      + " AND pr.discharge_type_id IS NOT NULL"
      + " AND pr.discharge_type_id = 3"
      + " AND pr.admitted_dept NOT IN #ADMDEPT#"
      + " GROUP BY"
      + " d.dept_name,"
      + "   CASE WHEN (EXTRACT(EPOCH FROM "
      + "     ((pd.death_date + pd.death_time) - (pr.reg_date + pr.reg_time)))/3600) < 48"
      + "     THEN 'U48' ELSE 'A48' END";

  private static final List<String> INVALID_ID_LIST = Arrays.asList("INVALID_TPA_DEP");

  private static final String INVALID_ID_KEY = "INVALID_TPA_DEP";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(centerId);
    params.add(yearStart);
    params.add(yearEnd);

    Map<String, List<String>> deptMapping = getDeptMapping();

    List<Object> allDeptIds = new ArrayList<>();
    List<String> genMedDeptIds = deptMapping.get("medicine");
    if (genMedDeptIds != null && genMedDeptIds.size() > 0) {
      allDeptIds.addAll(genMedDeptIds);
    }
    List<String> obDeptIds = deptMapping.get("obstetrics");
    if (obDeptIds != null && obDeptIds.size() > 0) {
      allDeptIds.addAll(obDeptIds);
    }
    List<String> gynDeptIds = deptMapping.get("gynecology");
    if (gynDeptIds != null && gynDeptIds.size() > 0) {
      allDeptIds.addAll(gynDeptIds);
    }
    List<String> pedDeptIds = deptMapping.get("pediatrics");
    if (pedDeptIds != null && pedDeptIds.size() > 0) {
      allDeptIds.addAll(pedDeptIds);
    }
    List<String> surDeptIds = deptMapping.get("ipsurgery");
    if (surDeptIds != null && surDeptIds.size() > 0) {
      allDeptIds.addAll(surDeptIds);
    }
    List<String> charityWards = getListMapping("charityserviceward_mapping_ohsrsdohgovph");
    if (charityWards == null || charityWards.size() == 0) {
      charityWards = INVALID_ID_LIST;
    }
    String wardPlaceholders = getDeptInClause(charityWards);
    String deptPlaceholders = DatabaseHelper.getInOperatorPlaceholder(allDeptIds.size());
    String accQuery = ADMDEPT_ACC_SPLIT.replaceAll("#ADMDEPT#", deptPlaceholders);
    accQuery = accQuery.replaceAll("#WARD#", wardPlaceholders);

    List<String> hmoTpaIds = getListMapping("hmo_tpaid_ohsrsdohgovph");
    if (hmoTpaIds == null || hmoTpaIds.size() == 0) {
      hmoTpaIds = INVALID_ID_LIST;
    }
    String hmoPlaceholders = getDeptInClause(hmoTpaIds);
    accQuery = accQuery.replaceAll("#HMO#", hmoPlaceholders);

    String owwaTpaId = (String) getReportMetaField("owwa_tpaid_ohsrsdohgovph");
    if (owwaTpaId == null || owwaTpaId.isEmpty()) {
      owwaTpaId = INVALID_ID_KEY;
    }
    accQuery = accQuery.replaceAll("#OWWA#", "'" + owwaTpaId + "'");

    String phTpaId = (String) getReportMetaField("philhealth_tpaid_ohsrsdohgovph");
    if (phTpaId == null || phTpaId.isEmpty()) {
      phTpaId = INVALID_ID_KEY;
    }
    accQuery = accQuery.replaceAll("#PH#", "'" + phTpaId + "'");

    params.addAll(allDeptIds);
    
    Map<String, Map<String, Object>> map = new HashMap<>();
    String stayQuery = ADMDEPT_STAY_LENGTH.replaceAll("#ADMDEPT#", deptPlaceholders);
    Object[] paramsArr = params.toArray();
    List<BasicDynaBean> beans = DatabaseHelper.queryToDynaList(stayQuery, paramsArr);
    if (beans != null) {
      for (BasicDynaBean bean : beans) {
        String key = (String) bean.get("service");
        if (!map.containsKey(key)) {
          map.put(key, createDefaultRowMap(key));          
        }
        Map<String, Object> rowMap = map.get(key);
        rowMap.put("nopatients", bean.get("cn"));
        rowMap.put("totallengthstay", bean.get("service_days"));
      }
    }
    
    String dischargeQuery = ADMDEPT_DISCHARGE_SPLIT.replaceAll("#ADMDEPT#", deptPlaceholders);
    beans = DatabaseHelper.queryToDynaList(dischargeQuery, paramsArr);
    if (beans != null) {
      for (BasicDynaBean bean : beans) {
        String key = (String) bean.get("service");
        if (!map.containsKey(key)) {
          map.put(key, createDefaultRowMap(key));          
        }
        Map<String, Object> rowMap = map.get(key);
        switch ((int) bean.get("discharge_type_id")) {
          case 1: rowMap.put("recoveredimproved", bean.get("cn"));
            break;
          case 2: rowMap.put("absconded", bean.get("cn")); 
            break;
          case 4: rowMap.put("hama", bean.get("cn")); 
            break;
          case 5: rowMap.put("transferred", bean.get("cn")); 
            break;
          default:
        }
      }
    }
    
    String deathSplitQuery = ADMDEPT_DEATH_SPLIT.replaceAll("#ADMDEPT#", deptPlaceholders);
    beans = DatabaseHelper.queryToDynaList(deathSplitQuery, paramsArr);
    if (beans != null) {
      for (BasicDynaBean bean : beans) {
        String key = (String) bean.get("service");
        if (!map.containsKey(key)) {
          map.put(key, createDefaultRowMap(key));          
        }
        Map<String, Object> rowMap = map.get(key);
        switch ((String) bean.get("deathsplit")) {
          case "A48": rowMap.put("deathsover48", bean.get("cn")); 
            break;
          case "U48": rowMap.put("deathsbelow48", bean.get("cn")); 
            break;
          default:
        }
      }
    }

    beans = DatabaseHelper.queryToDynaList(accQuery, paramsArr);
    if (beans != null) {
      for (BasicDynaBean bean : beans) {
        String key = (String) bean.get("service");
        if (!map.containsKey(key)) {
          map.put(key, createDefaultRowMap(key));          
        }
        String payor = (String) bean.get("payor");
        String wardType = (String) bean.get("ward_type");
        Map<String, Object> rowMap = map.get(key);
        if (rowMap.containsKey(payor)) {
          rowMap.put(payor, ((int) rowMap.get(payor))
              + (Integer.parseInt(String.valueOf(bean.get("cn")))));  
        } else {
          rowMap.put(payor + wardType, bean.get("cn"));
        }
      }
    }

    List<Map<String, Object>> list = new ArrayList<>();
    for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
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
    for (Map<String, String> data : uploadData) {
      updateIcdFields(data, "icd10code", "icd10desc", "icd10category");
      data.put("nphtotal", String.valueOf(Integer.parseInt(data.get("npservice")) 
          + Integer.parseInt(data.get("nppay"))));
      data.put("phtotal", String.valueOf(Integer.parseInt(data.get("phservice")) 
          + Integer.parseInt(data.get("phpay"))));
      data.put("totaldeaths", String.valueOf(Integer.parseInt(data.get("deathsbelow48")) 
          + Integer.parseInt(data.get("deathsover48"))));
      data.put("totaldischarges", String.valueOf(Integer.parseInt(data.get("recoveredimproved")) 
          + Integer.parseInt(data.get("transferred")) + Integer.parseInt(data.get("hama")) 
          + Integer.parseInt(data.get("absconded")) + Integer.parseInt(data.get("totaldeaths"))));
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospOptDischargesSpecialtyOthers(
            settings.getHfhudCode(), 
            data.get("othertypeofservicespecify"),
            data.get("nopatients"),
            data.get("totallengthstay"),
            data.get("nppay"),
            data.get("npservice"),
            data.get("nphtotal"),
            data.get("phpay"),
            data.get("phservice"),
            data.get("phtotal"),
            data.get("hmo"),
            data.get("owwa"),
            data.get("recoveredimproved"),
            data.get("transferred"),
            data.get("hama"),
            data.get("absconded"),
            "0",
            data.get("deathsbelow48"),
            data.get("deathsover48"),
            data.get("totaldeaths"),
            data.get("totaldischarges"),
            data.get("remarks"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesSpecialty(
            settings.getHfhudCode(), 
            data.get("othertypeofservicespecify"),
            data.get("nopatients"),
            data.get("totallengthstay"),
            data.get("nppay"),
            data.get("npservice"),
            data.get("nphtotal"),
            data.get("phpay"),
            data.get("phservice"),
            data.get("phtotal"),
            data.get("hmo"),
            data.get("owwa"),
            data.get("recoveredimproved"),
            data.get("transferred"),
            data.get("hama"),
            data.get("absconded"),
            "0",
            data.get("deathsbelow48"),
            data.get("deathsover48"),
            data.get("totaldeaths"),
            data.get("totaldischarges"),
            data.get("remarks"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

  private Map<String, Object> createDefaultRowMap(String service) {
    Map<String, Object> map = new HashMap<>();
    map.put("othertypeofservicespecify", service);
    map.put("nopatients", 0);
    map.put("totallengthstay", 0);
    map.put("nppay", 0);
    map.put("npservice", 0);
    map.put("phpay", 0);
    map.put("phservice", 0);
    map.put("hmo", 0);
    map.put("owwa", 0);
    map.put("recoveredimproved", 0);
    map.put("transferred", 0);
    map.put("hama", 0);
    map.put("absconded", 0);
    map.put("unimproved", 0);
    map.put("deathsbelow48", 0);
    map.put("deathsover48", 0);
    map.put("remarks", "");
    return map;
  }

  private String getDeptInClause(List<String> deptIds) {
    return "('" + StringUtils.arrayToDelimitedString(deptIds.toArray(), "','") + "')";
  }
}
