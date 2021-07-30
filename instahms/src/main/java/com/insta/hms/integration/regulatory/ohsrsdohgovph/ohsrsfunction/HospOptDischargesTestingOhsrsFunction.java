package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsFunctionMeta;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@OhsrsFunctionProcessor(supports = "hospOptDischargesTesting")
public class HospOptDischargesTestingOhsrsFunction extends GenericOhsrsFunction {

  private static final String TESTS_ISR = "select count(*) cn"
      + " FROM tests_prescribed tp"
      + " join incoming_sample_registration isr on isr.incoming_visit_id = tp.pat_id"
      + " JOIN diagnostics d on d.test_id = tp.test_id"
      + " WHERE conducted != 'X' AND isr.center_id = ? AND isr.date BETWEEN ?::date AND ?::date"
      + " AND d.ddept_id IN ";

  private static final String TESTS_VISIT = "select count(*) cn"
      + " FROM tests_prescribed tp"
      + " join patient_registration pr on pr.patient_id = tp.pat_id"
      + " JOIN diagnostics d on d.test_id = tp.test_id"
      + " WHERE tp.conducted != 'X' AND pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND d.ddept_id IN ";
  
  private static final String BLOOD_TRANSFUSE_SERVICES = "select count(*) cn"
      + " FROM services_prescribed sp"
      + " join patient_registration pr on pr.patient_id = sp.patient_id"
      + " WHERE sp.conducted != 'X' AND pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND sp.service_id IN ";

  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    String yearStart = String.valueOf(year) + "-01-01";
    OhsrsFunctionMeta meta = getMeta();  
    String yearEnd = String.valueOf(year) + "-12-31";
    List<Object> params = new ArrayList<>();
    params.add(centerId);
    params.add(yearStart);
    params.add(yearEnd);
    Map<String, String> testDept = metaRepo.getLookupMetaMap(meta.getKey()).get("testing");
    Map<String, List<String>> diagDeptMapping = getDiagDeptMapping();
    List<Map<String, Object>> list = new ArrayList<>();
    for (Entry<String, String> entry : testDept.entrySet()) {
      Map<String, Object> map = new HashMap<>();
      if (entry.getValue().equals("19")) {
        Map<String, Object> reportingMeta = centerService.getReportingMeta(centerId);
        List<String> services = getListMapping("blood_transfusion_services_ohsrsdohgovph");  
        if (services == null || services.size() == 0) {
          map.put("testing", entry.getKey());
          map.put("number", 0);
          list.add(map);
          continue;
        }
        List<Object> queryParams = new ArrayList<>(); 
        queryParams.addAll(params);
        for (String mapping : services) {
          queryParams.add(mapping);
        }
        map.put("testing", entry.getKey());
        String placeholders = DatabaseHelper.getInOperatorPlaceholder(services.size());
        map.put("number", DatabaseHelper.getInteger(
            BLOOD_TRANSFUSE_SERVICES + placeholders, queryParams.toArray()));
        list.add(map);
        continue;
      }
      String key = getDeptMappingKey(entry.getKey());
      List<String> mappings = diagDeptMapping.get(key);
      if (mappings == null || mappings.size() == 0) {
        map.put("testing", entry.getKey());
        map.put("number", 0);
        list.add(map);
        continue;
      }
      List<Object> queryParams = new ArrayList<>(); 
      queryParams.addAll(params);
      for (String mapping : mappings) {
        queryParams.add(mapping);
      }
      String placeholders = DatabaseHelper.getInOperatorPlaceholder(mappings.size());
      map.put("testing", entry.getKey());
      map.put("number", DatabaseHelper.getInteger(TESTS_ISR + placeholders, queryParams.toArray())
          + DatabaseHelper.getInteger(TESTS_VISIT + placeholders, queryParams.toArray()));
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
    for (Map<String, String> data : uploadData) {
      int testingId = Integer.parseInt(data.get("testing")); 
      if (testingId < 10) {
        data.put("testinggroup", "1");
      } else if (testingId > 18) {
        data.put("testinggroup", "3");
      } else {
        data.put("testinggroup", "2");        
      }
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().hospOptDischargesTesting(
            settings.getHfhudCode(), 
            data.get("testinggroup"),
            data.get("testing"),
            data.get("number"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesTesting(
            settings.getHfhudCode(), 
            data.get("testinggroup"),
            data.get("testing"),
            data.get("number"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }

}
