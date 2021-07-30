package com.insta.hms.mdm.codesets;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CodeSystemsRepository extends GenericRepository {
  public CodeSystemsRepository() {
    super("code_systems");
  }
}
