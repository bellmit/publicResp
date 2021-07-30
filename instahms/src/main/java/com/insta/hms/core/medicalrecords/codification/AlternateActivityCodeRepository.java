package com.insta.hms.core.medicalrecords.codification;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;

public class AlternateActivityCodeRepository extends GenericRepository {

  public AlternateActivityCodeRepository() {
    super("alternate_activity_codes");
  }

  private static final String GET_ALTERNATE_CODES = 
      " SELECT * FROM charge_alternate_codes_view WHERE last_submission_batch_id=? ";

  public List<BasicDynaBean> getAlternateCodes(String submissionBatchId) {
    return DatabaseHelper.queryToDynaList(GET_ALTERNATE_CODES);
  }

}
