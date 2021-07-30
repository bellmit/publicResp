package com.insta.hms.dischargesummary;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.PatientReport.TemplateReportDAO;
import com.insta.hms.medicalrecorddepartment.MRDCaseFileIssueDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/** The Class DischargeSummaryDAOImpl. */
public class DischargeSummaryDAOImpl {

  /** The template DAO. */
  TemplateReportDAO templateDAO = new TemplateReportDAO();

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DischargeSummaryDAOImpl.class);

  /** The con. */
  Connection con = null;

  /** The Constant GET_DISCHARGE_DETAILS. */
  private static final String GET_DISCHARGE_DETAILS = " SELECT "
      + " pr.*, doc.doctor_name, pd.death_date, pd.death_time, pd.stillborn, "
      + " drm.reason as death_reason, pd.death_reason_id, rfr.reason as reason_for_referral, "
      + " doc.doctor_id,pd.dead_on_arrival,dtm.discharge_type, pd.cause_of_death_icdcode "
      + " FROM patient_registration pr "
      + " LEFT JOIN doctors doc ON (pr.discharge_doctor_id = doc.doctor_id) "
      + " JOIN patient_details pd ON (pr.mr_no = pd.mr_no) "
      + " LEFT JOIN reason_for_referral rfr ON(rfr.id = pr.reason_for_referral_id)"
      + " LEFT JOIN discharge_type_master dtm ON(dtm.discharge_type_id = pr.discharge_type_id)"
      + " LEFT JOIN death_reason_master drm ON (drm.reason_id=pd.death_reason_id)"
      + " WHERE patient_id=? AND ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the discharge details.
   *
   * @param patientId
   *          the patientId
   * @return the discharge details
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getDischargeDetails(String patientId) throws SQLException {
    List dischDetails = DataBaseUtil.queryToDynaList(GET_DISCHARGE_DETAILS, patientId);
    if (dischDetails.size() > 0) {
      return (BasicDynaBean) dischDetails.get(0);
    }
    return null;
  }

  /** The Constant GET_DOCUMENT_REPORT. */
  private static final String GET_DOCUMENT_REPORT =
      " SELECT " + " dfd.*, df.template_caption " + " FROM discharge_format_detail dfd "
          + " JOIN discharge_format df USING(format_id) " + " WHERE dfd.docid=?";

  /**
   * Gets the document report.
   *
   * @param docid
   *          the docid
   * @return the document report
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getDocumentReport(int docid) throws SQLException {
    List docReportList = DataBaseUtil.queryToDynaList(GET_DOCUMENT_REPORT, docid);
    if (docReportList.size() > 0) {
      return (BasicDynaBean) docReportList.get(0);
    }
    return null;
  }

  /** The Constant GET_DOC_FORM. */
  private static final String GET_DOC_FORM = " SELECT dh.*, fh.form_caption "
      + " FROM dis_header dh " + " JOIN form_header fh USING(form_id) WHERE docid=?";

  /**
   * Gets the doc form.
   *
   * @param docid
   *          the docid
   * @return the doc form
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getDocForm(int docid) throws SQLException {
    List docForm = DataBaseUtil.queryToDynaList(GET_DOC_FORM, docid);
    if (docForm.size() > 0) {
      return (BasicDynaBean) docForm.get(0);
    }
    return null;
  }

  /** The Constant GET_FORM_FIELD_VALUES. */
  private static final String GET_FORM_FIELD_VALUES =
      " SELECT " + " f.field_id, f.caption, dd.field_value, " + " f.no_of_lines, dh.username "
          + " FROM dis_detail dd " + " JOIN dis_header dh on (dh.docid=dd.doc_id) "
          + " JOIN fields f ON (f.field_id = dd.field_id) "
          + " WHERE dd.doc_id=? ORDER BY f.displayorder";

  /**
   * Gets the form fields values from database.
   *
   * @param docid
   *          the docid
   * @return the form fields values from database
   * @throws SQLException
   *           the SQL exception
   */
  public List getFormFieldsValuesFromDatabase(int docid) throws SQLException {
    List arrFieldValues = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      arrFieldValues = new ArrayList();
      ps = con.prepareStatement(GET_FORM_FIELD_VALUES);
      ps.setInt(1, docid);
      arrFieldValues = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return arrFieldValues;
  }

  /** The Constant DELETE_FORM_HEADER. */
  private static final String DELETE_FORM_HEADER = "DELETE FROM dis_header WHERE docid=?";

  /**
   * Delete form header.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteFormHeader(Connection con, int docid) throws SQLException {
    int rows = executeUpdate1(con, DELETE_FORM_HEADER, docid);
    return (rows > 0);
  }

  /** The Constant DELETE_FORM_FIELDS. */
  private static final String DELETE_FORM_FIELDS = "DELETE FROM dis_detail WHERE doc_id=?";

  /**
   * Delete form fields.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteFormFields(Connection con, int docid) throws SQLException {
    int rows = executeUpdate1(con, DELETE_FORM_FIELDS, docid);
    return (rows > 0);
  }

  /** The Constant DELETE_HTML_REPORT. */
  private static final String DELETE_HTML_REPORT =
      "DELETE " + " FROM discharge_format_detail WHERE docid=?";

  /**
   * Delete html report.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteHtmlReport(Connection con, int docid) throws SQLException {
    int rows = executeUpdate1(con, DELETE_HTML_REPORT, docid);
    return (rows > 0);
  }

  /** The Constant DELETE_UPLOADED_FILE. */
  private static final String DELETE_UPLOADED_FILE =
      "DELETE " + " FROM discharge_fileupload WHERE docid=?";

  /**
   * Delete uploaded file.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteUploadedFile(Connection con, int docid) throws SQLException {
    int rows = executeUpdate1(con, DELETE_UPLOADED_FILE, docid);
    return (rows > 0);
  }

  /** The Constant UPDATE_DISCHARGE_DOCID. */
  private static final String UPDATE_DISCHARGE_DOCID = "UPDATE patient_registration "
      + " SET discharge_doc_id=?, " + " disch_date_for_disch_summary=null, "
      + " disch_time_for_disch_summary=null WHERE patient_id=?";

  /**
   * Update discharge docid.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @param newDocid
   *          the new docid
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updateDischargeDocid(Connection con, String patientId, int newDocid)
      throws SQLException {
    int rows = executeUpdate2(con, UPDATE_DISCHARGE_DOCID, newDocid, patientId);
    return (rows > 0);
  }

  /**
   * Execute update 1.
   *
   * @param con
   *          the con
   * @param query
   *          the query
   * @param val
   *          the val
   * @return the int
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * Probably belongs in GenericDAO
   */
  private int executeUpdate1(Connection con, String query, Object val) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      ps.setObject(1, val);
      return ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Execute update 2.
   *
   * @param con
   *          the con
   * @param query
   *          the query
   * @param val1
   *          the val 1
   * @param val2
   *          the val 2
   * @return the int
   * @throws SQLException
   *           the SQL exception
   */
  private int executeUpdate2(Connection con, String query, Object val1, Object val2)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      ps.setObject(1, val1);
      ps.setObject(2, val2);
      return ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /*
   * *****************
   */

  /** The Constant GET_PATIENT_DETAILS. */
  private static final String GET_PATIENT_DETAILS = "SELECT PR.MR_NO,PR.PATIENT_ID,"
      + " TO_CHAR(PR.REG_DATE,'DD-MM-YYYY') as REG_DATE,PR.REG_TIME, "
      + " PD.PATIENT_NAME, COALESCE(PD.DATEOFBIRTH, PD.EXPECTED_DOB) AS DOB, "
      + " CASE WHEN PD.PATIENT_GENDER='F' THEN 'FEMALE' "
      + " WHEN PD.PATIENT_GENDER='M' THEN 'MALE' END AS PATIENT_GENDER,"
      + " PR.VISIT_TYPE, (CASE WHEN PR.DISCHARGE_DOCTOR_ID IS NULL "
      + " THEN DOC.DOCTOR_ID ELSE PR.DISCHARGE_DOCTOR_ID END) AS DOCTOR_ID, "
      + " TO_CHAR(PR.DISCHARGE_DATE,'DD-MM-YYYY') as DISCHARGE_DATE,PR.DISCHARGE_TIME "
      + " FROM PATIENT_DETAILS PD ,PATIENT_REGISTRATION PR "
      + " LEFT OUTER JOIN DEPARTMENT DPT ON (PR.DEPT_NAME = DPT.DEPT_ID) "
      + " LEFT OUTER JOIN DOCTORS DOC ON (PR.DOCTOR = DOC.DOCTOR_ID) "
      + " WHERE PR.MR_NO=PD.MR_NO AND PR.PATIENT_ID=?";

  /**
   * Gets the discharge patient details.
   *
   * @param patientId
   *          the patientId
   * @return the discharge patient details
   * @throws ParseException
   *           the parse exception
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getDischargePatientDetails(String patientId)
      throws ParseException, SQLException {

    PreparedStatement ps = null;
    ArrayList arrDischargePatientDetails = new ArrayList();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PATIENT_DETAILS);
      ps.setString(1, patientId);
      arrDischargePatientDetails = DataBaseUtil.queryToArrayList(ps);
      for (Iterator i = arrDischargePatientDetails.iterator(); i.hasNext();) {
        Hashtable ht = (Hashtable) i.next();
        String dob = ht.get("DOB").toString();
        Map map = DateUtil.getAgeForDate(dob, "yyyy-MM-dd");
        ht.put("PATIENT_AGE", map.get("age").toString() + " " + map.get("ageIn").toString());
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return arrDischargePatientDetails;
  }

  /**
   * Save discharge summary.
   *
   * @param con
   *          the con
   * @param mapValuesFromDisForm
   *          the map values from dis form
   * @param docid
   *          the docid
   * @param formId
   *          the formId
   * @param mrno
   *          the mrno
   * @param patId
   *          the pat id
   * @param username
   *          the username
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String saveDischargeSummary(Connection con, Map mapValuesFromDisForm, int docid,
      String formId, String mrno, String patId, String username) throws SQLException {

    ResultSet rs = null;
    ResultSet rs2 = null;
    ResultSet rs3 = null;
    PreparedStatement ps3 = null;

    int intCheckSaveStatus = 0;
    int disUpdateHeader = 0;
    boolean flag = false;

    String msg = null;
    int headerResult = 0;
    boolean checkRSIfPresent = false;

    int disFieldValueUp = 0;
    int disFieldValueIns = 0;
    try {
      mapValuesFromDisForm.values();
      Set setFromMap = mapValuesFromDisForm.keySet();
      Iterator itrFromSet = setFromMap.iterator();
      itrFromSet.next();
      int mapsize = mapValuesFromDisForm.size();
      Object[] keyValuePairs2 = mapValuesFromDisForm.entrySet().toArray();

      if (docid == 0) {
        docid = DataBaseUtil.getIntValueFromDb("select nextval('dis_header_seq')");
        mapValuesFromDisForm.put("docid", docid);
        try (PreparedStatement ps = con.prepareStatement(
            "INSERT INTO DIS_HEADER(DOCID, " + " MR_NO, PATIENT_ID, FORM_ID,USERNAME, DIS_DATE) "
                + " VALUES (?, ?, ?, ?, ?, now()::date)")) {

          ps.setInt(1, docid);
          ps.setString(2, mrno);
          ps.setString(3, patId);
          ps.setString(4, formId);
          ps.setString(5, username);
          headerResult = ps.executeUpdate();
        }
      } else {
        try (PreparedStatement ps =
            con.prepareStatement("UPDATE dis_header SET username=? where docid=?")) {
          ps.setString(1, username);
          ps.setInt(2, docid);
          headerResult = ps.executeUpdate();
        }
      }

      rs2 = null;

      try (PreparedStatement ps2 =
          con.prepareStatement("SELECT field_id FROM dis_detail WHERE doc_id=?")) {
        ps2.setInt(1, docid);

        rs2 = ps2.executeQuery();
      }
      ArrayList fieldIdArray = new ArrayList();

      while (rs2.next()) {
        checkRSIfPresent = true;
        String strTemp = rs2.getString("FIELD_ID");
        fieldIdArray.add(strTemp);
      }

      if (checkRSIfPresent) {
        for (int i = 0; i < mapsize; i++) {

          String updateFormValues =
              "UPDATE DIS_DETAIL " + " SET FIELD_VALUE=? WHERE FIELD_ID= ? AND DOC_ID=?";
          String insertFormValues =
              "INSERT INTO DIS_DETAIL (DOCDETAIL_ID, " + " DOC_ID, FIELD_ID, FIELD_VALUE) "
                  + " values('D'||nextval('dis_detail_seq'),?,?,?)";

          Map.Entry entry = (Map.Entry) keyValuePairs2[i];
          String key = (String) entry.getKey();

          String strFld = key.substring(0, 3);

          Object value = entry.getValue();
          String[] values = (String[]) entry.getValue();

          if (strFld.contains("fld")) {
            if (fieldIdArray.contains(key)) {
              try (PreparedStatement ps1 = con.prepareStatement(updateFormValues)) {
                ps1.setString(1, values[0]);
                ps1.setString(2, key);
                ps1.setInt(3, docid);
                disFieldValueUp = ps1.executeUpdate();
                if (disFieldValueUp > 0) {
                  flag = true;
                }
              }
            } else {
              try (PreparedStatement ps1 = con.prepareStatement(insertFormValues)) {
                ps1.setInt(1, docid);
                ps1.setString(2, key);
                ps1.setString(3, values[0]);
                disFieldValueIns = ps1.executeUpdate();
                if (disFieldValueIns > 0) {
                  flag = true;
                }
              }
            }
          }
        }
      } else {

        for (int i = 0; i < mapsize; i++) {
          String insertFormValues =
              "insert into dis_detail (docdetail_id, " + " doc_id, field_id, field_value) "
                  + " values('D'||nextval('dis_detail_seq'),?,?,?)";

          Map.Entry entry = (Map.Entry) keyValuePairs2[i];
          String key = (String) entry.getKey();
          logger.debug("Adding key: " + key + " value: " + entry.getValue());

          String strFld = key.substring(0, 3);

          if (strFld.contains("fld")) {
            try (PreparedStatement ps1 = con.prepareStatement(insertFormValues)) {
              ps1.setInt(1, docid);
              ps1.setString(2, key);
              String[] values = (String[]) entry.getValue();
              ps1.setString(3, values[0]);

              disFieldValueIns = ps1.executeUpdate();
              if (disFieldValueIns > 0) {
                flag = true;
              }
            }
          }
        }
      }

      if (flag) {
        msg = "Discharge Summary Details are Saved";
      } else {
        msg = "Transaction Failure";
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (rs2 != null) {
        rs2.close();
      }
      if (rs3 != null) {
        rs3.close();
      }

      if (ps3 != null) {
        ps3.close();
      }
    }
    return msg;
  }

  /**
   * Gets the doc type.
   *
   * @return the doc type
   * @throws SQLException
   *           the SQL exception
   */
  public String getDocType() throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;
    String docType = "";
    try {
      con = DataBaseUtil.getConnection();
      pstmt = con.prepareStatement("SELECT doc_type_id,doc_type_name "
          + " FROM DOC_TYPE where Upper(doc_type_name) " + " like '%DISCHARGE%SUMMARY%'");

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {

          docType = rs.getString(1);
        }
      }
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return docType;
  }

  /**
   * Medical record details.
   *
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String medicalRecordDetails() throws SQLException {
    PreparedStatement ps = null;
    String medicalRecordDetailsContent = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT DISTINCT MR_NO,PATIENT_ID " + " FROM PATIENT_REGISTRATION  "
          + " WHERE CFLAG='0' AND STATUS='A' ORDER BY MR_NO ASC");
      medicalRecordDetailsContent = DataBaseUtil.getXmlContentWithNoChild(ps, "MEDICAL");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return medicalRecordDetailsContent;
  }

  /**
   * Fetch doc id.
   *
   * @param strMrno
   *          the str mrno
   * @param strPatientId
   *          the str patient id
   * @param formId
   *          the form id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String fetchDocId(String strMrno, String strPatientId, String formId) throws SQLException {

    Connection con = null;
    PreparedStatement ps1 = null;
    String docId = "";
    try {
      con = DataBaseUtil.getConnection();
      String query = "SELECT MF.DOCID, DH.FORM_ID " + " FROM MEDICAL_FILEUPLOAD MF "
          + " JOIN DIS_HEADER DH ON " + " MF.MR_NO=DH.MR_NO AND " + " MF.PATIENT_ID=DH.PATIENT_ID "
          + " AND DH.FORM_ID=? AND MF.MR_NO=? " + " AND MF.PATIENT_ID=? AND MF.DOC_TYPE=? "
          + " AND UPPER( MF.CONTENTFILENAME) LIKE '%DISCH_RPT%'";

      ps1 = con.prepareStatement(query);
      ps1.setString(1, formId);
      ps1.setString(2, strMrno);
      ps1.setString(3, strPatientId);
      ps1.setString(4, "6");
      try (ResultSet rs = ps1.executeQuery()) {
        if (rs.next()) {
          docId = rs.getString(1);
        }
      }
    } finally {
      if (ps1 != null) {
        ps1.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return docId;
  }

  /**
   * Fetch doc id for html.
   *
   * @param strMrno
   *          the str mrno
   * @param strPatientId
   *          the str patient id
   * @param templateId
   *          the template id
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String fetchDocIdForHtml(String strMrno, String strPatientId, String templateId)
      throws SQLException {

    Connection con = null;
    PreparedStatement ps1 = null;
    String docId = "";
    try {
      con = DataBaseUtil.getConnection();
      String query = "SELECT MF.DOCID, dfd.format_id " + " FROM MEDICAL_FILEUPLOAD MF "
          + " JOIN discharge_format_detail dfd ON "
          + " MF.MR_NO=dfd.MR_NO AND MF.PATIENT_ID=dfd.PATIENT_ID "
          + " AND dfd.format_id=? AND MF.MR_NO=? " + " AND MF.PATIENT_ID=? AND MF.DOC_TYPE=? "
          + " AND UPPER( MF.CONTENTFILENAME) LIKE '%DISCH_RPT%'";

      ps1 = con.prepareStatement(query);
      ps1.setString(1, templateId);
      ps1.setString(2, strMrno);
      ps1.setString(3, strPatientId);
      ps1.setString(4, "6");
      try (ResultSet rs = ps1.executeQuery()) {
        if (rs.next()) {
          docId = rs.getString(1);
        }
      }
    } finally {
      if (ps1 != null) {
        ps1.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return docId;
  }

  /**
   * Gets the form fields from database.
   *
   * @param formId
   *          the formId
   * @return the form fields from database
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getFormFieldsFromDatabase(String formId) throws SQLException {
    ArrayList al = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      al = new ArrayList();
      ps = con.prepareStatement(" SELECT FIELD_ID,CAPTION,NO_OF_LINES,DEFAULT_TEXT "
          + " FROM FIELDS " + " WHERE FORM_ID =? ORDER BY DISPLAYORDER  ");
      ps.setString(1, formId);
      al = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return al;
  }

  /**
   * Gets the form caption.
   *
   * @param templateType
   *          the template type
   * @return the form caption
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getFormCaption(String templateType) throws SQLException {
    ArrayList arrFormCaption = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      arrFormCaption = new ArrayList();
      ps = con.prepareStatement("SELECT FORM_ID AS FORM_ID,FORM_CAPTION AS FORM_CAPTION,"
          + "'FORM' AS DISPLAY_TYPE " + " FROM FORM_HEADER WHERE STATUS='A' AND FORM_TYPE=? "
          + " UNION " + " SELECT FORMAT_ID AS FORM_ID,TEMPLATE_CAPTION AS FORM_CAPTION,"
          + " 'HTML' AS DISPLAY_TYPE  FROM DISCHARGE_FORMAT "
          + " WHERE STATUS='A' AND TEMPLATE_TYPE=? ");
      ps.setString(1, templateType);
      ps.setString(2, templateType);
      arrFormCaption = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return arrFormCaption;
  }

  /**
   * Gets the status closed status.
   *
   * @param mrNo
   *          the mr no
   * @param patId
   *          the pat id
   * @return the status closed status
   * @throws SQLException
   *           the SQL exception
   */
  public String getStatusClosedStatus(String mrNo, String patId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String closeStatus = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("select count(*) " + " from bill " + " where bill_type!='X' "
          + " and discharge_status='N' and visit_id=?");
      ps.setString(1, patId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          closeStatus = String.valueOf(rs.getInt(1));
          if (closeStatus.equalsIgnoreCase("0")) {
            closeStatus = "Y";
          } else {
            closeStatus = "N";
          }
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return closeStatus;
  }

  /**
   * Save dis html.
   *
   * @param dto
   *          the dto
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean saveDisHtml(DischargeSummaryDTO dto, Connection con) throws SQLException {
    PreparedStatement ps = null;
    int resultCount = 0;
    String mrno = dto.getMrno();
    String patId = dto.getPatId();
    String templateId = dto.getTemplateId();
    try {
      if (dto.getDocid() != 0) {
        ps = con.prepareStatement(
            "UPDATE DISCHARGE_FORMAT_DETAIL " + " SET REPORT_FILE=?, username=? WHERE docid=?");
        ps.setString(1, dto.getTemplateContent());
        ps.setString(2, dto.getUserName());
        ps.setInt(3, dto.getDocid());
      } else {
        int docid = DataBaseUtil.getIntValueFromDb("select nextval('discharge_format_detail_seq')");
        dto.setDocid(docid);

        ps = con.prepareStatement("insert into DISCHARGE_FORMAT_DETAIL(mr_no, patient_id, "
            + " format_id, report_file, docid, pheader_template_id,username) "
            + " VALUES(?,?,?,?,?,?,?)");
        ps.setString(1, mrno);
        ps.setString(2, patId);
        ps.setString(3, templateId);
        ps.setString(4, dto.getTemplateContent());
        ps.setInt(5, docid);
        BasicDynaBean bean = templateDAO.getTemplateReport(templateId);
        ps.setObject(6, (Integer) bean.get("pheader_template_id"));
        ps.setString(7, dto.getUserName());
      }
      resultCount = ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return resultCount == 1; /* should update only one record */
  }

  /**
   * This method is used to get only template content of particular document id.
   *
   * @param docid
   *          the docid
   * @return the rich text document
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getRichTextDocument(int docid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT report_file, pheader_template_id, format_id, mr_no, "
          + " patient_id  FROM discharge_format_detail WHERE docid=?");
      ps.setInt(1, docid);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && !list.isEmpty()) {
        return (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /**
   * Ds exists.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @param format
   *          the format
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean dsExists(Connection con, int docid, String format) throws SQLException {
    GenericDAO dao = null;
    if (format.equals("F")) {
      dao = new GenericDAO("dis_header");
      return dao.findByKey(con, "docid", docid) != null;
    } else if (format.equals("T")) {
      dao = new GenericDAO("discharge_format_detail");
      return dao.findByKey(con, "docid", docid) != null;
    } else if (format.equals("P")) {
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
        ps = con.prepareStatement("SELECT doc_id FROM patient_documents WHERE doc_id=?");
        ps.setInt(1, docid);
        rs = ps.executeQuery();
        if (rs.next()) {
          return true;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps, rs);
      }
      return false;
    } else if (format.equals("U")) {
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
        ps = con.prepareStatement("SELECT docid FROM discharge_fileupload WHERE docid=?");
        ps.setInt(1, docid);
        rs = ps.executeQuery();
        if (rs.next()) {
          return true;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps, rs);
      }
      return false;
    }
    return false;
  }

  /** The Constant UPDATE_DISCHARGE_DETAILS_DIS. */
  private static final String UPDATE_DISCHARGE_DETAILS_DIS = " UPDATE patient_registration SET "
      + " discharge_doctor_id=?, discharge_format=?, " + " discharge_doc_id=?, user_name=?, "
      + " discharge_finalized_user=?, discharge_finalized_date=?," + " discharge_finalized_time=?, "
      + " disch_date_for_disch_summary=?, "
      + " disch_time_for_disch_summary=?, signatory_username=?" + " WHERE patient_id =?";

  /**
   * Save discharge details.
   *
   * @param dto
   *          the dto
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public boolean saveDischargeDetails(DischargeSummaryDTO dto, Connection con)
      throws SQLException, IOException, ParseException {

    PreparedStatement ps = null;
    int resultCount = 0;
    boolean mrdCaseStatus = false;
    try {
      // when multi user accessing the discharge summary,
      // one user is deleting the discharge summary and
      // one user2 is updating the discharge summary we need
      // to check for whether the discharge summary exists or not.
      // if the user1 deletes the DS the following condition
      // fails returns results with false.
      // and rollback the updation.
      if (dsExists(con, dto.getDocid(), dto.getFormat())) {
        ps = con.prepareStatement(UPDATE_DISCHARGE_DETAILS_DIS);

        int idx = 1;
        Time dischTimeForDischSummary = null;
        Date dischDateForDischSummary = null;

        if (!dto.getDisch_date_for_disch_summary().equals("")) {
          dischDateForDischSummary = DateUtil.parseDate(dto.getDisch_date_for_disch_summary());
          if (dto.getDisch_time_for_disch_summary().equals("")) {
            dischTimeForDischSummary = new java.sql.Time(new java.util.Date().getTime());
          } else {
            dischTimeForDischSummary = DateUtil.parseTime(dto.getDisch_time_for_disch_summary());
          }
        }

        ps.setString(idx++, dto.getDoctorId());
        ps.setString(idx++, dto.getFormat());
        ps.setInt(idx++, dto.getDocid());
        ps.setString(idx++, dto.getUserName());
        ps.setString(idx++, dto.getFinalizedUser());
        ps.setDate(idx++, dto.getFinalizedDate());
        ps.setTime(idx++, dto.getFinalizedTime());
        ps.setDate(idx++, dischDateForDischSummary);
        ps.setTime(idx++, dischTimeForDischSummary);
        ps.setString(idx++, dto.getSignatory_username());

        ps.setString(idx++, dto.getPatId());

        resultCount = ps.executeUpdate();
      }

      if (resultCount > 0) {
        mrdCaseStatus = MRDCaseFileIssueDAO.setMRDCaseFileStatus(con, dto.getMrno(),
            MRDCaseFileIssueDAO.MRD_CASE_FILE_STATUS_ON_DISCHARGE);
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return resultCount > 0 && mrdCaseStatus;
  }

  /**
   * Gets the template title for HTML print.
   *
   * @param templateId
   *          the template id
   * @return the template title for HTML print
   * @throws SQLException
   *           the SQL exception
   */
  public String getTemplateTitleForHTMLPrint(String templateId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String templateTitle = "";
    try {
      con = DataBaseUtil.getConnection();
      templateTitle = DataBaseUtil.getStringValueFromDb("select template_title "
          + " from discharge_format where format_id=?", templateId);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return templateTitle;
  }

  /**
   * Insert discharge upload files.
   *
   * @param dto
   *          the dto
   * @param con
   *          the con
   * @return the string
   * @throws SQLException
   *           the SQL exception
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public String insertDischargeUploadFiles(DischargeSummaryDTO dto, Connection con)
      throws SQLException, FileNotFoundException, IOException {

    String msg = "";
    PreparedStatement ps = null;
    int count = 0;
    boolean flag = false;

    try {

      FormFile formFile = dto.getTheFile();

      int docid = dto.getDocid();
      String fileName = formFile.getFileName();
      String contentType = MimeTypeDetector.getMimeTypes(formFile.getInputStream()).toString();
      String extension = "";
      if (fileName.contains(".")) {
        extension = fileName.substring(fileName.indexOf(".") + 1);

        if (extension.equals("odt") || extension.equals("ods")) {
          contentType = "application/vnd.oasis.opendocument.text";
        }
      }
      if (docid == 0) {
        docid = DataBaseUtil.getIntValueFromDb("SELECT NEXTVAL('discharge_fileupload_seq')");

        ps = con.prepareStatement("INSERT INTO discharge_fileupload (docid, patient_id, imagefile, "
            + " contenttype, original_extension, contentfilename) " + " VALUES (?, ?, ?, ?, ?, ?)");
        ps.setInt(1, docid);
        ps.setString(2, dto.getPatId());
        ps.setBinaryStream(3, formFile.getInputStream(), formFile.getFileSize());
        ps.setString(4, contentType);
        ps.setString(5, extension);
        ps.setString(6, formFile.getFileName());
        dto.setDocid(docid);

      } else {
        ps = con.prepareStatement("UPDATE discharge_fileupload SET imagefile=?, contenttype=?, "
            + " original_extension=?, contentfilename=? WHERE docid=?");
        ps.setBinaryStream(1, formFile.getInputStream(), formFile.getFileSize());
        ps.setString(2, contentType);
        ps.setString(3, extension);
        ps.setString(4, formFile.getFileName());
        ps.setInt(5, docid);
      }

      count = ps.executeUpdate();
      if (count > 0) {
        msg = "Successfully Uploaded Discharge Summary Files";
      } else {
        msg = "Transaction Failure";
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return msg;
  }

  /**
   * Gets the uploaded file details.
   *
   * @param docid
   *          the docid
   * @return the uploaded file details
   * @throws SQLException
   *           the SQL exception
   */
  public Map getUploadedFileDetails(int docid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List dischargeUploadList = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT PR.MR_NO, DF.PATIENT_ID, DF.CONTENTFILENAME,DF.DOCID "
          + " FROM DISCHARGE_FILEUPLOAD DF "
          + " JOIN PATIENT_REGISTRATION PR ON PR.PATIENT_ID=DF.PATIENT_ID " + " WHERE DF.DOCID=?");
      ps.setInt(1, docid);
      dischargeUploadList = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return ((BasicDynaBean) dischargeUploadList.get(0)).getMap();
  }

  /**
   * Gets the upload image result.
   *
   * @param docid
   *          the docid
   * @return the upload image result
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getUploadImageResult(int docid) throws SQLException {
    Connection con = null;
    ResultSet rs = null;
    PreparedStatement pstm = null;
    byte[] byteData = null;
    ArrayList alReturnValue = new ArrayList();
    try {
      con = DataBaseUtil.getConnection();
      pstm = con.prepareStatement("SELECT imagefile, docid, contenttype, contentfilename, "
          + " original_extension " + " FROM discharge_fileupload WHERE docid=?");
      pstm.setInt(1, docid);
      rs = pstm.executeQuery();
      if ((rs != null) && rs.next()) {
        byteData = rs.getBytes(1);
        alReturnValue.add(byteData);
        alReturnValue.add(rs.getObject(2));
        alReturnValue.add(rs.getObject(3));
        alReturnValue.add(rs.getObject(4));
        alReturnValue.add(rs.getObject(5));
      }
      rs.close();
      pstm.close();
    } finally {
      DataBaseUtil.closeConnections(con, pstm, rs);
    }
    return alReturnValue;
  }

  /**
   * Delete image.
   *
   * @param docId
   *          the doc id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteImage(int docId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int resultCount = 0;
    ResultSet rs = null;
    try {

      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("DELETE FROM DISCHARGE_FILEUPLOAD " + " WHERE DOCID=?");
      ps.setInt(1, docId);

      resultCount = ps.executeUpdate();

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return resultCount > 0;
  }

  // "FUD.FOLLOWUP_ID,TO_CHAR(FUD.FOLLOWUP_DATE,'DD-MM-YYYY')
  // AS FOLLOWUP_DATE,FUD.FOLLOWUP_DOCTOR_ID,FUD.FOLLOWUP_REMARKS

  /**
   * Gets the follow up details.
   *
   * @param patientId
   *          the patientId
   * @return the follow up details
   * @throws SQLException
   *           the SQL exception
   */
  public ArrayList getfollowUpDetails(String patientId) throws SQLException {

    PreparedStatement ps = null;
    ArrayList arrFollowUpDetails = new ArrayList();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT FUD.FOLLOWUP_ID,TO_CHAR(FOLLOWUP_DATE,'DD-MM-YYYY') "
          + " AS FOLLOWUP_DATE,FUD.FOLLOWUP_DOCTOR_ID,DOC.DOCTOR_NAME,"
          + " FUD.FOLLOWUP_REMARKS,DOC.DOCTOR_NAME " + " FROM FOLLOW_UP_DETAILS FUD "
          + " JOIN patient_registration pr ON(pr.patient_id = FUD.patient_id) "
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND"
          + " patient_confidentiality_check(pd.patient_group,pd.mr_no) ) "
          + " JOIN DOCTORS DOC ON DOC.DOCTOR_ID = FUD.FOLLOWUP_DOCTOR_ID "
          + " WHERE FUD.PATIENT_ID=? ORDER BY FUD.FOLLOWUP_DATE ASC");
      ps.setString(1, patientId);
      arrFollowUpDetails = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return arrFollowUpDetails;
  }

  /**
   * Save follow up details.
   *
   * @param dto
   *          the dto
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public boolean saveFollowUpDetails(DischargeSummaryDTO dto, Connection con)
      throws SQLException, ParseException {
    PreparedStatement ps = null;
    int resultCount = 0;
    ResultSet rs = null;

    boolean insertFlag = false;
    boolean updateFlag = false;
    boolean commitFlag = false;
    boolean insertCommitFlag = false;
    boolean deleteFlag = false;
    try {

      ps = con.prepareStatement("insert into follow_up_details values (?,?,?,?,?)");
      try (PreparedStatement ps1 =
          con.prepareStatement("update follow_up_details set followup_date=?,"
              + " followup_doctor_id=?,followup_remarks=? " + " where followup_id=?")) {
        if (dto.getFollowUpId() != null) {
          for (int j = 0; j < dto.getFollowUpId().length; j++) {

            String strFollowUpId = dto.getFollowUpId()[j];
            if (strFollowUpId.equalsIgnoreCase("GenerateNewFollowUpId")) {
              insertFlag = true;
              Date date = null;
              String followUpId =
                  AutoIncrementId.getSequenceId("follow_up_details_seq", "follow_up_details");
              if (dto.getFollowUpDate()[j] != null && !(dto.getFollowUpDate()[j].equals(""))) {
                SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy");
                date = new java.sql.Date(sf.parse(dto.getFollowUpDate()[j]).getTime());
              }

              ps.setString(1, followUpId);
              ps.setString(2, dto.getPatId());
              ps.setDate(3, date);
              ps.setString(4, dto.getFollowUpDoctorId()[j]);
              ps.setString(5, dto.getFollowUpRemarks()[j]);
              int resVal = ps.executeUpdate();

              if (resVal > 0) {
                insertCommitFlag = true;
              }

              if (!(insertCommitFlag)) {
                break;
              }

            } else {
              updateFlag = true;

              Date date = null;
              if (dto.getFollowUpDate()[j] != null && !(dto.getFollowUpDate()[j].equals(""))) {
                SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy");
                date = new java.sql.Date(sf.parse(dto.getFollowUpDate()[j]).getTime());
              }
              ps1.setDate(1, date);
              ps1.setString(2, dto.getFollowUpDoctorId()[j]);
              ps1.setString(3, dto.getFollowUpRemarks()[j]);
              ps1.setString(4, dto.getFollowUpId()[j]);
              ps1.addBatch();
            }
          }
        }

        try (PreparedStatement ps2 =
            con.prepareStatement("delete from follow_up_details " + " where followup_id=?")) {
          if (dto.getDeleteFollowUpIds() != null) {
            for (int i = 0; i < dto.getDeleteFollowUpIds().length; i++) {
              if (!(dto.getDeleteFollowUpIds()[i].equals(""))) {
                deleteFlag = true;
                ps2.setString(1, dto.getDeleteFollowUpIds()[i]);
                ps2.addBatch();
              }
            }
          }
          if (deleteFlag) {
            int[] result2 = ps2.executeBatch();
            for (int l = 0; l < result2.length; l++) {

              if (result2[l] > 0) {
                commitFlag = true;
              } else {
                commitFlag = false;
              }
            }
          }
        }
        if (insertFlag) {
          if (insertCommitFlag) {
            commitFlag = true;
          } else {
            commitFlag = false;
          }
        }
        if (updateFlag) {
          int[] result1 = ps1.executeBatch();
          for (int k = 0; k < result1.length; k++) {

            if (result1[k] > 0) {
              commitFlag = true;
            } else {
              commitFlag = false;
            }
          }
        }
      }

      return commitFlag;

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant GET_PATIENT_DOCUMENTS_MRNO. */
  /*
   * because of the performance issue we joined the patient registration directly with the
   * individuals document table. before it was getting all the documents and joining with the
   * patient registration. because of huge volume of documents in patient documents it was taking
   * time (to retrive all the documents and then join with patient_registration).
   */
  private static final String GET_PATIENT_DOCUMENTS_MRNO =
      " SELECT " + " 'U' as format, docid, contentfilename as name, "
          + " contenttype as content_type, null AS access_rights, null AS username, "
          + " pdis.discharge_finalized_user, pdis.discharge_date, "
          + " pdis.discharge_format, pdis.discharge_doc_id,  pdis.patient_id, "
          + " doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN discharge_fileupload  df ON (pdis.discharge_doc_id=df.docid "
          + " AND pdis.discharge_format='U') "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.mr_no=? " + " UNION ALL "
          + " SELECT 'F', docid, form_caption, '' as content_type, "
          + " access_rights, username,  pdis.discharge_finalized_user, "
          + " pdis.discharge_date, pdis.discharge_format, pdis.discharge_doc_id,"
          + " pdis.patient_id, doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN dis_header dh ON (pdis.discharge_doc_id=dh.docid AND pdis.discharge_format='F') "
          + " JOIN form_header fh using (form_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.mr_no=? " + " UNION ALL "
          + " SELECT 'T', docid, case when template_title is null or template_title ='' "
          + " then template_caption else template_title end, "
          + " '' as content_type, access_rights, username, pdis.discharge_finalized_user, "
          + " pdis.discharge_date, pdis.discharge_format, "
          + " pdis.discharge_doc_id,  pdis.patient_id, doc.doctor_name,pdis.reg_date,"
          + " pdis.disch_date_for_disch_summary " + " FROM patient_registration pdis "
          + " JOIN discharge_format_detail dfd ON (pdis.discharge_doc_id=dfd.docid  "
          + " AND pdis.discharge_format='T') " + " JOIN discharge_format df using(format_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.mr_no=? " + " UNION ALL "
          + " SELECT 'P', pd.doc_id, template_name, '' as content_type, access_rights, "
          + " null AS username, pdis.discharge_finalized_user, pdis.discharge_date, "
          + " pdis.discharge_format, pdis.discharge_doc_id,  pdis.patient_id, "
          + " doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN patient_documents pd  ON (pdis.discharge_doc_id=pd.doc_id "
          + " AND pdis.discharge_format='P') " + " JOIN doc_pdf_form_templates USING (template_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.mr_no=?";

  /** The Constant GET_PATIENT_DOCUMENTS_PATIENT_ID. */
  private static final String GET_PATIENT_DOCUMENTS_PATIENT_ID =
      " SELECT " + " 'U' as format, docid, contentfilename as name, "
          + " contenttype as content_type, null AS access_rights, "
          + " null AS username, pdis.discharge_finalized_user, "
          + " pdis.discharge_date, pdis.discharge_format, pdis.discharge_doc_id,  "
          + " pdis.patient_id, doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN discharge_fileupload  df ON (pdis.discharge_doc_id=df.docid "
          + " AND pdis.discharge_format='U') "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.patient_id=? " + " UNION ALL " + " SELECT 'F', docid, form_caption, "
          + " '' as content_type, access_rights, " + " username, pdis.discharge_finalized_user, "
          + " pdis.discharge_date, pdis.discharge_format, pdis.discharge_doc_id, "
          + " pdis.patient_id, doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN dis_header dh ON (pdis.discharge_doc_id=dh.docid AND pdis.discharge_format='F') "
          + " JOIN form_header fh using (form_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.patient_id=? " + " UNION ALL "
          + " SELECT 'T', docid, case when template_title is null or template_title ='' "
          + " then template_caption else template_title end, "
          + " '' as content_type, access_rights, username, pdis.discharge_finalized_user, "
          + " pdis.discharge_date, pdis.discharge_format, "
          + " pdis.discharge_doc_id,  pdis.patient_id, doc.doctor_name,pdis.reg_date, "
          + " pdis.disch_date_for_disch_summary " + " FROM patient_registration pdis "
          + " JOIN discharge_format_detail dfd ON (pdis.discharge_doc_id=dfd.docid "
          + " AND pdis.discharge_format='T') " + " JOIN discharge_format df using(format_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.patient_id=? " + " UNION ALL "
          + " SELECT 'P', pd.doc_id, template_name, '' as content_type, "
          + " access_rights, null AS username, "
          + " pdis.discharge_finalized_user, pdis.discharge_date, "
          + " pdis.discharge_format, pdis.discharge_doc_id,  pdis.patient_id, "
          + " doc.doctor_name,pdis.reg_date,pdis.disch_date_for_disch_summary "
          + " FROM patient_registration pdis "
          + " JOIN patient_documents pd  ON (pdis.discharge_doc_id=pd.doc_id "
          + " and pdis.discharge_format='P') " + " JOIN doc_pdf_form_templates USING (template_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id = pdis.discharge_doctor_id) "
          + " WHERE pdis.patient_id=?";

  /**
   * Gets the all visit docs.
   *
   * @param visitId
   *          the visit id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the all visit docs
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List<BasicDynaBean> getAllVisitDocs(String visitId, String mrNo,
      boolean allVisitsDocs) throws SQLException, ParseException {
    if (allVisitsDocs) {
      return DataBaseUtil.queryToDynaList(GET_PATIENT_DOCUMENTS_MRNO,
          new String[] { mrNo, mrNo, mrNo, mrNo });
    } else {
      return DataBaseUtil.queryToDynaList(GET_PATIENT_DOCUMENTS_PATIENT_ID,
          new String[] { visitId, visitId, visitId, visitId });
    }
  }

  /** The Constant FOLLOWUP_DISCHARGE_REPORT_QUERY. */
  private static final String FOLLOWUP_DISCHARGE_REPORT_QUERY =
      "SELECT " + " dh.patient_id, FUD.FOLLOWUP_ID, "
          + " TO_CHAR(FOLLOWUP_DATE,'DD-MM-YYYY') AS FOLLOWUP_DATE, " + " FUD.FOLLOWUP_DOCTOR_ID, "
          + " DOC.DOCTOR_NAME as followup_doctorname, FUD.FOLLOWUP_REMARKS, "
          + " COALESCE(fh.form_title,fh.form_caption) as form_title, "
          + " doc1.doctor_name as discharge_doctor, "
          + " doc1.specialization as discharge_doctor_specialization "
          + " FROM dis_header dh left outer join FOLLOW_UP_DETAILS FUD  "
          + " on (dh.patient_id = fud.patient_id) "
          + " left outer JOIN DOCTORS DOC ON DOC.doctor_id = FUD.FOLLOWUP_DOCTOR_ID "
          + " left outer join form_header fh on (dh.form_id = fh.form_id) "
          + " left outer join patient_registration dis " + " on (dh.patient_id = dis.patient_id) "
          + " left outer join doctors doc1 " + " on (dis.DISCHARGE_DOCTOR_ID = doc1.doctor_id) "
          + " WHERE dh.PATIENT_ID=? and dh.docid = ? " + " ORDER BY FUD.FOLLOWUP_DATE ASC";

  /**
   * Gets the followup and discharge details.
   *
   * @param patientId
   *          the patient id
   * @param docId
   *          the doc id
   * @return the followup and discharge details
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getFollowupAndDischargeDetails(String patientId, int docId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(FOLLOWUP_DISCHARGE_REPORT_QUERY);
      ps.setString(1, patientId);
      ps.setInt(2, docId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant HVF_TEMPLATE_TITLE. */
  private static final String HVF_TEMPLATE_TITLE = "select "
      + " case when (fh.form_title is null or fh.form_title = '') " + " then fh.form_caption else "
      + " form_title end as form_title " + " from form_header fh "
      + " join dis_header dh on dh.form_id=fh.form_id " + " where dh.docid=? and dh.patient_id=?";

  /**
   * Gets the HVF template title.
   *
   * @param docId
   *          the doc id
   * @param patientId
   *          the patient id
   * @return the HVF template title
   * @throws SQLException
   *           the SQL exception
   */
  public static String getHVFTemplateTitle(int docId, String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String title = "";
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(HVF_TEMPLATE_TITLE);
      ps.setInt(1, docId);
      ps.setString(2, patientId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          title = rs.getString("form_title");
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return title;
  }

  /** The Constant DISCHARGE_DOC_ID. */
  private static final String DISCHARGE_DOC_ID =
      "select pd.discharge_doc_id " + " from patient_registration pd " + " where pd.patient_id=?";

  /**
   * Gets the doc id.
   *
   * @param patientId
   *          the patient id
   * @return the doc id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getDocId(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int docId = 0;
    try {
      ps = con.prepareStatement(DISCHARGE_DOC_ID);
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        docId = rs.getInt("discharge_doc_id");
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return docId;
  }

  /** The Constant DISCHARGED_PATIENT_ID. */
  private static final String DISCHARGED_PATIENT_ID = "SELECT patient_id "
      + " FROM patient_registration WHERE " + " discharge_doc_id=? and discharge_format=?";

  /**
   * Gets the patient id.
   *
   * @param docid
   *          the docid
   * @param format
   *          the format
   * @return the patient id
   * @throws SQLException
   *           the SQL exception
   */
  public static String getPatientId(int docid, DischargeSummaryReportHelper.FormatType format)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String patientId = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(DISCHARGED_PATIENT_ID);
      ps.setInt(1, docid);
      ps.setString(2, format.getFormat());
      rs = ps.executeQuery();
      if (rs.next()) {
        patientId = rs.getString("patient_id");
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return patientId;
  }

  /**
   * Gets the uploaded file bytes.
   *
   * @param docid
   *          the docid
   * @return the uploaded file bytes
   * @throws SQLException
   *           the SQL exception
   */
  public static byte[] getUploadedFileBytes(Integer docid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT imagefile FROM discharge_fileupload " + " WHERE docid=?");
      ps.setInt(1, docid);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getBytes("imagefile");
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return null;
  }

  /** The Constant SEARCH_VISITS_FIELDS. */
  private static final String SEARCH_VISITS_FIELDS = "SELECT *";

  /** The Constant SEARCH_VISITS_COUNT. */
  private static final String SEARCH_VISITS_COUNT = "SELECT count(*)";

  /** The Constant SEARCH_VISITS_TABLES. */
  private static final String SEARCH_VISITS_TABLES = " FROM patient_visit_details_ext_view";

  /**
   * Search visits.
   *
   * @param filter
   *          the filter
   * @param listing
   *          the listing
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static PagedList searchVisits(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    SearchQueryBuilder qb = new SearchQueryBuilder(con, SEARCH_VISITS_FIELDS, SEARCH_VISITS_COUNT,
        SEARCH_VISITS_TABLES, listing);

    qb.addFilterFromParamMap(filter);
    Object[] finalized = (Object[]) filter.get("exclude_in_qb_finalized");

    if (finalized != null && finalized[0] != null) {
      String disDocStatus = (String) finalized[0];
      if (!disDocStatus.equals("")) {
        qb.addFilter(SearchQueryBuilder.STRING, "discharge_doc_status", "in",
            Arrays.asList(finalized));
      }
    }
    qb.addSecondarySort("patient_id");
    qb.build();

    PagedList visitList = qb.getMappedPagedList();

    qb.close();
    con.close();

    return visitList;
  }
}
