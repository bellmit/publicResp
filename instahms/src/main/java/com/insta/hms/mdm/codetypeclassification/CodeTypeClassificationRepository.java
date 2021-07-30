package com.insta.hms.mdm.codetypeclassification;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CodeTypeClassificationRepository extends GenericRepository {

  public CodeTypeClassificationRepository() {
    super("code_type_classification");
  }
}
