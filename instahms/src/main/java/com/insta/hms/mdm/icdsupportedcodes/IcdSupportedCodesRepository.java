package com.insta.hms.mdm.icdsupportedcodes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class IcdSupportedCodesRepository.
 */
@Repository
public class IcdSupportedCodesRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new icd supported codes repository.
   */
  public IcdSupportedCodesRepository() {
    super(
        new String[] {"code_category", "code_type"},
        new String[] {"code_category", "code_type", "code_type_classification"},
        "mrd_supported_codes",
        "id");
  }
}
