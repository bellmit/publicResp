package com.insta.hms.core.clinical.mar;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class MarSetupRepository.
 */
@Repository
public class MarSetupRepository extends GenericRepository {

  /**
   * Instantiates a new mar setup repository.
   */
  public MarSetupRepository() {
    super("patient_mar_setup");
  }
  
  private static final String GET_LAST_SAVED_SETUP = "SELECT serving_dosage,package_uom"
      + " from patient_mar_setup where prescription_id=?"
      + " ORDER BY mod_time DESC";

  public BasicDynaBean getLastSavedSetup(Integer prescriptionId) {    
    return DatabaseHelper.queryToDynaBean(GET_LAST_SAVED_SETUP, 
        new Object[] {prescriptionId});
  }

}
