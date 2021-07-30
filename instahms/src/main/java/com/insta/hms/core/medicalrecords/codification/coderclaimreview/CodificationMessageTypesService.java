package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CodificationMessageTypesService.
 */
@Service
public class CodificationMessageTypesService extends MasterService {

  @LazyAutowired
  CodificationMessageTypesRepository codificationMessageTypesRepository;

  /**
   * Instantiates a new codification message types service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public CodificationMessageTypesService(CodificationMessageTypesRepository repo,
      CodificationMessageTypesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("messageTypes", lookup(false));
    return map;
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ((CodificationMessageTypesRepository) this.getRepository()).getBean();
  }

  /**
   * Gets the message types list.
   *
   * @return the message types list
   */
  public List<BasicDynaBean> getMessageTypesList() {
    return codificationMessageTypesRepository.getMessageTypesList();
  }
  
  /**
   * Gets the message types list.
   *
   * @return the message types list
   */
  public List<BasicDynaBean> getMessageTypesList(Integer reviewTypeId) {
    return codificationMessageTypesRepository.getMessageTypesList(reviewTypeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#autocomplete(java.lang.String, java.util.Map)
   */
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("review_type", match, false, parameters);
  }

  /**
   * Gets the role for center.
   *
   * @return the role for center
   */
  public List<BasicDynaBean> getRoleForCenter() {
    return codificationMessageTypesRepository.getRoleForCenter();
  }

  /**
   * Gets review category details.
   *
   * @return the category details
   */
  public BasicDynaBean getReviewCategoryDetails(Integer reivewTypeId) {
    return codificationMessageTypesRepository.getReviewCategoryDetails(reivewTypeId);
  }
}
