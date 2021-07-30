package com.insta.hms.documents;

import org.springframework.stereotype.Component;

@Component
public class RadTestDocumentStore extends TestDocumentStore {

  public RadTestDocumentStore() {
    super("SYS_RR", true);
  }

}
