package com.insta.hms.documents;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DocPdfFormTemplateRepository extends GenericRepository {

  public DocPdfFormTemplateRepository() {
    super("doc_pdf_form_templates");
    // TODO Auto-generated constructor stub
  }

}
