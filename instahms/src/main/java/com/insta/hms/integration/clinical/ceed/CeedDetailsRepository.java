package com.insta.hms.integration.clinical.ceed;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * The Class CeedDetailsRepository.
 *
 * @author teja
 */
@Repository
public class CeedDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new ceed details repository.
   */
  public CeedDetailsRepository() {
    super("ceed_integration_details");
  }

  /** The get response details. */
  private static final String GET_RESPONSE_DETAILS = "SELECT "
      + "activity_id, claim_edit_rank, claim_edit_response_comments "
      + "FROM ceed_integration_details cid "
      + "JOIN ceed_integration_main cim ON (consultation_id=? "
      + "AND cim.status='A' AND cim.claim_id=cid.claim_id) ";

  /**
   * Gets the response details.
   *
   * @param consId the cons id
   * @return the response details
   */
  public List<BasicDynaBean> getResponseDetails(int consId) {
    return DatabaseHelper.queryToDynaList(GET_RESPONSE_DETAILS, new Object[] { consId });
  }

}
