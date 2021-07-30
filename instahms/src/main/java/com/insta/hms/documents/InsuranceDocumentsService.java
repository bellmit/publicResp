package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class InsuranceDocumentsService extends DocumentsService {

  public InsuranceDocumentsService(InsuranceDocumentStore store) {
    super(store);
  }

}
