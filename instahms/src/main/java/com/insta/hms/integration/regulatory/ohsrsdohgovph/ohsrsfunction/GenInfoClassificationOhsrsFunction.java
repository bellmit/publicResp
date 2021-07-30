package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsFunctionMeta;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;
import com.insta.hms.mdm.centers.CenterService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OhsrsFunctionProcessor(supports = "genInfoClassification")
public class GenInfoClassificationOhsrsFunction extends GenericOhsrsFunction {

  @LazyAutowired
  CenterService centerService;
    
  @Override
  protected List<Map<String, Object>> getData(int year, int centerId) {
    OhsrsFunctionMeta meta = getMeta();
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> reportingMeta = centerService.getReportingMeta(centerId);
    for (String field : meta.getFieldsAsMap().keySet()) {
      String reportingMetaKey = "ohsrsdohgovph_" + meta.getKey() + "_" + field;
      if (reportingMeta.containsKey(reportingMetaKey)) {
        map.put(field, reportingMeta.get(reportingMetaKey));
      }
    }
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
        responseXml = client.getTrainingPort().genInfoClassification(
            settings.getHfhudCode(), 
            data.get("servicecapability"),
            data.get("general"),
            data.get("specialty"),
            data.get("specialtyspecify"),
            data.get("traumacapability"),
            data.get("natureofownership"),
            data.get("government"),
            data.get("national"),
            data.get("local"),
            data.get("private"),
            reportYear,
            data.get("ownershipothers"));
      } else {
        responseXml = client.getProductionPort().genInfoClassification(
            settings.getHfhudCode(), 
            data.get("servicecapability"),
            data.get("general"),
            data.get("specialty"),
            data.get("specialtyspecify"),
            data.get("traumacapability"),
            data.get("natureofownership"),
            data.get("government"),
            data.get("national"),
            data.get("local"),
            data.get("private"),
            reportYear,
            data.get("ownershipothers"),
            settings.getWebserviceKey());
      }
      status &= client.parseWebserviceResponse(responseXml); 
    }
    return status;
  }
}
