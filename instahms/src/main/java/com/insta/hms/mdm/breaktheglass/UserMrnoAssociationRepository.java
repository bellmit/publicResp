package com.insta.hms.mdm.breaktheglass;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class UserMrnoAssociationRepository extends MasterRepository<Integer> {

  public UserMrnoAssociationRepository() {
    super(new String[]{"emp_username", "mr_no"}, null, "user_mrno_association", "id");
  }
}
