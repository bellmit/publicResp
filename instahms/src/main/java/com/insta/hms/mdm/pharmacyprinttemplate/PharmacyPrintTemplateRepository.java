package com.insta.hms.mdm.pharmacyprinttemplate;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/** @author yashwant.
 *  */
@Repository
public class PharmacyPrintTemplateRepository extends MasterRepository<String> {

  public PharmacyPrintTemplateRepository() {
    super("store_print_template", "template_name");
  }
}
