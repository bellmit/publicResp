package com.insta.hms.mdm.billprinttemplates;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillPrintTemplateService extends MasterService {
  
  @LazyAutowired
  private BillPrintTemplateRepository billPrintTemplateRepository;

  public BillPrintTemplateService(BillPrintTemplateRepository repository,
      BillPrintTemplateValidator validator) {
    super(repository, validator);
  }
  
  public List<BasicDynaBean> listAll() {
    return billPrintTemplateRepository.listAll();
  }
  
  public List<BasicDynaBean> getAllTemplates() {
    return billPrintTemplateRepository.getAllTemplates();
  }
  
}
