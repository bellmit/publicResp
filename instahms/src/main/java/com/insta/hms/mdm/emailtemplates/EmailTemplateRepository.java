package com.insta.hms.mdm.emailtemplates;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class EmailTemplateRepository extends MasterRepository<String> {

  public EmailTemplateRepository() {
    super("email_template", "email_template_id");
  }

  private static final String GET_MAIL_PARAMETERS_FROM_TEMPLATE_NAME_AND_CATEGORY =
      " SELECT email_template_id, template_name, from_address, subject, mail_message "
          + " FROM email_template WHERE template_name = ? and email_category = ? LIMIT 1 ";

  public BasicDynaBean getMailParametersFromTemplateNameAndEmailCategory(
      String templateName, String emailCategory) {
    return DatabaseHelper.queryToDynaBean(
        GET_MAIL_PARAMETERS_FROM_TEMPLATE_NAME_AND_CATEGORY, templateName, emailCategory);
  }
}
