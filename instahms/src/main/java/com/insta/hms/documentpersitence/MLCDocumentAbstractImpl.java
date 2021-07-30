package com.insta.hms.documentpersitence;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.CommonHelper;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.genericdocuments.PatientHVFDocValuesDAO;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author krishna.t
 *
 */
public class MLCDocumentAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    patient_id
  }

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  static PatientHVFDocValuesDAO hvfdocvaluesdao = new PatientHVFDocValuesDAO();
  static VisitDetailsDAO patientRegdao = new VisitDetailsDAO();
  static PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  private static final String UPDATE_MLC_STATUS = "UPDATE patient_registration SET doc_id = ?, "
      + "user_name = ? WHERE mr_no = ? and patient_id = ? ";

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {

    String mrNo = (String) requestParams.get("mr_no");
    String visitId = (String) requestParams.get("patient_id");
    String userName = (String) requestParams.get("username");
    PreparedStatement ps = null;
    boolean mlcStatus = false;
    boolean regUpdate = false;

    try {
      ps = con.prepareStatement(UPDATE_MLC_STATUS);
      ps.setInt(1, docid);
      ps.setString(2, userName);
      ps.setString(3, mrNo);
      ps.setString(4, visitId);
      mlcStatus = ps.executeUpdate() != 0;

      BasicDynaBean patientRegbean = patientRegdao.getBean();
      patientRegbean.set("doc_id", docid);
      patientRegbean.set("user_name", userName);

      Object key = requestParams.get("patient_id");
      Map<String, Object> keys = new HashMap<>();
      keys.put("patient_id", key);

      if (patientRegdao.update(con, patientRegbean.getMap(), keys) > 0) {
        regUpdate = true;
      }

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }

    return (mlcStatus && regUpdate);
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    // need to implement
    return true;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return true;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return null;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return null;
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {
    if (to == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    GenericDocumentsFields.copyPatientDetails(to, null, patientId, underscore);
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {

  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentdetailsbean = new GenericDAO("patient_registration").findByKey("doc_id",
        docid);
    HashMap hashMap = new HashMap();
    hashMap.put("patient_id", documentdetailsbean.get("patient_id"));
    return hashMap;
  }

  @Override
  public boolean create(Map requestParams, Connection con) throws SQLException, IOException {

    PreparedStatement ps = null;
    String status = "";

    boolean patientdocvaluesflag = true;

    int docId = 0;
    int valueId = 0;
    String[] tempIdAndFormat = ((String) requestParams.get("mlc_template_id")).split(",");
    Integer templateId = Integer.parseInt(tempIdAndFormat[0]);
    String format = tempIdAndFormat[1];

    String mrNo = (String) requestParams.get("mr_no");
    String visitId = (String) requestParams.get("patient_id");
    InputStream rtfStream = null;
    String contentType = null;

    GenericDAO templateDao = new GenericDAO(format);
    Integer pheaderTemplateId = null;

    // update doc_id to patient_registration
    docId = patientdocdao.getNextSequence();

    String docContent = null;
    if (format.equals("doc_hvf_templates")) {

      String getHvfFields = "SELECT field_id, default_value FROM doc_hvf_template_fields WHERE"
          + " template_id = ? ";
      ps = con.prepareStatement(getHvfFields);
      ps.setInt(1, templateId);
      List hvfList = DataBaseUtil.queryToDynaList(ps);

      if (!hvfList.isEmpty()) {

        List list = new ArrayList();
        Iterator it = hvfList.iterator();

        while (it.hasNext()) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesdao.getBean();
          valueId = hvfdocvaluesdao.getNextSequence();
          DynaBean row = (DynaBean) it.next();
          hvfdocvaluesbean.set("value_id", valueId);
          hvfdocvaluesbean.set("doc_id", docId);
          hvfdocvaluesbean.set("field_id", row.get("field_id"));
          hvfdocvaluesbean.set("field_value", row.get("default_value"));

          list.add(hvfdocvaluesbean);
        }
        // inserts hvf field values.
        patientdocvaluesflag = false;
        patientdocvaluesflag = hvfdocvaluesdao.insertAll(con, list);
      }

    } else if (format.equals("doc_rich_templates")) {
      BasicDynaBean bean = templateDao.findByKey("template_id", templateId);
      pheaderTemplateId = (Integer) bean.get("pheader_template_id");

      Map<String, String> fields = new HashMap<>();
      GenericDocumentsFields.copyStandardFields(fields, false);
      GenericDocumentsFields.copyPatientDetails(fields, mrNo, visitId, false);
      String templateContent = (String) bean.get("template_content");
      docContent = CommonHelper.replaceTags(templateContent, fields, false);
      try {
        docContent = CommonHelper.addRichTextTitle(docContent, (String) bean.get("title"),
            (Integer) bean.get("pheader_template_id"), visitId, mrNo);
      } catch (TemplateException exp) {
        throw new IOException(exp);
      }

    } else if ("doc_rtf_templates".equalsIgnoreCase(format)) {
      BasicDynaBean rtfbean = templateDao.getBean();
      templateDao.loadByteaRecords(rtfbean, "template_id", templateId);

      Map<String, String> fields = new HashMap<>();
      GenericDocumentsFields.copyStandardFields(fields, false);
      GenericDocumentsFields.copyPatientDetails(fields, mrNo, visitId, false);
      ByteArrayOutputStream outstream = new ByteArrayOutputStream();
      String templateContentType = (String) rtfbean.get("content_type");
      boolean isRtf = false;
      if (templateContentType.equals("application/rtf") || templateContentType.equals("text/rtf")) {
        isRtf = true;
      }
      CommonHelper.replaceTags((InputStream) rtfbean.get("template_content"), outstream, fields,
          isRtf);
      rtfStream = new ByteArrayInputStream(outstream.toByteArray());
      contentType = (String) rtfbean.get("content_type");
    }

    // inserts into the patient_documents
    BasicDynaBean patientdocbean = patientdocdao.getBean();
    patientdocbean.set("doc_id", docId);
    patientdocbean.set("template_id", templateId);
    patientdocbean.set("doc_format", format);
    patientdocbean.set("doc_content_text", docContent);
    patientdocbean.set("doc_content_bytea", rtfStream);
    patientdocbean.set("content_type", contentType);
    patientdocbean.set("doc_type", "4");
    patientdocbean.set("pheader_template_id", pheaderTemplateId);

    boolean patientdocflag = patientdocdao.insert(con, patientdocbean);
    boolean otherTx = otherTxWhileCreate(con, docId, requestParams, null);

    if (otherTx && patientdocflag && patientdocvaluesflag) {
      status = "commit";
    } else {
      status = "rollback";
    }

    if (status.equals("commit")) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }
}
