package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StoreMasterDAO extends GenericDAO {

  Connection con = null;

  public StoreMasterDAO() {
    super("store_item_details");
  }

  public StoreMasterDAO(Connection con) {
    super("store_item_details");
    this.con = con;
  }

  /**
   * GENERIC_MASTER table insertion.
   * 
   * @param genericName the GenericName
   * @return genericId the GenericId
   * @throws SQLException SQLException
   */

  public String insertGenericMaster(String genericName) throws SQLException {
    PreparedStatement ps = null;
    String genericId = AutoIncrementId.getSequenceId("generic_sequence", "GENERICNAME");
    try {
      ps = con.prepareStatement("INSERT INTO GENERIC_NAME VALUES(?,?)");
      ps.setString(1, genericName);
      ps.setString(2, genericId);
      int count = 0;
      count = ps.executeUpdate();
      if (count > 0) {
        return genericId;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return null;
  }

  /**
   * MANF_MASTER table insertion.
   * 
   * @param manfName the manfMame
   * @return String String
   * @throws SQLException SQLException
   */

  public String insertManfMaster(String manfName, String mnemonic) throws SQLException {
    PreparedStatement ps = null;
    String manfId = AutoIncrementId.getSequenceId("manufacturer_id_seq", "Manufacturer");
    try {
      ps = con.prepareStatement(
          "INSERT INTO MANF_MASTER (MANF_CODE,MANF_NAME,STATUS,MANF_MNEMONIC) VALUES(?,?,?,?)");
      ps.setString(1, manfId);
      ps.setString(2, manfName);
      ps.setString(3, "A");
      ps.setString(4, mnemonic);
      int count = 0;
      count = ps.executeUpdate();
      if (count > 0) {
        return manfId;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

    return null;
  }

  public Integer medicineNameToId(String medicineName) throws SQLException {
    BasicDynaBean item = findByKey("medicine_name", medicineName);
    return (Integer) item.get("medicine_id");
  }

}
