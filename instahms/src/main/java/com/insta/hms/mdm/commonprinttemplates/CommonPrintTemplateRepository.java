package com.insta.hms.mdm.commonprinttemplates;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommonPrintTemplateRepository extends MasterRepository<String> {

  public CommonPrintTemplateRepository() {
    super("common_print_templates", "template_name");
  }

  private static final String GET_TEMPLATE_NAMES =
      "SELECT template_name FROM common_print_templates WHERE template_type=?";

  public List<BasicDynaBean> getTemplateNames(String type) {
    return DatabaseHelper.queryToDynaList(GET_TEMPLATE_NAMES, type);
  }

  private static final String GET_TEMPLATE_LIST =
      "SELECT print_template_id, template_name FROM common_print_templates"
          + " WHERE template_type='InstaGenericForm' order by print_template_id desc";

  public List<BasicDynaBean> getGenericFormTemplateList() {
    return DatabaseHelper.queryToDynaList(GET_TEMPLATE_LIST);
  }
}
