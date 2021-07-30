package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class HvfTemplateFieldsRepository extends GenericRepository {

  public HvfTemplateFieldsRepository() {
    super("doc_hvf_template_fields");
  }
}
