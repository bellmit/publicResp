package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/labtestdocument")
public class LabTestDocumentController extends DocumentsController {

  public LabTestDocumentController(LabTestDocumentService service) {
    super(service);
  }

  @Override
  public String getURL(String contextPath, String docId) {
    if (null != docId && !docId.equals("")) {
      return contextPath + "/labtestdocument/update.json";
    } else {
      return contextPath + "/labtestdocument/create.json";
    }
  }
}
