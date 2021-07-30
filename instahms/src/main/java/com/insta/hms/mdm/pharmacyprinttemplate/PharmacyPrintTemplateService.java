package com.insta.hms.mdm.pharmacyprinttemplate;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** @author yashwant.
 *  */
@Service
public class PharmacyPrintTemplateService extends MasterService {

  public PharmacyPrintTemplateService(
      PharmacyPrintTemplateRepository repository, PharmacyPrintTemplateValidator validator) {
    super(repository, validator);
  }
}
