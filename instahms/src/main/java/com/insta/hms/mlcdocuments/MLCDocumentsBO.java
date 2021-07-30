package com.insta.hms.mlcdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The Class MLCDocumentsBO.
 */
public class MLCDocumentsBO {

  /** The Constant GET__TEMPLATE_MLC_PAT_LIST. */
  private static final String GET__TEMPLATE_MLC_PAT_LIST = " select pr.mr_no, pr.patient_id,"
      + " pd.doc_id, pd.doc_format, pd.template_id,dv.doc_type, dv.specialized "
      + " from doc_all_templates_view dv "
      + " join patient_documents pd on pd.template_id = dv.template_id and"
      + " dv.doc_format = pd.doc_format join patient_registration  pr on (pr.doc_id = pd.doc_id) "
      + " where pr.mlc_status = 'Y' AND pr.patient_id=?";

  /**
   * Gets the MLC template pat list.
   *
   * @param patientId
   *          the patient id
   * @return the MLC template pat list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getMLCTemplatePatList(String patientId) throws SQLException {
    List<BasicDynaBean> beanList = null;
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET__TEMPLATE_MLC_PAT_LIST);
      ps.setString(1, patientId);
      beanList = DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
    return beanList;
  }

  /** The Constant UPDATE_PATIENT_REG. */
  private static final String UPDATE_PATIENT_REG = " UPDATE patient_registration SET"
      + "  doc_id = null,mlc_status = 'N',user_name=? "
      + " WHERE doc_id = ? AND mr_no = ? AND patient_id = ? ";

  /** The Constant DELETE_FROM_PATIENT_DOCS. */
  private static final String DELETE_FROM_PATIENT_DOCS = "DELETE FROM patient_documents"
      + " WHERE doc_id = ?";

  /** The Constant DLETE_FROM_HVF_DOCS. */
  private static final String DLETE_FROM_HVF_DOCS = " DELETE FROM patient_hvf_doc_values"
      + " WHERE doc_id = ?";

  /** The Constant DOC_ID_QUERY. **/
  private static final String DOC_ID_QUERY = "select doc_id from patient_registration where mr_no=?"
      + " AND patient_id = ?"; 
      
  /** The Constant TEMPLATE_ID_QUERY. **/
  private static final String TEMPLATE_ID_QUERY = "select template_id from patient_documents"
      + " where doc_id =?";

  /** The Constant DOC_FORMAT_QUERY. **/
  private static final String DOC_FORMAT_QUERY = "SELECT doc_format from doc_all_templates_view"
      + "  where template_id = ?";

  /**
   * Delete MLC details.
   *
   * @param con
   *          the con
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean deleteMLCDetails(Connection con, String mrNo, String visitId,
      String userName) throws SQLException {
    boolean deleteSuccess = true;
    int mlcDocId = DataBaseUtil.getIntValueFromDb(DOC_ID_QUERY, new Object[]{mrNo, visitId});
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PATIENT_REG)) {
      ps.setString(1, userName);
      ps.setInt(2, mlcDocId);
      ps.setString(3, mrNo);
      ps.setString(4, visitId);
      deleteSuccess = ps.executeUpdate() != 0;
    }
    if (deleteSuccess) {
      try (PreparedStatement ps = con.prepareStatement(DELETE_FROM_PATIENT_DOCS)) {
        ps.setInt(1, mlcDocId);
        deleteSuccess = ps.executeUpdate() != 0;
      }
    }

    int mlcTemplateId = DataBaseUtil.getIntValueFromDb(TEMPLATE_ID_QUERY, new Object[]{mlcDocId});
    String docFormat = DataBaseUtil.getStringValueFromDb(DOC_FORMAT_QUERY, mlcTemplateId);
    if (deleteSuccess && ("doc_hvf_templates".equalsIgnoreCase(docFormat))) {
      try (PreparedStatement ps = con.prepareStatement(DLETE_FROM_HVF_DOCS)) {
        ps.setInt(1, mlcDocId);
        deleteSuccess = ps.executeUpdate() != 0;
      }
    }

    return deleteSuccess;
  }

  /** The mlcforms emr. */
  public static String MLCFORMS_EMR = " SELECT pr.doc_id, pr.patient_id, "
      + " CASE WHEN (dat.title = '' or dat.title is null) THEN dat.template_name"
      + "  ELSE dat.title END AS title,"
      + "   dat.doc_format,pr.reg_date,dat.access_rights FROM patient_registration pr"
      + " JOIN patient_documents pd on pd.doc_id = pr.doc_id"
      + " JOIN doc_all_templates_view dat USING (doc_format, template_id)";

  /**
   * Gets the MLC list for EMR.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the MLC list for EMR
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List<EMRDoc> getMLCListForEMR(String patientId, String mrNo, boolean allVisitsDocs)
      throws SQLException, ParseException {

    List<EMRDoc> docs = new ArrayList<EMRDoc>();
    List<BasicDynaBean> list = null;
    if (allVisitsDocs) {
      list = DataBaseUtil.queryToDynaList(MLCFORMS_EMR + " WHERE pr.mr_no=?", mrNo);
    } else {
      list = DataBaseUtil.queryToDynaList(MLCFORMS_EMR + " WHERE pr.patient_id=?", patientId);
    }

    BasicDynaBean printpref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");
    for (BasicDynaBean bean : list) {
      EMRDoc doc = new EMRDoc();

      doc.setPrinterId(printerId);
      doc.setVisitid((String) bean.get("patient_id"));
      doc.setProvider(EMRInterface.Provider.MLCFormProvider);
      String docId = bean.get("doc_id").toString();
      doc.setDocid(docId);
      doc.setTitle((String) bean.get("title"));
      doc.setDoctor("");
      doc.setVisitDate((Date) bean.get("reg_date"));
      String displayUrl = "/MLCDocuments/MLCDocumentPrint.do?_method=print&doc_id=" + docId
          + "&printerId=" + printerId;
      String docFormat = (String) bean.get("doc_format");
      if (docFormat.equals("doc_hvf_templates")) {
        displayUrl += "&allFields=Y";
      }
      doc.setDisplayUrl(displayUrl);
      doc.setAuthorized(true);
      doc.setAccessRights(bean.get("access_rights").toString());

      if (docFormat.equals("doc_rtf_templates")) {
        doc.setPdfSupported(false);
      } else {
        doc.setPdfSupported(true);
      }
      doc.setType("4");
      docs.add(doc);
    }
    return docs;
  }
}
