package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/radtestdocument")
public class RadTestDocumentController extends DocumentsController {

  public RadTestDocumentController(RadTestDocumentService service) {
    super(service);
  }

  @Override
  public String getURL(String contextPath, String docId) {
    if (null != docId && !docId.equals("")) {
      return contextPath + "/radtestdocument/update.json";
    } else {
      return contextPath + "/radtestdocument/create.json";
    }
  }
}
