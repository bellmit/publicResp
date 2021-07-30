package com.insta.hms.mdm.accounting;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.accountpreferences.AccountingPreferenceService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Accounting Group Service.
 *
 * @author yashwant/deepak
 */
@Service
public class AccountingGroupService extends MasterService {
  @LazyAutowired
  private AccountingPreferenceService prefService;

  @LazyAutowired
  AccountingGroupRepository accountingGroupRepository;

  public AccountingGroupService(AccountingGroupRepository repository,
      AccountingGroupValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the accounting preference service record.
   *
   * @return Map
   */
  public Map<String, List<BasicDynaBean>> getRecord() {
    List<BasicDynaBean> beanList = new ArrayList<BasicDynaBean>();
    beanList.add(prefService.getAllPreferences());
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();
    referenceMap.put("acc_prefs", beanList);
    return referenceMap;
  }

  /**
   * Get the list of account groups.
   * 
   * @return List
   */
  public List<BasicDynaBean> listAll() {
    return accountingGroupRepository.listAll();
  }

  /**
   * Get the list of account groups for claim id seq.
   * 
   * @return List
   */
  public List<BasicDynaBean> listAccountGroupsForClaimIdSeq() {
    List<BasicDynaBean> accGroupList = new ArrayList<>();
    accGroupList = accountingGroupRepository.listAll("account_group_id");
    BasicDynaBean accGrpBean = accountingGroupRepository.getBean();

    accGrpBean.set("account_group_id", 0);
    accGrpBean.set("account_group_name", "All");
    accGrpBean.set("status", "A");
    accGroupList.add(accGrpBean);

    return accGroupList;
  }

  /**
   * Setting dropdown list for Center/Account Group in Remittance Advice Upload Screen.
   *
   * @param userCenterId UserCenterId
   * @return List of dyna beans
   */
  public List<BasicDynaBean> accountGroupCenterView(int userCenterId) {
    List<BasicDynaBean> accGrpAndCenterList = ((AccountingGroupRepository) getRepository())
        .getAccountGrpCenterViewList();
    Map accGrpAndCenterType = null;
    List<BasicDynaBean> accGrpAndCenterDropdn = new ArrayList<BasicDynaBean>();

    if (userCenterId != 0) {
      for (int i = 0; i < accGrpAndCenterList.size(); i++) {
        accGrpAndCenterType = new HashMap(((BasicDynaBean) accGrpAndCenterList.get(i)).getMap());
        String type = (String) accGrpAndCenterType.get("type");
        if ("C".equals(type)) {
          int accountId = Integer.parseInt(String.valueOf(accGrpAndCenterType.get("ac_id")));
          if (accountId == userCenterId) {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(i));
          }
        } else if (null != accGrpAndCenterType.get("store_center_id")) {
          int storeCenterId = (Integer) accGrpAndCenterType.get("store_center_id");
          if (userCenterId == storeCenterId) {
            accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(i));
          }
        } else {
          accGrpAndCenterDropdn.add((BasicDynaBean) accGrpAndCenterList.get(i));
        }
      }
    }

    if (userCenterId == 0) {
      accGrpAndCenterDropdn = accGrpAndCenterList;
    }
    return accGrpAndCenterDropdn;
  }
  
  public List<BasicDynaBean> getAccountingGroups(int userCenterId) {
    return ((AccountingGroupRepository) getRepository()).getAccountingGroups(userCenterId);
  }
}

