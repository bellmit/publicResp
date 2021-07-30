/**
 * 
 */

package com.insta.hms.core.patient.followupdetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class FollowUpRepository.
 *
 * @author anup vishwas
 */

@Repository
public class FollowUpRepository extends GenericRepository {

  /**
   * Instantiates a new follow up repository.
   */
  public FollowUpRepository() {
    super("follow_up_details");
  }

  /** The get followup details. */
  private static String GET_FOLLOWUP_DETAILS = 
      "SELECT fud.followup_id,TO_CHAR(followup_date,'DD-MM-YYYY') AS followup_date, "
      + " fud.followup_doctor_id,doc.doctor_name,fud.followup_remarks"
      + " FROM follow_up_details fud"
      + " JOIN doctors doc ON doc.doctor_id = fud.followup_doctor_id"
      + " WHERE fud.patient_id=? ORDER BY fud.followup_date ASC";

  /**
   * Gets the follow up details.
   *
   * @param patientId
   *          the patient id
   * @return the follow up details
   */
  public List<BasicDynaBean> getfollowUpDetails(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_FOLLOWUP_DETAILS, new Object[] { patientId });
  }

}
