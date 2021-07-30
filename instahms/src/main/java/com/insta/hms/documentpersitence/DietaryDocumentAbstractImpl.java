package com.insta.hms.documentpersitence;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietaryDocumentAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    patient_id, mr_no
  }

  GenericDAO dietDocDAO = new GenericDAO("diet_chart_documents");

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    if (to == null) {
      return;
    }

    BasicDynaBean dietaryDocDetailsBean = dietDocDAO.findByKey("doc_id", docid);
    to.putAll(dietaryDocDetailsBean.getMap());
  }

  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException {

    if (fields == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    String mrno = (String) keyParams.get("mr_no");
    GenericDocumentsFields.copyPatientDetails(fields, mrno, patientId, underscore);
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean dietaryDocDetailsBean = dietDocDAO.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("patient_id", dietaryDocDetailsBean.get("patient_id"));

    return hashMap;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return null;
  }

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {

    BasicDynaBean dietDocBean = dietDocDAO.getBean();
    ConversionUtils.copyToDynaBean(requestParams, dietDocBean, errors);
    if (errors.isEmpty()) {
      dietDocBean.set("doc_id", docid);
      return dietDocDAO.insert(con, dietDocBean);
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }

    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {

    return dietDocDAO.delete(con, "doc_id", docId);
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {

    return true;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return null;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}
