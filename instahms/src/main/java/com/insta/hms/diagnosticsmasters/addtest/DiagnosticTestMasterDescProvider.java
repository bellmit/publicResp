package com.insta.hms.diagnosticsmasters.addtest;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public class DiagnosticTestMasterDescProvider implements AuditLogDescProvider {

  @Override
  public AuditLogDesc getAuditLogDesc(String tableName) {

    Map<String, String> statusMap = new LinkedHashMap<>();
    statusMap.put("A", "Active");
    statusMap.put("I", "InActive");
    Map<String, String> sampleMap = new LinkedHashMap<>();
    sampleMap.put("y", "Yes");
    sampleMap.put("n", "No");
    Map<String, String> resultValidityPeriodUnits = new LinkedHashMap<>();
    resultValidityPeriodUnits.put("D", "Days");
    resultValidityPeriodUnits.put("M", "Months");
    resultValidityPeriodUnits.put("Y", "Years");
    AuditLogDesc desc = new AuditLogDesc(tableName);
    desc.addField("test_name", "Test Name");
    desc.addField("ddept_id", "Department");
    desc.addField("sample_needed", "Sample Needed");
    desc.addField("house_status", "House Status");
    desc.addField("type_of_specimen", "Type Of Specimen");
    desc.addField("diag_code", "Diag Code");
    desc.addField("conduction_format", "Conduction Format");
    desc.addField("status", "Status");
    desc.addField("conduction_applicable", "Conduction Applicable");
    desc.addField("result_validity_period", "Test Result Validity Period");
    desc.addField("result_validity_period_units", "Test Result Validity Period Units");

    desc.addFieldValue("status", statusMap);
    desc.addFieldValue("sample_needed", sampleMap);
    desc.addFieldValue("result_validity_period_units", resultValidityPeriodUnits);

    desc.addFieldValue("ddept_id", "diagnostics_departments", "ddept_id", "ddept_name");

    return desc;
  }
}
