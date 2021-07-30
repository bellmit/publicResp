/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientDocumentsDAO.
 *
 * @author krishna.t
 */
public class PatientDocumentsDAO extends GenericDAO {

  /**
   * Instantiates a new patient documents DAO.
   */
  public PatientDocumentsDAO() {
    super("patient_documents");
  }

  /** The Constant INSERT_PDF_FORM_FIELD_VALUES. */
  private static final String INSERT_PDF_FORM_FIELD_VALUES =
      "insert into " + "patient_pdf_form_doc_values(doc_id, field_name, field_value, username)"
          + " values(?, ?, ?, ?)";

  /**
   * Insert PDF form field values.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param docId
   *          the doc id
   * @param username
   *          the username
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static boolean insertPDFFormFieldValues(Connection con, Map params, Object docId,
      String username) throws SQLException, IOException {

    Map allFields = ConversionUtils.flatten(params);
    allFields.remove("mr_no");
    allFields.remove("patient_id");
    allFields.remove("pat_name");
    allFields.remove("dept_name");
    allFields.remove("method");
    allFields.remove("doc_date");
    allFields.remove("template_id");
    allFields.remove("doc_name");
    allFields.remove("format");
    allFields.remove("doc_id");

    try (PreparedStatement ps = con.prepareStatement(INSERT_PDF_FORM_FIELD_VALUES)) {
      for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {

        if (((String) e.getKey()).startsWith("_")) {
          // _ names are supposed to be temp variables and skipped.
          continue;
        }
        ps.setObject(1, docId);
        ps.setString(2, (String) e.getKey());
        Object object = e.getValue();
        ps.setString(3, (String) e.getValue());
        ps.setString(4, username);

        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      return DataBaseUtil.checkBatchUpdates(results);
    }
  }

  /** The Constant UPDATE_PDF_FORM_FIELD_VALUES. */
  private static final String UPDATE_PDF_FORM_FIELD_VALUES =
      "update patient_pdf_form_doc_values set " + "field_value=? where doc_id=? and field_name=?";

  /** The Constant GET_PDF_FORM_FIELD_VALUES. */
  private static final String GET_PDF_FORM_FIELD_VALUES =
      "select field_name from patient_pdf_form_doc_values where doc_id = ?";

  /**
   * Update PDF form field values.
   *
   * @param con
   *          the con
   * @param params
   *          the params
   * @param docId
   *          the doc id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static boolean updatePDFFormFieldValues(Connection con, Map params, Object docId)
      throws SQLException, IOException {

    PreparedStatement pstmt = con.prepareStatement(GET_PDF_FORM_FIELD_VALUES);
    pstmt.setObject(1, docId);

    Map allFields = ConversionUtils.flatten(params);
    allFields.remove("mr_no");
    allFields.remove("patient_id");
    allFields.remove("pat_name");
    allFields.remove("dept_name");
    allFields.remove("method");
    allFields.remove("doc_date");
    allFields.remove("template_id");
    allFields.remove("doc_name");
    allFields.remove("format");
    allFields.remove("doc_id");

    Integer templateId =
        (params.get("template_id") != null && !((String[]) params.get("template_id"))[0].equals(""))
            ? Integer.parseInt(((String[]) params.get("template_id"))[0]) : null;
    List<BasicDynaBean> imageTemplateFieldvalues =
        PatientPDFDocValuesDAO.getPDFTemplateImageValues(templateId);
    String userName =
        params.get("user_name") == null ? "" : ((String[]) params.get("user_name"))[0];
    userName = userName == null ? "" : userName;

    if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {
      for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
        allFields.remove("field_id" + "_" + i);
        allFields.remove("field_input" + "_" + i);
        allFields.remove("device_ip" + "_" + i);
        allFields.remove("device_info" + "_" + i);
        allFields.remove("fieldImgText" + "_" + i);
      }
    }

    try (PreparedStatement psInsert = con.prepareStatement(INSERT_PDF_FORM_FIELD_VALUES);
        PreparedStatement psUpdate = con.prepareStatement(UPDATE_PDF_FORM_FIELD_VALUES);) {
      boolean exists = false;
      Map<String, Boolean> savedFields = new HashMap<String, Boolean>();
      List<BasicDynaBean> fieldList = DataBaseUtil.queryToDynaList(pstmt);
      if (fieldList.size() > 0) {
        for (int i = 0; i < fieldList.size(); i++) {
          BasicDynaBean bean = fieldList.get(i);
          savedFields.put((String) bean.get("field_name"), false);
        }
        for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
          if (((String) e.getKey()).startsWith("_")) {
            // _ names are supposed to be temp variables and skipped.
            continue;
          }
          for (int i = 0; i < fieldList.size(); i++) {
            BasicDynaBean bean = fieldList.get(i);

            if (bean.get("field_name").equals((String) e.getKey())) {
              psUpdate.setString(1, (String) e.getValue());
              psUpdate.setObject(2, docId);
              psUpdate.setString(3, (String) e.getKey());
              psUpdate.addBatch();
              savedFields.put((String) bean.get("field_name"), true); // updated fields
              exists = true;
              break;
            }
          }
          if (!exists) {
            psInsert.setObject(1, docId);
            psInsert.setString(2, (String) e.getKey());
            psInsert.setString(3, (String) e.getValue());
            psInsert.setString(4, userName);
            psInsert.addBatch();
          }
          exists = false;
        }
        // updating checkbox fields with no, which were saved earlier transaction, and want to
        // uncheck the values now.
        for (Map.Entry<String, Boolean> e : savedFields.entrySet()) {
          if (!e.getValue()) {
            psUpdate.setString(1, "");
            psUpdate.setObject(2, docId);
            psUpdate.setString(3, (String) e.getKey());
            psUpdate.addBatch();
          }
        }

      } else {
        for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
          if (((String) e.getKey()).startsWith("_")) {
            // _ names are supposed to be temp variables and skipped.
            continue;
          }
          psInsert.setObject(1, docId);
          psInsert.setString(2, (String) e.getKey());
          psInsert.setString(3, (String) e.getValue());
          psInsert.setString(4, userName);
          psInsert.addBatch();
        }
      }
      int[] updateResults = psUpdate.executeBatch();
      int[] insertResults = psInsert.executeBatch();

      return (DataBaseUtil.checkBatchUpdates(insertResults)
          && DataBaseUtil.checkBatchUpdates(updateResults));
    }
  }

  /** The Constant GET_PATIENT_DOCUMENT. */
  public static final String GET_PATIENT_DOCUMENT =
      "SELECT doc_type, doc_status, center_id FROM patient_documents WHERE doc_id = ?";

  /**
   * Gets the patient document.
   *
   * @param docId
   *          the doc id
   * @return the patient document
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getPatientDocument(int docId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_PATIENT_DOCUMENT, new Object[] { docId });
  }

}
