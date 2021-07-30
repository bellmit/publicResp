package com.insta.hms.mdm.opvisittyperules;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * The Class OpVisitTypeRulesRepository.
 */
@Repository("opVisitTypeRulesRepository")
public class OpVisitTypeRulesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new discount plan repository.
   */
  public OpVisitTypeRulesRepository() {
    super("op_visit_type_rule_master", "rule_id", "rule_name");
  }

  /** The Constant DISCOUNT_FILEDS. */
  private static final String FILEDS = "SELECT * ";

  /** The discount from. */
  private static final String FROM = " FROM "
      + " (SELECT oprm.* FROM op_visit_type_rule_master oprm " + "  ) as foo ";

  /** The Constant DISCOUNT_FROM1. */
  private static final String MASTER_FROM = " FROM op_visit_type_rule_master ";

  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(rule_id) ";

  /**
   * Gets the op visit type rules query.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the op visit type rules
   */
  @SuppressWarnings("rawtypes")
  public SearchQueryAssembler getOpVisitTypeRulesQa(Map params,
      Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qa = new SearchQueryAssembler(FILEDS, COUNT, MASTER_FROM, listingParams);
    qa.addFilterFromParamMap(params);
    return qa;
  }

  private static final String GET_RULE = "SELECT ovtrm.*, "
      + " (SELECT COALESCE(MAX(valid_to), 0) FROM op_visit_type_rule_details"
      + " WHERE prev_main_visit_type = 'O' AND rule_id = ?) AS max_op_validity,"
      + " (SELECT COALESCE(MAX(valid_to), 0) FROM op_visit_type_rule_details"
      + " WHERE prev_main_visit_type = 'I' AND rule_id = ?) AS max_ip_validity,"
      + " (select COUNT(rule_id) from  op_visit_type_rule_details where rule_id = ?"
      + " and prev_main_visit_type = 'O') AS op_rule_details_count, "
      + "(select COUNT(rule_id) from  op_visit_type_rule_details where rule_id = ?"
      + " and prev_main_visit_type = 'I') AS ip_rule_details_count " 
      + " FROM op_visit_type_rule_master ovtrm LEFT JOIN op_visit_type_rule_details ovtrd "
      + " ON (ovtrm.rule_id = ovtrd.rule_id) WHERE ovtrm.rule_id = ?"
      + " GROUP BY ovtrm.rule_id ";

  public BasicDynaBean getRule(Integer ruleId) {
    return DatabaseHelper.queryToDynaBean(GET_RULE,
        new Object[] { ruleId, ruleId, ruleId, ruleId, ruleId });
  }
  
  private static final String GET_VISIT_TYPE = 
      "SELECT op_visit_type FROM op_visit_type_rule_details "
      + "where rule_id = ? AND ? >= valid_from AND ? <= valid_to AND prev_main_visit_type = ? ";

  public BasicDynaBean getVisitType(String mainVisitType, int daysFromMainVisit, int ruleId) {
    return DatabaseHelper.queryToDynaBean(GET_VISIT_TYPE, 
        new Object[] {ruleId, daysFromMainVisit, daysFromMainVisit, mainVisitType});
  }
}
