package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class RadTestDocumentService extends DocumentsService {

  public RadTestDocumentService(RadTestDocumentStore store) {
    super(store);
  }
}
