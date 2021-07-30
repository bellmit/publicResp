package com.insta.hms.mdm.theatre;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class TheatreRepository.
 */
@Repository
public class TheatreRepository extends MasterRepository<String> {

  /**
   * Instantiates a new theatre repository.
   */
  public TheatreRepository() {
    super("theatre_master", "theatre_id");
  }

  /** The Constant GET_THEATRE_ITEM_SUB_GROUP_TAX_DETAILS. */
  private static final String GET_THEATRE_ITEM_SUB_GROUP_TAX_DETAILS = 
      " SELECT isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM theatre_item_sub_groups tisg "
      + " JOIN item_sub_groups isg ON(tisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE tisg.theatre_id = ? ";

  /**
   * Gets the theatre item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the theatre item sub group tax details
   */
  public List<BasicDynaBean> getTheatreItemSubGroupTaxDetails(String actDescriptionId) {
    return DatabaseHelper.queryToDynaList(GET_THEATRE_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { actDescriptionId });
  }

  private static final String GET_THEATRE_LIST_IN_CENTERS = "SELECT tm.* from  theatre_master tm "
      + " JOIN patient_registration pr ON (pr.patient_id = ? AND pr.center_id = tm.center_id) "
      + " WHERE tm.status = 'A' ORDER BY theatre_name";

  /**
   * Gets the theatre list for patient id.
   *
   * @param visitId
   *          the visit id
   * @return the theatre list for patient id
   */
  public List<BasicDynaBean> getTheatreListForPatientId(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_THEATRE_LIST_IN_CENTERS, new Object[] { visitId });
  }

}
