package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class HvfTemplateRepository extends GenericRepository {

  public HvfTemplateRepository() {
    super("doc_hvf_templates");
    // TODO Auto-generated constructor stub
  }

}
