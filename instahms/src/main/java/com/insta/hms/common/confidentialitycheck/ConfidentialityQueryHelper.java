package com.insta.hms.common.confidentialitycheck;

public class ConfidentialityQueryHelper {
  
  public static final String QUERY_CONFIDENTIALITY_GROUP_ACCESS_USER = 
      "SELECT cgm.confidentiality_grp_id "
      + " from user_confidentiality_association ufa "
      + " JOIN confidentiality_grp_master cgm "
      + " on (cgm.confidentiality_grp_id = ufa.confidentiality_grp_id) "
      + " where emp_username = current_setting('application.username') "
      + " AND ufa.status = 'A' AND cgm.status = 'A' UNION SELECT 0";
  
  public static final String QUERY_MRNO_USER_ACCESS = "SELECT mr_no from "
      + " user_mrno_association where emp_username = current_setting('application.username') ";

}
