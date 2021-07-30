package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class HvfPrintTemplateRepository.
 */
@Repository
public class HvfPrintTemplateRepository extends GenericRepository {

  /**
   * Instantiates a new hvf print template repository.
   */
  public HvfPrintTemplateRepository() {
    super("hvf_print_template");
  }

  /** The Constant HVF_TEMPLATE_CONTENT. */
  public static final String HVF_TEMPLATE_CONTENT = "SELECT hvf_template_content, template_mode  "
      + " FROM hvf_print_template WHERE template_name= ? ";

  /**
   * Gets the template content.
   *
   * @param templateName the template name
   * @return the template content
   */
  public BasicDynaBean getTemplateContent(String templateName) {
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(HVF_TEMPLATE_CONTENT, templateName);
    if (!list.isEmpty()) {
      BasicDynaBean bean = list.get(0);
      return bean;
    } else {
      return null;
    }
  }
}
