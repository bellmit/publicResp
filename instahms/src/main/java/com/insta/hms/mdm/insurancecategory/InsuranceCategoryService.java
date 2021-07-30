package com.insta.hms.mdm.insurancecategory;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class InsuranceCategoryService. */
@Service
public class InsuranceCategoryService extends MasterService {

  /**
   * Instantiates a new insurance category service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public InsuranceCategoryService(
      InsuranceCategoryRepository repo, InsuranceCategoryValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the insurance category active list.
   *
   * @param totalCenters the total centers
   * @return the insurance category active list
   */
  public List<BasicDynaBean> getInsuranceCategoryActiveList(int totalCenters) {
    int centerId = RequestContext.getCenterId();
    return ((InsuranceCategoryRepository) getRepository())
        .getInsuCategoryActiveList(totalCenters, centerId);
  }

  /** The Constant GET_EDIT_VISIT__INC_CAT. */
  private static final String GET_EDIT_VISIT__INC_CAT =
      "SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status "
          + " FROM insurance_category_master icm "
          + " LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id)"
          + " WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0) and iccm.status='A'"
          + " UNION "
          + " SELECT icm.category_id,icm.insurance_co_id,icm.category_name,icm.status  "
          + " FROM patient_registration pr "
          + " JOIN insurance_category_master icm ON (pr.category_id=icm.category_id) "
          + "  WHERE patient_id  = ?  ORDER BY category_name";

  /**
   * Gets the insurance plan types.
   *
   * @param visitId the visit id
   * @param centerId the center id
   * @return the insurance plan types
   */
  public List<BasicDynaBean> getInsurancePlanTypes(String visitId, int centerId) {

    return DatabaseHelper.queryToDynaList(
        GET_EDIT_VISIT__INC_CAT, new Object[] {centerId, visitId});
  }

  public BasicDynaBean getBean() {
    return getRepository().getBean();
  }
}
