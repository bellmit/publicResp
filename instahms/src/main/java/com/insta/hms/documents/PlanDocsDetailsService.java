package com.insta.hms.documents;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PlanDocsDetailsService.
 */
@Service
public class PlanDocsDetailsService {

  /** The policy doc img repo. */
  @LazyAutowired
  private PlanDocsDetailsRepository policyDocImgRepo;

  /**
   * Insert.
   *
   * @param bean the bean
   * @return the integer
   */
  public Integer insert(BasicDynaBean bean) {
    return policyDocImgRepo.insert(bean);
  }

  /**
   * Find by PK.
   *
   * @param key the key
   * @return the basic dyna bean
   */
  public BasicDynaBean findByPK(int key) {
    return policyDocImgRepo.findByKey("doc_id", key);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return policyDocImgRepo.getBean();
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
    return policyDocImgRepo.listAll(columns, filterBy, filterValue);
  }

  /**
   * Checks if is plan document exist.
   *
   * @param patientPolicyId the patient policy id
   * @return true, if is plan document exist
   */
  public boolean isPlanDocumentExist(Integer patientPolicyId) {
    // TODO Auto-generated method stub

    List<BasicDynaBean> planDocsList = policyDocImgRepo.getPlanDocumentList(patientPolicyId);
    boolean planDocsExists = null != planDocsList && planDocsList.size() > 0;

    return planDocsExists;
  }

  /**
   * Find by key.
   *
   * @param key the key
   * @param value the value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String key, Object value) {
    // TODO Auto-generated method stub
    return policyDocImgRepo.findByKey(key, value);
  }

}
