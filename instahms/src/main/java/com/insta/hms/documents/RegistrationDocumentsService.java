package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class RegistrationDocumentsService extends DocumentsService {

  public RegistrationDocumentsService(RegistrationDocumentStore store) {
    super(store);
  }

}
