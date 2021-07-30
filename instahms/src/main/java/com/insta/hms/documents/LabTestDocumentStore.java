package com.insta.hms.documents;

import org.springframework.stereotype.Component;

@Component
public class LabTestDocumentStore extends TestDocumentStore {

  public LabTestDocumentStore() {
    super("SYS_LR", true);
  }
}
