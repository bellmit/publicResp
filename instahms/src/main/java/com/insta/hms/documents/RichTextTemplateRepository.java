package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RichTextTemplateRepository extends GenericRepository {

  public RichTextTemplateRepository() {
    super("doc_rich_templates");
    // TODO Auto-generated constructor stub
  }

}
