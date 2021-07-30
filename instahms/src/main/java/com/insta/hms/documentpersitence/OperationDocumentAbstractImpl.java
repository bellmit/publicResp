package com.insta.hms.documentpersitence;

import com.insta.hms.OTServices.OTReportsDAO;
import com.insta.hms.OTServices.OTServicesDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationDocumentAbstractImpl extends AbstractDocumentPersistence {

  OTReportsDAO opDocDao = new OTReportsDAO();

  public static enum KEYS {
    prescription_id
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = opDocDao.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException {
    if (fields == null || keyParams == null || keyParams.isEmpty()) {
      return;
    }
    String prescriptionId = (String) keyParams.get("prescription_id");
    String patientId = (String) OTServicesDAO.getOPDetails(Integer.parseInt(prescriptionId))
        .get("patient_id");
    GenericDocumentsFields.copyPatientDetails(fields, null, patientId, underscore);

  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentdetailsbean = opDocDao.findByKey("doc_id", docid);
    Integer prescriptionId = (Integer) documentdetailsbean.get("prescription_id");
    HashMap hashMap = new HashMap();
    hashMap.put("prescription_id", prescriptionId + "");
    return hashMap;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    // TODO Auto-generated method stub
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
    BasicDynaBean opdocbean = opDocDao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, opdocbean, errors);

    if (errors.isEmpty()) {
      opdocbean.set("doc_id", docid);
      return opDocDao.insert(con, opdocbean);

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return opDocDao.delete(con, "doc_id", docId);
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean opdocbean = opDocDao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, opdocbean, errors);

    if (errors.isEmpty()) {
      opdocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date, doc_name, user etc..
      if (opDocDao.update(con, opdocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return OTReportsDAO.getOperationDocs(listingParams, extraParams, true);
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}
