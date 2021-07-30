package com.insta.hms.documentpersitence;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.CommonHelper;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class RegDocumentAbstractImpl.
 *
 * @author krishna.t
 */
public class RegDocumentAbstractImpl extends AbstractDocumentPersistence {

  /**
   * The Enum KEYS.
   */
  public static enum KEYS {

    /** The patient id. */
    patient_id
  }

  /** The regcarddao. */
  GenericDAO regcarddao = new GenericDAO("patient_registration_cards");

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getKeys()
   */
  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileCreate(java.sql.
   * Connection, int, java.util.Map, java.util.List)
   */
  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    BasicDynaBean regcarddocbean = regcarddao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, regcarddocbean, errors);
    if (errors.isEmpty()) {
      regcarddocbean.set("doc_id", docid);
      return regcarddao.insert(con, regcarddocbean);

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileUpdate(java.sql.
   * Connection, int, java.util.Map, java.util.List)
   */
  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean regcarddocbean = regcarddao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, regcarddocbean, errors);

    if (errors.isEmpty()) {
      regcarddocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date, doc_name, user etc..
      if (regcarddao.update(con, regcarddocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileDelete(java.sql.
   * Connection, java.lang.Object, java.lang.String)
   */
  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getDocumentsList(java.lang.String,
   * java.lang.Object, java.lang.Boolean, java.lang.String)
   */
  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#searchDocuments(java.util.Map,
   * java.util.Map, java.lang.Boolean, java.lang.String)
   */
  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return PatientDetailsDAO.getRegistrationDocs(listingParams, extraParams, specialized);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#copyReplaceableFields(java.util.
   * Map, java.util.Map, boolean)
   */
  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {
    if (to == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    GenericDocumentsFields.copyPatientDetails(to, null, patientId, underscore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#copyDocumentDetails(int,
   * java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    if (to == null) {
      return;
    }

    BasicDynaBean documentdetailsbean = regcarddao.findByKey("doc_id", docid);
    // copies document details like doc date, username, and visit id.
    to.putAll(documentdetailsbean.getMap());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentdetailsbean = regcarddao.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("patient_id", documentdetailsbean.get("patient_id"));
    return hashMap;
  }

  /**
   * Auto generate reg documents.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @param user
   *          the user
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws DocumentException
   *           the document exception
   */
  public boolean autoGenerateRegDocuments(Connection con, String patientId, String mrNo,
      String visitType, String user) throws SQLException, IOException, DocumentException {

    GenericDocumentTemplateDAO pdfTempDao = new GenericDocumentTemplateDAO(
        "doc_pdf_form_templates");
    GenericDocumentTemplateDAO rchTextTempDao = new GenericDocumentTemplateDAO(
        "doc_rich_templates");

    Map<String, String> fields = new HashMap<>();

    GenericDAO pdftemplatedao = new GenericDAO("doc_pdf_form_templates");
    String keyCol = visitType.equals("opreg") ? "auto_gen_op" : "auto_gen_ip";
    Map<String, String> filterMap = new HashMap<>();
    filterMap.put(keyCol, "Y");
    filterMap.put("status", "A");

    List<BasicDynaBean> autoGenPDFDocList = pdfTempDao
        .listAll(Arrays.asList(new String[] { "template_id", "template_name" }), filterMap, null);
    List<BasicDynaBean> autoGenRichTxtDocList = rchTextTempDao.listAll(Arrays.asList(
        new String[] { "template_id", "template_name", "template_content", "pheader_template_id" }),
        filterMap, null);

    Map<String, List<BasicDynaBean>> templatesMap = new HashMap<>();
    templatesMap.put("doc_pdf_form_templates", autoGenPDFDocList);
    templatesMap.put("doc_rich_templates", autoGenRichTxtDocList);

    for (Map.Entry<String, List<BasicDynaBean>> autoGenDocEntry : templatesMap.entrySet()) {

      String docFormat = autoGenDocEntry.getKey();
      List<BasicDynaBean> autoGenDocList = autoGenDocEntry.getValue();
      for (BasicDynaBean tempBean : autoGenDocList) {

        int templateId = (Integer) tempBean.get("template_id");

        BasicDynaBean bean = pdftemplatedao.getBean();
        pdftemplatedao.loadByteaRecords(bean, "template_id", templateId);
        InputStream pdf = (InputStream) bean.get("template_content");

        Map<String, Object[]> resMap = new HashMap<>();
        String docContent = "";

        if (!docFormat.equals("doc_pdf_form_templates")) {
          GenericDocumentsFields.copyPatientDetails(con, fields, null, patientId, false);
          docContent = (String) tempBean.get("template_content");
          docContent = CommonHelper.replaceTags(docContent, fields, true);
          resMap.put("pheader_template_id",
              new Object[] { (Integer) tempBean.get("pheader_template_id") });
        }

        resMap.put("format", new String[] { docFormat });
        resMap.put("template_id", new Object[] { templateId });
        resMap.put("doc_type", new String[] { "SYS_RG" });
        resMap.put("doc_content_text", new Object[] { docContent });
        resMap.put("mr_no", new String[] { mrNo });
        resMap.put("patient_id", new String[] { patientId });
        String docName = (String) tempBean.get("template_name");
        resMap.put("doc_name", new String[] { docName });
        Date docDate = (new java.sql.Date((new java.util.Date()).getTime()));
        resMap.put("doc_date", new Object[] { docDate });
        resMap.put("username", new Object[] { user });

        boolean success = create(resMap, con);
        if (!success) {
          return false;
        }
      }
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getCenterId(java.util.Map)
   */
  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}
