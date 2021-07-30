package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "hospOptDischargesOPV")
public class HospOptDischargesOpvOhsrsFunction extends GenericOhsrsFunction {

  private static final String TOTAL_OP = "SELECT count(*) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_details pd on pd.mr_no = pr.mr_no"
      + " WHERE pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND pr.visit_type='o'";
  
  private static final String TOTAL_OP_NOT_ER = TOTAL_OP   
      + " AND pr.admitted_dept NOT IN ";
  
  private static final String NEWPATIENTS = " AND pd.first_visit_reg_date = pr.reg_date";

  private static final String REVISITS = " AND pd.first_visit_reg_date != pr.reg_date";

  private static final String ADULTS = " AND"
      + " ((pr.reg_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25) >=19.0";

  private static final String PEDIA = " AND"
      + " ((pr.reg_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25) < 19.0";
  
  private static final String ADMDEPT_IN = " AND pr.admitted_dept IN ";

  private static final String ADULT_ADMDEPT_IN = ADULTS + ADMDEPT_IN;

  private static final String TOTAL_OP_POSTNATAL = "SELECT COUNT(*) cn FROM patient_registration pr"
      + " JOIN (SELECT pri.mr_no, a.admit_time FROM admission a "
      + "   JOIN patient_registration pri ON pri.patient_id = a.parent_id"
      + "   WHERE isbaby='Y'"
      + " ) prd ON prd.mr_no = pr.mr_no"
      + " AND EXTRACT(EPOCH FROM ((pr.reg_date+pr.reg_time)-prd.admit_time))/86400 BETWEEN 0 AND 42"
      + " WHERE pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND pr.visit_type='o'"
      + " AND pr.admitted_dept in ";

  private static final String TOTAL_OP_ANTENATAL = "SELECT COUNT(DISTINCT pr.patient_id) cn"
      + " FROM patient_registration pr"
      + " JOIN patient_section_details psd ON psd.patient_id = pr.patient_id"
      + "   AND psd.section_id = -14"
      + " JOIN antenatal_main anm ON anm.section_detail_id = psd.section_detail_id"
      + " WHERE pr.center_id = ?"
      + " AND pr.reg_date BETWEEN ?::date AND ?::date"
      + " AND pr.visit_type = 'o'";

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
    String baseQuery = TOTAL_OP;
    if (erMapping != null && erMapping.size() > 0) {
      baseQuery = TOTAL_OP_NOT_ER + DatabaseHelper.getInOperatorPlaceholder(erMapping.size());
      for (String mapping : erMapping) {
        params.add(mapping);
      }
    }
    Object[] paramsArr = params.toArray();
    Map<String, Object> map = new HashMap<>();
    map.put("newpatient",DatabaseHelper.getInteger(baseQuery + NEWPATIENTS, paramsArr));
    map.put("revisit",DatabaseHelper.getInteger(baseQuery + REVISITS, paramsArr));
    map.put("adult",DatabaseHelper.getInteger(baseQuery + ADULTS, paramsArr));
    map.put("pediatric",DatabaseHelper.getInteger(baseQuery + PEDIA, paramsArr));
    List<String> genMedicineMapping = deptMapping.get("medicine");
    if (genMedicineMapping != null && genMedicineMapping.size() > 0) {
      List<Object> adultGmParams = new ArrayList<>();
      adultGmParams.addAll(params);
      for (String mapping : genMedicineMapping) {
        adultGmParams.add(mapping);
      }
      map.put("adultgeneralmedicine", DatabaseHelper.getInteger(baseQuery + ADULT_ADMDEPT_IN 
          + DatabaseHelper.getInOperatorPlaceholder(genMedicineMapping.size()), 
          adultGmParams.toArray()));
    } else {
      map.put("adultgeneralmedicine", "");
    }
    List<String> opSpecialtyMappings = deptMapping.get("opspecialty");
    if (opSpecialtyMappings != null && opSpecialtyMappings.size() > 0) {
      List<Object> opSpecialtyParams = new ArrayList<>();
      opSpecialtyParams.add(centerId);
      opSpecialtyParams.add(yearStart);
      opSpecialtyParams.add(yearEnd);
      for (String mapping : opSpecialtyMappings) {
        opSpecialtyParams.add(mapping);
      }
      map.put("specialtynonsurgical", DatabaseHelper.getInteger(TOTAL_OP + ADMDEPT_IN 
          + DatabaseHelper.getInOperatorPlaceholder(opSpecialtyMappings.size()), 
          opSpecialtyParams.toArray()));
    } else {
      map.put("specialtynonsurgical", "");
    }
    List<String> opSurgicalMappings = deptMapping.get("opsurgery");
    if (opSurgicalMappings != null && opSurgicalMappings.size() > 0) {
      List<Object> opSurgicalParams = new ArrayList<>();
      opSurgicalParams.add(centerId);
      opSurgicalParams.add(yearStart);
      opSurgicalParams.add(yearEnd);
      for (String mapping : opSurgicalMappings) {
        opSurgicalParams.add(mapping);
      }
      map.put("surgical", DatabaseHelper.getInteger(TOTAL_OP + ADMDEPT_IN 
          + DatabaseHelper.getInOperatorPlaceholder(opSurgicalMappings.size()), 
          opSurgicalParams.toArray()));
    } else {
      map.put("surgical", "");
    }
    List<String> obgynMappings = new ArrayList<>();
    List<String> obMappings = deptMapping.get("obstetrics");
    List<String> gynMappings = deptMapping.get("gynecology");
    if (obMappings != null && obMappings.size() > 0) {
      obgynMappings.addAll(obMappings);
    }
    if (gynMappings != null && gynMappings.size() > 0) {
      obgynMappings.addAll(gynMappings);
    }
    if (obgynMappings.size() > 0) {
      List<Object> opSpecialtyParams = new ArrayList<>();
      opSpecialtyParams.add(centerId);
      opSpecialtyParams.add(yearStart);
      opSpecialtyParams.add(yearEnd);
      for (String mapping : obgynMappings) {
        opSpecialtyParams.add(mapping);
      }
      map.put("postnatal", DatabaseHelper.getInteger(TOTAL_OP_POSTNATAL 
          + DatabaseHelper.getInOperatorPlaceholder(obgynMappings.size()), 
          opSpecialtyParams.toArray()));
    } else {
      map.put("postnatal", "");
    }
    map.put("antenatal",DatabaseHelper.getInteger(TOTAL_OP_ANTENATAL, new Object[] {
        centerId, yearStart, yearEnd}));
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
        responseXml = client.getTrainingPort().hospOptDischargesOPV(
            settings.getHfhudCode(), 
            data.get("newpatient"),
            data.get("revisit"),
            data.get("adult"),
            data.get("pediatric"),
            data.get("adultgeneralmedicine"),
            data.get("specialtynonsurgical"),
            data.get("surgical"),
            data.get("antenatal"),
            data.get("postnatal"),
            reportYear);
      } else {
        responseXml = client.getProductionPort().hospOptDischargesOPV(
            settings.getHfhudCode(), 
            data.get("newpatient"),
            data.get("revisit"),
            data.get("adult"),
            data.get("pediatric"),
            data.get("adultgeneralmedicine"),
            data.get("specialtynonsurgical"),
            data.get("surgical"),
            data.get("antenatal"),
            data.get("postnatal"),
            reportYear,
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }
}
