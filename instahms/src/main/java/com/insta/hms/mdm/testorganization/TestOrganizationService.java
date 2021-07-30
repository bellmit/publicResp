package com.insta.hms.mdm.testorganization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * The Class TestOrganizationService.
 */
@Service
public class TestOrganizationService extends MasterService {

  /** The test organization repository. */
  @LazyAutowired
  private TestOrganizationRepository testOrganizationRepository;

  /**
   * Instantiates a new test organization service.
   *
   * @param tor the tor
   * @param tov the tov
   */
  public TestOrganizationService(TestOrganizationRepository tor, TestOrganizationValidator tov) {
    super(tor, tov);
  }

  /**
   * Gets the org item code.
   *
   * @param orgId the org id
   * @param testId the test id
   * @return the org item code
   */
  public List<BasicDynaBean> getOrgItemCode(String orgId, String testId) {
    return testOrganizationRepository.getOrgItemCode(orgId, testId);
  }

  /**
   * Gets the test not applicable rate plans.
   *
   * @param testId the test id
   * @param orgId the org id
   * @return the test not applicable rate plans
   */
  public List<BasicDynaBean> getTestNotApplicableRatePlans(String testId, String orgId) {
    return testOrganizationRepository.getTestNotApplicableRatePlans(testId, orgId);
  }

  /**
   * Update test organization details.
   *
   * @param testId the test id
   * @param orgId the org id
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTestOrganizationDetails(String testId, String orgId) {
    return testOrganizationRepository.updateTestOrganizationDetails(testId, orgId);
  }

  /**
   * Gets the respository.
   *
   * @return the respository
   */
  public TestOrganizationRepository getRespository() {
    return testOrganizationRepository;
  }

  /**
   * Update org for derived rate plans.
   *
   * @param ratePlanIds the rate plan ids
   * @param applicable the applicable
   * @param testId the test id
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateOrgForDerivedRatePlans(String[] ratePlanIds, String[] applicable,
      String testId) {
    return testOrganizationRepository.updateOrgForDerivedRatePlans(ratePlanIds, applicable,
        testOrganizationRepository, "diagnostics", "test_id", testId);
  }

  /**
   * Update applicableflag for derived rate plans.
   *
   * @param derivedRatePlanIds the derived rate plan ids
   * @param chargeCategory the charge category
   * @param categoryIdName the category id name
   * @param categoryIdValue the category id value
   * @param orgDetailTblName the org detail tbl name
   * @param orgId the org id
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateApplicableflagForDerivedRatePlans(List<BasicDynaBean> derivedRatePlanIds,
      String chargeCategory, String categoryIdName, String categoryIdValue, String orgDetailTblName,
      String orgId) {

    return testOrganizationRepository.updateApplicableflagForDerivedRatePlans(derivedRatePlanIds,
        chargeCategory, categoryIdName, categoryIdValue, orgDetailTblName, orgId);
  }

  /**
   * Insert details.
   *
   * @param testId the test id
   * @param orgId the org id
   * @param applicable the applicable
   * @return the int
   */
  public int insertDetails(String testId, String orgId, boolean applicable) {
    return DatabaseHelper.insert(
        "INSERT INTO test_org_details(test_id, org_id, applicable) values (?, ?, ?)", testId, orgId,
        applicable);
  }

  /**
   * Update applicable flag.
   *
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @param applicable
   *          the applicable
   * @param itemCode
   *          the item code
   * @return the int
   */
  public int updateApplicableFlag(String testId, String orgId, boolean applicable,
      String itemCode) {
    return DatabaseHelper.update(
        "UPDATE test_org_details SET applicable = ?, item_code = ? WHERE test_id = ? "
        + " AND org_id = ? ",
        applicable, itemCode, testId, orgId);
  }
}
