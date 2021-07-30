package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class LabTestDocumentPrintService extends GenericDocumentsPrintService {

  public LabTestDocumentPrintService(LabTestDocumentStore store) {
    super(store);
  }
}
