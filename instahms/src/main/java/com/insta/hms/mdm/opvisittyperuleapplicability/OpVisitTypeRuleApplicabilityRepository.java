package com.insta.hms.mdm.opvisittyperuleapplicability;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OpVisitTypeRuleApplicabilityRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new op visit type rule applicability repository.
   */
  public OpVisitTypeRuleApplicabilityRepository() {
    super("op_visit_type_rule_applicability", "rule_id");
  }

  /** The Constant GET_APPLICABLE_RULES. */
  private static final String GET_APPLICABLE_RULES = "SELECT * "
      + " FROM op_visit_type_rule_applicability ovtra "
      + " JOIN op_visit_type_rule_master ovtrm ON(ovtra.rule_id = ovtrm.rule_id)"
      + "WHERE center_id IN (?,-1) AND tpa_id IN (?, '*') "
      + "AND dept_id IN (?, '*') AND doctor_id IN (?, '*', '#') ";

  /**
   * Gets the applicable rules.
   *
   * @param centerId the center id
   * @param tpaId the tpa id
   * @param deptId the dept id
   * @param doctorId the doctor id
   * @return the applicable rules
   */
  public List<BasicDynaBean> getApplicableRules(Integer centerId, String tpaId, String deptId,
      String doctorId) {
    return DatabaseHelper.queryToDynaList(GET_APPLICABLE_RULES,
        new Object[] { centerId, tpaId, deptId, doctorId });
  }

  /** The Constant GET_APPLICABLES_LIST. */
  private static final String GET_APPLICABLES_LIST = "SELECT a.rule_applicability_id, "
      + " COALESCE(c.center_name,'ALL') AS center_name, "
      + " CASE WHEN a.tpa_id = '$' THEN 'CASH' ELSE COALESCE(t.tpa_name,'ALL') END AS tpa_name, "
      + " COALESCE(d.dept_name,'ALL') AS dept_name,"
      + " CASE WHEN a.doctor_id = '#' THEN 'Not Applicable' "
      + "  ELSE COALESCE(doc.doctor_name,'ALL') END AS doctor_name, "
      + " r.rule_name AS rule_name, a.rule_id, a.center_id, a.tpa_id, a.dept_id, a.doctor_id "
      + " FROM op_visit_type_rule_applicability a "
      + " JOIN op_visit_type_rule_master r ON (r.rule_id = a.rule_id)"
      + " LEFT JOIN hospital_center_master c ON a.center_id=c.center_id "
      + " LEFT JOIN tpa_master t ON a.tpa_id = t.tpa_id "
      + " LEFT JOIN department d ON a.dept_id = d.dept_id "
      + " LEFT JOIN doctors doc ON a.doctor_id = doc.doctor_id "
      + " ORDER BY center_name ;";

  /**
   * Gets the applicable rules list.
   *
   * @return the applicable rules list
   */
  public List<BasicDynaBean> getApplicableRulesList() {
    return DatabaseHelper.queryToDynaList(GET_APPLICABLES_LIST);
  }
  

  /** The Constant GET_RULE_APPLICABILITIES. */
  private static final String GET_RULE_APPLICABILITIES = "SELECT a.rule_applicability_id"
      + " FROM op_visit_type_rule_applicability a "
      + " WHERE a.rule_id = :ruleId ;";

  /**
   * Gets the rule applicabilities.
   *
   * @param ruleId the rule id
   * @return the rule applicabilities
   */
  public List<BasicDynaBean> getRuleApplicabilities(Integer ruleId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("ruleId", ruleId);
    return DatabaseHelper.queryToDynaList(GET_RULE_APPLICABILITIES, parameters);
  }
}
