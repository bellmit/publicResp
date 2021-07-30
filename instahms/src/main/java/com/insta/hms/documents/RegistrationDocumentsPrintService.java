package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class RegistrationDocumentsPrintService extends GenericDocumentsPrintService {

  public RegistrationDocumentsPrintService(RegistrationDocumentStore store) {
    super(store);
  }

}
