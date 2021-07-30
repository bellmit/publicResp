package com.insta.hms.documentpersitence;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDocumentAbstractImpl.
 */
public class TestDocumentAbstractImpl extends AbstractDocumentPersistence {

  /**
   * The Enum KEYS.
   */
  public static enum KEYS {

    /** The prescribed id. */
    prescribed_id
  }

  /** The testdocdao. */
  public GenericDAO testdocdao = new GenericDAO("test_documents");

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
    BasicDynaBean testdocbean = testdocdao.getBean();

    ConversionUtils.copyToDynaBean(requestParams, testdocbean, errors);

    if (errors.isEmpty()) {
      testdocbean.set("doc_id", docid);
      return testdocdao.insert(con, testdocbean);
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

    BasicDynaBean testdocbean = testdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, testdocbean, errors);

    if (errors.isEmpty()) {
      testdocbean.set("doc_id", docid);
      if (testdocdao.update(con, testdocbean.getMap(), keys) > 0) {
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
    return testdocdao.delete(con, "doc_id", docId);
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

    return LaboratoryDAO.searchTestDocuments(listingParams, extraParams, specialized);
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

    int prescribedId = Integer.parseInt((String) keyParams.get("prescribed_id"));
    BasicDynaBean presBean = LaboratoryDAO.getPrescribedDetails(prescribedId);

    // copy all the test details
    GenericDocumentsFields.convertAndCopy(presBean.getMap(), to, underscore);

    // copy all patient details
    String mrNo = (String) presBean.get("mr_no");
    String patientId = (String) presBean.get("pat_id");
    GenericDocumentsFields.copyPatientDetails(to, mrNo, patientId, underscore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#copyDocumentDetails(int,
   * java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = testdocdao.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentDetails = testdocdao.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("prescribed_id", documentDetails.get("prescribed_id"));
    return hashMap;
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

  /**
   * Gets the test documents.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the test documents
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getTestDocuments(int prescribedId) throws SQLException {
    return LaboratoryDAO.getTestDocuments(prescribedId);
  }

  /**
   * Copy test documents.
   *
   * @param con
   *          the con
   * @param prescribedId
   *          the prescribed id
   * @param newPrescId
   *          the new presc id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean copyTestDocuments(Connection con, int prescribedId, int newPrescId)
      throws SQLException {
    PreparedStatement patdocps = null;
    PreparedStatement testdocps = null;
    boolean success = true;
    try {
      List<BasicDynaBean> list = testdocdao.findAllByKey(con, "prescribed_id", prescribedId);
      GenericDAO patDocDAO = new GenericDAO("patient_documents");

      patdocps = con.prepareStatement(
          " INSERT INTO patient_documents(doc_id, template_id, doc_format, doc_content_text, "
              + "doc_content_bytea,"
              + "content_type, doc_type, original_extension, pheader_template_id, doc_number, "
              + "doc_location, "
              + " doc_status, center_id) (select ?, template_id, doc_format, doc_content_text, "
              + "doc_content_bytea, "
              + " content_type, doc_type, original_extension, pheader_template_id, "
              + " doc_number, doc_location, doc_status, center_id FROM patient_documents"
              + " WHERE doc_id=?) ");

      testdocps = con.prepareStatement(
          " INSERT INTO test_documents(prescribed_id, doc_id, doc_name, doc_date, username)"
              + " (select ?, ?, doc_name, "
              + " current_date, username FROM test_documents where doc_id=?) ");

      for (BasicDynaBean testbean : list) {
        int docId = (Integer) testbean.get("doc_id");

        int newDocId = patDocDAO.getNextSequence();

        int counter1 = 1;
        patdocps.setInt(counter1++, newDocId);
        patdocps.setInt(counter1++, docId);
        patdocps.addBatch();

        int counter2 = 1;
        testdocps.setInt(counter2++, newPrescId);
        testdocps.setInt(counter2++, newDocId);
        testdocps.setInt(counter2++, docId);
        testdocps.addBatch();
      }

      if (list.size() > 0) {
        int[] patdoccount = patdocps.executeBatch();
        int[] testdoccount = testdocps.executeBatch();

        if (list.size() != patdoccount.length || list.size() != testdoccount.length) {
          success = false;
        }
      }

    } finally {
      if (patdocps != null) {
        patdocps.close();
      }
      if (testdocps != null) {
        testdocps.close();
      }

    }

    return success;
  }
}
