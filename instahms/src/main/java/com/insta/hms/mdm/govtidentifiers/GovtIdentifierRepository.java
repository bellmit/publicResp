package com.insta.hms.mdm.govtidentifiers;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class GovtIdentifierRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new govt identifier repository.
   */
  public GovtIdentifierRepository() {
    super("govt_identifier_master", "identifier_id", "identifier_type", new String[] {
        "identifier_id", "value_mandatory", "unique_id", "remarks", "default_option",
        "govt_id_pattern", "identifier_type" });
  }
  
  private static final String MARK_DEFAULT_FALSE = "update govt_identifier_master set "
      + " default_option='N' where identifier_id != ?";

  public Integer markOtherDefaultFalse(Integer id) {
    return DatabaseHelper.update(MARK_DEFAULT_FALSE, new Object[] { id });
  }

}
