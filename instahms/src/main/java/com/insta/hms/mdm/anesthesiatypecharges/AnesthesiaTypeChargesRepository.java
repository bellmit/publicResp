package com.insta.hms.mdm.anesthesiatypecharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class AnesthesiaTypeChargesRepository extends GenericRepository {

  public AnesthesiaTypeChargesRepository() {
    super("anesthesia_type_charges");
  }

  private static final String GET_ANASTHESIA_TYPE = " SELECT * FROM anesthesia_type_charges atc "
      + "  JOIN anesthesia_type_org_details aod USING (org_id, anesthesia_type_id) "
      + "  JOIN anesthesia_type_master atm using(anesthesia_type_id) "
      + " WHERE org_id=? AND bed_type=? AND anesthesia_type_id=? ";

  /**
   * Gets the anesthesia type charge.
   *
   * @param anesthesiaTypeId the anesthesia type id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the anesthesia type charge
   */
  public BasicDynaBean getAnesthesiaTypeCharge(String anesthesiaTypeId, String bedType,
      String orgId) {
    BasicDynaBean anaechargebean = DatabaseHelper.queryToDynaBean(GET_ANASTHESIA_TYPE, orgId,
        bedType, anesthesiaTypeId);
    if (anaechargebean == null) {
      anaechargebean = DatabaseHelper.queryToDynaBean(GET_ANASTHESIA_TYPE, "ORG0001", "GENERAL",
          anesthesiaTypeId);
    }
    return anaechargebean;
  }

}
