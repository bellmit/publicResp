package com.insta.hms.mdm.confidentialitygrpmaster;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class UserConfidentialityAssociationRepository extends MasterRepository<Integer> {

  /**
   * Constructor with emp_username and confidentiality_grp_id as composite primary key and
   * emp_username,confidentiality_grp_id,user_confidentiality_association and id should all be
   * unique.
   */
  public UserConfidentialityAssociationRepository() {
    super(new String[] { "emp_username", "confidentiality_grp_id" },
        new String[] { "emp_username", "confidentiality_grp_id" },
        "user_confidentiality_association", "id");
  }
}
