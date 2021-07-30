package com.insta.hms.core.diagnostics.incomingsampleregistration;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class IncomingSampleRegistrationDetailsRepository extends GenericRepository {

  public IncomingSampleRegistrationDetailsRepository() {
    super("incoming_sample_registration_details");
  }

}
