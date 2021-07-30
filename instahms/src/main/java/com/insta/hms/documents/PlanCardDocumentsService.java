package com.insta.hms.documents;

import org.springframework.stereotype.Service;

@Service
public class PlanCardDocumentsService extends DocumentsService {

  public PlanCardDocumentsService(PlanCardDocumentStore store) {
    super(store);
  }

}
