package com.insta.hms.mdm.sequences;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.sequences.hospitalidpatterns.HospitalIdPatternsService;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SequenceMasterService.
 */
public class SequenceMasterService extends MasterService {

  /** The center service. */
  @LazyAutowired public CenterService centerService;
  
  /** The hospital id patterns service. */
  @LazyAutowired public HospitalIdPatternsService hospitalIdPatternsService;
  
  @LazyAutowired public AccountingGroupService accountingGroupService;

  /**
   * Instantiates a new sequence master service.
   *
   * @param seqRepo the seqRepo
   * @param seqValidator the seqValidator
   */
  public SequenceMasterService(SequenceMasterRepository<?> seqRepo, 
                            SequenceMasterValidator seqValidator) {
    super(seqRepo, seqValidator);
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> centers = centerService.listAll(true);
    map.put("centers", centers);
    List<BasicDynaBean> accountGroups = accountingGroupService.listAccountGroupsForClaimIdSeq();
    map.put("accountGroups", accountGroups);
    return map;
  }

  /**
   * Gets the hospital id pattern list.
   *
   * @param params the params
   * @return the hospital id pattern list
   */
  public Map<String, List<BasicDynaBean>> getHospitalIdPatternList(Map params) {
    BasicDynaBean bean = toBean(params);
    return hospitalIdPatternsService.getHospitalIdPatternList(bean, getTransactionType());
  }

  /**
   * Gets the transaction type.
   *
   * @return the transaction type
   */
  protected String getTransactionType() {
    return ((SequenceMasterRepository) getRepository()).getTransactionType();
  }

  /**
   * Gets the confict rules.
   *
   * @param bean the bean
   * @return the confict rules
   */
  public List<BasicDynaBean> getConfictRules(BasicDynaBean bean) {
    return ((SequenceMasterRepository) getRepository()).getConfictRules(bean);
  }
}
