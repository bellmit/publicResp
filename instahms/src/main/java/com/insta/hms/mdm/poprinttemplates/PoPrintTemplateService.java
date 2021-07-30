package com.insta.hms.mdm.poprinttemplates;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class PoPrintTemplateService extends MasterService {

  public PoPrintTemplateService(PoPrintTemplateRepository repository, 
      PoPrintTemplateValidator validator) {
    super(repository, validator);
  }
}
