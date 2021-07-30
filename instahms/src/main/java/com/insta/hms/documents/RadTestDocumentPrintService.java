package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class RadTestDocumentPrintService extends GenericDocumentsPrintService {

  public RadTestDocumentPrintService(RadTestDocumentStore store) {
    super(store);
  }

}
