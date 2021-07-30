package com.insta.hms.integration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for All the Integration configurations that happen with HIMS.
 *
 */
@Repository
public class InstaIntegrationRepository extends GenericRepository {

  static Logger logger = LoggerFactory.getLogger(InstaIntegrationRepository.class);

  private static final String GET_CENTER_INTEGRATION_DETAILS = "SELECT it.integration_name,"
      + "it.agent_host,it.agent_port, it.application_id,it.application_secret,cid.center_id,"
      + " cid.store_code, cid.establishment_key, cid.merchant_id FROM insta_integration it "
      + "JOIN center_integration_details cid ON(it.integration_id = cid.integration_id) "
      + "WHERE it.integration_name = ? AND cid.center_id = ? ";

  // additional condition on store_code used as an account_group
  private static final String STORE_CENTER_INTEGRATION_DETAILS = GET_CENTER_INTEGRATION_DETAILS
      + " AND cid.store_code = ? ";

  InstaIntegrationRepository() {
    super("insta_integration");
  }

  protected BasicDynaBean getCenterIntegrationDetails(String integrationName, int centerId) {
    return getCenterIntegrationDetails(integrationName, centerId, null);
  }

  protected BasicDynaBean getCenterIntegrationDetails(String integrationName, int centerId,
      Integer accountGroupId) {
    logger.info("Integration Name :" + integrationName + " Center Id :" + centerId
        + " Account Group : " + accountGroupId);
    if (null != accountGroupId && accountGroupId != 1) {
      return DatabaseHelper.queryToDynaBean(STORE_CENTER_INTEGRATION_DETAILS,
          new Object[] { integrationName, centerId, Integer.toString(accountGroupId) });
    }
    return DatabaseHelper.queryToDynaBean(GET_CENTER_INTEGRATION_DETAILS, integrationName,
        centerId);
  }

  private static final String GET_WHITELISTED_IPS = "SELECT ip_start, ip_end FROM"
      + " public_api_ip_whitelist WHERE used_for = ?";

  public List<BasicDynaBean> getWhitelistedIps(String usedFor) {
    return DatabaseHelper.queryToDynaList(GET_WHITELISTED_IPS,usedFor);
  }

}
