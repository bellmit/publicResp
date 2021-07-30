package com.insta.hms.core.clinical.consultation;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author anup vishwas.
 *
 */

@Repository
public class SecondaryComplaintRepository extends GenericRepository {

  public SecondaryComplaintRepository() {
    super("secondary_complaints");
  }

  private static final String GET_SECONDARY_COMPLAINTS = "SELECT "
      + " sc.complaint, sc.visit_id, sc.row_id"
      + " FROM secondary_complaints sc" + " WHERE sc.visit_id = ?";

  public List<BasicDynaBean> getSecondaryComplaints(String patientId) {

    return DatabaseHelper.queryToDynaList(GET_SECONDARY_COMPLAINTS, patientId);
  }
}
