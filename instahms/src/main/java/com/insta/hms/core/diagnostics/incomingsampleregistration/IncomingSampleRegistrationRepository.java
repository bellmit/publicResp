package com.insta.hms.core.diagnostics.incomingsampleregistration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class IncomingSampleRegistrationRepository.
 */
@Repository
public class IncomingSampleRegistrationRepository extends GenericRepository {

  /**
   * Instantiates a new incoming sample registration repository.
   */
  public IncomingSampleRegistrationRepository() {
    super("incoming_sample_registration");
  }

  /** The Constant GET_ISR_BASED_ON_VISIT_ID. */
  private static final String GET_ISR_BASED_ON_VISIT_ID =
      "SELECT * FROM incoming_sample_registration WHERE incoming_visit_id IN (:visitid)";

  /**
   * List ISR details.
   *
   * @param visitId the visit id
   * @return the list
   */
  public List<BasicDynaBean> listISRDetails(List<String> visitId) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("visitid", visitId);
    List<BasicDynaBean> listISRDetails =
        DatabaseHelper.queryToDynaList(GET_ISR_BASED_ON_VISIT_ID, parameters);
    return listISRDetails;
  }
}
