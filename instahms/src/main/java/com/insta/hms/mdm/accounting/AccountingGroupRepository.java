package com.insta.hms.mdm.accounting;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The accounting group repository.
 *
 * @author yashwant/Deepak.
 */
@Repository
class AccountingGroupRepository extends MasterRepository<Integer> {

  public AccountingGroupRepository() {
    super("account_group_master", "account_group_id", "account_group_name");
  }

  private static final String GET_ACCOUNT_GROUP_AND_CENTER_VIEW = " SELECT * from "
      + " accountgrp_and_center_view";

  public List<BasicDynaBean> getAccountGrpCenterViewList() {
    return DatabaseHelper
        .queryToDynaList(GET_ACCOUNT_GROUP_AND_CENTER_VIEW);
  }

  private static final String GET_DISTINCT_ACCOUNT_GROUPS =
      // First query is all centers which are account groups on their own,
      // filtered by user-logged-in center
      // This will come up only when all_centers_same_acc_company = 'N' in
      // hosp_accounting_prefs
      // (view definition, takes care of this)
      // Second is all account groups for pharmacies attached to the user logged
      // in center
      // Third is the hospital account group applicable to all centers when all of
      // them belong to the same
      // business enity. This will come up only when all_centers_same_acc_company
      // is 'Y' in hosp_accounting_prefs
      // (view definition, takes care of this)

      " SELECT DISTINCT ac_id, id, ac_name, accounting_company_name, ser_reg_no, type "
      + " FROM accountgrp_and_center_view WHERE type='C' ##acid## "
      + " UNION ALL "
      + " SELECT DISTINCT ac_id, id, ac_name, accounting_company_name, ser_reg_no, type "
      + " FROM accountgrp_and_center_view WHERE type='A' and ac_id != 1 ##storecenter##"
      + " UNION ALL "
      + " SELECT DISTINCT ac_id, id, ac_name, accounting_company_name, ser_reg_no, type "
      + " FROM accountgrp_and_center_view WHERE type = 'A' and ac_id = 1";

  public List<BasicDynaBean> getAccountingGroups(Integer userCenterId) {
    String qry = null;
    if (null != userCenterId && userCenterId != 0) {
      qry = GET_DISTINCT_ACCOUNT_GROUPS.replace("##acid##",
          "and ac_id=? ").replace("##storecenter##",
          "and store_center_id = ? ");
      return DatabaseHelper.queryToDynaList(qry, new Object[] {
          userCenterId, userCenterId });
    } else {
      qry = GET_DISTINCT_ACCOUNT_GROUPS.replace("##acid##", "").replace(
          "##storecenter##", "");
      return DatabaseHelper.queryToDynaList(qry);
    }
  }

}
