package com.insta.hms.mdm.insurancecategory;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class InsuranceCategoryRepository. */
@Repository
public class InsuranceCategoryRepository extends MasterRepository<Integer> {

  /** Instantiates a new insurance category repository. */
  public InsuranceCategoryRepository() {
    super("insurance_category_master", "category_id", "category_name");
  }

  /** The Constant CENTERWISE_INSURANCE_CATEGORIES_ACTIVE_LISTQUERY. */
  private static final String CENTERWISE_INSURANCE_CATEGORIES_ACTIVE_LISTQUERY =
      "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name "
          + "from insurance_category_master icm "
          + "LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id) "
          + "WHERE icm.status='A' AND (iccm.center_id=? or iccm.center_id=0 or "
          + "coalesce(iccm.center_id,0)=0 ) and "
          + "coalesce(iccm.status,'A')='A'  order by category_name ";

  /** The Constant INSURANCE_CATEGORIES_ACTIVE_LISTQUERY. */
  private static final String INSURANCE_CATEGORIES_ACTIVE_LISTQUERY =
      "SELECT distinct icm.category_id,icm.insurance_co_id,icm.category_name "
          + "from insurance_category_master icm "
          + "LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id) "
          + "WHERE icm.status='A' and coalesce(iccm.status,'A')='A'  order by category_name ";

  /**
   * Gets the insu category active list.
   *
   * @param totalCenters the total centers
   * @param centerId the center id
   * @return the insu category active list
   */
  public List<BasicDynaBean> getInsuCategoryActiveList(int totalCenters, int centerId) {

    if (centerId != 0 && totalCenters > 1) {
      return DatabaseHelper.queryToDynaList(
          CENTERWISE_INSURANCE_CATEGORIES_ACTIVE_LISTQUERY, new Object[] {centerId});
    } else {
      return DatabaseHelper.queryToDynaList(INSURANCE_CATEGORIES_ACTIVE_LISTQUERY);
    }
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
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getInsurancePlanTypes(String visitId, int centerId)
      throws SQLException {

    return DatabaseHelper.queryToDynaList(
        GET_EDIT_VISIT__INC_CAT, new Object[] {centerId, visitId});
  }
}
