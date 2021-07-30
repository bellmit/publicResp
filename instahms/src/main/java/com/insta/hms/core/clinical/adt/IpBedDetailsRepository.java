package com.insta.hms.core.clinical.adt;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;

public class IpBedDetailsRepository extends GenericRepository {

  public IpBedDetailsRepository() {
    super("ip_bed_details");
  }

  private static final String VISIT_BEDS =
      " SELECT * FROM ip_bed_details" + " JOIN bed_names using(bed_id)"
          + " JOIN bed_types on(bed_type_name = bed_type)" + " WHERE patient_id = ? ";

  /**
   * Gets the vist main beds.
   *
   * @param visitId the visit id
   * @return the vist main beds
   */
  public List<BasicDynaBean> getVistMainBeds(String visitId) {
    return DatabaseHelper.queryToDynaList(VISIT_BEDS
        + " AND ip_bed_details.status != 'X'  AND ref_admit_id is null" + " ORDER BY admit_id  ",
        visitId);

  }

  private static final String GET_ACTIVE_BED_DETAILS =
      " SELECT ipb.*, bn.*, adm.daycare_status, adm.isbaby " + "  FROM ip_bed_details ipb "
          + "  JOIN bed_names bn ON (bn.bed_id = ipb.bed_id) "
          + "  JOIN admission adm ON (adm.patient_id = ?) "
          + " WHERE ipb.patient_id=? AND (NOT is_bystander) AND ipb.status IN ('A','C')";

  public BasicDynaBean getActiveBedDetails(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_ACTIVE_BED_DETAILS, visitId);
  }

  private static final String GET_ADMISSION_DETAILS =
      "SELECT ad.* FROM admission ad WHERE ad.patient_id=? ";

  public BasicDynaBean getAdmissionDetails(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_ADMISSION_DETAILS, visitId);
  }

  public List<BasicDynaBean> getReferencedBeds(String visitId, int mainAdmitId) {
    return DatabaseHelper.queryToDynaList(VISIT_BEDS + " AND ip_bed_details.status != 'X' "
        + "AND ( admit_id = ? or ref_admit_id = ? ) ORDER BY admit_id ");
  }

}
