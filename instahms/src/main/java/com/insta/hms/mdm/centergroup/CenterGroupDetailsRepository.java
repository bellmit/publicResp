package com.insta.hms.mdm.centergroup;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * The Class CenterGroupDetailsRepository.
 */
@Repository
public class CenterGroupDetailsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new center group details repository.
   */
  public CenterGroupDetailsRepository() {
    super("center_group_details", "center_group_id");
    // TODO Auto-generated constructor stub
  }

  /** The select center wise association. */
  public static String SELECT_CENTER_WISE_ASSOCIATION = 
      "SELECT * FROM center_group_details where center_group_id = ? and center_id = ?";

  /** The delete center wise association. */
  public static String DELETE_CENTER_WISE_ASSOCIATION = 
      "DELETE FROM center_group_details  where center_group_id = ? and center_id = ?";
  

  /**
   * Delete association.
   *
   * @param centerId the center id
   * @param entityId the entity id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteAssociation(Integer centerId, Object entityId) throws SQLException {

    List<BasicDynaBean> selectedRecords = DatabaseHelper
        .queryToDynaList(SELECT_CENTER_WISE_ASSOCIATION, entityId, centerId);
    if (selectedRecords.size() == 0) {
      return true;
    }
    Integer isdelete = DatabaseHelper.delete(DELETE_CENTER_WISE_ASSOCIATION, entityId, centerId);
    return (isdelete != 0);

  }

  /**
   * Update associations.
   *
   * @param entityId the entity id
   * @param centerIds the center ids
   * @param assocIds the assoc ids
   * @param assocStatus the assoc status
   * @param assocDeleted the assoc deleted
   * @param assocEdited the assoc edited
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateAssociations(Object entityId, String[] centerIds, String[] assocIds,
      String[] assocStatus, String[] assocDeleted, String[] assocEdited)
      throws SQLException, IOException {
    String error = "";
    HashMap<String, Object> deleteMap = new HashMap<String, Object>();
    HashMap<String, Object> updateMap = new HashMap<String, Object>();
    for (int i = 0; i < centerIds.length - 1; i++) {
      BasicDynaBean bean = getBean();
      bean.set("center_group_id", entityId);
      bean.set("center_id", Integer.parseInt(centerIds[i]));
      bean.set("status", "A");

      if (assocIds[i].equals("_")) {
        bean.set("center_group_id", entityId);
        if (insert(bean) == 0) {
          error = "Failed to insert center association for selected centers..";
          return false;
        }
      } else if (new Boolean(assocDeleted[i])) {
        deleteMap.put("center_id", Integer.parseInt(centerIds[i]));
        deleteMap.put("center_group_id", entityId);
        if (delete(deleteMap) == null) {
          error = "Failed to delete center association for selected center..";
          return false;
        }
      }
    }
    return true;
  }
}