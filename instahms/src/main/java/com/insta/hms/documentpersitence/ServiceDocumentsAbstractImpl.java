package com.insta.hms.documentpersitence;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.services.ServiceDocumentsDAO;
import com.insta.hms.services.ServicesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class ServiceDocumentsAbstractImpl extends AbstractDocumentPersistence {

  ServicesDAO servicesDAO = new ServicesDAO();
  ServiceDocumentsDAO serviceDocDao = new ServiceDocumentsDAO();

  public static enum KEYS {
    prescription_id
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = serviceDocDao.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
    int prescriptionId = (Integer) to.get("prescription_id");
    BasicDynaBean serviceDetails = ServicesDAO.getConductingDoctorId(prescriptionId);
    String conductingDoctorId = (String) serviceDetails.get("conductedby");
    to.put("conducting_doctor_id", conductingDoctorId);
  }

  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException {
    if (fields == null || keyParams == null || keyParams.isEmpty()) {
      return;
    }
    String prescriptionId = (String) keyParams.get("prescription_id");
    String patientId = (String) servicesDAO
        .findByKey("prescription_id", Integer.parseInt(prescriptionId)).get("patient_id");
    GenericDocumentsFields.copyPatientDetails(fields, null, patientId, underscore);
    GenericDocumentsFields.convertAndCopy(
        ServicesDAO.getServiceDetails(Integer.parseInt(prescriptionId)).getMap(), fields,
        underscore);

  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentdetailsbean = serviceDocDao.findByKey("doc_id", docid);
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
    BasicDynaBean servicedocbean = serviceDocDao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, servicedocbean, errors);

    if (errors.isEmpty()) {
      servicedocbean.set("doc_id", docid);
      return serviceDocDao.insert(con, servicedocbean);

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return serviceDocDao.delete(con, "doc_id", docId);
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean servicedocbean = serviceDocDao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, servicedocbean, errors);

    if (errors.isEmpty()) {
      servicedocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date, doc_name, user etc..
      if (serviceDocDao.update(con, servicedocbean.getMap(), keys) > 0) {
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}
