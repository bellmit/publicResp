package com.insta.hms.mdm.chargeheads;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChargeHeadsRepository extends MasterRepository<String> {

  private static final String CHARGE_HEADS_SEARCH_TABLES = " FROM( SELECT ch.chargehead_id, "
      + " ch.chargehead_name, ch.payment_eligible, cg.chargegroup_name, ah.account_head_name,"
      + "  ch.display_order FROM chargehead_constants ch "
      + " JOIN chargegroup_constants cg ON ch.chargegroup_id=cg.chargegroup_id "
      + " JOIN bill_account_heads ah ON ch.account_head_id=ah.account_head_id) AS foo ";

  public ChargeHeadsRepository() {
    super("chargehead_constants", "chargehead_id", null,
        new String[] { "chargehead_id", "chargehead_name" });
  }

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(CHARGE_HEADS_SEARCH_TABLES);
  }

  @Override
  public boolean supportsAutoId() {
    return false;
  }

  private static final String CHARGE_HEAD_LIST = "SELECT chargehead_id, chargehead_name, "
      + "insurance_category_id  FROM chargehead_constants " + "ORDER BY chargehead_name";

  public List<BasicDynaBean> getDiscountApplicableChargeHead() {
    return DatabaseHelper.queryToDynaList(CHARGE_HEAD_LIST);
  }

  private static final String ELIGIBLE_FOR_PAYMENT = "SELECT payment_eligible "
      + "FROM chargehead_constants WHERE chargehead_id = ?";

  public String isEligibleForPayment(Object[] object) {
    return DatabaseHelper.getString(ELIGIBLE_FOR_PAYMENT, object);
  }

  private static final String GET_OT_DOC_CHARGE_HEADS = " SELECT chargehead_id, "
      + " chargehead_name, display_order " + " FROM chargehead_constants "
      + " WHERE chargehead_id IN ('ANAOPE','SUOPE','AANOPE','ASUOPE','COSOPE')"
      + " ORDER BY display_order ";

  public List<BasicDynaBean> getOtDoctorChargeHeads() {
    return DatabaseHelper.queryToDynaList(GET_OT_DOC_CHARGE_HEADS);
  }

  public static final String CHARGE_HEAD_QUERY = " SELECT cc.*, service_group_id "
      + " FROM chargehead_constants cc "
      + " LEFT JOIN service_sub_groups USING(service_sub_group_id) ";

  public List<BasicDynaBean> getChargeHeadBean(String chargeheadId) {
    StringBuilder query = new StringBuilder(CHARGE_HEAD_QUERY + " WHERE chargehead_id = ? ");
    return DatabaseHelper.queryToDynaList(query.toString(), chargeheadId );
  }
  
  /**
   * Gets the list charge head constants given a list of chargeHeadIds.
   *
   * @param chargeheadId the chargehead id
   * @return the charge head bean
   */
  public static List<BasicDynaBean> getChargeHeadBean(List<String> chargeheadId) {
    StringBuilder query = new StringBuilder();
    query.append(CHARGE_HEAD_QUERY);
    DataBaseUtil.addWhereFieldInList(query, "chargehead_id", chargeheadId, false);
    Object[] vals = new Object[chargeheadId.size()];
    for (int j = 0; j < chargeheadId.size(); j++) {
      vals[j] = chargeheadId.get(j);
    }
    return DatabaseHelper.queryToDynaList(query.toString(), vals);
  }
}
