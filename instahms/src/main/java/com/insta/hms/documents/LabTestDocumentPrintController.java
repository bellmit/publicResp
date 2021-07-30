package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/labtestdocumentsprint")
public class LabTestDocumentPrintController extends GenericDocumentsPrintController {

  public LabTestDocumentPrintController(LabTestDocumentPrintService service) {
    super(service);
  }
}
