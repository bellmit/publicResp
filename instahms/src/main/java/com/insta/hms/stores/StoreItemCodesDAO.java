/**
 * mithun.saha
 */

package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * The Class StoreItemCodesDAO.
 *
 * @author mithun.saha
 */
public class StoreItemCodesDAO extends GenericDAO {

  /**
   * Instantiates a new store item codes DAO.
   */
  public StoreItemCodesDAO() {
    super("store_item_codes");
  }

  /** The Constant GET_ITEM_CODE_TYPES. */
  private static final String GET_ITEM_CODE_TYPES = "SELECT msc.code_type FROM"
      + " mrd_supported_codes msc WHERE msc.code_category = ? order by code_type";

  /**
   * Gets the item code types.
   *
   * @param codeCategory the code category
   * @return the item code types
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getItemCodeTypes(String codeCategory) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_ITEM_CODE_TYPES);
      ps.setString(1, codeCategory);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the drug code type.
   *
   * @param medicineId the medicine id
   * @param drugCodeTypes the drug code types
   * @return the drug code type
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDrugCodeType(Integer medicineId, String[] drugCodeTypes)
      throws SQLException {
    StringBuilder codeQuery = new StringBuilder(
        "SELECT * FROM store_item_codes WHERE medicine_id = ? ");
    Connection con = null;
    if (drugCodeTypes == null || drugCodeTypes.length == 0) {
      return null;
    }
    PreparedStatement ps = null;
    int index = 1;
    try {
      List<String> values = Arrays.asList(drugCodeTypes);
      DataBaseUtil.addWhereFieldInList(codeQuery, "code_type", values, true);
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(codeQuery.toString());
      ps.setInt(index++, medicineId);
      for (String drugCodeType : drugCodeTypes) {
        ps.setString(index++, drugCodeType);
      }
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pharma codes.
   *
   * @param drugCodeTypes the drug code types
   * @return the pharma codes
   * @throws SQLException the SQL exception
   */
  public static List getPharmaCodes(String[] drugCodeTypes) throws SQLException {
    StringBuilder query = new StringBuilder("SELECT * FROM  getItemCodesForCodeType('*') # ");
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      if (drugCodeTypes != null) {
        DataBaseUtil.addWhereFieldInList(query, "code_type", Arrays.asList(drugCodeTypes));
      } else {
        DataBaseUtil.addWhereFieldInList(query, "code_type",
            Arrays.asList(new String[] { "Drug", "HCPCS" }));
      }
      ps = con.prepareStatement(query.toString());
      for (String codeType : drugCodeTypes) {
        ps.setString(index++, codeType);
      }
      return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(ps));
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_ITEM_CODE_AND_CODE_TYPES. */
  private static final String GET_ITEM_CODE_AND_CODE_TYPES = "SELECT * FROM "
      + " (select distinct code_type,medicine_id FROM ha_item_code_type) as hict"
      + "    LEFT  JOIN store_item_codes sic ON(hict.medicine_id=sic.medicine_id"
      + " AND hict.code_type = sic.code_type)   WHERE hict.medicine_id = ?";

  /**
   * Gets the item code and code types.
   *
   * @param medicineId the medicine id
   * @return the item code and code types
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getItemCodeAndCodeTypes(Integer medicineId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ITEM_CODE_AND_CODE_TYPES);
      ps.setInt(1, medicineId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
