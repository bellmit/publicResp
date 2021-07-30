package com.insta.hms.insurance;

import com.insta.hms.auditlog.AuditLogDesc;
import com.insta.hms.auditlog.AuditLogDescProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class InsuranceMainPlanDescProvider.
 */
public class InsuranceMainPlanDescProvider implements AuditLogDescProvider {

  /**
   * Gets the audit log desc.
   *
   * @param tableName the table name
   * @return the insurance category names and ids map
   */
  public AuditLogDesc getAuditLogDesc(String tableName) {
    AuditLogDesc desc = new AuditLogDesc(tableName);

    desc.addField("plan_name", "Plan Name", false);
    desc.addField("category_id", "Network/Plan Type", false);
    desc.addField("overall_treatment_limit", "Over All Treatment Limit", false);
    desc.addField("plan_notes", "Plan Notes", false);
    desc.addField("plan_exclusions", "Plan Exclusions", false);
    desc.addField("default_rate_plan", "Default Rate Plan", false);
    desc.addField("insurance_co_id", "Insurance Company Name");
    desc.addField("ip_applicable", "IP Applicable", false);
    desc.addField("op_applicable", "OP Applicable", false);
    desc.addField("is_copay_pc_on_post_discnt_amt", " Copay pc on Post Discount amt", false);
    desc.addField("base_rate", "Base Rate", false);
    desc.addField("status", "Status", false);
    desc.addField("gap_amount", "Gap Amount", false);
    desc.addField("marginal_percent", "Marginal percent", false);
    desc.addField("perdiem_copay_per", "Perdiem Copay Per", false);
    desc.addField("perdiem_copay_amount", "Perdiem Copay Amount", false);
    desc.addField("require_pbm_authorization", "Require pbm Authorization", false);
    desc.addField("op_visit_copay_limit", "Op Visit Copay Limit", false);
    desc.addField("ip_visit_copay_limit", "Ip Visit Copay Limit", false);

    Map<String, String> ipApplicableMap = new LinkedHashMap<String, String>();
    ipApplicableMap.put("Y", "Yes");
    ipApplicableMap.put("N", "No");

    desc.addFieldValue("ip_applicable", ipApplicableMap);

    Map<String, String> opApplicableMap = new LinkedHashMap<String, String>();
    opApplicableMap.put("Y", "Yes");
    opApplicableMap.put("N", "No");

    desc.addFieldValue("op_applicable", opApplicableMap);

    Map<String, String> iscopaypconpostdisctamtMap = new LinkedHashMap<String, String>();
    iscopaypconpostdisctamtMap.put("Y", "Yes");
    iscopaypconpostdisctamtMap.put("N", "No");

    desc.addFieldValue("is_copay_pc_on_post_discnt_amt", iscopaypconpostdisctamtMap);

    Map<String, String> requirepbmauthorizationMap = new LinkedHashMap<String, String>();
    requirepbmauthorizationMap.put("Y", "Yes");
    requirepbmauthorizationMap.put("N", "No");

    desc.addFieldValue("require_pbm_authorization", requirepbmauthorizationMap);

    desc.addFieldValue("insurance_co_id", "insurance_company_master", "insurance_co_id",
        "insurance_co_name");
    desc.addFieldValue("default_rate_plan", "organization_details", "org_id", "org_name");
    desc.addFieldValue("category_id", "insurance_category_master", "category_id", "category_name");

    return desc;
  }
}
