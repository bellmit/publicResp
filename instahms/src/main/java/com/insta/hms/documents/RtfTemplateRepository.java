package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RtfTemplateRepository extends GenericRepository {

  public RtfTemplateRepository() {
    super("doc_rtf_templates");
    // TODO Auto-generated constructor stub
  }

}
