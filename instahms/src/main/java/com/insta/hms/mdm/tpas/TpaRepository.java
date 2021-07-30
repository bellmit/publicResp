package com.insta.hms.mdm.tpas;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class TpaRepository. */
@Repository
public class TpaRepository extends MasterRepository<String> {
   
  /** Instantiates a new tpa repository. */
  public TpaRepository() {
    super("tpa_master", "tpa_id", "tpa_name");
  }

  /** The get details. */
  private static final String GET_DETAILS =
      "SELECT * FROM tpa_master tm "
          + " LEFT JOIN sponsor_type st "
          + " ON st.sponsor_type_id= tm.sponsor_type_id "
          + " WHERE tm.tpa_id= ?";

  /**
   * Gets the details.
   *
   * @param tpaId the tpa id
   * @return the details
   */
  public BasicDynaBean getDetails(String tpaId) {
    return DatabaseHelper.queryToDynaBean(GET_DETAILS, new Object[] {tpaId});
  }

  
  /** The get patient category tpa all. */
  private static final String GET_PATIENT_CATEGORY_TPA_ALL =
      " select tm.tpa_id,tm.tpa_name "
          + " from tpa_master tm join (select regexp_split_to_table"
          + " (op_allowed_sponsors, E',')"
          + " as tpa_id from patient_category_master where category_id= ? ) "
          + " as foo ON(foo.tpa_id = "
          + "tm.tpa_id OR foo.tpa_id = '*') "
          + " where tm.status = 'A' "
          + " order by lower(trim(tpa_name))::bytea limit ?";

  private static final String GET_SPONSOR_BY_PATIENT_CATEGORY = 
          "select op_allowed_sponsors "
        + "as tpa_id from patient_category_master where category_id= ?";
  
  
  private static final String SEARCH_TPA_MASTER = 
        " select tm.tpa_id,tm.tpa_name "
        + " from tpa_master tm "
        + " where tpa_name ilike  '%'||?||'%' "
        + " and status='A'"
        + " order by lower(trim(tpa_name))::bytea limit ?";

  /**
   * Gets the details.
   *
   * @param objects the objects
   * @return the details
   */
  public List<BasicDynaBean> getDetails(Object[] objects) {
    
    if (objects.length != 3) {
      throw new ValidationException("Unexpected number of query parameters");
    }
    BasicDynaBean patientCategorySponsors = DatabaseHelper.queryToDynaBean(
          GET_SPONSOR_BY_PATIENT_CATEGORY, new Object[]{objects[0]});
 
    if (!patientCategorySponsors.get("tpa_id").equals(null)
           && !((String) patientCategorySponsors.get("tpa_id")).isEmpty()
           && !((String)patientCategorySponsors.get("tpa_id")).equals("*")) {
 
      String tpaId = (String)patientCategorySponsors.get("tpa_id");
      
      tpaId = tpaId.replace(",", "','");
      tpaId = "'" + tpaId + "'";
      
      String query = "select tm.tpa_id,tm.tpa_name from tpa_master tm where tpa_id in "
            + "(" + tpaId + ") "
            + "and tpa_name ilike  '%'||?||'%' "
            + "and status='A' "
            + "order by lower(trim(tpa_name))::bytea limit ?";
      
      return DatabaseHelper.queryToDynaList(
              query, 
              new Object[] {objects[1], objects[2]});
    }
   
    return DatabaseHelper.queryToDynaList(
            SEARCH_TPA_MASTER, new Object[] {objects[1], objects[2]});
  }

  /**
   * Gets the details all.
   *
   * @param objects the objects
   * @return the details all
   */
  public List<BasicDynaBean> getDetailsAll(Object[] objects) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_CATEGORY_TPA_ALL, objects);
  }

  /** The Constant GET_COMPANY_TPA_XML_LIST. */
  private static final String GET_COMPANY_TPA_XML_LIST =
      " SELECT ictm.insurance_co_id,"
          + " ictm.tpa_id, icm.insurance_co_name, tm.tpa_name, "
          + " icm.status AS ins_co_status,"
          + " tm.status AS tpa_status "
          + " FROM insurance_company_tpa_master ictm "
          + " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) "
          + " LEFT JOIN insurance_company_master  icm "
          + " ON (icm.insurance_co_id = ictm.insurance_co_id) "
          + " LEFT JOIN tpa_center_master  tcm ON (tcm.tpa_id = ictm.tpa_id) ";

  /** The Constant WHERE_COND_FOR_CENTER_XML_TPA_LIST. */
  private static final String WHERE_COND_FOR_CENTER_XML_TPA_LIST =
      " WHERE icm.status = 'A' "
          + " AND tm.status='A' AND CASE WHEN tcm.center_id=-1 THEN "
          + " tm.claim_format='XML' ELSE"
          + " tcm.claim_format='XML' END "
          + " AND (tcm.center_id=? OR tcm.center_id=-1) "
          + " GROUP BY ictm.tpa_id,ictm.insurance_co_id,icm.insurance_co_name,"
          + " tm.tpa_name,icm.status,tm.status "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /** The Constant WHERE_COND_FOR_NON_CENTER_XML_TPA_LIST. */
  private static final String WHERE_COND_FOR_NON_CENTER_XML_TPA_LIST =
      " WHERE icm.status = 'A' "
          + " AND tm.status='A' AND tm.claim_format='XML' "
          + " GROUP BY ictm.tpa_id,ictm.insurance_co_id,icm.insurance_co_name, "
          + " tm.tpa_name, icm.status,tm.status "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /** The Constant WHERE_COND_FOR_DEFAULT_CENTER_XML_TPA_LIST. */
  private static final String WHERE_COND_FOR_DEFAULT_CENTER_XML_TPA_LIST =
      " WHERE icm.status ='A'"
          + " AND tm.status='A' AND CASE WHEN tcm.center_id=-1 "
          + " THEN tm.claim_format='XML' ELSE "
          + " tcm.claim_format='XML' END GROUP BY ictm.tpa_id,"
          + " ictm.insurance_co_id, icm.insurance_co_name, tm.tpa_name,"
          + " icm.status,tm.status "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /**
   * Gets the company tpa XML list.
   *
   * @param maxCenters the max centers
   * @param centerId the center id
   * @return the company tpa XML list
   */
  public List<BasicDynaBean> getCompanyTpaXmlList(Integer maxCenters, int centerId) {

    if (maxCenters > 1) {
      if (centerId > 0) {
        return DatabaseHelper.queryToDynaList(
            GET_COMPANY_TPA_XML_LIST + WHERE_COND_FOR_CENTER_XML_TPA_LIST, new Object[] {centerId});
      } else {
        return DatabaseHelper.queryToDynaList(
            GET_COMPANY_TPA_XML_LIST + WHERE_COND_FOR_DEFAULT_CENTER_XML_TPA_LIST);
      }

    } else {
      return DatabaseHelper.queryToDynaList(
          GET_COMPANY_TPA_XML_LIST + WHERE_COND_FOR_NON_CENTER_XML_TPA_LIST);
    }
  }

  /** The Constant GET_XML_TPA_LIST. */
  private static final String GET_XML_TPA_LIST =
      " SELECT * from tpa_master where status = " + "? and claim_format = ?";

  /**
   * Gets the xml tpa list.
   *
   * @param xmlTpaKeyMap the xml tpa key map
   * @return the xml tpa list
   */
  public List<BasicDynaBean> getXmlTpaList(Map xmlTpaKeyMap) {
    String status = (String) xmlTpaKeyMap.get("status");
    String claimformat = (String) xmlTpaKeyMap.get("claim_format");

    return DatabaseHelper.queryToDynaList(GET_XML_TPA_LIST, new Object[] {status, claimformat});
  }

  /** The Constant GET_ALL_TPA_XML_LIST. */
  private static final String GET_ALL_TPA_XML_LIST =
      " SELECT  tm.tpa_id, tm.tpa_name, tm.status "
          + " AS tpa_status FROM tpa_master tm "
          + " LEFT JOIN tpa_center_master tcm ON (tcm.tpa_id = tm.tpa_id) ";

  /** The Constant WHERE_COND_FOR_ALL_TPA_CENTER. */
  private static final String WHERE_COND_FOR_ALL_TPA_CENTER =
      " WHERE tm.status='A' "
          + " AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XML' "
          + " ELSE tcm.claim_format='XML' "
          + " END AND (tcm.center_id=? OR tcm.center_id=-1) "
          + "  GROUP BY tm.tpa_id"
          + " ORDER BY tm.tpa_name ";

  /** The Constant WHERE_COND_FOR_ALL_TPA_NON_CENTER. */
  private static final String WHERE_COND_FOR_ALL_TPA_NON_CENTER =
      " WHERE tm.status='A' AND "
          + " tm.claim_format='XML' GROUP BY tm.tpa_id"
          + " ORDER BY tm.tpa_name ";

  /** The Constant WHERE_COND_FOR_ALL_TPA_DEFAULT_CENTER. */
  private static final String WHERE_COND_FOR_ALL_TPA_DEFAULT_CENTER =
      " WHERE tm.status='A' "
          + " AND CASE WHEN tcm.center_id=-1 THEN tm.claim_format='XML' "
          + " ELSE tcm.claim_format='XML'"
          + " END GROUP BY tm.tpa_id"
          + " ORDER BY tm.tpa_name ";

  /**
   * Gets the all tpa XML list.
   *
   * @param maxCenters the max centers
   * @param centerId the center id
   * @return the all tpa XML list
   */
  public List<BasicDynaBean> getAllTpaXmlList(Integer maxCenters, int centerId) {

    if (maxCenters > 1) {
      if (centerId > 0) {
        return DatabaseHelper.queryToDynaList(
            GET_ALL_TPA_XML_LIST + WHERE_COND_FOR_ALL_TPA_CENTER, new Object[] {centerId});
      } else {
        return DatabaseHelper.queryToDynaList(
            GET_ALL_TPA_XML_LIST + WHERE_COND_FOR_ALL_TPA_DEFAULT_CENTER);
      }
    } else {
      return DatabaseHelper.queryToDynaList(
          GET_ALL_TPA_XML_LIST + WHERE_COND_FOR_ALL_TPA_NON_CENTER);
    }
  }

  /** The Constant GET_SPONSOR_TYPE. */
  private static final String GET_SPONSOR_TYPE =
      " SELECT sponsor_type_id from tpa_master" + " where tpa_id =?";

  /**
   * Gets the sponsor type.
   *
   * @param tpaId the tpa id
   * @return the sponsor type
   */
  public int getSponsorType(Object[] tpaId) {
    return DatabaseHelper.getInteger(GET_SPONSOR_TYPE, tpaId);
  }

  /** The Constant GET_COMPANY_TPA_LIST. */
  private static final String GET_COMPANY_TPA_LIST =
      "SELECT ictm.insurance_co_id, ictm.tpa_id, "
          + " icm.insurance_co_name, tm.tpa_name, icm.status "
          + " AS ins_co_status, tm.status AS tpa_status"
          + " FROM insurance_company_tpa_master ictm "
          + " LEFT JOIN tpa_master tm ON (tm.tpa_id = ictm.tpa_id) "
          + " LEFT JOIN insurance_company_master  icm "
          + " ON (icm.insurance_co_id = ictm.insurance_co_id) "
          + " WHERE icm.status = 'A'  AND tm.status='A' "
          + " ORDER BY icm.insurance_co_name, tm.tpa_name ";

  /**
   * Gets the comp tpa list.
   *
   * @return the comp tpa list
   */
  public List<BasicDynaBean> getCompTpaList() {
    return DatabaseHelper.queryToDynaList(GET_COMPANY_TPA_LIST);
  }

  /** The Constant IP_ALLOWED_SPONSORS_QUERY. */
  private static final String IP_ALLOWED_SPONSORS_QUERY =
      "SELECT t.tpa_id, t.tpa_name FROM tpa_master t  "
          + "JOIN (select * from patient_category_master where category_id=?) p "
          + "ON p.ip_allowed_sponsors='*' "
          + "OR p.ip_allowed_sponsors LIKE t.tpa_id "
          + "OR p.ip_allowed_sponsors LIKE t.tpa_id || ',%' "
          + "OR p.ip_allowed_sponsors LIKE '%,' || t.tpa_id || ',%' "
          + "OR p.ip_allowed_sponsors LIKE '%,' || t.tpa_id "
          + "WHERE t.status='A';";

  /** The Constant OP_ALLOWED_SPONSORS_QUERY. */
  private static final String OP_ALLOWED_SPONSORS_QUERY =
      "SELECT t.tpa_id, t.tpa_name FROM tpa_master t  "
          + "JOIN (select * from patient_category_master where category_id=?) p "
          + "ON p.op_allowed_sponsors='*' "
          + "OR p.op_allowed_sponsors LIKE t.tpa_id "
          + "OR p.op_allowed_sponsors LIKE t.tpa_id || ',%' "
          + "OR p.op_allowed_sponsors LIKE '%,' || t.tpa_id || ',%' "
          + "OR p.op_allowed_sponsors LIKE '%,' || t.tpa_id "
          + "WHERE t.status='A';";

  /**
   * Gets the allowed sponsors.
   *
   * @param patientCategoryId the patient category id
   * @param visitType the visit type
   * @return the allowed sponsors
   */
  public List<BasicDynaBean> getAllowedSponsors(int patientCategoryId, String visitType) {

    if (visitType.equals("i")) {
      return DatabaseHelper.queryToDynaList(
          IP_ALLOWED_SPONSORS_QUERY, new Object[] {patientCategoryId});
    } else {
      return DatabaseHelper.queryToDynaList(
          OP_ALLOWED_SPONSORS_QUERY, new Object[] {patientCategoryId});
    }
  }

  /**
   * Gets the tpa health authority details.
   *
   * @param tpaId the tpa id
   * @param centerId the center id
   * @return the tpa health authority details
   */
  public BasicDynaBean getTpaHealthAuthorityDetails(String tpaId, Integer centerId) {
    String getTpaHealthAuthDetails = "SELECT htc.enable_eligibility_authorization,"
        + " htc.enable_eligibility_auth_in_xml FROM ha_tpa_code htc " + " @ "
        + "WHERE htc.tpa_id = ?";

    if (centerId != null) {
      getTpaHealthAuthDetails = getTpaHealthAuthDetails.replaceAll("@",
          "JOIN hospital_center_master hcm ON "
              + " (hcm.center_id = ? AND hcm.health_authority = htc.health_authority) ");
      return DatabaseHelper.queryToDynaBean(getTpaHealthAuthDetails,
          new Object[] { centerId, tpaId });
    } else {
      getTpaHealthAuthDetails = getTpaHealthAuthDetails.replaceAll("@", "");
      return DatabaseHelper.queryToDynaBean(getTpaHealthAuthDetails, new Object[] { tpaId });
    }
  }
}
