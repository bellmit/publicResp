package com.insta.hms.mdm.emailtemplates;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends MasterService {

  public EmailTemplateService(
      EmailTemplateRepository emailTemplateRepository,
      EmailTemplateValidator emailTemplateValidator) {
    super(emailTemplateRepository, emailTemplateValidator);
  }

  public BasicDynaBean getMailParametersFromTemplateNameAndEmailCategory(
      String templateName, String emailCategory) {
    return ((EmailTemplateRepository) this.getRepository())
        .getMailParametersFromTemplateNameAndEmailCategory(templateName, emailCategory);
  }
}
