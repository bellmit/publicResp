package com.insta.hms.mdm.receiptrefundprinttemplates;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ReceiptRefundPrintTemplateService extends MasterService {

  @LazyAutowired
  private ReceiptRefundPrintTemplateRepository receiptRefundPrintTemplateRepository;

  public ReceiptRefundPrintTemplateService(ReceiptRefundPrintTemplateRepository repo,
      ReceiptRefundPrintTemplateValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the template list.
   *
   * @return the template list
   */
  public Map<String, String> getTemplateList() {
    List<String> columns = new ArrayList<String>();
    columns.add("template_name");
    Map<String, String> templateMap = new HashMap<String, String>();
    templateMap.put("BILL-DET-ALL", "Bill - Detailed");
    templateMap.put("CUSTOM-BUILTIN_HTML", "Built-in Default HTML template");
    templateMap.put("CUSTOM-BUILTIN_TEXT", "Built-in Default Text template");
    List<BasicDynaBean> printTemplateBeanList = receiptRefundPrintTemplateRepository
        .listAll(columns);
    for (BasicDynaBean template : printTemplateBeanList) {
      templateMap.put(template.get("template_name").toString(),
          template.get("template_name").toString());
    }
    return templateMap;
  }

}
