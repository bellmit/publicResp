package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class LabTestDocumentService extends DocumentsService {

  public LabTestDocumentService(LabTestDocumentStore store) {
    super(store);
  }
}
