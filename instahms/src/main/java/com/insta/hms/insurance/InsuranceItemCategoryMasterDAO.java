package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsuranceItemCategoryMasterDAO.
 */
public class InsuranceItemCategoryMasterDAO extends GenericDAO {

  /**
   * Instantiates a new insurance item category master DAO.
   */
  public InsuranceItemCategoryMasterDAO() {
    super("item_insurance_categories");
  }

  /** The Constant INSU_ITEM_CATEGORY_NAME_AND_IDS. */
  private static final String INSU_ITEM_CATEGORY_NAME_AND_IDS = "select "
      + " insurance_category_id,insurance_category_name,"
      + "insurance_payable from item_insurance_categories";

  /**
   * Gets the insurance category names and ids.
   *
   * @return the insurance category names and ids
   * @throws SQLException the SQL exception
   */
  public static List getInsuranceCategoryNamesAndIds() throws SQLException {
    return ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(INSU_ITEM_CATEGORY_NAME_AND_IDS));
  }

  /**
   * Gets the insurance category names and ids map.
   *
   * @return the insurance category names and ids map
   * @throws SQLException the SQL exception
   */
  public static Map getInsuranceCategoryNamesAndIdsMap() throws SQLException {
    List<BasicDynaBean> lst = DataBaseUtil.queryToDynaList(INSU_ITEM_CATEGORY_NAME_AND_IDS);
    Map catNameIdMap = new HashMap();
    for (BasicDynaBean item : lst) {
      catNameIdMap.put(item.get("insurance_category_name"), item.get("insurance_category_id"));
    }
    return catNameIdMap;
  }

  /** The insert into existing plan details. */
  private static String INSERT_INTO_EXISTING_PLAN_DETAILS = " INSERT INTO "
      + " insurance_plan_details  (plan_id, insurance_category_id, "
      + " patient_amount, patient_percent, patient_amount_per_category,"
      + " patient_amount_cap, per_treatment_limit, patient_type,category_payable) "
      + " select plan_id, @ , 0, 0, 0, null, null, @@, Y from insurance_plan_main ";

  /**
   * Insert insurance category names and ids into plan.
   *
   * @param insuranceCatId   the insurance cat id
   * @param insurancePayable the insurance payable
   * @return the boolean
   * @throws SQLException the SQL exception
   */
  public static Boolean insertInsuranceCategoryNamesAndIdsIntoPlan(int insuranceCatId,
      String insurancePayable) throws SQLException {
    String query = INSERT_INTO_EXISTING_PLAN_DETAILS.replaceFirst("@",
        String.valueOf(insuranceCatId));
    query = query.replaceFirst("@@", "'i'");
    if ("N".equals(insurancePayable)) {
      query = query.replaceFirst("Y", "'N'");
    } else {
      query = query.replaceFirst("Y", "'Y'");
    }
    Connection con = DataBaseUtil.getConnection();
    Boolean success = false;
    con.setAutoCommit(false);
    int rows = 0;
    try (PreparedStatement ps = con.prepareStatement(query);) {
      rows = ps.executeUpdate();
      success = rows == 1;
      try (PreparedStatement opreplacedQuery = con
          .prepareStatement(query.replaceFirst("'i'", "'o'"));) {
        rows = opreplacedQuery.executeUpdate();
      }
      success = rows >= 1;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    return success;
  }

}
