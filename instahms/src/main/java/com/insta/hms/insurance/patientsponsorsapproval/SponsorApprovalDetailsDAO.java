package com.insta.hms.insurance.patientsponsorsapproval;

import com.insta.hms.master.MasterDAO;

/**
 * The Class SponsorApprovalDetailsDAO.
 */
public class SponsorApprovalDetailsDAO extends MasterDAO {

  /**
   * Instantiates a new sponsor approval details DAO.
   */
  public SponsorApprovalDetailsDAO() {
    super("patient_sponsor_approval_details", "sponsor_approval_detail_id");
  }

  /* (non-Javadoc)
   * @see com.insta.hms.master.MasterDAO#getStatusColumnName()
   */
  @Override
  protected String getStatusColumnName() {
    return "item_status";
  }



}
