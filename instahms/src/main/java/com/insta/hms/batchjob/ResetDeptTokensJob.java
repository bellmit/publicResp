package com.insta.hms.batchjob;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author insta
 *
 *         # # resetting the department wise tokens for tests prescribed. #
 *
 */

public class ResetDeptTokensJob extends SQLUpdateJob {

  private static final String query = "UPDATE test_dept_tokens SET token_number=0";

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<String>();
    queryList.add(query);
    return queryList;
  }
}
