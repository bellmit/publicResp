package com.insta.hms.documentpersitence;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.CommonHelper;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.InsuranceDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuracePreauthAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    insurance_id
  }

  public GenericDAO preauthdocdao = new GenericDAO("insurance_case");

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

    Map<String, Object> keys = new HashMap<>();
    String insuranceId = CommonHelper.getValueFromMap(requestParams, "insurance_id");
    keys.put("insurance_id", Integer.parseInt(insuranceId));

    BasicDynaBean insdocbean = preauthdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);
    Object[] userNameObj = (Object[]) requestParams.get("username");

    if (errors.isEmpty()) {
      insdocbean.set("preauth_doc_id", docid);
      if (userNameObj != null) {
        insdocbean.set("preauth_username", userNameObj[0]);
      }
      if (preauthdocdao.update(con, insdocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {

    Map<String, Object> keys = new HashMap<>();
    String insuranceId = CommonHelper.getValueFromMap(requestParams, "insurance_id");
    keys.put("insurance_id", Integer.parseInt(insuranceId));

    BasicDynaBean insdocbean = preauthdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);
    Object[] userNameObj = (Object[]) requestParams.get("username");

    if (errors.isEmpty()) {
      insdocbean.set("preauth_doc_id", docid);
      if (userNameObj != null) {
        insdocbean.set("preauth_username", userNameObj[0]);
      }
      if (preauthdocdao.update(con, insdocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return preauthdocdao.delete(con, "preauth_doc_id", docId);
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return null;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return InsuranceDAO.getPreauthForm(listingParams, extraParams, specialized);
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {

    if (to == null) {
      return;
    }

    int insuranceId = Integer.parseInt((String) keyParams.get("insurance_id"));
    BasicDynaBean caseBean = new InsuranceDAO().getCaseDetails(insuranceId);

    // copy all the case details
    GenericDocumentsFields.convertAndCopy(caseBean.getMap(), to, underscore);

    // copy all patient details
    String mrNo = (String) caseBean.get("mr_no");
    String patientId = (String) caseBean.get("patient_id");

    GenericDocumentsFields.copyPatientDetails(to, mrNo, patientId, underscore);
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = preauthdocdao.findByKey("preauth_doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentDetails = preauthdocdao.findByKey("preauth_doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("insurance_id", documentDetails.get("insurance_id").toString());
    return hashMap;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }
}
