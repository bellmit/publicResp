package com.insta.hms.extension.payments;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentRulesRepository extends GenericRepository {

  public PaymentRulesRepository() {
    super("payment_rules");
  }

  private static final String GET_RULES = "select * from payment_rules where charge_head=? "
      + " AND (activity_id is null OR activity_id = '') OR activity_id=? ORDER BY precedance ASC";

  public List<BasicDynaBean> getRules(String chargeHead, String activityId) {
    return DatabaseHelper.queryToDynaList(GET_RULES, new Object[] { chargeHead, activityId });
  }

}
