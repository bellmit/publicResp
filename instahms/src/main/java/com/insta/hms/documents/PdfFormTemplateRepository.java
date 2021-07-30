package com.insta.hms.documents;

import org.springframework.stereotype.Repository;

@Repository
public class PdfFormTemplateRepository extends GenericDocumentTemplateRepository {

  public PdfFormTemplateRepository() {
    super("doc_pdf_form_templates");
    // TODO Auto-generated constructor stub
  }

}
