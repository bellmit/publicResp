package com.insta.hms.mdm.tpapreauthforms;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TpaPreauthFormsRepository extends MasterRepository<String> {

  /**
   * Instantiates a new tpa preauth forms repository.
   */
  public TpaPreauthFormsRepository() {
    super("tpa_forms_pdf", "tpa_form_id");
  }
}
