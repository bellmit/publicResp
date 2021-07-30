package com.insta.hms.mdm.theatrecharges;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class TheatreChargesRepository extends GenericRepository {

  public TheatreChargesRepository() {
    super("theatre_charges");
  }

  public static final String GET_THEATRE = " SELECT t.theatre_id, t.theatre_name, tc.daily_charge,"
      + " tc.min_charge, tc.incr_charge, t.min_duration, t.incr_duration, "
      + " tc.daily_charge_discount,  tc.min_charge_discount, tc.incr_charge_discount,"
      + " tc.slab_1_charge, tc.slab_1_charge_discount, t.duration_unit_minutes,t.slab_1_threshold, "
      + " t.allow_zero_claim_amount, t.billing_group_id "
      + " FROM theatre_master t "
      + " JOIN theatre_charges tc USING (theatre_id) WHERE t.status='A' ";

  public static final String GET_THEATRE_CHARGES_OF_A_THEATRE = GET_THEATRE
      + "  AND tc.bed_type=? AND tc.org_id=? AND theatre_id =? ";

  /**
   * Gets the theatre charge details.
   *
   * @param theatreId
   *          the theatre id
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @return the theatre charge details
   */
  public BasicDynaBean getTheatreChargeDetails(String theatreId, String bedType, String orgid) {
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_THEATRE_CHARGES_OF_A_THEATRE,
        new Object[] { bedType, orgid, theatreId });
    if (bean == null) {
      bean = DatabaseHelper.queryToDynaBean(GET_THEATRE_CHARGES_OF_A_THEATRE,
          new Object[] { "GENERAL", "ORG0001", theatreId });
    }
    return bean;
  }

}
