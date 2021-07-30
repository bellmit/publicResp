package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

/**
 * PatientTokenResetJob will run in midnight to reset doctor consultation tokens.
 *
 * @author yashwant
 *
 */
public class PatientTokenResetJob extends SQLUpdateJob {

  @Override
  protected List<String> getQueryList() {
    List<String> query = new ArrayList<String>();
    query.add("UPDATE doctor_consultation_tokens SET consultation_token = DEFAULT");
    return query;
  }

}
