package com.insta.hms.mdm.issueuser;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

/** The Class IssueUserRepository. */
@Repository
public class IssueUserRepository extends MasterRepository<Integer> {

  /** Instantiates a new issue user repository. */
  public IssueUserRepository() {
    super("store_hosp_user", "hosp_user_id", "hosp_user_name");
  }
}
