package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericMasterDAO.
 */
public class GenericMasterDAO {
  
  /** The con. */
  Connection con = null;

  /**
   * Instantiates a new generic master DAO.
   */
  public GenericMasterDAO() {
  }

  /**
   * Instantiates a new generic master DAO.
   *
   * @param con the con
   */
  public GenericMasterDAO(Connection con) {
    this.con = con;
  }

  /** The Constant GET_GEN_DET. */
  private static final String GET_GEN_DET = " SELECT g.generic_code, g.generic_name , "
      + " g.status,g.classification_id,g.sub_classification_id, g.standard_adult_dose, "
      + " g.criticality, c.classification_name, sc.sub_classification_name FROM generic_name g "
      + " LEFT OUTER JOIN generic_classification_master c USING (classification_id) "
      + " LEFT OUTER JOIN generic_sub_classification_master sc ON g.sub_classification_id "
      + " = sc.sub_classification_id WHERE generic_code=?";

  /**
   * Gets the selected gen details.
   *
   * @param genId the gen id
   * @return the selected gen details
   * @throws Exception the exception
   */
  public static GenericDTO getSelectedGenDetails(String genId) throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    GenericDTO dto = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_GEN_DET);
      ps.setString(1, genId);
      rs = ps.executeQuery();

      if (rs.next()) {
        dto = new GenericDTO();
        dto.setGmaster_name(rs.getString("GENERIC_NAME"));
        dto.setGenCode(rs.getString("GENERIC_CODE"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setClassification_id(String.valueOf(rs.getInt("CLASSIFICATION_ID")));
        dto.setSub_classification_id(String.valueOf(rs.getInt("SUB_CLASSIFICATION_ID")));
        String stdAdultDose = rs.getString("STANDARD_ADULT_DOSE");
        if (stdAdultDose == null) {
          stdAdultDose = "";
        }
        dto.setStandard_adult_dose(stdAdultDose);
        String criticality = rs.getString("CRITICALITY");
        if (criticality == null) {
          criticality = "";
        }
        dto.setCriticality(criticality);
        String classificationname = rs.getString("CLASSIFICATION_NAME");
        if (classificationname == null) {
          classificationname = "";
        }
        dto.setClassificationName(classificationname);
        String subclassificationname = rs.getString("SUB_CLASSIFICATION_NAME");
        if (subclassificationname == null) {
          subclassificationname = "";
        }
        dto.setSub_ClassificationName(subclassificationname);
      }
      return dto;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  /**
   * Gets the gen details list.
   *
   * @param genId the gen id
   * @return the gen details list
   * @throws Exception the exception
   */
  public static ArrayList getGenDetailsList(String genId) throws Exception {
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    GenericDTO dto = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_GEN_DET);
      ps.setString(1, genId);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  /** The Constant GETGENERICS. */
  private static final String GETGENERICS = "SELECT GENERIC_CODE,GENERIC_NAME FROM GENERIC_NAME"
      + "  ORDER BY GENERIC_NAME";

  /**
   * Gets the generic names in master.
   *
   * @return the generic names in master
   * @throws SQLException the SQL exception
   */
  public static ArrayList getGenericNamesInMaster() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GETGENERICS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GENERIC_NAME_TO_ID. */
  private static final String GENERIC_NAME_TO_ID = "SELECT generic_code FROM generic_name"
      + " WHERE generic_name=?";

  /**
   * Generic name to id.
   *
   * @param genericName the generic name
   * @return the string
   * @throws SQLException the SQL exception
   */
  public static String genericNameToId(String genericName) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GENERIC_NAME_TO_ID);
      ps.setString(1, genericName);

      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  /** The Constant GENERIC_EXT_QUERY_FIELDS. */
  private static final String GENERIC_EXT_QUERY_FIELDS = " SELECT *";

  /** The Constant GENERIC_EXT_QUERY_COUNT. */
  private static final String GENERIC_EXT_QUERY_COUNT = " SELECT count(generic_code) ";

  /** The Constant GENERIC_EXT_QUERY_TABLES. */
  private static final String GENERIC_EXT_QUERY_TABLES = " FROM generic_name";

  /**
   * Search generics.
   *
   * @param filter the filter
   * @param listing the listing
   * @return the paged list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList searchGenerics(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, GENERIC_EXT_QUERY_FIELDS,
        GENERIC_EXT_QUERY_COUNT, GENERIC_EXT_QUERY_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    qb.addSecondarySort("generic_code");
    qb.build();

    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /** The Constant GENERICDETAILS_NAMESAND_iDS. */
  private static final String GENERICDETAILS_NAMESAND_iDS = "SELECT generic_name,generic_code"
      + " FROM  generic_name";

  /**
   * Gets the generic details names and ids.
   *
   * @return the generic details names and ids
   * @throws SQLException the SQL exception
   */
  public static List getGenericDetailsNamesAndIds() throws SQLException {

    return ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GENERICDETAILS_NAMESAND_iDS));
  }
}
