package com.insta.hms.documents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registrationdocuments")
public class RegistrationDocumentsController extends DocumentsController {

  public RegistrationDocumentsController(RegistrationDocumentsService service) {
    super(service);
    // TODO Auto-generated constructor stub
  }

  private String screenId;

  @Override
  public String getScreenId() {
    return screenId;
  }

  @Override
  public void setScreenId(String screenId) {
    this.screenId = screenId;
  }

  @Override
  public String getURL(String contextPath, String docId) {
    // TODO Auto-generated method stub
    if (null != docId && !docId.equals("")) {
      return contextPath + "/registrationdocuments/update.json";
    } else {
      return contextPath + "/registrationdocuments/create.json";
    }
  }

}
