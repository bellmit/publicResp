package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospitalOperationsDeaths")
public class HospitalOperationsDeathsOhsrsFunction extends GenericOhsrsFunction {

  private static final String TOTAL_IP_ALIVE_DISCHARGES =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND (discharge_type_id not in  (6,3))"
      + " AND (discharge_date between ?::date AND ?::date);";

  private static final String TOTAL_IP_DEATH_UNDER_48H =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.dead_on_arrival is null OR pd.dead_on_arrival != 'Y')"
      + " AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + " AND pr.discharge_type_id = 3"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND (EXTRACT(EPOCH FROM"
      + "       ((pd.death_date + pd.death_time) - (pr.reg_date + pr.reg_time)))/3600) < 48";

  private static final String TOTAL_IP_DEATH_ABOVE_48H =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.dead_on_arrival is null OR pd.dead_on_arrival != 'Y')"
      + " AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND (EXTRACT(EPOCH FROM"
      + "       ((pd.death_date + pd.death_time) - (pr.reg_date + pr.reg_time)))/3600) >= 48";

  private static final String TOTAL_IP_DEATH_DOA =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.dead_on_arrival = 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)";
  
  private static final String TOTAL_IP_DEATH_STILLBORN =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.stillborn = 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)";

  private static final String TOTAL_IP_DEATH_MATERNAL =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND pd.cause_of_death_icdcode IN ";

  private static final String TOTAL_DEATH_ER =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'o'"
      + " AND pr.center_id = ?"
      + " AND (pd.dead_on_arrival is null OR pd.dead_on_arrival != 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND pr.admitted_dept in";

  private static final String TOTAL_IP_DEATH_NEONATAL =  "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE"
      + " pr.visit_type = 'i'"
      + " AND pr.center_id = ?"
      + " AND (pd.dead_on_arrival is null OR pd.dead_on_arrival != 'Y')"
      + " AND (pd.stillborn is null OR pd.stillborn != 'Y')"
      + " AND (pr.discharge_type_id = 3)"
      + " AND (pr.discharge_date between ?::date AND ?::date)"
      + " AND (pd.death_date - COALESCE(pd.dateofbirth, pd.expected_dob)) <= 28";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    Object[] paramsArr = new Object[] {centerId, yearStart, yearEnd};
    Map<String, Object> map = new HashMap<>();
    Integer du48 = DatabaseHelper.getInteger(TOTAL_IP_DEATH_UNDER_48H, paramsArr);
    Integer da48 = DatabaseHelper.getInteger(TOTAL_IP_DEATH_ABOVE_48H, paramsArr);
    Integer doa = DatabaseHelper.getInteger(TOTAL_IP_DEATH_DOA, paramsArr);
    map.put("totaldeaths48down", du48);
    map.put("totaldeaths48up", da48);
    map.put("totaldoa", doa);
    map.put("totalstillbirths", DatabaseHelper.getInteger(TOTAL_IP_DEATH_STILLBORN, paramsArr));
    map.put("totalneonataldeaths", DatabaseHelper.getInteger(TOTAL_IP_DEATH_NEONATAL, paramsArr));
    Map<String, List<String>> icdMappings = getIcdMapping();
    List<String> exclusionIcds = icdMappings.get("maternaldeath");
    if (exclusionIcds != null && exclusionIcds.size() > 0) {
      List<Object> params = new ArrayList<>();
      params.add(centerId);
      params.add(yearStart);
      params.add(yearEnd);
      String erQuery = TOTAL_IP_DEATH_MATERNAL 
          + DatabaseHelper.getInOperatorPlaceholder(exclusionIcds.size());
      for (String mapping : exclusionIcds) {
        params.add(mapping);
      }
      Object[] maternalDeathParamsArr = params.toArray();
      map.put("totalmaternaldeaths", DatabaseHelper.getInteger(erQuery, maternalDeathParamsArr));
    } else {
      map.put("totalmaternaldeaths", 0);
    }
    Map<String, List<String>> deptMapping = getDeptMapping();
    List<String> erMapping = deptMapping.get("emergency");
    Integer erd = 0;
    if (erMapping != null && erMapping.size() > 0) {
      List<Object> params = new ArrayList<>();
      params.add(centerId);
      params.add(yearStart);
      params.add(yearEnd);
      String erQuery = TOTAL_DEATH_ER + DatabaseHelper.getInOperatorPlaceholder(erMapping.size());
      for (String mapping : erMapping) {
        params.add(mapping);
      }
      Object[] erParamsArr = params.toArray();
      erd = DatabaseHelper.getInteger(erQuery, erParamsArr);
    }
    map.put("totalerdeaths", erd);
    map.put("totaldischargedeaths", 
        DatabaseHelper.getInteger(TOTAL_IP_ALIVE_DISCHARGES, paramsArr) + du48 + da48 + doa + erd);
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
      Integer totalIpDeathD48 = Integer.parseInt(data.get("totaldeaths48down"));
      Integer totalIpDeaths = totalIpDeathD48 + Integer.parseInt(data.get("totaldeaths48up"));
      Integer totalDischarges = Integer.parseInt(data.get("totaldischargedeaths"));
      Integer totalDeaths = totalIpDeaths
          + Integer.parseInt(data.get("totalerdeaths"))
          + Integer.parseInt(data.get("totaldoa"));
      Integer ndrNumerator = totalDeaths - totalIpDeathD48;
      Integer ndrDenominator = totalDischarges - totalIpDeathD48;
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospitalOperationsDeaths(
            settings.getHfhudCode(), 
            String.valueOf(totalDeaths),
            data.get("totaldeaths48down"),
            data.get("totaldeaths48up"),
            data.get("totalerdeaths"),
            data.get("totaldoa"),
            data.get("totalstillbirths"),
            data.get("totalneonataldeaths"),
            data.get("totalmaternaldeaths"),
            String.valueOf(totalIpDeaths),
            data.get("totaldischargedeaths"),
            String.valueOf(totalIpDeaths / totalDischarges * 100),
            String.valueOf(ndrNumerator),
            String.valueOf(ndrDenominator),
            String.valueOf(ndrNumerator / ndrDenominator * 100),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospitalOperationsDeaths(
            settings.getHfhudCode(), 
            String.valueOf(totalDeaths),
            data.get("totaldeaths48down"),
            data.get("totaldeaths48up"),
            data.get("totalerdeaths"),
            data.get("totaldoa"),
            data.get("totalstillbirths"),
            data.get("totalneonataldeaths"),
            data.get("totalmaternaldeaths"),
            String.valueOf(totalIpDeaths),
            data.get("totaldischargedeaths"),
            String.valueOf(totalIpDeaths / totalDischarges * 100),
            String.valueOf(ndrNumerator),
            String.valueOf(ndrDenominator),
            String.valueOf(ndrNumerator / ndrDenominator * 100),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
