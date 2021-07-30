package com.insta.hms.mdm.billprinttemplates;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class BillPrintTemplateRepository.
 */
@Repository
public class BillPrintTemplateRepository extends MasterRepository<String> {

  /**
   * Instantiates a new bill print template repository.
   */
  public BillPrintTemplateRepository() {
    super("bill_print_template", "template_name");
  }

  private static final String GET_ALL_TEMPLATES = " select 'CUSTOM-'||template_name "
      + " as custom_template_name, "
      + " template_name, bill_template_content, template_mode, user_name, "
      + " reason, download_content_type, "
      + " download_extn from bill_print_template ;";
  
  public List<BasicDynaBean> getAllTemplates() {
    return DatabaseHelper.queryToDynaList(GET_ALL_TEMPLATES);
  }

}
