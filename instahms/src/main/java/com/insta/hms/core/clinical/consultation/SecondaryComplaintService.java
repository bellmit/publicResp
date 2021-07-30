package com.insta.hms.core.clinical.consultation;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author anup vishwas.
 *
 */

@Service
public class SecondaryComplaintService {

  @LazyAutowired
  private SecondaryComplaintRepository secondaryComplaintRepo;

  public List<BasicDynaBean> getSecondaryComplaints(String patientId) {

    return secondaryComplaintRepo.getSecondaryComplaints(patientId);
  }

  /**
   * Copy complaints.
   * @param visitDetailsBean the basic dyna bean
   * @param latestEpisodeVisitId the string
   * @param username the string
   * @return the boolean value
   */
  public boolean copyComplaints(BasicDynaBean visitDetailsBean, String latestEpisodeVisitId,
      String username) {
    boolean success = true;
    String opType = (String) visitDetailsBean.get("op_type");

    if (latestEpisodeVisitId != null && opType != null
        && (opType.equals("F") || opType.equals("D"))) {

      String visitId = (String) visitDetailsBean.get("patient_id");

      List<BasicDynaBean> previousVisitSecComplaints = secondaryComplaintRepo.listAll(null,
          "visit_id", latestEpisodeVisitId);
      if (previousVisitSecComplaints != null && previousVisitSecComplaints.size() > 0) {
        for (BasicDynaBean complbean : previousVisitSecComplaints) {
          int id = DatabaseHelper.getNextSequence("secondary_complaints");
          complbean.set("row_id", id);
          complbean.set("visit_id", visitId);
        }
        secondaryComplaintRepo.batchInsert(previousVisitSecComplaints);
      }
    }
    return success;
  }
}
