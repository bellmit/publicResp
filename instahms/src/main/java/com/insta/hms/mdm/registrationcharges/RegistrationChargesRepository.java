package com.insta.hms.mdm.registrationcharges;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RegistrationChargesRepository extends MasterRepository<String> {

  public RegistrationChargesRepository() {
    super("registration_charges", "org_id");
  }
}
