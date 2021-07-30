package com.insta.hms.core.clinical.order.ordersets;

import com.bob.hms.common.DateUtil;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OrderSetsOrderItemRepository.
 *
 * @author chetan
 */
@Repository
public class OrderSetsOrderItemRepository extends OrderItemRepository {

  public OrderSetsOrderItemRepository() {
    super("packages");
  }

  private static final String ORDER_SETS_QUERY = " SELECT (CASE WHEN p.type = 'P' THEN 'Package' "
      + "  ELSE 'Order Sets' END) AS type, "
      + "  p.package_id::text AS id, package_name AS name, package_code AS code, "
      + "  CASE WHEN visit_applicability = 'i' then 'IP' when visit_applicability = 'o' THEN 'OP' "
      + "    ELSE 'BOTH' END AS  department, "
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  'N' as prior_auth_required, -1 as insurance_category_id, "
      + "  false as conduction_applicable,false as conducting_doc_mandatory, "
      + "  false AS results_entry_applicable,'N' AS tooth_num_required, "
      + "  false as multi_visit_package, cpa.center_id, dpa.dept_id, "
      + "  'N' AS mandate_additional_info, '' as additional_info_reqts, p.gender_applicability "
      + "  FROM packages p " + "  JOIN service_sub_groups s using(service_sub_group_id) "
      + "  JOIN center_package_applicability cpa ON (p.package_id = cpa.package_id and "
      + "  (cpa.center_id=? or cpa.center_id=-1)) "
      + "  JOIN dept_package_applicability dpa ON (p.package_id = dpa.package_id #DEPT_FILTER#) ";

  private static final String GENDER_APPLICABILITY_CONDITION = " AND ( p.gender_applicability = ? "
      + " or p.gender_applicability = '*' ) ";

  private static final String VALIDITY_DATES_CONDITION = " WHERE ( valid_from <= CURRENT_DATE OR "
      + " valid_from is NULL ) AND  ( valid_till >= CURRENT_DATE OR valid_till is NULL ) ";

  private static final String VALIDITY_DATES_CONDITION_FOR_APPOINTMENT = " WHERE ( valid_from <= ? "
      + " OR  valid_from is NULL ) AND ( valid_till >= ? OR valid_till is NULL ) ";
  
  private static final String FILTER_SCHEDULABLE = " AND ( scheduleable_by = ? ) ";

  /**
   * get item details.
   * @param entityIdList the entityIdList
   * @param deptId the deptId
   * @param centerId the centerId
   * @param genderApplicability the genderApplicability
   * @param date the date
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList, String deptId,
      Integer centerId, String genderApplicability, String date , String schedulable) {

    List<Object> otherParams = new ArrayList<Object>();
    StringBuilder query = null;
    if (date != null && !date.equals("")) {
      query = new StringBuilder(ORDER_SETS_QUERY + VALIDITY_DATES_CONDITION_FOR_APPOINTMENT);
    } else {
      query = new StringBuilder(ORDER_SETS_QUERY + VALIDITY_DATES_CONDITION);
    }
    otherParams.add(centerId);
    String finalQuery = query.toString();
    if (deptId.equalsIgnoreCase("any_dept")) {
      finalQuery = finalQuery.replace("#DEPT_FILTER#", "");
    } else {
      finalQuery = finalQuery.replace("#DEPT_FILTER#", " and (dpa.dept_id=? OR dpa.dept_id = '*')");
      otherParams.add(deptId);
    }
    if (genderApplicability != null) {
      otherParams.add(genderApplicability);
      finalQuery = finalQuery + GENDER_APPLICABILITY_CONDITION;
    }
    
    if (schedulable != null) {
      finalQuery = finalQuery +  FILTER_SCHEDULABLE;
      otherParams.add(schedulable);
    }
    if (date != null && !date.equals("")) {
      try {
        otherParams.add(DateUtil.parseDate(date));
        otherParams.add(DateUtil.parseDate(date));
      } catch (Exception exception) {
        ValidationErrorMap validationErrors = new ValidationErrorMap();
        ValidationException ex = new ValidationException(validationErrors);
        Map nestedException = new HashMap<>();
        nestedException.put("exception.scheduler.appointment.invalid.date", ex.getErrors());
        throw new NestableValidationException(nestedException);
      }
    }
    return super.getItemDetails(finalQuery, convertStringToInteger(entityIdList), "p.package_id",
        otherParams, false);
  }

  @Override
  public String getPackageRefQuery() {
    return null;
  }

  @Override
  public String getOperationRefQuery() {
    return null;
  }

  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return Collections.emptyList();
  }

}
