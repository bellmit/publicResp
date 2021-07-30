package com.insta.hms.mdm.tests;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class TestsRepository.
 */
@Repository
public class TestsRepository extends MasterRepository<String> {

  /**
   * Instantiates a new tests repository.
   */
  public TestsRepository() {
    super("diagnostics", "test_id", "test_name");
  }

  /** The Constant QUERY_FOR_TEST_CHARGE. */
  private static final String QUERY_FOR_TEST_CHARGE = " SELECT "
      + "  d.diag_code, tod.item_code as rate_plan_code, d.test_name, d.test_id, d.status, "
      + "  d.ddept_id, d.conduction_format, dd.ddept_name, dc.charge, dd.category, dc.discount, "
      + "  d.conduction_applicable,tod.applicable,d.service_sub_group_id, tod.code_type, "
      + "  d.conducting_doc_mandatory, d.insurance_category_id,tod.org_id,"
      + "  d.allow_rate_increase,d.allow_rate_decrease,dependent.test_name as dependent_test_name, "
      + "  d.dependent_test_id, d.results_entry_applicable, d.allow_zero_claim_amount, "
      + "  d.billing_group_id, "
      + "  CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' END AS house_status  "
      + "  FROM diagnostics d "
      + "  LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + "  JOIN diagnostics_departments dd ON  (dd.ddept_id = d.ddept_id) "
      + "  JOIN diagnostic_charges dc ON (d.test_id = dc.test_id) "
      + "  JOIN test_org_details tod ON  (tod.test_id = dc.test_id AND tod.org_id = dc.org_name ) "
      + "  WHERE dc.test_id =? AND dc.org_name =? " + "  AND dc.bed_type =? AND dc.priority = 'R' ";

  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @param centerId
   *          the center id
   * @return the test details
   */
  public BasicDynaBean getTestDetails(String testId, String bedType, String orgId, int centerId) {
    BasicDynaBean bean = null;
    String generalorgid = "ORG0001";
    String generalbedtype = "GENERAL";

    if (bedType == null || bedType.equals("")) {
      bedType = generalbedtype;
    }

    if (orgId == null || orgId.equals("")) {
      orgId = generalorgid;
    }

    Object[] params = { centerId, testId, orgId, bedType };
    BasicDynaBean testDetailBeanList = DatabaseHelper.queryToDynaBean(QUERY_FOR_TEST_CHARGE,
        params);

    bean = testDetailBeanList;
    if (bean == null) {
      Object[] defaultParams = { centerId, testId, generalorgid, generalbedtype };
      BasicDynaBean list = DatabaseHelper.queryToDynaBean(QUERY_FOR_TEST_CHARGE, defaultParams);
      bean = list;
    }
    return bean;
  }

  /** The Constant TEST_CHARGE_QUERY. */
  private static final String TEST_CHARGE_QUERY = " SELECT "
      + "  d.diag_code, tod.item_code as rate_plan_code, d.test_name, d.test_id, d.status, "
      + "  d.ddept_id, d.conduction_format, dd.ddept_name, dc.charge, dd.category, dc.discount, "
      + "  d.conduction_applicable,tod.applicable,d.service_sub_group_id, tod.code_type, "
      + "  d.conducting_doc_mandatory, d.insurance_category_id,tod.org_id,"
      + "  d.allow_rate_increase,d.allow_rate_decrease,dependent.test_name as dependent_test_name"
      + "  ,d.dependent_test_id, d.results_entry_applicable  " + " FROM diagnostics d "
      + " LEFT JOIN diagnostics dependent ON (d.dependent_test_id = dependent.test_id) "
      + "  JOIN diagnostics_departments dd ON  (dd.ddept_id = d.ddept_id) "
      + "  JOIN diagnostic_charges dc ON (d.test_id = dc.test_id) "
      + "  JOIN test_org_details tod ON  (tod.test_id = dc.test_id AND tod.org_id = dc.org_name ) "
      + " WHERE dc.test_id =? AND dc.org_name =? " + "  AND dc.bed_type =? AND dc.priority = 'R' ";

  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @return the test details
   */
  public BasicDynaBean getTestDetails(String testId, String bedType, String orgId) {
    BasicDynaBean bean = null;

    Object[] params = { testId, orgId, bedType };
    bean = DatabaseHelper.queryToDynaBean(TEST_CHARGE_QUERY, params);
    if (bean == null) {
      Object[] defaultParams = { testId, "ORG0001", "GENERAL" };
      bean = DatabaseHelper.queryToDynaBean(TEST_CHARGE_QUERY, defaultParams);
    }

    return bean;
  }

  /**
   * The Constant QUERY_FOR_TEST_CHARGE.
   */
  private static final String QUERY_FOR_ALL_BED_TYPE_TEST_CHARGE = " SELECT "
      + "  charge, bed_type, discount "
      + "  FROM diagnostic_charges "
      + "  WHERE test_id =? AND org_name =? ";

  /**
   * Gets the test details.
   *
   * @param testId the test id
   * @param orgId  the org id
   * @return the test details
   */
  public List<BasicDynaBean> getAllBedTypeTestDetails(String testId, String orgId) {
    Object[] params = {testId, orgId};
    return DatabaseHelper.queryToDynaList(QUERY_FOR_ALL_BED_TYPE_TEST_CHARGE, params);
  }

  /** The Constant GET_TAT_DETAILS_CHAIN. */
  public static final String GET_TAT_DETAILS_CHAIN = " select dtcm.test_id,"
      + " dtcm.center_id as source_center_id, "
      + " dtcm.center_id as outsource_dest_id, cast(center_id as text),conduction_tat_hours, "
      + " logistics_tat_hours, conduction_start_time, processing_days "
      + " FROM diag_tat_center_master dtcm "
      + " LEFT JOIN  diag_outsource_detail dod ON (dod.test_id = dtcm.test_id "
      + "   AND dod.source_center_id = dtcm.center_id) "
      + " where dtcm.test_id = ? and dtcm.center_id = ? and coalesce(dod.status, 'A') = 'A' "
      + " union all "
      + " select test_id,source_center_id, outsource_dest_id,outsource_dest,conduction_tat_hours, "
      + "  logistics_tat_hours, " + " conduction_start_time, processing_days "
      + " from (with recursive " + " outsource_chain(test_id,source_center_id, outsource_dest_id, "
      + " outsource_dest,conduction_tat_hours,logistics_tat_hours,conduction_start_time, "
      + "  processing_days) as "
      + " (select dod.test_id,source_center_id,dod.outsource_dest_id,outsource_dest, "
      + "  conduction_tat_hours, " + " logistics_tat_hours,conduction_start_time, processing_days "
      + " from diag_outsource_detail dod "
      + " join diag_outsource_master dom on (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " join diag_tat_center_master dtcm on (cast (dom.outsource_dest as int) = dtcm.center_id "
      + " and dod.test_id = dtcm.test_id) and (dom.outsource_dest_type = 'C') "
      + " where dod.test_id = ? and dod.source_center_id = ? and dod.status = 'A' " + " union all "
      + " select foo.test_id, foo.source_center_id, foo.outsource_dest_id,foo.outsource_dest, "
      + "  foo.conduction_tat_hours, "
      + " foo.logistics_tat_hours,foo.conduction_start_time, foo.processing_days "
      + " from (select dod.test_id,source_center_id, dod.outsource_dest_id,outsource_dest, "
      + "  conduction_tat_hours, " + " logistics_tat_hours,conduction_start_time, processing_days "
      + " from diag_outsource_detail dod "
      + " join diag_outsource_master dom on (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " join diag_tat_center_master dtcm on (cast (dom.outsource_dest as int) = dtcm.center_id "
      + "  and dod.test_id = dtcm.test_id) "
      + " and (dom.outsource_dest_type = 'C') AND (dod.source_center_id = ?) "
      + " and (dod.status = 'A')) as foo, "
      + " outsource_chain oc, diag_outsource_master dom1 "
      + " where (oc.outsource_dest_id = dom1.outsource_dest_id) and (oc.test_id = foo.test_id) "
      + " and (foo.source_center_id = cast (oc.outsource_dest as int)) "
      + " and (dom1.outsource_dest_type = 'C')) " + " select * from outsource_chain oc1) as foo1 ";

  /**
   * Gets the turn around time details chain.
   *
   * @param testId
   *          the test id
   * @param centerId
   *          the center id
   * @return the turn around time details chain
   */
  public List<BasicDynaBean> getTurnAroundTimeDetailsChain(String testId, int centerId) {
    return DatabaseHelper.queryToDynaList(GET_TAT_DETAILS_CHAIN, testId, centerId, testId,
        centerId, centerId);
  }

  /** The is out source test. */
  private static String isOutSourceTest = "SELECT test_id FROM diag_outsource_detail dod "
      + "WHERE dod.test_id = ? AND dod.source_center_id = ? AND dod.status = 'A' LIMIT 1";

  /**
   * Checks if is outsource test.
   *
   * @param testId
   *          the test id
   * @param centerId
   *          the center id
   * @return true, if is outsource test
   */
  public boolean isOutsourceTest(String testId, int centerId) {
    String res = DatabaseHelper.getString(isOutSourceTest, new Object[] { testId, centerId });
    return res != null && !res.equals("");
  }

}
