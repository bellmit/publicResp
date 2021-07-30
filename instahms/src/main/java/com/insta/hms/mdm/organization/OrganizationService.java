package com.insta.hms.mdm.organization;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/** The Class OrganizationService. */
@Service
public class OrganizationService extends MasterService {

  /**
   * Instantiates a new organization service.
   *
   * @param organizationRepository the organization repository
   * @param organizationValidator the organization validator
   */
  public OrganizationService(
      OrganizationRepository organizationRepository, OrganizationValidator organizationValidator) {
    super(organizationRepository, organizationValidator);
  }

  /**
   * Gets the all base rate plan list.
   *
   * @return the all base rate plan list
   */
  public List<BasicDynaBean> getAllBaseRatePlanList() {
    return ((OrganizationRepository) getRepository()).getAllBaseRatePlanList();
  }

  /**
   * Gets the orgdetails dyna bean.
   *
   * @param orgId String.
   * @return BasicDynaBean
   */
  public BasicDynaBean getOrgdetailsDynaBean(String orgId) {
    List<BasicDynaBean> bean =
        ((OrganizationRepository) getRepository()).listAll(null, "org_id", orgId);
    return bean.get(0);
  }

  /**
   * Gets the active org id names exclude org.
   *
   * @param orgId the org id
   * @return the active org id names exclude org
   */
  public List<BasicDynaBean> getActiveOrgIdNamesExcludeOrg(String orgId) {
    return ((OrganizationRepository) getRepository()).getActiveOrgIdNamesExcludeOrg(orgId);
  }

  /**
   * Gets the rate sheet for charge.
   *
   * @return the rate sheet for charge
   */
  public List<BasicDynaBean> getRateSheetForCharge() {
    return ((OrganizationRepository) getRepository()).getRateSheetForCharge();
  }

  /**
   * Check for op allowed rate plans.
   *
   * @param orgId the org id
   * @param categoryId the category id
   * @return the basic dyna bean
   */
  public BasicDynaBean checkForOpAllowedRatePlans(String orgId, int categoryId) {
    return ((OrganizationRepository) getRepository())
        .checkForOpAllowedRatePlans(new Object[] {categoryId, orgId});
  }

  /**
   * Gets the valid rate plans.
   *
   * @return the valid rate plans
   */
  public List<BasicDynaBean> getValidRatePlans() {
    // TODO Auto-generated method stub
    return ((OrganizationRepository) getRepository()).getValidRatePlans();
  }

  public List<BasicDynaBean> getRateSheetList() {
    return ((OrganizationRepository) getRepository()).getRateSheetList();
  }

  /**
   * Gets the rate Sheets and rate plans.
   *
   * @return the rate Sheets and rate plans
   */
  public List<BasicDynaBean> getRateSheetAndPlanList() {
    return ((OrganizationRepository) getRepository()).getRateSheetAndPlanList();
  }

  public List<BasicDynaBean> getAllRatePlanList() {
    return ((OrganizationRepository) getRepository()).getAllRatePlanList();
  }

  /**
   * Gets the rate plan beans for the given orgIds.
   * @return the rate plan beans for the given orgIds
   */
  public List<BasicDynaBean> getRatePlanList(List<String> orgIds) {
    return ((OrganizationRepository) getRepository()).getRatePlanList(orgIds);
  }

  /**
   * Get List of orgIds.
   * @return list of orgIds
   */
  public List<String> getAllOrgIds() {
    List<BasicDynaBean> orgs = ((OrganizationRepository) getRepository()).getAllRatePlanList();
    List<String> orgIds = new ArrayList<>();
    for (BasicDynaBean bean : orgs) {
      orgIds.add((String) bean.get("org_id"));
    }
    return orgIds;
  }
}
