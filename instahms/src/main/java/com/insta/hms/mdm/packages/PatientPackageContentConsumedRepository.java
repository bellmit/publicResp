package com.insta.hms.mdm.packages;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientPackageContentConsumedRepository.
 */
@Repository
public class PatientPackageContentConsumedRepository extends GenericRepository {

  /**
   * Instantiates a new patient package content consumed repository.
   */
  public PatientPackageContentConsumedRepository() {
    super("patient_package_content_consumed");
  }

  /** The Constant GET_CONSUMED_PACKAGE_CONTENT. */
  private static final String GET_CONSUMED_PACKAGE_CONTENT =
          "SELECT ppcc.patient_package_content_id, ppc.activity_qty as content_qty,"
          + "  SUM(ppcc.quantity) as consumed_qty FROM patient_package_content_consumed ppcc "
          + " JOIN patient_package_contents ppc ON("
          + "ppc.patient_package_content_id = ppcc.patient_package_content_id) "
          + " WHERE ppc.patient_package_id=? GROUP BY 1,2"; 

  /**
   * Gets the patient package contents consumed.
   *
   * @param patientPackageId the patient package id
   * @return the patient package contents consumed
   */
  public List<BasicDynaBean> getPatientPackageContentsConsumed(Object[] patientPackageId) {
    return DatabaseHelper.queryToDynaList(GET_CONSUMED_PACKAGE_CONTENT, patientPackageId);
  }
  
  /** The Constant UPDATE_CONSUMED_PACKAGE_CONTENT_QUANTITY. */
  private static final String UPDATE_CONSUMED_PACKAGE_CONTENT_QUANTITY = " UPDATE "
      + "patient_package_content_consumed SET quantity = quantity - 1 "
      + "WHERE patient_package_content_id = ? AND prescription_id = ? AND quantity > 0 ";

  
  /** The Constant UPDATE_CONSUMED_PACKAGE_OPERATION_CONTENT_QUANTITY. */
  private static final String UPDATE_CONSUMED_PACKAGE_OPERATION_CONTENT_QUANTITY = " UPDATE "
      + "patient_package_content_consumed SET quantity = quantity - 1 "
      + "WHERE patient_package_content_id IN ( SELECT patient_package_content_id FROM "
      + " patient_package_contents WHERE content_id_ref = ? and activity_type = 'Operation' ) "
      + "AND quantity > 0 ";
  
  
  /**
   * Update pkg content consumed quantity.
   *
   * @param patPkgContentId the pat pkg content id
   * @param prescId the presc id
   * @param itemType the item type
   * @param contentId the content id
   */
  public void updatePkgContentConsumedQuantity(Integer patPkgContentId, Integer prescId, 
      String itemType, Integer contentId) {
    if ("Operation".equals(itemType)) {
      Object[] values = new Object[] { contentId };      
      DatabaseHelper.update(UPDATE_CONSUMED_PACKAGE_OPERATION_CONTENT_QUANTITY,
          values); 
    } else {
      Object[] values = new Object[] { patPkgContentId, prescId };      
      DatabaseHelper.update(UPDATE_CONSUMED_PACKAGE_CONTENT_QUANTITY,
          values);
    }
    
  }


}
