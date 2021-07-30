package com.insta.hms.mdm.allergy;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class AllergenMasterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates allergen master repository.
   */
  public AllergenMasterRepository() {
    super("allergen_master", "allergen_code_id", null, new String[] {"allergen_code_id",
        "allergen_description"});

  }

  private static final String EXISITNG_ALLERGEN_ENTRY = "SELECT allergen_code_id "
      + "FROM allergen_master "
      + "WHERE upper(allergen_description)= upper(BTRIM(BTRIM(?),'.')) "
      + "AND allergy_type_id =? LIMIT 1";

  public BasicDynaBean existingAllergenEntry(String allergy, int allergyTypeId) {
    return DatabaseHelper.queryToDynaBean(EXISITNG_ALLERGEN_ENTRY, new Object[] {allergy,
        allergyTypeId});
  }
}
