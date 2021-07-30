package com.insta.hms.mdm.insuranceplantypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class InsurancePlanTypeRespository.
 */
@Repository
public class InsurancePlanTypeRespository extends MasterRepository<Integer> {

  /**
   * Instantiates a new insurance plan type respository.
   */
  public InsurancePlanTypeRespository() {
    super("insurance_category_master", "category_id");
  }

  /** The Constant GET_INSURANCE_PLAN_TYPES_CENTER_ASSOCIATION. */
  // TODO generalize
  private static final String GET_INSURANCE_PLAN_TYPES_CENTER_ASSOCIATION = " "
      + " SELECT distinct icm.category_id,icm.insurance_co_id, "
      + " icm.category_name,icm.status from insurance_category_master icm "
      + " LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id) "
      + " WHERE icm.status='A' AND icm.insurance_co_id=? "
      + " AND (iccm.center_id=? or iccm.center_id=0) and iccm.status='A' order by category_name";

  /** The Constant GET_INSURANCE_PLAN_TYPES. */
  private static final String GET_INSURANCE_PLAN_TYPES = "SELECT distinct icm.category_id,"
      + " icm.insurance_co_id, icm.category_name,icm.status "
      + " from insurance_category_master icm "
      + " LEFT JOIN insurance_category_center_master iccm ON(icm.category_id=iccm.category_id) "
      + " WHERE icm.status='A' AND iccm.status='A' AND icm.insurance_co_id=? ";

  /**
   * Gets the plan types.
   *
   * @param insCompId
   *          the ins comp id
   * @param centerId
   *          the center id
   * @return the plan types
   */
  public List<BasicDynaBean> getPlanTypes(String insCompId, Integer centerId) {
    if (centerId != null) {
      return DatabaseHelper.queryToDynaList(GET_INSURANCE_PLAN_TYPES_CENTER_ASSOCIATION,
          new Object[] { insCompId, centerId });
    } else {
      return DatabaseHelper.queryToDynaList(GET_INSURANCE_PLAN_TYPES, new Object[] { insCompId });
    }
  }

  /** The Constant SELECT_PLAN_TYPES_FOR_SPONSOR. */
  private static final String SELECT_PLAN_TYPES_FOR_SPONSOR = "SELECT distinct icatm.category_id,"
      + " icatm.insurance_co_id, icatm.category_name,icatm.status "
      + " from insurance_category_master icatm "
      + " LEFT JOIN insurance_category_center_master iccm ON(icatm.category_id=iccm.category_id) ";

  /** The Constant JOIN_MAPPED_INSURANCE_COMPANIES. */
  private static final String JOIN_MAPPED_INSURANCE_COMPANIES = "select * from "
      + " ( select distinct icm.insurance_co_id from insurance_company_master icm "
      + " LEFT JOIN insurance_company_tpa_master ictm "
      + " ON ictm.insurance_co_id = icm.insurance_co_id where icm.status = 'A' "
      + " AND case when ( (select count(tpa_id) as count from insurance_company_tpa_master ictm_t "
      + " JOIN insurance_company_master icm_t "
      + " ON (ictm_t.insurance_co_id = icm_t.insurance_co_id) "
      + " where ictm_t.tpa_id= ? and icm_t.status = 'A' limit 1) = 0  ) "
      + " then true else ictm.tpa_id= ? end ) as foo ";

  /** The join patient category mapped. */
  private static String JOIN_PATIENT_CATEGORY_MAPPED = " JOIN "
      + "(select regexp_split_to_table(op_allowed_insurance_co_ids, E',') "
      + " as insurance_co_id_split " + " from patient_category_master where category_id= ? ) "
      + " as foo1 ON (foo1.insurance_co_id_split = foo.insurance_co_id "
      + " OR foo1.insurance_co_id_split = '*') ";

  /** The Constant WHERE_PLAN_TYPES. */
  private static final String WHERE_PLAN_TYPES = " WHERE icatm.status='A' AND iccm.status='A' ";

  /** The Constant ORDER_BY_PLAN_TYPES. */
  private static final String ORDER_BY_PLAN_TYPES = " order by category_name";

  /**
   * Gets the plan types for sponsor.
   *
   * @param tpaId
   *          the tpa id
   * @param categoryId
   *          the category id
   * @param centerId
   *          the center id
   * @return the plan types for sponsor
   */
  public List<BasicDynaBean> getPlanTypesForSponsor(String tpaId, Integer categoryId,
      Integer centerId) {
    List<Object> values = new ArrayList<Object>();
    String tpaSelect = null;
    values.add(tpaId);
    values.add(tpaId);
    if (categoryId == null) {
      tpaSelect = JOIN_MAPPED_INSURANCE_COMPANIES;
    } else {
      values.add(categoryId);
      tpaSelect = JOIN_MAPPED_INSURANCE_COMPANIES + JOIN_PATIENT_CATEGORY_MAPPED;
    }
    StringBuilder query = new StringBuilder(SELECT_PLAN_TYPES_FOR_SPONSOR);
    query.append(" JOIN ( " + tpaSelect
        + " ) as foo3 ON (foo3.insurance_co_id = icatm.insurance_co_id) ");
    query.append(WHERE_PLAN_TYPES);
    if (centerId != null) {
      query.append(" AND ( iccm.center_id = ? or iccm.center_id = 0 ) ");
      values.add(centerId);
    }
    query.append(ORDER_BY_PLAN_TYPES);
    return DatabaseHelper.queryToDynaList(query.toString(), values.toArray());
  }

}
