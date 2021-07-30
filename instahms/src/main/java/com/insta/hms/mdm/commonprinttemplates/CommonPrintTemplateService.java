package com.insta.hms.mdm.commonprinttemplates;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommonPrintTemplateService extends MasterService {

  public CommonPrintTemplateService(
      CommonPrintTemplateRepository repo, CommonPrintTemplateValidator validator) {
    super(repo, validator);
  }

  public List<BasicDynaBean> getTemplateNames(String type) {
    return ((CommonPrintTemplateRepository) getRepository()).getTemplateNames(type);
  }

  public List<BasicDynaBean> getGenericFormTemplateList() {
    return ((CommonPrintTemplateRepository) getRepository()).getGenericFormTemplateList();
  }
}
