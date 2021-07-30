package com.insta.hms.batchjob;

import com.insta.hms.common.DatabaseHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * # # Script to Automatically close any open casefile indents, to be called at end-of-day # based
 * on preference for this. # # Summary: # Fetch all schemas where the preference for
 * auto_close_indented_casefiles is set to Y # For each schema, run the query that updates the case
 * file indent status to N #
 *
 * @author insta
 *
 */

public class CasefileAutoCloseJob extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(CasefileAutoCloseJob.class);

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<String>();
    String schemaCheckQuery = "SELECT auto_close_indented_casefiles FROM generic_preferences "
        + " where auto_close_indented_casefiles='Y'";

    if (DatabaseHelper.queryToDynaList(schemaCheckQuery).size() > 0) {
      logger.debug("Apply auto-close casefile indents on ");
      String updateQuery = "UPDATE mrd_casefile_attributes SET requesting_dept=NULL,"
          + " request_date=NULL, indented='N',requested_by='' WHERE indented='Y'";
      queryList.add(updateQuery);
    }

    return queryList;
  }

}
