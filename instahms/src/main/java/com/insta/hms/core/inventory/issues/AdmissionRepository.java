package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class AdmissionRepository extends GenericRepository {

  public AdmissionRepository() {
    super("admission");
  }

}
