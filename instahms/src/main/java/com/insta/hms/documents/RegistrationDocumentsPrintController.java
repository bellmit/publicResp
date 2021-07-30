package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registrationdocumentsprint")
public class RegistrationDocumentsPrintController extends GenericDocumentsPrintController {

  public RegistrationDocumentsPrintController(RegistrationDocumentsPrintService service) {
    super(service);
    // TODO Auto-generated constructor stub
  }

}
