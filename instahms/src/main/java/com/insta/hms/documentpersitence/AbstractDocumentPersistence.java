package com.insta.hms.documentpersitence;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.CommonHelper;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.genericdocuments.PatientHVFDocValuesDAO;
import com.insta.hms.genericdocuments.PatientPDFDocValuesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractDocumentPersistence.
 *
 * @author krishna.t
 * 
 *         AbstractDocumentPersistace is provided for persisting the documents.
 * 
 *         since it is abstract implementation u cant use this directly for creating the document.
 *         when u want to persist one document, or update the document, or delete the document, u
 *         have to extend this class and provide the implementation for the abstract methods.
 * 
 *         1) otherTxWhileCreate 2) otherTxWhileUpdate 3) otherTxWhileDelete
 * 
 *         are abstract api's left to the implementors to cater their stuff when creating or
 *         updating the document.
 * 
 * 
 *         create, update and delete these implementation is almost same for all the document
 *         persistince. if create, update and delete api's are not catering ur needs then override
 *         it in ur own implementation.
 */
public abstract class AbstractDocumentPersistence {

  /** The patientdocdao. */
  private PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  /** The hvfdocvaluesdao. */
  private PatientHVFDocValuesDAO hvfdocvaluesdao = new PatientHVFDocValuesDAO();

  /** The pdfdocvaluesdao. */
  private PatientPDFDocValuesDAO pdfdocvaluesdao = new PatientPDFDocValuesDAO();
  
  private static final GenericDAO patientHvfDocImagesDAO = new GenericDAO("patient_hvf_doc_images");
  private static final GenericDAO docHvfTemplateFieldsDAO =
      new GenericDAO("doc_hvf_template_fields");

  /**
   * Gets the documents list.
   *
   * @param key
   *          the key
   * @param valueCol
   *          the value col
   * @param specialized
   *          the specialized
   * @param specializedDocType
   *          the specialized doc type
   * @return the documents list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public abstract List<BasicDynaBean> getDocumentsList(String key, Object valueCol,
      Boolean specialized, String specializedDocType) throws SQLException, IOException;

  /**
   * Search documents.
   *
   * @param listingParams
   *          the listing params
   * @param extraParams
   *          the extra params
   * @param specialized
   *          the specialized
   * @param specializedDocType
   *          the specialized doc type
   * @return the paged list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public abstract PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException, ParseException;

  /*
   * Replaceable fields are static fields that can be plugged into the document. This is done for
   * both template based documents (at the time of generating the document before edits) as well as
   * field based documents (at the time of display/print). Replaceable fields depends on the entity
   * that the document is attached to.
   *
   * Example: Patient documents: all patient_details fields can be plugged in Visit related
   * documents: all patient_details and patient_registration (and ext.) fields Insurance Documents:
   * this is attached to a case, so all case details (in addition to the patient details) can be
   * plugged in.
   *
   * In order to find the set of field values that need to be plugged in, we need to identify what
   * the document is attached to. This is the "key" for the document. Example keys: Patient
   * documents: mr_no Visit documents: patient_id Insurance documents: insurance_id
   *
   * Note that there is nothing specific to the doc_id that can affect replaceable fields. But, we
   * may be required to fill in a document with replaceable fields given a doc_id. In that case, the
   * entity to which the doc is attached can be got from the doc_id and then we can get the key of
   * the entity to get the replaceable fields.
   *
   * Also note that this does not enforce that a document be related to a patient.
   */

  /**
   * Copy replaceable fields.
   *
   * @param fields
   *          the fields
   * @param keyParams
   *          the key params
   * @param underscore
   *          the underscore
   * @throws SQLException
   *           the SQL exception
   * @throws Exception
   *           the exception
   */
  public abstract void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException, Exception;

  /**
   * Gets the doc key params.
   *
   * @param docid
   *          the docid
   * @return the doc key params
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * Return the key param values for the given doc ID
   */
  public abstract Map<String, Object> getDocKeyParams(int docid) throws SQLException;

  /**
   * Copy document details.
   *
   * @param docid
   *          the docid
   * @param to
   *          the to
   * @throws SQLException
   *           the SQL exception
   */
  public abstract void copyDocumentDetails(int docid, Map to) throws SQLException;

  /**
   * Other tx while create.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @param requestParams
   *          the request params
   * @param errors
   *          the errors
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public abstract boolean otherTxWhileCreate(Connection con, int docid, Map requestParams,
      List errors) throws SQLException, IOException;

  /**
   * Other tx while update.
   *
   * @param con
   *          the con
   * @param docid
   *          the docid
   * @param requestParams
   *          the request params
   * @param errors
   *          the errors
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public abstract boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams,
      List errors) throws SQLException, IOException;

  /**
   * Other tx while delete.
   *
   * @param con
   *          the con
   * @param docId
   *          the doc id
   * @param format
   *          the format
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public abstract boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException, IOException;

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  /*
   * Get the key(s) as a map pointing to a null value: this is a list of field names that uniquely
   * determines the object/entity that the document is attached to. Eg, insurance_id for Insurance
   * Documents.
   */
  public abstract Map<String, Object> getKeys();

  /**
   * Populate the values of the keys into a map, based on request parameters.
   *
   * @param requestParams
   *          the request params
   * @param keys
   *          the keys
   * @return the map
   */
  public Map populateKeys(Map requestParams, Map<String, Object> keys) {

    if (keys == null) {
      keys = getKeys();
    }

    for (Entry<String, Object> entry : keys.entrySet()) {
      keys.put(entry.getKey(), CommonHelper.getValueFromMap(requestParams, entry.getKey()));
    }

    return keys;
  }

  /**
   * Get an instance of a sub-class based on specialization.
   *
   * @param specializedDocType
   *          the specialized doc type
   * @param specialized
   *          the specialized
   * @return single instance of AbstractDocumentPersistence
   */
  public static AbstractDocumentPersistence getInstance(String specializedDocType,
      Boolean specialized) {
    AbstractDocumentPersistence implClass = null;
    if (specialized) {
      if ("mlc".equalsIgnoreCase(specializedDocType)) {
        implClass = new MLCDocumentAbstractImpl();
      } else if ("service".equalsIgnoreCase(specializedDocType)) {
        implClass = new ServiceDocumentsAbstractImpl();
      } else if ("reg".equalsIgnoreCase(specializedDocType)) {
        implClass = new RegDocumentAbstractImpl();
      } else if ("insurance".equalsIgnoreCase(specializedDocType)) {
        implClass = new InsuraceDocumentAbstractImpl();
      } else if ("dietary".equalsIgnoreCase(specializedDocType)) {
        implClass = new DietaryDocumentAbstractImpl();
      } else if ("tpapreauth".equalsIgnoreCase(specializedDocType)) {
        implClass = new InsuracePreauthAbstractImpl();
      } else if ("op_case_form_template".equalsIgnoreCase(specializedDocType)) {
        implClass = new OpDocumentAbstractImpl();
      } else if ("ot".equalsIgnoreCase(specializedDocType)) {
        implClass = new OperationDocumentAbstractImpl();
      } else if ("plan_card".equalsIgnoreCase(specializedDocType)) {
        implClass = new PlanCardDocumentAbstractImpl();
      } else if ("corporate_card".equalsIgnoreCase(specializedDocType)) {
        implClass = new CorporateCardDocumentAbstractImpl();
      } else if ("national_card".equalsIgnoreCase(specializedDocType)) {
        implClass = new NationalCardDocumentAbstractImpl();
      } else if ("lab_test_doc".equalsIgnoreCase(specializedDocType)
          || "rad_test_doc".equalsIgnoreCase(specializedDocType)) {
        implClass = new TestDocumentAbstractImpl();
      }

    } else {
      implClass = new GeneralDocumentAbstractImpl();
    }
    return implClass;
  }

  /**
   * Gets the center id.
   *
   * @param requestParams
   *          the request params
   * @return the center id
   * @throws SQLException
   *           the SQL exception
   */
  public abstract int getCenterId(Map requestParams) throws SQLException;

  /**
   * Creates the.
   *
   * @param requestParams
   *          the request params
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean create(Map requestParams, Connection con) throws SQLException, IOException {
    List errors = new ArrayList();

    int docId = 0;
    BasicDynaBean patientdocbean = patientdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientdocbean, errors);
    docId = patientdocdao.getNextSequence();
    requestParams.put("doc_id", new Object[] { docId });
    patientdocbean.set("doc_id", docId);
    String format = CommonHelper.getValueFromMap(requestParams, "format", 0);
    patientdocbean.set("doc_format", format);
    String docSeqPattern = CommonHelper.getValueFromMap(requestParams, "doc_seq_pattern_id", 0);
    if (docSeqPattern != null) {
      patientdocbean.set("doc_number", DataBaseUtil.getNextPatternId(docSeqPattern));
    }
    String[] action = ((String[])requestParams.get("_action"));
    if (action != null && action[0].equals("finalize")) {
      patientdocbean.set("doc_status", "F");
    } 

    Object[] fileNameObj = (Object[]) requestParams.get("fileName");
    if (format != null && format.equals("doc_fileupload")
        && (fileNameObj == null || fileNameObj.equals(""))) {
      // user trying to save the document details without uploading the file. refer bug 23383
      requestParams.put("error", "Trying to save the document without uploading the file");
      return false;
    }

    if (fileNameObj != null && fileNameObj[0].toString().contains(".")) {
      String fileName = fileNameObj[0].toString();
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
      patientdocbean.set("original_extension", extension);

      if (extension.equals("odt") || extension.equals("ods")) {
        patientdocbean.set("content_type", "application/vnd.oasis.opendocument.text");
      }
    }

    int centerId = getCenterId(requestParams);
    patientdocbean.set("center_id", centerId);
    patientdocbean.set("created_at", DateUtil.getCurrentTimestamp());
    patientdocbean.set("mod_time", DateUtil.getCurrentTimestamp());
    Object[] object = (Object[]) requestParams.get("field_id");
    List list = new ArrayList();
    if (object != null && object[0] != null) {
      for (int i = 0; i < object.length; i++) {
        if (CommonHelper.getValueFromMap(requestParams, "field_value", i) != null) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          hvfdocvaluesbean.set("doc_id", docId);
          int valueId = hvfdocvaluesdao.getNextSequence();
          hvfdocvaluesbean.set("value_id", valueId);
          list.add(hvfdocvaluesbean);
        }
      }
    }
    String success = null;
    String error = null;
    boolean status = true;
    try {
      if (errors.isEmpty()) {
        // inserts into the patient_documents
        boolean patientdocflag = patientdocdao.insert(con, patientdocbean);
        boolean patientdocvaluesflag = true;
        boolean patienthvfimagevaluesflag = true;

        if (format != null && format.equals("doc_hvf_templates")) {
          if (!list.isEmpty()) {
            // inserts hvf field values.
            patientdocvaluesflag = false;

            List imgValueBeanList = new ArrayList();


            String[] fieldIdArr = (String[]) requestParams.get("field_id");
            String[] fldInputArr = (String[]) requestParams.get("field_input");
            String[] deviceIpArr = (String[]) requestParams.get("device_ip");
            String[] deviceInfoArr = (String[]) requestParams.get("device_info");
            String[] fldImgTextArr = (String[]) requestParams.get("fieldImgText");

            for (int i = 0; i < fieldIdArr.length; i++) {

              if (fldInputArr[i] != null && fldInputArr[i].equals("E")) {

                BasicDynaBean imgValueBean = patientHvfDocImagesDAO.getBean();
                imgValueBean.set("doc_image_id", patientHvfDocImagesDAO.getNextSequence());
                imgValueBean.set("doc_id", docId);
                imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                InputStream is = new ByteArrayInputStream(decodedBytes);

                imgValueBean.set("field_image", is);
                imgValueBean.set("field_image_content_type", "image/png");
                imgValueBean.set("device_ip", deviceIpArr[i]);
                imgValueBean.set("device_info", deviceInfoArr[i]);
                imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());

                imgValueBeanList.add(imgValueBean);
              }
            }

            if (!imgValueBeanList.isEmpty()) {
              patienthvfimagevaluesflag = patientHvfDocImagesDAO.insertAll(con, imgValueBeanList);
            }

            patientdocvaluesflag = patienthvfimagevaluesflag
                && hvfdocvaluesdao.insertAll(con, list);

          }

        } else if (format != null && format.equals("doc_pdf_form_templates")) {
          HttpSession session = RequestContext.getSession();
          String username = (String) session.getAttribute("userid");
          patientdocvaluesflag = false;
          patientdocvaluesflag = PatientDocumentsDAO.insertPDFFormFieldValues(con, requestParams,
              docId, username);
        }

        boolean otherTx = otherTxWhileCreate(con, docId, requestParams, errors);

        if (patientdocflag && patientdocvaluesflag && otherTx) {
          status = true;
        } else {
          status = false;
        }

      } else {
        status = false;
        error = "Incorrectly formatted values supplied";
      }
    } finally {
      if (status) {
        success = "Document inserted successfully..";
      } else {
        if (!errors.isEmpty()) {
          error = error = (String) requestParams.get("error");
        }
        error = (error == null ? "Failed to insert Document.." : error);
      }

      requestParams.put("success", success);
      requestParams.put("error", error);
    }
    return status;
  }

  /**
   * Update.
   *
   * @param requestParams
   *          the request params
   * @param con
   *          the con
   * @param saveExtFields
   *          the save ext fields
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean update(Map requestParams, Connection con, boolean saveExtFields)
      throws SQLException, IOException {

    String format = CommonHelper.getValueFromMap(requestParams, "format", 0);
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);

    List errors = new ArrayList();
    List<Map<String, BasicDynaBean>> list = new ArrayList<>();
    copyHvfDocValues(requestParams, list, errors);

    List<Map<String, BasicDynaBean>> hvfImageList = new ArrayList<>();
    if (format != null && format.equals("doc_hvf_templates")) {
      copyHvfDocImageValues(requestParams, hvfImageList, errors);
    }

    List<Map<String, BasicDynaBean>> pdfImageList = new ArrayList<>();
    if (format != null && format.equals("doc_pdf_form_templates")) {
      copyPdfDocImageValues(requestParams, pdfImageList, errors, saveExtFields);
    }

    BasicDynaBean patientdocbean = patientdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientdocbean, errors);
    /*
     * int center_id = getCenterId(requestParams); patientdocbean.set("center_id", center_id);
     */
    boolean commit = false;

    if (errors.isEmpty()) {
      boolean patientdocvaluesflag = true;
      boolean patientdocimagevaluesflag = true;
      boolean patientdocflag = false;
      boolean otherTx = false;

      try {

        Object[] fileNameObj = (Object[]) requestParams.get("fileName");

        if (fileNameObj != null && fileNameObj[0].toString().contains(".")) {
          String fileName = fileNameObj[0].toString();
          String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
          patientdocbean.set("original_extension", extension);

          if (extension.equals("odt") || extension.equals("ods")) {
            patientdocbean.set("content_type", "application/vnd.oasis.opendocument.text");
          }
        }
        patientdocbean.set("mod_time", DateUtil.getCurrentTimestamp());
        // updates the patient documents.
        if (patientdocdao.update(con, patientdocbean.getMap(), keys) > 0) {
          patientdocflag = true;
        }

        if (format != null && format.equals("doc_hvf_templates")) {
          Map<String, Object> valuesKeys = new HashMap<>();
          valuesKeys.put("doc_id", docId);

          if (!list.isEmpty()) {

            // updates the hvf document image field values.
            if (!hvfImageList.isEmpty()) {
              patientdocimagevaluesflag = hvfdocvaluesdao.updateHVFDocImageValues(con, hvfImageList,
                  docId);
            }

            // updates the hvf document field values.
            patientdocvaluesflag = false;
            patientdocvaluesflag = hvfdocvaluesdao.updateHVFDocValues(con, list, docId);

            patientdocvaluesflag = patientdocvaluesflag && patientdocimagevaluesflag;

          }
        } else if (format != null && format.equals("doc_pdf_form_templates")) {

          // updates the pdf form image field values.
          if (!pdfImageList.isEmpty()) {
            patientdocimagevaluesflag = pdfdocvaluesdao.updatePDFDocImageValues(con, pdfImageList,
                docId);
          }

          if (!saveExtFields) {
            // updates the pdf form field values.
            patientdocvaluesflag = false;
            patientdocvaluesflag = PatientDocumentsDAO.updatePDFFormFieldValues(con, requestParams,
                docId);
          }

          patientdocvaluesflag = patientdocvaluesflag && patientdocimagevaluesflag;
        }
        otherTx = otherTxWhileUpdate(con, docId, requestParams, errors);

        if (otherTx && patientdocflag && patientdocvaluesflag) {
          commit = true;
        } else {
          commit = false;
        }

      } finally {
        if (commit) {
          requestParams.put("success", "Document updated Successfully..");
        } else {
          String error = null;
          if (!errors.isEmpty()) {
            error = (String) requestParams.get("error");
          }
          error = (error == null ? "Failed to insert Document.." : error);
          requestParams.put("error", error);
        }
      }
    } else {
      requestParams.put("error", "Incorrectly formatted values supplied");
    }
    return commit;
  }

  /**
   * Delete.
   *
   * @param requestParams
   *          the request params
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean delete(Map requestParams) throws SQLException, IOException {
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = delete(requestParams, con);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  /**
   * Delete.
   *
   * @param requestParams
   *          the request params
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean delete(Map requestParams, Connection con) throws SQLException, IOException {
    String[] docIds = (String[]) requestParams.get("deleteDocument");
    String msg = null;
    String error = null;
    boolean success = false;

    try {
      if (docIds != null) {
        for (String docidFormat : docIds) {
          String format = docidFormat.split(",")[1];
          int docId = Integer.parseInt(docidFormat.split(",")[0]);

          boolean flag = patientdocdao.delete(con, "doc_id", docId);
          if (flag) {
            if (format.equals("doc_hvf_templates")) {
              PatientHVFDocValuesDAO hvfValuesDao = new PatientHVFDocValuesDAO();
              List valuesList = hvfValuesDao.findAllByKey("doc_id", docId);
              if (valuesList != null && !valuesList.isEmpty()) {
                flag = false;
                flag = hvfValuesDao.delete(con, "doc_id", docId);
              }

              patientHvfDocImagesDAO.delete(con, "doc_id", docId);

            } else if (format.equals("doc_pdf_form_templates")) {
              flag = false;
              GenericDAO pdfvaluesdao = new GenericDAO("patient_pdf_form_doc_values");
              flag = pdfvaluesdao.delete(con, "doc_id", docId);

              PatientPDFDocValuesDAO pdfImageValuesDao = new PatientPDFDocValuesDAO();
              pdfImageValuesDao.delete(con, "doc_id", docId);
            }
          }
          boolean otherTxFlag = otherTxWhileDelete(con, docId, format);

          if (flag && otherTxFlag) {
            success = true;
          } else {
            success = false;
            break;
          }
        }
        if (success) {
          msg = ((docIds.length > 1) ? "Documents" : "Document") + " deleted successfully..";
          requestParams.put("success", msg);
        } else {
          error = "Failed to delete " + ((docIds.length > 1) ? "Documents" : "Document..");
          requestParams.put("error", error);
        }
      }
    } finally {
      // finally
    }
    return success;
  }

  /**
   * Finalize.
   *
   * @param con
   *          the con
   * @param requestParams
   *          the request params
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean finalize(Connection con, Map requestParams) throws SQLException, IOException {
    String[] docIds = (String[]) requestParams.get("deleteDocument");
    boolean success = false;
    if (docIds != null) {
      for (String docidFormat : docIds) {
        String format = docidFormat.split(",")[1];
        int docId = Integer.parseInt(docidFormat.split(",")[0]);

        Map fields = new HashMap();
        fields.put("doc_id", docId);
        fields.put("doc_status", "F");

        int result = patientdocdao.updateWithName(con, fields, "doc_id");
        success = (result > 0);
        if (!success) {
          break;
        }

      }
    }
    return success;
  }

  /**
   * Finalize.
   *
   * @param requestParams
   *          the request params
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean finalize(Map requestParams) throws SQLException, IOException {
    String[] docIds = (String[]) requestParams.get("deleteDocument");
    Connection con = null;
    boolean success = false;
    String msg = null;
    String error = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);

      success = finalize(con, requestParams);

      if (success) {
        con.commit();
        msg = ((docIds.length > 1) ? "Documents" : "Document") + " finalized successfully..";
        requestParams.put("success", msg);
      } else {
        con.rollback();
        error = "Failed to finalize " + ((docIds.length > 1) ? "Documents" : "Document..");
        requestParams.put("error", error);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return success;
  }

  /**
   * Copy hvf doc values.
   *
   * @param requestParams
   *          the request params
   * @param addTo
   *          the add to
   * @param errors
   *          the errors
   * @throws SQLException
   *           the SQL exception
   */
  private void copyHvfDocValues(Map requestParams, List<Map<String, BasicDynaBean>> addTo,
      List errors) throws SQLException {

    Object[] object = (Object[]) requestParams.get("field_id");
    if (object != null && object[0] != null) {
      for (int i = 0; i < object.length; i++) {
        BasicDynaBean templatevalueBean = docHvfTemplateFieldsDAO.findByKey("field_id",
            new Integer(object[i].toString()));

        if (templatevalueBean == null || templatevalueBean.get("field_status") == null) {
          continue;
        }

        String valueId = CommonHelper.getValueFromMap(requestParams, "value_id", i);
        String fieldValue = CommonHelper.getValueFromMap(requestParams, "field_value", i);

        if ((fieldValue != null && valueId != null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("update", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        } else if ((fieldValue != null && valueId == null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("insert", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        } else if ((valueId != null && fieldValue == null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("delete", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        }

        if (((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("deleteField", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        }
      }
    }
  }

  /**
   * Copy hvf doc image values.
   *
   * @param requestParams
   *          the request params
   * @param addToImageList
   *          the add to image list
   * @param errors
   *          the errors
   * @throws SQLException
   *           the SQL exception
   */
  private void copyHvfDocImageValues(Map requestParams,
      List<Map<String, BasicDynaBean>> addToImageList, List errors) throws SQLException {
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    String username = RequestContext.getUserName();

    String[] fieldIdArr = (String[]) requestParams.get("field_id");
    String[] fldInputArr = (String[]) requestParams.get("field_input");
    String[] deviceIpArr = (String[]) requestParams.get("device_ip");
    String[] deviceInfoArr = (String[]) requestParams.get("device_info");
    String[] fldImgTextArr = (String[]) requestParams.get("fieldImgText");

    if (fieldIdArr != null && fieldIdArr.length > 0) {
      for (int i = 0; i < fieldIdArr.length; i++) {
        if (fldInputArr[i] != null && fldInputArr[i].equals("E")) {

          BasicDynaBean templatevalueBean = null;
          Integer fldId = null;

          if (fieldIdArr[i] != null && !fieldIdArr[i].equals("")) {
            fldId = Integer.parseInt(fieldIdArr[i]);
            templatevalueBean = docHvfTemplateFieldsDAO.findByKey("field_id", fldId);
          }
          if (fldId != null) {

            LinkedHashMap hvfkeys = new LinkedHashMap();
            hvfkeys.put("doc_id", docId);
            hvfkeys.put("field_id", fldId);

            List cols = new ArrayList();
            cols.add("doc_image_id");
            BasicDynaBean hvfimagebean = patientHvfDocImagesDAO.findByKey(cols, hvfkeys);
            Integer imgId = null;

            if (!((String) templatevalueBean.get("field_status")).equals("I")) {
              if (hvfimagebean != null && hvfimagebean.get("doc_image_id") != null) {
                imgId = (Integer) hvfimagebean.get("doc_image_id");

                if (fldImgTextArr[i] != null && !fldImgTextArr[i].trim().equals("")) {
                  BasicDynaBean imgValueBean = patientHvfDocImagesDAO.getBean();
                  imgValueBean.set("doc_image_id", imgId);
                  imgValueBean.set("doc_id", docId);
                  imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                  byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                  InputStream is = new ByteArrayInputStream(decodedBytes);

                  imgValueBean.set("field_image", is);
                  imgValueBean.set("field_image_content_type", "image/png");
                  imgValueBean.set("device_ip", deviceIpArr[i]);
                  imgValueBean.set("device_info", deviceInfoArr[i]);
                  imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
                  imgValueBean.set("username", username);

                  Map<String, BasicDynaBean> map = new HashMap<>();
                  map.put("update", imgValueBean);
                  addToImageList.add(map);
                }
              } else {
                imgId = patientHvfDocImagesDAO.getNextSequence();

                BasicDynaBean imgValueBean = patientHvfDocImagesDAO.getBean();
                imgValueBean.set("doc_image_id", imgId);
                imgValueBean.set("doc_id", docId);
                imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                InputStream is = new ByteArrayInputStream(decodedBytes);

                imgValueBean.set("field_image", is);
                imgValueBean.set("field_image_content_type", "image/png");
                imgValueBean.set("device_ip", deviceIpArr[i]);
                imgValueBean.set("device_info", deviceInfoArr[i]);
                imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
                imgValueBean.set("username", username);

                Map<String, BasicDynaBean> map = new HashMap<>();
                map.put("insert", imgValueBean);
                addToImageList.add(map);
              }
            } else if (hvfimagebean != null && hvfimagebean.get("doc_image_id") != null) {
              imgId = (Integer) hvfimagebean.get("doc_image_id");
              BasicDynaBean imgValueBean = patientHvfDocImagesDAO.getBean();
              imgValueBean.set("doc_image_id", imgId);
              imgValueBean.set("doc_id", docId);
              imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

              Map<String, BasicDynaBean> map = new HashMap<>();
              map.put("deleteField", imgValueBean);
              addToImageList.add(map);
            }
          }
        }
      }
    }
  }

  /**
   * Copy pdf doc image values.
   *
   * @param requestParams
   *          the request params
   * @param addToImageList
   *          the add to image list
   * @param errors
   *          the errors
   * @param saveExtFields
   *          the save ext fields
   * @throws SQLException
   *           the SQL exception
   */
  private void copyPdfDocImageValues(Map requestParams,
      List<Map<String, BasicDynaBean>> addToImageList, List errors, boolean saveExtFields)
      throws SQLException {
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    String username = RequestContext.getUserName();
    GenericDAO pdfimagevaluesdao = new GenericDAO("patient_pdf_doc_images");

    Integer templateId = (requestParams.get("template_id") != null
        && !((String[]) requestParams.get("template_id"))[0].equals(""))
            ? Integer.parseInt(((String[]) requestParams.get("template_id"))[0])
            : null;

    List<BasicDynaBean> imageTemplateFieldvalues = PatientPDFDocValuesDAO
        .getPDFTemplateImageValues(templateId);

    if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {

      for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
        String fieldId = null;
        String fldInput = null;
        String deviceIp = null;
        String deviceInfo = null;
        String fldImgText = null;

        if (!saveExtFields) {
          fieldId = (requestParams.get("field_id" + "_" + i) != null
              && !((String[]) requestParams.get("field_id" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("field_id" + "_" + i))[0]
                  : null;
          fldInput = (requestParams.get("field_input" + "_" + i) != null
              && !((String[]) requestParams.get("field_input" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("field_input" + "_" + i))[0]
                  : null;
          deviceIp = (requestParams.get("device_ip" + "_" + i) != null
              && !((String[]) requestParams.get("device_ip" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("device_ip" + "_" + i))[0]
                  : null;
          deviceInfo = (requestParams.get("device_info" + "_" + i) != null
              && !((String[]) requestParams.get("device_info" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("device_info" + "_" + i))[0]
                  : null;
          fldImgText = (requestParams.get("fieldImgText" + "_" + i) != null
              && !((String[]) requestParams.get("fieldImgText" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("fieldImgText" + "_" + i))[0]
                  : null;
        } else {
          fieldId = (requestParams.get("field_id") != null
              && !((String[]) requestParams.get("field_id"))[i].equals(""))
                  ? ((String[]) requestParams.get("field_id"))[i]
                  : null;
          fldInput = (requestParams.get("field_input") != null
              && !((String[]) requestParams.get("field_input"))[i].equals(""))
                  ? ((String[]) requestParams.get("field_input"))[i]
                  : null;
          deviceIp = (requestParams.get("device_ip") != null
              && !((String[]) requestParams.get("device_ip"))[i].equals(""))
                  ? ((String[]) requestParams.get("device_ip"))[i]
                  : null;
          deviceInfo = (requestParams.get("device_info") != null
              && !((String[]) requestParams.get("device_info"))[i].equals(""))
                  ? ((String[]) requestParams.get("device_info"))[i]
                  : null;
          fldImgText = (requestParams.get("fieldImgText") != null
              && !((String[]) requestParams.get("fieldImgText"))[i].equals(""))
                  ? ((String[]) requestParams.get("fieldImgText"))[i]
                  : null;
        }

        deviceIp = (deviceIp == null) ? "" : deviceIp;
        deviceInfo = (deviceInfo == null) ? "" : deviceInfo;
        fldImgText = (fldImgText == null) ? "" : fldImgText;

        if (fieldId != null && fldInput != null && (fldInput.equals("E") || fldInput.equals("C"))) {

          LinkedHashMap pdfkeys = new LinkedHashMap();
          pdfkeys.put("doc_id", docId);
          pdfkeys.put("field_id", Integer.parseInt(fieldId));

          List cols = new ArrayList();
          cols.add("doc_image_id");
          BasicDynaBean pdfimagebean = pdfimagevaluesdao.findByKey(cols, pdfkeys);
          Integer imgId = null;

          if (pdfimagebean != null && pdfimagebean.get("doc_image_id") != null) {
            imgId = (Integer) pdfimagebean.get("doc_image_id");

            if (fldImgText != null && !fldImgText.trim().equals("")) {
              BasicDynaBean imgValueBean = pdfimagevaluesdao.getBean();
              imgValueBean.set("doc_image_id", imgId);
              imgValueBean.set("doc_id", docId);
              imgValueBean.set("field_id", Integer.parseInt(fieldId));

              byte[] decodedBytes = Base64.decodeBase64(fldImgText.getBytes());
              InputStream is = new ByteArrayInputStream(decodedBytes);

              imgValueBean.set("field_image", is);
              imgValueBean.set("field_image_content_type", "image/png");
              imgValueBean.set("device_ip", deviceIp);
              imgValueBean.set("device_info", deviceInfo);
              imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
              imgValueBean.set("username", username);

              Map<String, BasicDynaBean> map = new HashMap<>();
              map.put("update", imgValueBean);
              addToImageList.add(map);
            }
          } else {
            imgId = pdfimagevaluesdao.getNextSequence();

            BasicDynaBean imgValueBean = pdfimagevaluesdao.getBean();
            imgValueBean.set("doc_image_id", imgId);
            imgValueBean.set("doc_id", docId);
            imgValueBean.set("field_id", Integer.parseInt(fieldId));

            byte[] decodedBytes = Base64.decodeBase64(fldImgText.getBytes());
            InputStream is = new ByteArrayInputStream(decodedBytes);

            imgValueBean.set("field_image", is);
            imgValueBean.set("field_image_content_type", "image/png");
            imgValueBean.set("device_ip", deviceIp);
            imgValueBean.set("device_info", deviceInfo);
            imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
            imgValueBean.set("username", username);

            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("insert", imgValueBean);
            addToImageList.add(map);
          }
        }
      }
    }
  }
}