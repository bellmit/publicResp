package com.insta.hms.fpmodule;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class PurposeFpVerificationRepository extends MasterRepository<Integer> {

  public boolean supportsAutoId() {
    return false;
  }

  public PurposeFpVerificationRepository() {
    super("fp_log_purpose", "purpose_id", "purpose",
        new String[] { "purpose_id", "purpose", "status" });
  }

  private static String PURPOSE_TABLE = " FROM ( SELECT p.purpose_id,p.purpose,p.status "
      + "FROM fp_log_purpose p where p.purpose_id > 5 ) AS foo";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(PURPOSE_TABLE);
  }

  private static String PURPOSE_LOOKUP_QUERY = "SELECT * "
      + "FROM (SELECT p.purpose_id, p.purpose, p.status," + "FROM fp_log_purpose p "
      + "where p.purpose_id > 5 ) as foo";

  @Override
  public String getLookupQuery() {
    return PURPOSE_LOOKUP_QUERY;
  }

  public static final String GET_TICKET_RECENTLY_INSERT_ID = "SELECT purpose_id "
      + "FROM fp_log_purpose ORDER BY purpose_id DESC LIMIT 1";

  /**
   * Gets the latest purpose id.
   *
   * @return the latest purpose id
   */
  public int getLatestPurposeId() {
    int res = -1;

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_TICKET_RECENTLY_INSERT_ID);
    res = (int) bean.get("purpose_id");

    return res;
  }
}
