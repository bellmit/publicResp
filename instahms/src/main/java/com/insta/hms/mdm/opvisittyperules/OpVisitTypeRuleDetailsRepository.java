package com.insta.hms.mdm.opvisittyperules;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The Class OpVisitTypeRuleDetailsRepository.
 */
@Repository
public class OpVisitTypeRuleDetailsRepository extends MasterRepository<Integer> {
  /**
   * Instantiates a new details repository.
   */
  public OpVisitTypeRuleDetailsRepository() {
    super(
        new String[] { "op_visit_type", "valid_from", "valid_to", "prev_main_visit_type",
            "rule_id" },
        new String[] { "op_visit_type", "valid_from", "valid_to" }, "op_visit_type_rule_details",
        "rule_details_id");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#allowsDuplicates()
   */
  @Override
  protected boolean allowsDuplicates() {
    return false;
  }

  private static final String OP_VISIT_TYPE_DETAILS = "SELECT rule_details_id, "
      + " rule_id, op_visit_type, prev_main_visit_type, "
      + " valid_from, valid_to FROM op_visit_type_rule_details " + " WHERE rule_id = :ruleId ";

  /**
   * Gets the visit type rules.
   *
   * @param ruleId the rule id
   * @return the visit type rules
   */
  public List<BasicDynaBean> getVisitTypeRules(Integer ruleId) {  
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("ruleId", ruleId);
    return DatabaseHelper.queryToDynaList(OP_VISIT_TYPE_DETAILS, parameters);
  }
}
