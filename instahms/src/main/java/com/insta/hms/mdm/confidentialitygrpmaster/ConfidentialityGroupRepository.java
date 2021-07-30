package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConfidentialityGroupRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new confidentiality group repository with name and abbreviation as unique name
   * groups and name as a lookup field.
   */
  public ConfidentialityGroupRepository() {
    super(new String[] { "name", "abbreviation" }, new String[] { "name" },
        "confidentiality_grp_master", "confidentiality_grp_id");

  }

  private static final String GET_USER_CONFIDENTIALITY_GROUPS = " SELECT "
      + " c.confidentiality_grp_id, c.name FROM confidentiality_grp_master c JOIN "
      + " user_confidentiality_association u "
      + " ON (c.confidentiality_grp_id = u.confidentiality_grp_id)"
      + " WHERE u.emp_username = ? AND c.status = 'A' AND u.status = 'A' UNION "
      + " SELECT c.confidentiality_grp_id, c.name FROM confidentiality_grp_master c "
      + " WHERE c.confidentiality_grp_id = 0 ";

  public List<BasicDynaBean> getUserConfidentialityGroups(String username) {
    return DatabaseHelper.queryToDynaList(GET_USER_CONFIDENTIALITY_GROUPS, username);
  }

  private static final String GET_USER_DEFINED_CONFIDENTIALITY_GROUPS = " SELECT "
      + " confidentiality_grp_id, name FROM confidentiality_grp_master "
      + " WHERE confidentiality_grp_id != 0 ";

  public List<BasicDynaBean> getUserDefinedConfidentialityGroups() {
    return DatabaseHelper.queryToDynaList(GET_USER_DEFINED_CONFIDENTIALITY_GROUPS);
  }

}
