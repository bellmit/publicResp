package com.insta.hms.mdm.patientcategories;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class PatientCategoryService.
 */
@Service
public class PatientCategoryService extends MasterService {

  /**
   * Instantiates a new patient category service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public PatientCategoryService(PatientCategoryRepository repository,
      PatientCategoryValidator validator) {
    super(repository, validator);
  }

  /**
   * List by center.
   *
   * @param centerIds
   *          the center ids
   * @param includeDefault
   *          the include default
   * @return the list
   */
  public List<BasicDynaBean> listByCenter(List<Integer> centerIds, boolean includeDefault) {
    if (includeDefault) {
      centerIds.add(0);
    }
    return ((PatientCategoryRepository) this.getRepository()).listByCenter(centerIds);
  }

  /**
   * Gets the pat category default rate plan.
   *
   * @param categoryId
   *          the category id
   * @param centerId
   *          the center id
   * @return the pat category default rate plan
   */
  public List<BasicDynaBean> getPatCategoryDefaultRatePlan(String categoryId, Integer centerId) {
    return ((PatientCategoryRepository) this.getRepository()).getPatCategoryDefaultRatePlan(
        categoryId, centerId);
  }
  
  public List<BasicDynaBean> getDefaultRatePlan(int categoryId, String visitType) {
    return ((PatientCategoryRepository) this.getRepository()).getDefaultRatePlan(categoryId,
        visitType);
  }

  /**
   * Gets the all categories inc super center.
   *
   * @param centerId
   *          the center id
   * @return the all categories inc super center
   */
  public List<BasicDynaBean> getAllCategoriesIncSuperCenter(int centerId) {
    return ((PatientCategoryRepository) this.getRepository())
        .getAllCategoriesIncSuperCenter(centerId);
  }

  /**
   * Get allowed insurance companies.
   * 
   * @param patientCategoryId
   *          the patient category id
   * @param visitType
   *          the visit type
   * @return allowed insurance companies for a patient category
   */
  public List<BasicDynaBean> getAllowedInsCompanies(int patientCategoryId, String visitType) {
    // TODO Auto-generated method stub
    return ((PatientCategoryRepository) this.getRepository()).getAllowedInsCompanies(
        patientCategoryId, visitType);
  }

  /**
   * Get allowed rate plans.
   * 
   * @param patientCategoryId
   *          the patient category id
   * @param visitType
   *          the visit type
   * @return allowed rate plans for a patient category
   */
  public List<BasicDynaBean> getAllowedRatePlans(int patientCategoryId, String visitType) {
    // TODO Auto-generated method stub
    return ((PatientCategoryRepository) this.getRepository()).getAllowedRatePlans(
        patientCategoryId, visitType);
  }

}
