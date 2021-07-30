package com.insta.hms.quickestimate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class QuickEstimateDAO.
 *
 * @author lakshmi.p
 */
public class QuickEstimateDAO {

  /** The Constant CHARGE_QUERY. */
  private static final String CHARGE_QUERY = "SELECT estimate_charge.*, "
      + " chargegroup_name, chargehead_name, dept_name,"
      + " claim_service_tax_applicable,service_charge_applicable" + " FROM estimate_charge "
      + " LEFT OUTER JOIN department ON (estimate_charge.act_department_id = department.dept_id) "
      + " JOIN chargehead_constants ON "
      + " (estimate_charge.charge_head = chargehead_constants.chargehead_id) "
      + " JOIN chargegroup_constants "
      + " ON (estimate_charge.charge_group = chargegroup_constants.chargegroup_id)";

  /** The Constant GET_ESTIMATE_CHARGES. */
  public static final String GET_ESTIMATE_CHARGES = CHARGE_QUERY + " WHERE estimate_no=? "
      + " ORDER BY chargegroup_constants.display_order, chargehead_constants.display_order ";

  /**
   * Gets the charge details bean.
   *
   * @param estimateNo
   *          the estimate no
   * @return the charge details bean
   * @throws SQLException
   *           the SQL exception
   */
  public static List getChargeDetailsBean(int estimateNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ESTIMATE_CHARGES, estimateNo);
  }

  /** The Constant ESTIMATE_BILL_QUERY. */
  private static final String ESTIMATE_BILL_QUERY = "select eb.*,org.org_id,"
      + " org.org_name,sm.salutation_id,sm.salutation,"
      + " pcm.category_id,pcm.category_name,tpa.tpa_id,tpa.tpa_name,"
      + " icm.insurance_co_id,icm.insurance_co_name,"
      + " ipm.plan_id,ipm.plan_name,icm1.category_id as plan_type_id,"
      + " icm1.category_name as plan_type_name" + " FROM estimate_bill eb"
      + " LEFT OUTER JOIN organization_details org ON(org.org_id = eb.rate_plan) "
      + " LEFT JOIN salutation_master sm ON (sm.salutation_id = eb.salutation_id)"
      + " LEFT JOIN patient_category_master pcm on (pcm.category_id = eb.patient_category_id) "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = eb.tpa_id) "
      + " LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = eb.insurance_co_id) "
      + " LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id = eb.plan_id) "
      + " LEFT JOIN insurance_category_master icm1 ON(icm1.category_id=eb.plan_type_id)"
      + " WHERE estimate_no=? ";

  /**
   * Gets the estimate bill details.
   *
   * @param estimateNo
   *          the estimate no
   * @return the estimate bill details
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getEstimateBillDetails(int estimateNo) throws SQLException {
    List list = DataBaseUtil.queryToDynaList(ESTIMATE_BILL_QUERY, estimateNo);
    if (list != null && !list.isEmpty()) {
      return (BasicDynaBean) list.get(0);
    } 
    return null;
  }

  /** The Constant ESTIMATE_LIST_FIELDS. */
  private static final String ESTIMATE_LIST_FIELDS = "SELECT eb.*, od.org_name ";

  /** The Constant ESTIMATE_LIST_COUNT. */
  private static final String ESTIMATE_LIST_COUNT = "SELECT count(estimate_no)";

  /** The Constant ESTIMATE_LIST_TABLES. */
  private static final String ESTIMATE_LIST_TABLES = "FROM estimate_bill eb"
      + " LEFT JOIN  organization_details od ON(od.org_id = eb.rate_plan)";

  /**
   * Gets the quick estimate list.
   *
   * @param parameterMap
   *          the parameter map
   * @param pagingParams
   *          the paging params
   * @return the quick estimate list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList getQuickEstimateList(Map parameterMap, Map<LISTING, Object> pagingParams)
      throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, ESTIMATE_LIST_FIELDS, ESTIMATE_LIST_COUNT,
          ESTIMATE_LIST_TABLES, pagingParams);
      qb.addFilterFromParamMap(parameterMap);
      qb.addSecondarySort("estimate_no");
      qb.build();
      return qb.getMappedPagedList();

    } finally {
      if (qb != null) {
        qb.close();
      } 
      DataBaseUtil.closeConnections(con, null);
    }
  }

}
