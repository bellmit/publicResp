package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plancarddocuments")
public class PlanCardDocumentsController extends DocumentsController {

  public PlanCardDocumentsController(PlanCardDocumentsService service) {
    super(service);
  }

  @Override
  public String getURL(String contextPath, String docId) {
    return null;
  }

}
