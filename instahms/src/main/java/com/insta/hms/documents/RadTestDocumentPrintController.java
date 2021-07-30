package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/radtestdocumentsprint")
public class RadTestDocumentPrintController extends GenericDocumentsPrintController {

  public RadTestDocumentPrintController(RadTestDocumentPrintService service) {
    super(service);
  }

}
