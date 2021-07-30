package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.AwsS3Util;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientDetailsDAO extends GenericDAO {

  private static AwsS3Util awsS3Util = ApplicationContextProvider.getBean(AwsS3Util.class);

  public PatientDetailsDAO() {
    super("patient_details");
  }

  /**
   * Gets the patient mr no details map.
   * Only details that are used in EMRMainDisplay list method
   *
   * @param mrNo the mr no
   * @return the patient mr no details map
   * @throws SQLException the SQL exception
   */

  private static final String GET_PATIENT_DETAILS_WITH_EMR_ACCESS =
      "SELECT mr_no, original_mr_no, cgm.emr_access AS mandate_emr_comments "
      + "FROM patient_details pd "
    + "JOIN confidentiality_grp_master cgm "
    + "ON (cgm.confidentiality_grp_id=pd.patient_group "
    + "AND (patient_confidentiality_check(pd.patient_group,pd.mr_no))) "
    + "WHERE pd.mr_no=?";

  public static Map getPatientDetailsWithEmrAccess(String mrNo) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_DETAILS_WITH_EMR_ACCESS);
      ps.setString(1, mrNo);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return bean != null ? bean.getMap() : null;
    }finally {
      DataBaseUtil.closeConnections(con, ps);
    }  
  }
  
  /**
   * Get patient general details (without including visit details).
   * 
   * @param mr_no
   * @return map of patient general details.
   * @throws SQLException
   */

  private static final String GENERAL_PATIENT_DETAILS = " SELECT * FROM patient_details_display_view WHERE mr_no= ? ";

  public static Map getPatientGeneralDetailsMap(Connection con, String mr_no) throws SQLException {
    BasicDynaBean b = getPatientGeneralDetailsBean(con, mr_no);
    return (b != null) ? b.getMap() : null;
  }

  public static Map getPatientGeneralDetailsMap(String mr_no) throws SQLException {
    BasicDynaBean b = getPatientGeneralDetailsBean(mr_no);
    return (b != null) ? b.getMap() : null;
  }

  public static BasicDynaBean getPatientGeneralDetailsBean(String mr_no) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      return getPatientGeneralDetailsBean(con, mr_no);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static BasicDynaBean getPatientGeneralDetailsBean(Connection con, String mr_no)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GENERAL_PATIENT_DETAILS);
      ps.setString(1, mr_no);

      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      if (bean != null) {
        DateUtil.checkAndSetAgeComponents(bean);
        return bean;
      }
      return bean;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static final String PATIENT_VISIT_ID = " SELECT visit_id FROM patient_details WHERE mr_no=? ";

  public boolean isVisitIdExists(String mrNo) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    String visitId = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PATIENT_VISIT_ID);
      ps.setString(1, mrNo);
      visitId = DataBaseUtil.getStringValueFromDb(ps);

      if (visitId != null)
        return true;
      return false;

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public BasicDynaBean findPatientByMrno(String mrNo) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      List<BasicDynaBean> list = findPatientByMrno(con, mrNo);
      return list.get(0);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  public static final String GET_ALL_PATIENT_DETAILS = "select  mr_no, oldmrno, salutation, patient_name, middle_name,last_name,"
      + " patient_gender, patient_address, patient_city, patient_state, patient_area, country,patient_phone, patient_phone2,"
      + " dateofbirth, custom_list1_value, custom_list2_value, custom_list3_value, custom_list4_value, custom_list5_value,"
      + " custom_list6_value, custom_list7_value, custom_list8_value, custom_list9_value, custom_field1, custom_field2,"
      + " custom_field3, custom_field4, custom_field5, custom_field6, custom_field7, custom_field8,"
      + " custom_field9, custom_field10, custom_field11,"
      + " custom_field12, custom_field13, custom_field14, custom_field15, custom_field16, custom_field17,"
      + " custom_field18, custom_field19, expected_dob, email_id,  patient_category_id,  death_date,  "
      + " death_time, first_visit_reg_date, no_allergies, med_allergies, food_allergies,"
      + " other_allergies, government_identifier, identifier_id, dead_on_arrival, death_reason_id, "
      + " original_mr_no, mobile_password, nationality_id  FROM  patient_details  WHERE  mr_no=?";

  public List<BasicDynaBean> findPatientByMrno(Connection con, String mrNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ALL_PATIENT_DETAILS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  private static String GET_REGISTRATION_DOC_FIELDS = " select prc.patient_id,prc.doc_id,prc.doc_date,"
      + " prc.username,pd.template_id,pd.doc_format,pd.doc_status,dv.doc_type,dv.template_name, pr.status, pr.reg_date, "
      + " dv.access_rights ";

  private static String GET_REGISTRATION_DOC_TABLES = "from patient_registration_cards prc "
      + " JOIN patient_documents pd on pd.doc_id = prc.doc_id "
      + " JOIN doc_all_templates_view dv on pd.doc_format = dv.doc_format and pd.template_id=dv.template_id "
      + " JOIN patient_registration pr using (patient_id)";

  private static String GET_REGISTRATION_DOC_COUNT = "select count(*)";

  public static PagedList getRegistrationDocs(Map listingParams, Map extraParams,
      Boolean specialized) throws SQLException {

    int totalRecords = 0;
    SearchQueryBuilder qb = null;
    Connection con = null;
    List list = null;
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, GET_REGISTRATION_DOC_FIELDS, GET_REGISTRATION_DOC_COUNT,
          GET_REGISTRATION_DOC_TABLES, null, null, null, false, pageSize, pageNum);

      qb.addFilter(qb.STRING, "prc.patient_id", "=", (String) extraParams.get("patient_id"));
      qb.build();

      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      list = DataBaseUtil.queryToDynaList(psData);
      ResultSet rsCount = psCount.executeQuery();
      if (rsCount.next()) {
        totalRecords = rsCount.getInt(1);
      }
      rsCount.close();
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
    return new PagedList(list, totalRecords, pageSize, pageNum);
  }

  private static final String INSURANCE_CARD_IMAGE_BY_ID = " SELECT pdc.doc_content_bytea, pdc.content_type, "
      + " pdc.is_migrated, pdc.doc_id FROM patient_insurance_plans "
      + " JOIN plan_docs_details pdd USING(patient_policy_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE pdd.doc_id=?  AND patient_policy_id!=0 ";

  private static final String CORPORATE_CARD_IMAGE_BY_ID = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration pr "
      + " JOIN corporate_docs_details pdd ON (pdd.patient_corporate_id = pr.patient_corporate_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE pdd.doc_id=? AND pr.patient_corporate_id!=0 "
      + " UNION "
      + " SELECT spdc.doc_content_bytea, spdc.content_type "
      + " FROM patient_registration pr "
      + " JOIN corporate_docs_details spdd ON (spdd.patient_corporate_id = pr.secondary_patient_corporate_id) "
      + " JOIN patient_documents spdc ON(spdc.doc_id = spdd.doc_id) "
      + " WHERE spdd.doc_id=? AND secondary_patient_corporate_id!=0 ";

  private static final String NATIONAL_CARD_IMAGE_BY_ID = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration pr "
      + " LEFT JOIN national_sponsor_docs_details pdd ON (pdd.patient_national_sponsor_id = pr.patient_national_sponsor_id) "
      + " LEFT JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE pdd.doc_id=? AND pr.patient_national_sponsor_id!=0 "
      + " UNION "
      + " SELECT spdc.doc_content_bytea, spdc.content_type "
      + " FROM patient_registration pr "
      + " LEFT JOIN national_sponsor_docs_details spdd ON (spdd.patient_national_sponsor_id = pr.secondary_patient_national_sponsor_id) "
      + " LEFT JOIN patient_documents spdc ON(spdc.doc_id = spdd.doc_id) "
      + " WHERE spdd.doc_id=? AND secondary_patient_national_sponsor_id!=0 ";

  public static Map getPatientCardImageMap(int doc_id, String sponsorType) throws SQLException {
    if (doc_id == 0)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType.equals("I")) {
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE_BY_ID);
        ps.setInt(1, doc_id);
      } else if (sponsorType.equals("C")) {
        ps = con.prepareStatement(CORPORATE_CARD_IMAGE_BY_ID);
        ps.setInt(1, doc_id);
        ps.setInt(2, doc_id);
      } else if (sponsorType.equals("N")) {
        ps = con.prepareStatement(NATIONAL_CARD_IMAGE_BY_ID);
        ps.setInt(1, doc_id);
        ps.setInt(2, doc_id);
      }
      rs = ps.executeQuery();
      if (rs.next()) {
        Map cardMap = new HashMap();
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            cardMap.put("CONTENT", new ByteArrayInputStream(file));
          }
        }
        if (!cardMap.containsKey("CONTENT")) {
          cardMap.put("CONTENT", rs.getBinaryStream(1));
        }
        cardMap.put("CONTENT_TYPE", rs.getString(2));
        return cardMap;
      } else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  public static InputStream getPatientCardImage(int doc_id, String sponsorType) throws SQLException {
    if (doc_id == 0)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE_BY_ID);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(CORPORATE_CARD_IMAGE_BY_ID);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(NATIONAL_CARD_IMAGE_BY_ID);
      ps.setInt(1, doc_id);
      rs = ps.executeQuery();
      if (rs.next()) {
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            return new ByteArrayInputStream(file);
          }
        }
        return rs.getBinaryStream(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  public static Integer getPatientPolicyId(String patientId) throws SQLException {
    String query = "SELECT patient_policy_id FROM patient_insurance_plans WHERE patient_id = ?";
    return DataBaseUtil.getIntValueFromDb(query, new Object[]{patientId});
  }

  private static final String INSURANCE_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type, "
      + " pdc.is_migrated, pdc.doc_id FROM patient_insurance_plans "
      + " JOIN plan_docs_details pdd USING(patient_policy_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_policy_id!=0  ORDER BY pdd.doc_id DESC  ";

  private static final String CORPORATE_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration "
      + " JOIN corporate_docs_details pdd USING(patient_corporate_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_corporate_id!=0  ORDER BY pdd.doc_id DESC  ";

  private static final String NATIONAL_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration "
      + " JOIN national_sponsor_docs_details pdd USING(patient_national_sponsor_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND patient_national_sponsor_id!=0 ORDER BY pdd.doc_id DESC ";

  public static Map getCurrentPatientCardImageMap(String patientId, String sponsorType)
      throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType == null || sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(CORPORATE_CARD_IMAGE);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(NATIONAL_CARD_IMAGE);
      else
        return null;
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        Map cardMap = new HashMap();
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            cardMap.put("CONTENT", new ByteArrayInputStream(file));
          }
        }
        if (!cardMap.containsKey("CONTENT")) {
          cardMap.put("CONTENT", rs.getBinaryStream(1));
        }
        cardMap.put("CONTENT_TYPE", rs.getString(2));
        return cardMap;
      } else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /*
   * public static InputStream getCurrentPatientCardImage(String patientId, String sponsorType)
   * throws SQLException { return getCurrentPatientCardImage(patientId, sponsorType); }
   */

  public static InputStream getCurrentPatientCardImage(String patientId, String sponsorType)
      throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String mainVisitId = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      if (sponsorType == null || sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE);

      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(CORPORATE_CARD_IMAGE);

      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(NATIONAL_CARD_IMAGE);

      else
        return null;
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            return new ByteArrayInputStream(file);
          }
        }
        return rs.getBinaryStream(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private static final String SEC_CORPORATE_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration pr "
      + " JOIN corporate_docs_details pdd ON (pr.secondary_patient_corporate_id = pdd.patient_corporate_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND secondary_patient_corporate_id!=0  ORDER BY pdd.doc_id DESC  ";

  private static final String SEC_NATIONAL_CARD_IMAGE = " SELECT pdc.doc_content_bytea, pdc.content_type "
      + " FROM patient_registration pr "
      + " JOIN national_sponsor_docs_details pdd ON (pr.secondary_patient_national_sponsor_id = pdd.patient_national_sponsor_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE patient_id=?  AND secondary_patient_national_sponsor_id!=0  ORDER BY pdd.doc_id DESC  ";

  public static Map getCurrentPatientSecCardImageMap(String patientId, String sponsorType)
      throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(SEC_CORPORATE_CARD_IMAGE);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(SEC_NATIONAL_CARD_IMAGE);
      else
        return null;
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        Map cardMap = new HashMap();
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            cardMap.put("CONTENT", new ByteArrayInputStream(file));
          }
        }
        if (!cardMap.containsKey("CONTENT")) {
          cardMap.put("CONTENT", rs.getBinaryStream(1));
        }
        cardMap.put("CONTENT_TYPE", rs.getString(2));
        return cardMap;
      } else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  public static InputStream getCurrentPatientSecCardImage(String patientId, String sponsorType)
      throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_CARD_IMAGE);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(SEC_CORPORATE_CARD_IMAGE);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(SEC_NATIONAL_CARD_IMAGE);
      else
        return null;
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            return new ByteArrayInputStream(file);
          }
        }
        return rs.getBinaryStream(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private static final String PATIENT_PHOTO = " SELECT patient_photo  FROM patient_details WHERE mr_no=?";

  public static InputStream getPatientPhoto(String mrNo) throws SQLException {

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PATIENT_PHOTO);
      ps.setString(1, mrNo);
      rs = ps.executeQuery();
      if (rs.next())
        return rs.getBinaryStream(1);
      else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private String PHOTO_SIZE = "SELECT length(patient_photo)as photo_size from patient_details where mr_no=?";

  public int getPhotoSize(String mrno) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    int size = 0;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PHOTO_SIZE);
      ps.setString(1, mrno);
      rs = ps.executeQuery();
      while (rs.next()) {
        size = rs.getInt(1);
      }
    } finally {
      rs.close();
      ps.close();
      con.close();
    }
    return size;
  }

  // Check for name, age & gender already exists for any patient
  public List<BasicDynaBean> checkDetailsExist(BasicDynaBean patDetailsBean) throws SQLException,
      ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    try {

      String dob = patDetailsBean.get("remarks").toString();
      StringBuilder q1 = new StringBuilder();
      q1.append(" SELECT mr_no, patient_name, middle_name, last_name, dateofbirth, expected_dob, "
          + " to_char(dateofbirth,'dd-MM-yyyy')::text AS dob, to_char(expected_dob,'dd-MM-yyyy')::text AS eob, "
          + " get_patient_age(dateofbirth, expected_dob)::text AS age,  "
          + " get_patient_age_in(dateofbirth, expected_dob)::text AS agein, "
          + " CASE WHEN patient_gender='M' THEN 'Male' WHEN patient_gender='F' THEN 'Female' ELSE 'Other' END AS patient_gender,"
          + " patient_phone, patient_address, patient_city, city_name, patient_area, government_identifier"
          + " FROM patient_details pd "
          + " JOIN city ON (city_id = patient_city) "
          + " WHERE (lower(patient_name) LIKE ? AND lower(last_name) LIKE ? AND patient_gender = ? "
          + " AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )");

      if (null == dob || ("").equals(dob)) {
        q1.append(" and  (select get_patient_age(dateofbirth, expected_dob)) = ? ");
      } else {
        q1.append(" and (select get_patient_age(dateofbirth, expected_dob )) = (select get_patient_age(null,?)) ");
      }

      q1.append(")");

      if (null != patDetailsBean.get("patient_phone").toString()
          && (!(patDetailsBean.get("patient_phone").toString().equals("")))) {
        q1.append(" AND (( patient_phone = ? ) OR (replace(patient_phone, CASE WHEN patient_phone_country_code IS NULL THEN '' ELSE patient_phone_country_code END,'') = ?))");
      }
      if (null != patDetailsBean.get("government_identifier")
          && (!(patDetailsBean.get("government_identifier").toString().equals("")))) {
        q1.append(" OR ( lower(government_identifier) = ? )");
      }
      if (null != patDetailsBean.get("middle_name")
          && !patDetailsBean.get("middle_name").toString().equals(""))
        q1.append(
            " AND lower(middle_name) LIKE '" + patDetailsBean.get("middle_name").toString() + "'");
      else
        q1.append(" AND (lower(middle_name) LIKE '' OR lower(middle_name) IS NULL )");
        
      // q1.append(" order by mr_no desc  limit 1  ");
      q1.append(" ORDER BY mr_no DESC ");

      int i = 1;
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(q1.toString());
      ps.setString(i++, patDetailsBean.get("patient_name").toString().toLowerCase());
      ps.setString(i++, patDetailsBean.get("last_name").toString().toLowerCase());
      ps.setString(i++, patDetailsBean.get("patient_gender").toString());

      if (null == dob || ("").equals(dob))
        ps.setInt(i++, Integer.parseInt(patDetailsBean.get("patient_care_oftext").toString()));
      else {

        java.sql.Date dateOfBirth = null;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

        if (!patDetailsBean.get("remarks").toString().equalsIgnoreCase("")) {
          dateOfBirth = new java.sql.Date(
              (sf.parse((patDetailsBean.get("remarks").toString()))).getTime());
        }

        ps.setDate(i++, dateOfBirth);
      }

      if (null != patDetailsBean.get("patient_phone").toString()
          && (!(patDetailsBean.get("patient_phone").toString().equals("")))) {
        ps.setString(i++, patDetailsBean.get("patient_phone").toString());
        ps.setString(i++, patDetailsBean.get("patient_phone").toString());
      }

      if (null != patDetailsBean.get("government_identifier")
          && (!(patDetailsBean.get("government_identifier").toString().equals("")))) {
        ps.setString(i++, patDetailsBean.get("government_identifier").toString().toLowerCase());
      }
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String PATIENT_CONSULTATION_INFO = "UPDATE patient_details set "
      + " patient_consultation_info = ?, custom_field11=?, custom_field12=?, custom_field13=? WHERE "
      + " mr_no = (SELECT mr_no FROM patient_registration WHERE patient_id=?)";

  public static boolean updatePatientConsulationInfo(Connection con,
      String patientConsultationInfo, String customField11, String customField12,
      String customField13, String patient_id) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(PATIENT_CONSULTATION_INFO);
      ps.setString(1, patientConsultationInfo);
      ps.setString(2, customField11);
      ps.setString(3, customField12);
      ps.setString(4, customField13);
      ps.setString(5, patient_id);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  public static final String IP_PATIENT_VISIT_ID = "SELECT visit_id FROM patient_details pd "
      + " JOIN patient_registration pr ON (visit_id = pr.patient_id AND visit_type = 'i') WHERE pd.mr_no= ? ";

  public boolean isIPVisitIdExists(String mrno) throws SQLException {
    BasicDynaBean bean = DataBaseUtil.queryToDynaBean(IP_PATIENT_VISIT_ID, mrno);
    if (bean != null && bean.get("visit_id") != null && !bean.get("visit_id").equals(""))
      return true;
    else
      return false;
  }

  public static final String INCOMING_PATIENTS = "   SELECT center_id, incoming_visit_id, orig_lab_name, patient_name, patient_age,patient_gender,referring_doctor,address,billno,date,category,phone_no,patient_other_info,source_center_id,mr_no,incoming_source_type,isr_dateofbirth,"
      + "   his_visit_id, phone_no_country_code, government_identifier, identifier_id, test_id, orig_sample_no, prescribed_id, source_test_prescribed, center_name, status, center_code,"
      + "   city_id, state_id, country_id, center_address, accounting_company_name, hospital_center_service_reg_no, center_contact_phone, created_timestamp, updated_timestamp, region_id, health_authority, dhpo_facility_user_id,"
      + "   dhpo_facility_password,shafafiya_user_id,shafafiya_password,shafafiya_pbm_active,shafafiya_preauth_user_id,"
      + "shafafiya_preauth_password,shafafiya_preauth_active,shafafiya_pbm_test_member_id,shafafiya_pbm_test_provider_id,"
      + "   shafafiya_preauth_test_member_id,shafafiya_preauth_test_provider_id,eclaim_active,tin_number,ha_username,"
      + "ha_password, get_patient_age(null,null,inc.isr_dateofbirth,inc.patient_age) age,"
      + "   incoming_visit_id as patient_id, inc.isr_dateofbirth AS expected_dob, 'in' as visit_type,'A' as visit_status,'ORG0001' as org_id, hcm.center_name, get_patient_age_in(null,null,inc.isr_dateofbirth,inc.age_unit) as age_unit"
      + "   FROM incoming_sample_registration  inc "
      + "   LEFT JOIN incoming_sample_registration_details ind USING(incoming_visit_id)"
      + "   LEFT JOIN hospital_center_master hcm USING(center_id)"
      + "   WHERE incoming_visit_id = ? ";

  public static BasicDynaBean getIncomingPatientDetails(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(INCOMING_PATIENTS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String INCOMING_PATIENTS_DETAILS = "  SELECT  COALESCE(pd.dateofbirth, pd.expected_dob, isr.isr_dateofbirth) AS expected_dob,"
      + "  isr.patient_age * (CASE WHEN isr.age_unit = 'Y' THEN 365.25 WHEN isr.age_unit = 'M' THEN 30.43 "
      + "  ELSE 1 end) AS patient_age_days, COALESCE(pd.patient_gender, isr.patient_gender) AS patient_gender "
      + "  FROM incoming_sample_registration  isr "
      + "  LEFT JOIN patient_details pd ON (pd.mr_no = isr.mr_no) "
      + "  WHERE incoming_visit_id = ? ";

  public static BasicDynaBean getIncomingPatientResultDetails(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(INCOMING_PATIENTS_DETAILS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String IS_MRNO_VISIT_EXISTS = " SELECT mr_no FROM patient_registration WHERE mr_no=? ";

  public boolean isMrnoVisitExists(Connection con, String mrNo) throws SQLException {
    PreparedStatement ps = con.prepareStatement(IS_MRNO_VISIT_EXISTS);
    ps.setString(1, mrNo);
    String mrno = DataBaseUtil.getStringValueFromDb(ps);
    if (ps != null)
      ps.close();
    if (mrno != null && !mrno.equals(""))
      return true;
    return false;
  }

  public boolean updatePatientDetails(Connection con, BasicDynaBean patientDetails)
      throws Exception {
    Map<String, String> keys = new HashMap<String, String>();
    keys.put("mr_no", (String) patientDetails.get("mr_no"));
    return update(con, patientDetails.getMap(), keys) > 0;
  }

  /*
   * Returns the number of IP registrations, for the given dates.
   */
  public static final String IPREG_COUNT = "SELECT count(patient_id) "
      + " FROM patient_registration WHERE reg_date::date BETWEEN ? AND ? "
      + " AND visit_type = 'i'";

  public static int getIpRegCount(Date fromDate, Date toDate, int centerId) throws SQLException {
    String query = centerId == 0 ? IPREG_COUNT : IPREG_COUNT + " AND center_id =  " + centerId;
    return DataBaseUtil.getIntValueFromDbDates(query, fromDate, toDate);
  }

  /*
   * Returns the number of OP registrations, for the given dates.
   */
  public static final String OPREG_COUNT = "SELECT count(patient_id) "
      + " FROM patient_registration WHERE reg_date::date BETWEEN ? AND ? "
      + " AND visit_type = 'o' AND op_type != 'O'";

  public static int getOpRegCount(Date fromDate, Date toDate, int centerId) throws SQLException {
    String query = centerId == 0 ? OPREG_COUNT : OPREG_COUNT + " AND center_id =  " + centerId;
    return DataBaseUtil.getIntValueFromDbDates(query, fromDate, toDate);
  }

  /*
   * Returns the number of OP registrations, for the given dates.
   */
  public static final String OSPREG_COUNT = "SELECT count(patient_id) "
      + " FROM patient_registration WHERE reg_date::date BETWEEN ? AND ? "
      + " AND visit_type = 'o' AND op_type = 'O'";

  public static int getOspRegCount(Date fromDate, Date toDate, int centerId) throws SQLException {
    String query = centerId == 0 ? OSPREG_COUNT : OSPREG_COUNT + " AND center_id =  " + centerId;
    return DataBaseUtil.getIntValueFromDbDates(query, fromDate, toDate);
  }

  /*
   * Returns the number of discharges between the given dates
   */
  public static final String DISCHARGE_COUNT = "SELECT count(patient_id) "
      + " FROM patient_registration WHERE discharge_date::date BETWEEN ? AND ?";

  public static int getDischargeCount(Date fromDate, Date toDate, int centerId) throws SQLException {
    String query = centerId == 0 ? DISCHARGE_COUNT : DISCHARGE_COUNT + " AND center_id =  "
        + centerId;
    return DataBaseUtil.getIntValueFromDbDates(query, fromDate, toDate);
  }

  /*
   * Returns the number of ip discharges between the given dates
   */
  public static final String IP_DISCHARGE_COUNT = "SELECT COUNT(pr.patient_id) FROM  patient_registration pr "
      + " WHERE pr.visit_type='i' AND  pr.discharge_flag='D' "
      + " AND pr.discharge_date::date BETWEEN ? AND ? ";

  public static int getIpDischargeCount(Date fromDate, Date toDate, int centerId)
      throws SQLException {
    String query = centerId == 0 ? IP_DISCHARGE_COUNT : IP_DISCHARGE_COUNT + " AND center_id =  "
        + centerId;
    return DataBaseUtil.getIntValueFromDbDates(query, fromDate, toDate);
  }

  public static final String COUNT_ACTIVE_IN_PATIENTS = " SELECT COUNT(*) FROM "
      + "(SELECT DISTINCT mr_no FROM  patient_registration "
      + "WHERE status ='A' AND visit_type='i' ) AS foo ";

  public static final String COUNT_ACTIVE_CENTER_IN_PATIENTS = " SELECT COUNT(*) FROM "
      + "(SELECT DISTINCT mr_no FROM  patient_registration "
      + "WHERE status ='A' AND visit_type='i' AND center_id = ?) AS foo ";

  public static int getActiveInPatientCount(int centerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId == 0) {
        ps = con.prepareStatement(COUNT_ACTIVE_IN_PATIENTS);
      } else {
        ps = con.prepareStatement(COUNT_ACTIVE_CENTER_IN_PATIENTS);
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String COUNT_ACTIVE_CENTER_OUT_PATIENTS = " SELECT COUNT(*) FROM "
      + "(SELECT DISTINCT mr_no FROM  patient_registration WHERE status ='A' AND "
      + "visit_type='o' AND center_id = ?) AS foo ";

  public static final String COUNT_ACTIVE_OUT_PATIENTS = " SELECT COUNT(*) FROM "
      + "(SELECT DISTINCT mr_no FROM  patient_registration WHERE status ='A' AND "
      + "visit_type='o' ) AS foo ";

  public static int getActiveOutPatientCount(int centerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (centerId == 0) {
        ps = con.prepareStatement(COUNT_ACTIVE_OUT_PATIENTS);
      } else {
        ps = con.prepareStatement(COUNT_ACTIVE_CENTER_OUT_PATIENTS);
        ps.setInt(1, centerId);
      }
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String COUNT_TOTAL_ACTIVE_PATIENTS = "SELECT COUNT(*) FROM "
      + "(SELECT DISTINCT mr_no FROM " + " patient_registration WHERE status ='A' ) AS foo";

  public static int getActivePatientTotal() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(COUNT_TOTAL_ACTIVE_PATIENTS);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static final String CUSTOM_FIELDS = "SELECT custom_field1_label, custom_field2_label, custom_field3_label,"
      + " custom_field4_label, custom_field5_label, old_reg_field_label, "
      + " custom_field6_label, custom_field7_label, custom_field8_label, "
      + " custom_field9_label, custom_field10_label, "
      + " 'Patient Address', 'Next of Kin','Next of Kin address', 'Next of Kin Phone',"
      + " custom_field11_label, custom_field12_label, custom_field13_label,custom_field14_label,"
      + " custom_field15_label,custom_field16_label,custom_field17_label,custom_field18_label,"
      + " custom_field19_label,family_id " + " FROM registration_preferences";

  private static String customFieldDisplayNames[] = { "custom_field1", "custom_field2",
      "custom_field3", "custom_field4", "custom_field5", "oldmrno", "custom_field6",
      "custom_field7", "custom_field8", "custom_field9", "custom_field10", "patient_address",
      "relation", "patient_careof_address", "patient_care_oftext ", "custom_field11",
      "custom_field12", "custom_field13", "custom_field14", "custom_field15", "custom_field16",
      "custom_field17", "custom_field18", "custom_field19", "family_id" };

  public static Map getCustomRegFieldsMap() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Map<String, String> customRegFieldsMap = new LinkedHashMap<String, String>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(CUSTOM_FIELDS);
      rs = ps.executeQuery();
      if (rs.next()) {
        for (int j = 0; j < customFieldDisplayNames.length; j++) {
          if (rs.getString(j + 1) != null && !rs.getString(j + 1).equals("")) {
            customRegFieldsMap.put(customFieldDisplayNames[j], rs.getString(j + 1));
          }
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return customRegFieldsMap;
  }

  private static String ICD_PATIENT_FIELDS = "SELECT * ";

  private static String ICD_PATIENT_TABLES = " FROM (SELECT "
      + "  pr.mr_no, pd.oldmrno, pd.salutation as salutation_id, sm.salutation, pd.patient_name,  "
      + "  pd.middle_name, pd.last_name, pr.reg_date, pr.visit_type, pr.discharge_date, pr.op_type, "
      + "  icm.insurance_co_name AS primary_insurance_co_name, sicm.insurance_co_name as secondary_insurance_co_name,"
      + "	 pr.codification_status, pr.patient_id,  "
      + "  pr.status AS visit_status, dept.dept_id, dept.dept_name, dr.doctor_id, dr.doctor_name, pr.primary_insurance_co as primary_insurance_co_id, "
      + "	pr.secondary_insurance_co as secondary_insurance_co_id,"
      + "  tm.tpa_name AS primary_sponsor_name, stm.tpa_name as secondary_sponsor_name,"
      + "  pr.primary_sponsor_id, pr.secondary_sponsor_id,"
      + "  COALESCE(md.description, '') ||' ' || COALESCE(md.icd_code , '') AS diagnosis_icd, "
      + "  pr.codified_by, "
      + "  CASE WHEN pr.codified_by is NULL OR pr.codified_by='' THEN 'N' ELSE 'Y'"
      + "  END AS assigned, "
      + "  CASE WHEN pr.primary_sponsor_id is not null AND pr.primary_sponsor_id != '' THEN 'Y' ELSE 'N' END AS insurance_status,"
      + "  CASE WHEN pr.plan_id is not null AND pr.plan_id != 0 THEN 'Y' ELSE 'N' END AS insurance_plan_status, pr.center_id, "
      + "  pcat.category_name AS primary_network_type,pr.reg_date AS visit_date,pcat.category_id::text AS primary_category_id,"
      + "  scat.category_id::text AS secondary_category_id  "
      + " FROM patient_registration pr "
      + "  LEFT JOIN mrd_diagnosis md ON (md.visit_id = pr.patient_id AND diag_type = 'P') "
      + "  JOIN patient_details pd ON (pd.mr_no = pr.mr_no "
      + "  AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + "  JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + "  LEFT JOIN department dept ON (pr.dept_name=dept.dept_id) "
      + "  LEFT JOIN doctors dr ON (pr.doctor=dr.doctor_id) "
      + "  LEFT JOIN insurance_company_master icm ON (icm.insurance_co_id = pr.primary_insurance_co) "
      + "  LEFT JOIN insurance_company_master sicm ON (sicm.insurance_co_id = pr.secondary_insurance_co) "
      + "  LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
      + "  LEFT JOIN tpa_master stm ON (stm.tpa_id = pr.secondary_sponsor_id) "
      + "  LEFT JOIN patient_insurance_plans pip ON (pip.patient_id=pr.patient_id AND "
      + "  pip.insurance_co=pr.primary_insurance_co AND pip.priority=1) "
      + "  LEFT JOIN insurance_category_master pcat ON pcat.category_id=pip.plan_type_id "
      + "  LEFT JOIN patient_insurance_plans pips ON (pips.patient_id=pr.patient_id AND "
      + "  pips.insurance_co=pr.primary_insurance_co AND pips.priority=2) "
      + "  LEFT JOIN insurance_category_master scat ON scat.category_id=pips.plan_type_id "
      + " ) AS foo ";

  private static String ICD_PATIENT_COUNT = "SELECT count(patient_id) ";

  public static PagedList searchICDPatients(Map filterParams, Map<LISTING, Object> listingParams)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = null;
    try {
      qb = new SearchQueryBuilder(con, ICD_PATIENT_FIELDS, ICD_PATIENT_COUNT, ICD_PATIENT_TABLES,
          listingParams);

      qb.addFilterFromParamMap(filterParams);
      int centerId = RequestContext.getCenterId();
      if (centerId != 0)
        qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      qb.build();
      PagedList l = qb.getMappedPagedList();

      return l;

    } finally {
      qb.close();
      con.close();
    }
  }

  public static final String GET_USER_NAME_BY_FILTER = " SELECT emp_username " + " FROM u_user u "
      + " LEFT JOIN screen_rights sr ON(sr.role_id = u.role_id) "
      + " WHERE ((sr.rights = 'A' and sr.screen_id = 'icd_report') "
      + " OR u.role_id = 1 or u.role_id = 2) AND u.hosp_user = 'Y' ORDER BY u.emp_username ";

  public static List getUserNameByRights() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_USER_NAME_BY_FILTER);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /*
   * The following are used in various auto-complete patient searches: Given a string, searches
   * through patients, matching the name/MRNO/Phone and returns critical patient + active visit
   * details. We could have joined with patient_visit_details_ext_view, but that seems to take too
   * long, it is not optimized.
   */
  private static final String FIND_PATIENTS = " SELECT Distinct mr_no, patient_name, last_name, salutation_name, patient_full_name, age, dateofbirth, expected_dob, age_in, patient_gender, "
      + " coalesce(patient_phone, '') as patient_phone,patient_phone_country_code, status, death_date, government_identifier, abbreviation, confidentiality_grp_id "
      + " FROM all_mrnos_view ";

  private static final String FIND_CONTACTS = " SELECT CONCAT_WS(' ','contact',contact_id) as mr_no ,contact_id, patient_name, last_name, salutation, CONCAT_WS(' ',sm.salutation,patient_name,last_name) as patient_full_name,"
      + " patient_age as age, patient_dob as dateofbirth, patient_dob as expected_dob,patient_age_units as age_in, patient_gender,"
      + " coalesce(patient_contact, '') as patient_phone,patient_contact_country_code as patient_phone_country_code FROM contact_details cd ";

  /*
   * All patients (active/inactive/unvisited)
   */
  public static List findPatients(String findString, int limit, String visitType,
      Boolean showDuplicateMrNos, int centerId, int sampleCollectionCenterId,
      boolean showOtherCenterPatients, BasicDynaBean genprefs, Boolean scheduler) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (!visitType.equals("all")) {
      visitFilter = " AND visit_type = ? ";
      params.add(visitType);
    }
    // Refer: BUG 45807 for center filter
    String enableForceSelection = (String) genprefs.get("enable_force_selection_for_mrno_search");
    enableForceSelection = enableForceSelection == null ? "N" : enableForceSelection;

    // get a pre-registered patients and patients visited atleast ones in the logged center
    String centerFilter = "";
    String collectionCenterFilter = "";
    if (enableForceSelection.equals("Y")) {
      if (!showOtherCenterPatients && centerId != 0) {
        centerFilter = " AND (status = 'N' or mr_no in (select mr_no from patient_registration inner_pr where inner_pr.mr_no=mr_no "
            + "	AND center_id= ? #collection_center_id#)) ";
        params.add(centerId);
      }

      if (sampleCollectionCenterId != -1) {
        collectionCenterFilter = " AND collection_center_id= ? ";
        params.add(sampleCollectionCenterId);
      }
    }
    centerFilter = centerFilter.replace("#collection_center_id#", collectionCenterFilter);
    List patientList = findPatients(findString, visitFilter + centerFilter, limit, visitType,
        showDuplicateMrNos,params);
    if (scheduler) {
      patientList.addAll(findContacts(findString, limit));
    }
    return patientList;
  }

  // All active patients
  public static List findPatientsActive(String findString, int limit, String visitType,
      Boolean showDuplicateMrNos, int centerId, int sampleCollectionCenterId, Boolean scheduler) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (!visitType.equals("all")) {
      visitFilter = " AND visit_type = ? ";
      params.add(visitType);
    }
    String centerFilter = "";
    if (centerId != 0) {
      centerFilter = " AND active_visit_center = ? " ;
      params.add(centerId);
    }
    if (sampleCollectionCenterId != -1) {
      centerFilter = " AND collection_center_id= ? ";
      params.add(sampleCollectionCenterId);
    }
    List patientList = findPatients(findString, " AND status='A' " + visitFilter + centerFilter, limit,
        visitType, showDuplicateMrNos, params);
    if (scheduler) {
      patientList.addAll(findContacts(findString, limit));
    }
    return patientList;
  }

  // All patients inactive (ie, with no active visits, may or may not have previous visit)
  public static List findPatientsInactive(String findString, int limit, String visitType,
      Boolean showDuplicateMrNos, int centerId, int sampleCollectionCenterId,
      boolean showOtherCenterPatients, BasicDynaBean genprefs) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (!visitType.equals("all")) {
      visitFilter = " AND visit_type = ? ";
      params.add(visitType);
    }

    // Refer: BUG 45807 for center filter
    String enableForceSelection = (String) genprefs.get("enable_force_selection_for_mrno_search");
    enableForceSelection = enableForceSelection == null ? "N" : enableForceSelection;

    // get a pre-registered patients and patients visited atleast ones in the logged center
    String centerFilter = "";
    String collectionCenterFilter = "";
    if (enableForceSelection.equals("Y")) {
      if (!showOtherCenterPatients && centerId != 0) {
        centerFilter = " AND (mr_no in (select mr_no from patient_registration inner_pr where inner_pr.mr_no=mr_no "
            + "	AND center_id= ? #collection_center_id#)) ";
        params.add(centerId);
      }

      if (sampleCollectionCenterId != -1) {
        collectionCenterFilter = " AND collection_center_id= ?" ;
        params.add(sampleCollectionCenterId);
      }
    }
    centerFilter = centerFilter.replace("#collection_center_id#", collectionCenterFilter);
    return findPatients(findString, " AND ((status='I' " + centerFilter
        + ") OR status='N') AND (death_date is null) " + visitFilter, limit, visitType,
        showDuplicateMrNos, params);
  }

  public static List findPatients(String findString, String extraFilter, int limit,
      String visitType, Boolean showDuplicateMrNos, List<Object> params) throws SQLException {
    if (limit <= 0)
      limit = 100;
    if (extraFilter == null)
      extraFilter = "";

    Connection con = null;
    PreparedStatement ps = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }

    // if the string contains a space, we consider it as a further filter on the part of the
    // string after the space.
    StringBuilder filterOn = new StringBuilder();
    String[] findStringComponents = findString.split(" ", 3);
    try {
      con = DataBaseUtil.getConnection(true);
      boolean first = true;
      for (String userInput : findStringComponents) {
        if (!first)
          filterOn.append(" AND ");
        // since phone number is allowing the special chars like (,),+,-and space we should check
        // for them also.
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          filterOn
              .append("(reverse(mr_no) LIKE ? OR patient_phone LIKE ?"
                  + " OR replace(patient_phone, CASE WHEN patient_phone_country_code IS NULL THEN '' ELSE patient_phone_country_code END,'')  LIKE ?"
                  + " OR government_identifier LIKE ?"
                  + " OR reverse(COALESCE(oldmrno,'')) LIKE ?)");
        } else if (userInput.matches("\\D+")) {
          filterOn.append("(lower(patient_name) LIKE ?" + " OR lower(middle_name) LIKE ?"
              + " OR lower(last_name) LIKE ?" + " OR lower(government_identifier) LIKE ?)");
        } else {
          filterOn.append("(reverse(mr_no) LIKE ?" + " OR lower(government_identifier) LIKE ?"
              + " OR reverse(COALESCE(oldmrno,'')) LIKE ?)");
        }
        first = false;
      }
      String duplicateMrNosStr = showDuplicateMrNos ? ""
          : " AND coalesce(original_mr_no, '') = '' ";
      String finalQuery = FIND_PATIENTS + " WHERE " + filterOn.toString() + extraFilter
          + duplicateMrNosStr + " LIMIT " + limit;
      ps = con.prepareStatement(finalQuery);
      int i = 1;
      for (String userInput : findStringComponents) {
        String reversedString = new StringBuilder(userInput).reverse().toString();
        String lowerString = userInput.toLowerCase();
        String reversedUpperString = reversedString.toUpperCase();
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          ps.setString(i++, reversedString + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, reversedString + "%");
        } else if (userInput.matches("\\D+")) {
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, lowerString + "%");
        } else {
          ps.setString(i++, reversedUpperString + "%");
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, reversedUpperString + "%");
        }
      }
      
      for (Object paramObj : params) {
        if (paramObj instanceof String) {
          ps.setString(i++, (String) paramObj);
        } else if (paramObj instanceof Integer) {
          ps.setInt(i++, (Integer) paramObj);
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static List findContacts(String findString, int limit)
      throws SQLException {
    if (limit <= 0)
      limit = 100;

    Connection con = null;
    PreparedStatement ps = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }

    // if the string contains a space, we consider it as a further filter on the part of the
    // string after the space.
    StringBuilder filterOn = new StringBuilder();
    String[] findStringComponents = findString.split(" ", 3);
    try {
      con = DataBaseUtil.getConnection(true);
      boolean first = true;
      for (String userInput : findStringComponents) {
        if (!first)
          filterOn.append(" AND ");
        // since phone number is allowing the special chars like (,),+,-and space we should check
        // for them also.
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          filterOn
              .append("("
                  + "patient_contact LIKE ? "
                  + "OR replace(patient_contact, "
                  + "CASE WHEN patient_contact_country_code IS NULL THEN '' ELSE patient_contact_country_code END,'')  LIKE ?"
                  + ")");
        } else if (userInput.matches("\\D+")) {
          filterOn.append("(lower(patient_name) LIKE ?" + " OR lower(middle_name) LIKE ?"
              + " OR lower(last_name) LIKE ?)");
        } else {
          return Collections.EMPTY_LIST;
        }
        first = false;
      }
      String salutationJoinClause = " LEFT JOIN salutation_master sm ON (sm.salutation_id = cd.salutation_name) ";
      String finalQuery = FIND_CONTACTS + salutationJoinClause + " WHERE " + filterOn.toString()
          + " LIMIT " + limit;
      ps = con.prepareStatement(finalQuery);
      int i = 1;
      for (String userInput : findStringComponents) {
        String lowerString = userInput.toLowerCase();
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          ps.setString(i++, userInput + "%");
          ps.setString(i++, userInput + "%");
        } else if (userInput.matches("\\D+")) {
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, lowerString + "%");
          ps.setString(i++, lowerString + "%");
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  public static String FIND_CONTACT_DETAILS = "select * from contact_details where contact_id = ?";
  
  public static BasicDynaBean getContactBean(int contactId) throws SQLException {
    
    return DataBaseUtil.queryToDynaBean(FIND_CONTACT_DETAILS,new Object[]{contactId});
  }
  
  public static String getMrNo(String findString, String visitType, String status,
      Boolean showDuplicateMrNos, int centerId, boolean showOtherCenterPatients,
      BasicDynaBean genprefs) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }
    try {
      String statusCheck = "";
      if (!status.equals(""))
        statusCheck = " AND status=" + (status.equals("active") ? "'A'" : "'I'");
      String duplicateMrNosStr = showDuplicateMrNos ? ""
          : " AND coalesce(original_mr_no, '') = '' ";
      con = DataBaseUtil.getConnection(true);

      // Refer: BUG 45807 for center filter
      String enableForceSelection = (String) genprefs.get("enable_force_selection_for_mrno_search");
      enableForceSelection = enableForceSelection == null ? "N" : enableForceSelection;

      String centerFilter = "";
      if (enableForceSelection.equals("Y")) {
        if (!showOtherCenterPatients && centerId != 0)
          centerFilter = "AND mr_no in (select mr_no from patient_registration inner_pr where inner_pr.mr_no=? "
              + "	AND center_id=?) ";
      }
      // when user searches with the government identifier, if multiple(patients with same
      // government identifier) records found
      // then picks up anyone among them and returns.
      String finalQuery = FIND_PATIENTS + " WHERE (mr_no=? or government_identifier=?) "
          + centerFilter + duplicateMrNosStr
          + (visitType.equals("all") ? "" : " AND visit_type=? " + statusCheck);
      ps = con.prepareStatement(finalQuery);
      int i = 1;
      ps.setString(i++, findString);
      ps.setString(i++, findString);
      if (enableForceSelection.equals("Y")) {
        if (!showOtherCenterPatients && centerId != 0) {
          ps.setString(i++, findString);
          ps.setInt(i++, centerId);
        }
      }

      if (!visitType.equals("all"))
        ps.setString(i++, visitType.toLowerCase());
      rs = ps.executeQuery();
      if (rs.next())
        return rs.getString("mr_no");

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  public static String getVisitId(String findString, String visitType, String status)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }
    try {
      String statusCheck = "";
      if (!status.equals("") && !status.equals("all"))
        statusCheck = " AND status=" + (status.equals("active") ? "'A'" : "'I'");
      int centerId = RequestContext.getCenterId();
      String centerCheck = "";
      if (centerId != 0)
        centerCheck = " AND center_id=?";

      String finalQuery = FIND_PATIENTS_VISITS
          + " WHERE visit_id IS NOT NULL AND (mr_no=? or government_identifier=?) "
          + (visitType.equals("all") ? "" : " AND visit_type=? ") + statusCheck + centerCheck;

      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(finalQuery);
      int i = 1;
      ps.setString(i++, findString);
      ps.setString(i++, findString);
      if (!visitType.equals("all"))
        ps.setString(i++, findString);
      if (centerId != 0)
        ps.setInt(i++, centerId);
      rs = ps.executeQuery();
      if (rs.next())
        return rs.getString("patient_id");

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /*
   * it searches for matching the patient id too. excludes the patients with no visits.
   */
  private static final String FIND_PATIENTS_VISITS = "SELECT mr_no, patient_id, visit_reg_date, patient_name, last_name, salutation_name, patient_full_name, age, dateofbirth, expected_dob, "
      + "	age_in, patient_gender, coalesce(patient_phone, '') as patient_phone,patient_phone_country_code, visit_id, previous_visit_id, "
      + "	doctor_name, dept_name, status, op_type, government_identifier "
      + "FROM visit_search_view ";

  // All patients with one or more visits
  public static List findAllPatientsWithVisits(String findString, int limit, String visitType,
      int centerId, int sampleCollectionCenterId) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (!visitType.equals("all")) {
      visitFilter = " AND visit_type = ?";
      params.add(visitType);
    }
    String centerWisePatients = "";
    if (centerId != 0) {
      centerWisePatients = " AND center_id = ?" ;
      params.add(centerId);
    }
    String collectionCenterWisePatients = "";
    if (sampleCollectionCenterId != -1) {
      collectionCenterWisePatients = " AND collection_center_id = ? ";
      params.add(sampleCollectionCenterId);
    }
    return findPatientVisits(findString, visitFilter + centerWisePatients
        + collectionCenterWisePatients, limit,params);
  }

  /*
   * This method use to get only active IP patient and both active and inactive OP patient when
   * active only check box is unchecked.
   */

  public static List findAllPatientsWithVisitsForSales(String findString, int limit,
      String visitType, int centerId, int sampleCollectionCenterId) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (visitType.equals("all"))
      visitFilter = " AND(visit_type ='o' OR (visit_type ='i' AND status ='A'))";
    String centerWisePatients = "";
    if (centerId != 0) {
      centerWisePatients = " AND center_id = ? ";
      params.add(centerId);
    }
    String collectionCenterWisePatients = "";
    if (sampleCollectionCenterId != -1) {
      collectionCenterWisePatients = " AND collection_center_id = ? " ;
      params.add(sampleCollectionCenterId);
    }
    return findPatientVisits(findString, visitFilter + centerWisePatients
        + collectionCenterWisePatients, limit,params);
  }

  // op Patient only
  public static List findOpPatientsWithVisitsForSales(String findString, int limit,
      String visitType, int centerId, int sampleCollectionCenterId) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (visitType.equals("all"))
      visitFilter = " AND(visit_type ='i' OR (visit_type ='o' AND status ='A'))";
    String centerWisePatients = "";
    if (centerId != 0) {
      centerWisePatients = " AND center_id = ? " ;
      params.add(centerId);
    }
    String collectionCenterWisePatients = "";
    if (sampleCollectionCenterId != -1) {
      collectionCenterWisePatients = " AND collection_center_id = ? " ;
      params.add(sampleCollectionCenterId);
    }
    return findPatientVisits(findString, visitFilter + centerWisePatients
        + collectionCenterWisePatients, limit,params);
  }

  // for both IP and OP
  public static List findBothPatientsWithVisitsForSales(String findString, int limit,
      String visitType, int centerId, int sampleCollectionCenterId) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    String visitFilter = "";
    if (visitType.equals("all"))
      visitFilter = " AND((visit_type ='o' AND status ='A') OR (visit_type ='i' AND status ='A'))";
    String centerWisePatients = "";
    if (centerId != 0) {
      centerWisePatients = " AND center_id = ?";
      params.add(centerId);
    }
    String collectionCenterWisePatients = "";
    if (sampleCollectionCenterId != -1) {
      collectionCenterWisePatients = " AND collection_center_id = ? ";
      params.add(sampleCollectionCenterId);
    }
    return findPatientVisits(findString, visitFilter + centerWisePatients
        + collectionCenterWisePatients, limit,params);
  }

  public static List findPatientsWithVisitsWithStatus(String findString, int limit, String visitType,
      int centerId, int sampleCollectionCenterId, String status) throws SQLException {
    List<Object> params = new ArrayList<Object>();
    params.add(status);
    String visitFilter = "";
    if (!visitType.equals("all")) {
      visitFilter = " AND visit_type = ? ";
      params.add(visitType);
    }
    String centerWisePatients = "";
    if (centerId != 0) {
      centerWisePatients = " AND center_id = ? ";
      params.add(centerId);
    }
    String collectionCenterWisePatients = "";
    if (sampleCollectionCenterId != -1) {
      collectionCenterWisePatients = " AND collection_center_id = ? ";
      params.add(sampleCollectionCenterId);
    }
    return findPatientVisits(findString, " AND status = ? " + visitFilter + centerWisePatients
        + collectionCenterWisePatients, limit,params);
  }


  public static List findPatientVisits(String findString, String extraFilter, int limit, List<Object> params)
      throws SQLException {
    if (limit <= 0)
      limit = 100;
    if (extraFilter == null)
      extraFilter = "";

    Connection con = null;
    PreparedStatement ps = null;

    if ((findString == null) || (findString.length() < 2)) {
      return null;
    }
    StringBuilder filterOn = new StringBuilder();
    String[] findStringComponents = findString.split(" ", 3);
    try {
      con = DataBaseUtil.getConnection(30);
      boolean first = true;

      for (String userInput : findStringComponents) {
        if (!first)
          filterOn.append(" AND ");

        // since phone number is allowing the special chars like (,),+,-and space we should check
        // for them also.
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          filterOn
              .append("(reverse(mr_no) LIKE ?"
                  + " OR patient_phone LIKE ?"
                  + " OR replace(patient_phone, CASE WHEN patient_phone_country_code IS NULL THEN '' ELSE patient_phone_country_code END,'') LIKE ?"
                  + " OR government_identifier LIKE ?"
                  + " OR reverse(COALESCE(oldmrno,'')) LIKE ?)");
        } else if (userInput.matches("\\D+")) {
          filterOn.append("(LOWER(patient_name) LIKE ?" + " OR LOWER(middle_name) LIKE ?"
              + " OR LOWER(last_name) LIKE ?" + " OR LOWER(patient_name) LIKE ?"
              + " OR LOWER(middle_name) LIKE ?" + " OR LOWER(last_name) LIKE ?"
              + " OR lower(government_identifier) LIKE ?)");
        } else {
          filterOn.append("(reverse(mr_no) LIKE ?" + " OR lower(government_identifier) LIKE ?"
              + " OR reverse(COALESCE(oldmrno,'')) LIKE ?)");
        }
        first = false;
      }

      String finalQuery = FIND_PATIENTS_VISITS + " WHERE " + filterOn.toString() + extraFilter
          + " ORDER BY visit_reg_date desc LIMIT " + limit;
      ps = con.prepareStatement(finalQuery);
      int i = 1;
      for (String userInput : findStringComponents) {
        String reverseUserInput = new StringBuilder().append(userInput).reverse().toString();
        String lowerUserInput = userInput.toLowerCase();
        String reverseUpperUserInput = reverseUserInput.toUpperCase();
        if (userInput.matches("(\\d|\\+|-|\\(|\\)|\\s)*")) {
          ps.setString(i++, reverseUserInput + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, userInput + "%");
          ps.setString(i++, reverseUpperUserInput + "%");//
        } else if (userInput.matches("\\D+")) {
          ps.setString(i++, lowerUserInput + "%");
          ps.setString(i++, lowerUserInput + "%");
          ps.setString(i++, lowerUserInput + "%");
          ps.setString(i++, "% " + lowerUserInput + "%");
          ps.setString(i++, "% " + lowerUserInput + "%");
          ps.setString(i++, "% " + lowerUserInput + "%");
          ps.setString(i++, lowerUserInput + "%");
        } else {
          ps.setString(i++, reverseUpperUserInput + "%");
          ps.setString(i++, lowerUserInput + "%");
          ps.setString(i++, reverseUpperUserInput + "%");//
        }
      }
      
      for (Object paramObj : params) {
        if (paramObj instanceof String) {
          ps.setString(i++, (String) paramObj);
        } else if (paramObj instanceof Integer) {
          ps.setInt(i++, (Integer) paramObj);
        }
      }

      return DataBaseUtil.queryToDynaList(ps);
    } catch (SQLException e) {
      throw e;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_DUPLICATE_MR_NOS = "SELECT mr_no FROM patient_details WHERE original_mr_no = ?";

  public static String[] getDuplicateMrNos(String originalMrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String[] dupMrNos = null;
    List<String> duMrnosList = new ArrayList<String>();
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DUPLICATE_MR_NOS);
      ps.setString(1, originalMrNo);
      rs = ps.executeQuery();
      while (rs.next()) {
        duMrnosList.add(rs.getString("mr_no"));
      }
      dupMrNos = new String[duMrnosList.size()];
      for (int i = 0; i < duMrnosList.size(); i++) {
        dupMrNos[i] = duMrnosList.get(i);
      }
      return dupMrNos;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_DUPLICATE_PATIENT_DETAILS_FIELDS = "SELECT pd.mr_no, "
      + " get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name, "
      + " pd.patient_phone, pd.patient_gender, get_patient_age(pd.dateofbirth, pd.expected_dob) AS age ";

  private static final String GET_DUPLICATE_PATIENT_DETAILS_COUNT = "SELECT count(pd.mr_no)";

  private static final String GET_DUPLICATE_PATIENT_DETAILS_TABLE = " FROM patient_details pd "
      + " LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) ";

  public static PagedList getDuplicatePatientsDetails(String originalMrNo, Map listingParams)
      throws SQLException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String sortOrder = "pd.mr_no";
      Integer pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
      Integer pageNum = (Integer) listingParams.get(LISTING.PAGENUM);

      if (originalMrNo == null || originalMrNo.equals(""))
        return new PagedList(new ArrayList(), 0, pageSize, pageNum);

      qb = new SearchQueryBuilder(con, GET_DUPLICATE_PATIENT_DETAILS_FIELDS,
          GET_DUPLICATE_PATIENT_DETAILS_COUNT, GET_DUPLICATE_PATIENT_DETAILS_TABLE, null,
          sortOrder, false, pageSize, pageNum);
      qb.addFilter(SearchQueryBuilder.STRING, "pd.original_mr_no", "=", originalMrNo);

      qb.build();
      return qb.getMappedPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null)
        qb.close();
    }
  }

  private static final String INSURANCE_INS_CARD_IMAGE = " SELECT "
      + " pdc.doc_content_bytea, pdc.content_type, pdc.is_migrated, pdc.doc_id"
      + " FROM patient_insurance_plans pip"
      + " JOIN plan_docs_details pdd USING(patient_policy_id) "
      + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
      + " WHERE pip.patient_id=?  AND pip.patient_policy_id=? ";

  public static Map getCurrentPatientInsCardImageMap(String patientId, String sponsorType,
      int policyID) throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType == null || sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_INS_CARD_IMAGE);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(CORPORATE_CARD_IMAGE);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(NATIONAL_CARD_IMAGE);
      else
        return null;
      if (sponsorType == null || sponsorType.equals("I")) {
        ps.setString(1, patientId);
        ps.setInt(2, policyID);
      } else {
        ps.setString(1, patientId);
      }

      rs = ps.executeQuery();
      if (rs.next()) {
        Map cardMap = new HashMap();
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            cardMap.put("CONTENT", new ByteArrayInputStream(file));
          }
        }
        if (!cardMap.containsKey("CONTENT")) {
          cardMap.put("CONTENT", rs.getBinaryStream(1));
        }
        cardMap.put("CONTENT_TYPE", rs.getString(2));
        return cardMap;
      } else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  public static Map getCurrentPatientInsSecCardImageMap(String patientId, String sponsorType,
      int policyID) throws SQLException {
    if (patientId == null)
      return null;
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (sponsorType.equals("I"))
        ps = con.prepareStatement(INSURANCE_INS_CARD_IMAGE);
      else if (sponsorType.equals("C"))
        ps = con.prepareStatement(SEC_CORPORATE_CARD_IMAGE);
      else if (sponsorType.equals("N"))
        ps = con.prepareStatement(SEC_NATIONAL_CARD_IMAGE);
      else
        return null;
      if (sponsorType == null || sponsorType.equals("I")) {
        ps.setString(1, patientId);
        ps.setInt(2, policyID);
      } else {
        ps.setString(1, patientId);
      }
      rs = ps.executeQuery();
      if (rs.next()) {
        Map cardMap = new HashMap();
        if (sponsorType == null || sponsorType.equals("I")) {
          if (rs.getString(3).equals("1")) {
            byte[] file = awsS3Util.getDocument(rs.getInt(4));
            cardMap.put("CONTENT", new ByteArrayInputStream(file));
          }
        }
        if (!cardMap.containsKey("CONTENT")) {
          cardMap.put("CONTENT", rs.getBinaryStream(1));
        }
        cardMap.put("CONTENT_TYPE", rs.getString(2));
        return cardMap;
      } else
        return null;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private static final String GET_BABY_DETAILS_FOR_MRNO = " SELECT pd.dateofbirth,sm.salutation,adm.parent_id, pd.visit_id "
      + "	FROM patient_details pd "
      + "	LEFT JOIN salutation_master sm ON(pd.salutation = sm.salutation_id) "
      + "	JOIN admission adm ON (adm.mr_no = ?)"
      + "	WHERE (adm.parent_id is not null AND adm.parent_id != '') AND  pd.mr_no = ? ";

  public static BasicDynaBean getBabyDateOfBirtsAndSalutationDetails(String mrNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BABY_DETAILS_FOR_MRNO);
      ps.setString(1, mrNo);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  private static final String GET_BABY_DETAILS_FOR_PATIENTID = " SELECT pd.dateofbirth,sm.salutation,adm.parent_id, pd.visit_id "
      + "	FROM patient_details pd "
      + "	LEFT JOIN salutation_master sm ON(pd.salutation = sm.salutation_id) "
      + "	JOIN admission adm ON (adm.patient_id = ?)"
      + "	WHERE (adm.parent_id is not null AND adm.parent_id != '') AND  pd.mr_no = ? ";

  public static BasicDynaBean getBabyDateOfBirtsAndSalutationDetails(String mrNo, String patientId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BABY_DETAILS_FOR_PATIENTID);
      ps.setString(1, patientId);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  public static BasicDynaBean getBabyDateOfBirtsAndSalutationDetails(Connection con,
      String mrNo, String patientId)
      throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(GET_BABY_DETAILS_FOR_PATIENTID);
      ps.setString(1, patientId);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
  }
  
  private static final String GET_PATIENT_PHONE_DETAILS =
      " select mobile_password,mr_no from patient_details WHERE mr_no= ? ";

  public static BasicDynaBean getPatientPhoneDetails(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_PHONE_DETAILS);
      ps.setString(1, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String IS_UNIQUE_GOVT_ID =
      "SELECT government_identifier FROM patient_details pd"
      + " WHERE pd.government_identifier = ? "
      + "AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) ";

  public static boolean isUniqueGovtID(String govtID, String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      String iS_UNIQUE_GOVT_ID_STRING = IS_UNIQUE_GOVT_ID
          + ((mrNo != null && !mrNo.equals("")) ? " AND mr_no != ?" : "");
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(iS_UNIQUE_GOVT_ID_STRING);
      pstmt.setString(1, govtID);
      if (null != mrNo && !mrNo.equals(""))
        pstmt.setString(2, mrNo);
      return DataBaseUtil.queryToDynaBean(pstmt) == null;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  private static String PAT_REG_DATE_DETAILS = "select reg_date, reg_time,pd.expected_dob, dateofbirth "
      + " from patient_details pd "
      + " left join patient_registration pr on(pd.mr_no = pr.mr_no) "
      + " where pr.patient_id =?";

  public static Map getAgeAsPerRegistraionDate(String visitId, int reportId) throws SQLException,
      ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    Map ageMap = null;
    BasicDynaBean dateBean = null;
    Date reportDate = null;
    try {
      dateBean = DiagnosticsDAO.getSampleAndConductionDate(reportId);
      ps = con.prepareStatement(PAT_REG_DATE_DETAILS);
      ps.setString(1, visitId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      if (dateBean != null) {
        reportDate = (Date) dateBean.get("sample_date");
        if (reportDate == null || reportDate.equals(""))
          reportDate = (Date) dateBean.get("conducted_date");

        if (bean != null) {
          if (bean.get("dateofbirth") != null) {
            ageMap = DateUtil.getAgeBetweenDates((java.util.Date) bean.get("dateofbirth"),
                reportDate);
          } else if (bean.get("expected_dob") != null) {
            ageMap = DateUtil.getAgeBetweenDates((java.util.Date) bean.get("expected_dob"),
                reportDate);
          }
        }
      }
      return ageMap;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String UPDATE_CONTACT_LANG_PREFERENCE = "UPDATE contact_preferences"
  		+ " SET lang_code = ?, receive_communication=? WHERE mr_no = ?";
  
  private static final String ADD_CONTACT_LANG_PREFERENCE = "INSERT INTO contact_preferences"
  		+ " (mr_no , receive_communication, lang_code) VALUES (?,?,?)";
  private static final String GET_CONTACT_PREFERENCE = "SELECT lang_code FROM contact_preferences"
  		+ " WHERE mr_no = ?";
  
  private static final String GET_EXISTING_COMM_PREFERENCE = "SELECT receive_communication FROM "
  		+ "contact_preferences WHERE mr_no = ?";

  public static boolean updateContactPreference(String mrNo, String langCode) throws SQLException {
	  return updateContactPreference( mrNo,langCode,"B");
  }
  
  public static boolean updateContactPreference(String mrNo, String langCode, String communication)
		  throws SQLException {
    Connection connection = DataBaseUtil.getConnection();
    PreparedStatement selectStatement = null;
    PreparedStatement updateStatement = null;
    try {
      selectStatement = connection.prepareStatement(GET_CONTACT_PREFERENCE);
      selectStatement.setString(1, mrNo);
      BasicDynaBean existingLangCode = DataBaseUtil.queryToDynaBean(selectStatement);
      if (existingLangCode == null || existingLangCode.get("lang_code") == null
          || ((String) existingLangCode.get("lang_code")).isEmpty()) {
        updateStatement = connection.prepareStatement(ADD_CONTACT_LANG_PREFERENCE);
        updateStatement.setString(1, mrNo);
        updateStatement.setString(2, communication);
        updateStatement.setString(3, langCode);
        return updateStatement.executeUpdate() > 0;
      } else {
    	String existingCommPref = DataBaseUtil.getStringValueFromDb(GET_EXISTING_COMM_PREFERENCE,
    			mrNo);
        updateStatement = connection.prepareStatement(UPDATE_CONTACT_LANG_PREFERENCE);
        updateStatement.setString(1, langCode);
        updateStatement.setString(2, communication);
        updateStatement.setString(3, mrNo);
        if(!communication.equals("B") && !existingCommPref.equals(communication)) {
    		updateExistingMessagePrefs(existingCommPref,communication,mrNo );
    	}
        return updateStatement.executeUpdate() > 0;
      }

    } finally {
      DataBaseUtil.closeConnections(connection, updateStatement);
      DataBaseUtil.closeConnections(connection, selectStatement);
    }
  }
  
  private static final String UPDATE_MESSAGE_PREFS = "update patient_communication_preferences "
  		+ " set communication_type = ? where mr_no = ? ";
  
  private static final String UPDATE_MESSAGE_PREFS_WHERE = " AND communication_type = ? ";
  
	private static void updateExistingMessagePrefs(String existingCommPref, String communication, String mrNo) {
		Connection connection = DataBaseUtil.getConnection();
	    PreparedStatement updateStatement = null;
	    try {
	      if(communication.equals("N")) {
	    	  updateStatement = connection.prepareStatement(UPDATE_MESSAGE_PREFS);
	    	  updateStatement.setString(1, communication);
	    	  updateStatement.setString(2, mrNo);
		      updateStatement.executeUpdate();
	    	  
	      } else {
	    	  String query = UPDATE_MESSAGE_PREFS + UPDATE_MESSAGE_PREFS_WHERE;
	    	  String oppositeMode = communication.equals("S") ? "E" : "S";
	    	  updateStatement = connection.prepareStatement(query);
	    	  updateStatement.setString(1, communication);
	    	  updateStatement.setString(2, mrNo);
	    	  updateStatement.setString(3, "B");
		      updateStatement.executeUpdate();
		      
		      updateStatement.setString(1, "N");
	    	  updateStatement.setString(2, mrNo);
	    	  updateStatement.setString(3, oppositeMode);
		      updateStatement.executeUpdate();
	    	}
	    } catch (SQLException e) {
			e.printStackTrace();
		}
	    finally {
	        DataBaseUtil.closeConnections(connection, updateStatement);
	    }
}

private static final String GET_GENERIC_CONTACT_PREFERENCE = "SELECT contact_pref_lang_code FROM generic_preferences";

  public static final String getContactPreference(String mrNo) throws SQLException {
    Connection connection = DataBaseUtil.getConnection();
    PreparedStatement contactPreferencesStatement = null;
    PreparedStatement genericContactPreferenceStatement = null;
    try {
      contactPreferencesStatement = connection.prepareStatement(GET_CONTACT_PREFERENCE);
      contactPreferencesStatement.setString(1, mrNo);
      BasicDynaBean langCode = DataBaseUtil.queryToDynaBean(contactPreferencesStatement);

      if (langCode == null || langCode.get("lang_code") == null
          || ((String) langCode.get("lang_code")).isEmpty()) {
        genericContactPreferenceStatement = connection
            .prepareStatement(GET_GENERIC_CONTACT_PREFERENCE);
        langCode = DataBaseUtil.queryToDynaBean(genericContactPreferenceStatement);
        return (String) langCode.get("contact_pref_lang_code");
      } else {
        return (String) langCode.get("lang_code");
      }
    } finally {
      DataBaseUtil.closeConnections(connection, contactPreferencesStatement);
      DataBaseUtil.closeConnections(connection, genericContactPreferenceStatement);
    }
  }

  private static final String GET_DISTRICT_DETAILS = "SELECT dm.* FROM district_master dm "
      + " JOIN city ct ON (ct.district_id = dm.district_id AND ct.city_id = ?) "
      + " JOIN state_master sm ON (sm.state_id = dm.state_id AND sm.state_id = ?) ";

  public BasicDynaBean getDistrictDetails(String cityId, String stateId) throws SQLException {
    Connection con = null; // DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_DISTRICT_DETAILS);
      ps.setString(1, cityId);
      ps.setString(2, stateId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  /** The Constant FLUSH_CONTACT_FROM_SCHEDULER. */
  private static final String FLUSH_CONTACT_FROM_SCHEDULER = 
      "Update scheduler_appointments set mr_no = ?, contact_id = null where contact_id = ? ";
  
  /** The Constant DELETE_CONTACT_RECORD. */
  private static final String DELETE_CONTACT_RECORD = 
      "delete from contact_details where contact_id = ?";
  
  /**
   * Flush contact.
   *
   * @param mrNo the mr no
   * @param contactId the contact id
   */
  public static void flushContact(String mrNo, Integer contactId) {
    DatabaseHelper.update(FLUSH_CONTACT_FROM_SCHEDULER, new Object[] { mrNo,contactId });
    DatabaseHelper.update(DELETE_CONTACT_RECORD, new Object[] { contactId });
  }
  
  private static final String CHECK_IF_PATIENT_EXISTS = 
      "select * from patient_details where CASE WHEN (middle_name IS NULL OR middle_name = '')"
        + " THEN trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(last_name,'')))))"
        + " ELSE trim(CONCAT(trim(lower(COALESCE(patient_name,''))),' ',trim(lower(COALESCE(middle_name,''))),' ',trim(lower(COALESCE(last_name,''))))) "
        + " END = ?"
        + " AND (patient_phone = ? OR patient_phone2 = ?)";
  
  public static Boolean checkIfPatientExists(BasicDynaBean bean) {
    String query = CHECK_IF_PATIENT_EXISTS;
    String fullName = (String)bean.get("patient_name");
    String patientContact = (String)bean.get("patient_contact");
    BasicDynaBean contactBean = DatabaseHelper.queryToDynaBean(query, new Object[]{fullName.trim().toLowerCase(),patientContact,patientContact});
    if (contactBean != null) {
      return true;
    }
    return false;
  }
  
  public static String getCommunicationMode(String mrNo) throws SQLException {
	  String preference =  DataBaseUtil.getStringValueFromDb(GET_EXISTING_COMM_PREFERENCE, mrNo);
	  return preference!=null ? preference : "B";
	  }
  
  private static final String GET_MR_NO_FOR_VISIT = "SELECT mr_no from patient_registration where "
	  		+ " patient_id = ? ";
	  
  public static String getMrForVisit(String visitId) throws SQLException {
	  return DataBaseUtil.getStringValueFromDb(GET_MR_NO_FOR_VISIT, visitId);
	  }
  
  private static final String GET_MR_NO_FOR_APPOINTMENT = "SELECT mr_no from "
  		+ "scheduler_appointments where appointment_id = ? ";
  
  public static String getMrForAppointmentId(Integer appointmentId) throws SQLException {
	  return DataBaseUtil.getStringValueFromDb(GET_MR_NO_FOR_APPOINTMENT, appointmentId);
	}
  
}