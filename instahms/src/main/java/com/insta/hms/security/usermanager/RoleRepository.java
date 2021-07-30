package com.insta.hms.security.usermanager;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RoleRepository extends GenericRepository {

  public RoleRepository() {
    super("u_role");
  }

}
