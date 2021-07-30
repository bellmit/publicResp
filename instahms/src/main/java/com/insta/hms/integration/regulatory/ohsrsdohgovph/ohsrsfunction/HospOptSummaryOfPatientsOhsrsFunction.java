package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospOptSummaryOfPatients")
public class HospOptSummaryOfPatientsOhsrsFunction extends GenericOhsrsFunction {

  private static final String TOTAL_IP =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND (discharge_type_id IS NULL OR discharge_type_id != 6)"
      + " AND ((reg_date between ?::date AND ?::date)"
      + "  OR (reg_date < ?::date "
      + "      AND (discharge_date is null OR discharge_date > ?::date)))";
  
  private static final String TOTAL_IP_OPENINGCOUNT =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND (discharge_type_id IS NULL OR discharge_type_id != 6)"
      + " AND reg_date < ?::date "
      + " AND (discharge_date is null OR discharge_date > ?::date)";
  
  private static final String TOTAL_IP_SAMEDAY =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND discharge_type_id != 6"
      + " AND (reg_date between ?::date AND ?::date)"
      + " AND (reg_date = discharge_date)";
  
  private static final String TOTAL_IP_ALIVE_DISCHARGES =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND (discharge_type_id not in (6,3))"
      + " AND (discharge_date between ?::date AND ?::date);";
      
  private static final String TOTAL_IP_TRFIN =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND (discharge_type_id IS NULL OR discharge_type_id != 6)"
      + " AND (reg_date between ?::date AND ?::date)"
      + " AND transfer_source is not null"
      + " AND transfer_source != ''";
  
  private static final String TOTAL_IP_TRFOUT =  "SELECT count(*) cn"
      + " FROM patient_registration"
      + " WHERE"
      + " visit_type = 'i'"
      + " AND center_id = ?"
      + " AND discharge_type_id = 5"
      + " AND (discharge_date between ?::date AND ?::date)"
      + " AND transfer_destination is not null"
      + " AND transfer_destination != ''";
  
  private static final String TOTAL_NEWBORN = "SELECT count(*) cn"
      + " FROM admission ad"
      + " JOIN patient_registration pr on pr.patient_id = ad.parent_id and pr.visit_type='i' "
      + "   AND center_id = ? "
      + " JOIN patient_details pd on pd.mr_no = ad.mr_no"
      + " WHERE ad.isbaby = 'Y'"
      + " AND (ad.admit_date BETWEEN ?::date AND ?::date) "
      + " AND (pd.death_date IS NULL OR pd.death_date != pd.dateofbirth)";
      
  private static final String TOTAL_SERVICEDAYS = " SELECT"
      + " sum(EXTRACT(EPOCH FROM (service_end - service_start))/86400)::int service_days "
      + " FROM (select CASE WHEN reg_date < ?::date THEN ?::timestamp ELSE reg_date+reg_time END "
      + "      as service_start, "
      + "    CASE WHEN (discharge_date is null or discharge_date > ?::date) THEN ?::timestamp "
      + "      ELSE discharge_date+discharge_time END as service_end"
      + "    FROM patient_registration "
      + "    WHERE visit_type = 'i'"
      + "    AND center_id = ?"
      + "    AND (discharge_type_id IS NULL OR discharge_type_id != 6)"
      + "    AND ((reg_date between ?::date AND ?::date)"
      + "      OR (reg_date < ?::date "
      + "         AND (discharge_date is null OR discharge_date > ?::date)))) foo";
  
  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    Map<String, Object> map = new HashMap<>();
    String yearStart = String.valueOf(year) + "-01-01";
    String yearEnd = String.valueOf(year) + "-12-31";
    String nextYearStart = String.valueOf(year + 1) + "-01-01";
    map.put("totalinpatients",DatabaseHelper.getInteger(TOTAL_IP, 
        new Object[] {centerId, yearStart, yearEnd, yearStart, yearStart}));
    map.put("totalnewborn",DatabaseHelper.getInteger(TOTAL_NEWBORN, 
        new Object[] {centerId, yearStart, yearEnd}));
    map.put("totaldischarges",DatabaseHelper.getInteger(TOTAL_IP_ALIVE_DISCHARGES, 
        new Object[] {centerId, yearStart, yearEnd}));
    map.put("totalpad",DatabaseHelper.getInteger(TOTAL_IP_SAMEDAY, 
        new Object[] {centerId, yearStart, yearEnd}));
    map.put("totalibd",DatabaseHelper.getInteger(TOTAL_SERVICEDAYS, 
        new Object[] {yearStart, yearStart, yearEnd, nextYearStart, centerId, yearStart, yearEnd, 
            yearStart, yearStart}));
    map.put("totalinpatienttransto",DatabaseHelper.getInteger(TOTAL_IP_TRFIN, 
        new Object[] {centerId, yearStart, yearEnd}));
    map.put("totalinpatienttransfrom",DatabaseHelper.getInteger(TOTAL_IP_TRFOUT, 
        new Object[] {centerId, yearStart, yearEnd}));
    map.put("totalpatientsremaining",DatabaseHelper.getInteger(TOTAL_IP_OPENINGCOUNT, 
        new Object[] {centerId, yearStart, yearStart}));
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
        responseXml = client.getTrainingPort().hospOptSummaryOfPatients(
            settings.getHfhudCode(), 
            data.get("totalinpatients"),
            data.get("totalnewborn"),
            data.get("totaldischarges"),
            data.get("totalpad"),
            data.get("totalibd"),
            data.get("totalinpatienttransto"),
            data.get("totalinpatienttransfrom"),
            data.get("totalpatientsremaining"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptSummaryOfPatients(
            settings.getHfhudCode(), 
            data.get("totalinpatients"),
            data.get("totalnewborn"),
            data.get("totaldischarges"),
            data.get("totalpad"),
            data.get("totalibd"),
            data.get("totalinpatienttransto"),
            data.get("totalinpatienttransfrom"),
            data.get("totalpatientsremaining"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
