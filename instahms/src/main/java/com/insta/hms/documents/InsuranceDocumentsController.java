package com.insta.hms.documents;

import org.springframework.web.bind.annotation.RequestMapping;

// @Controller
@RequestMapping("/insurancedocuments")
public class InsuranceDocumentsController extends DocumentsController {

  public InsuranceDocumentsController(InsuranceDocumentsService service) {
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
      return contextPath + "/insurancedocuments/update.json";
    } else {
      return contextPath + "/insurancedocuments/create.json";
    }
  }

}
