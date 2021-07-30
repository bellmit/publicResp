package com.insta.hms.mdm.dietary;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class DietaryService.
 */
@Service
public class DietaryService extends MasterService {

  /**
   * Instantiates a new dietary service.
   *
   * @param dietaryRepository the dietary repository
   * @param dietaryValidator the dietary validator
   */
  public DietaryService(DietaryRepository dietaryRepository, DietaryValidator dietaryValidator) {
    super(dietaryRepository, dietaryValidator);
  }

  /**
   * Gets the dietary item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the dietary item sub group tax details
   */
  public List<BasicDynaBean> getDietaryItemSubGroupTaxDetails(String actDescriptionId) {
    return ((DietaryRepository) getRepository()).getDietaryItemSubGroupTaxDetails(actDescriptionId);
  }
  
  /**
   * Find by key.
   *
   * @param keycolumn the keycolumn
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keycolumn, Object identifier) {
    return ((DietaryRepository) getRepository()).findByKey(keycolumn, identifier);
  }

  /**
   * Gets the charge for meal.
   *
   * @param orgId the org id
   * @param dietId the diet id
   * @param bedType the bed type
   * @return the charge for meal
   */
  public BasicDynaBean getChargeForMeal(String orgId, int dietId, String bedType) {
    return ((DietaryRepository) getRepository()).getChargeForMeal(orgId, dietId, bedType);
  }
  
}
