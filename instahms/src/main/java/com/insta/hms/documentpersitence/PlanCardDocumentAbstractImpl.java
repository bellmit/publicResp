package com.insta.hms.documentpersitence;

import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.InsuranceDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author deepasri.prasad
 *
 */
public class PlanCardDocumentAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    patient_policy_id
  }

  GenericDAO patientPlanDAO = new GenericDAO("patient_policy_details");
  GenericDAO policyDocImgDAO = new GenericDAO("plan_docs_details");

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    if (to == null) {
      return;
    }
    BasicDynaBean docDetailsBean = policyDocImgDAO.findByKey("doc_id", docid);
    to.putAll(docDetailsBean.getMap());
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
    BasicDynaBean docDetailsBean = policyDocImgDAO.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.putAll(docDetailsBean.getMap());
    return hashMap;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    // TODO Auto-generated method stub
    if (key.equalsIgnoreCase("mr_no")) {
      return InsuranceDAO.getAllPatientPlanCardDocuments((String) valueCol);
    } else if (key.equalsIgnoreCase("patient_id")) {
      return InsuranceDAO.getAllVisitPlanCardDocuments((String) valueCol);
    }

    return null;
  }

  @Override
  public Map<String, Object> getKeys() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Boolean success = false;
    BasicDynaBean planImgDocBean = policyDocImgDAO.getBean();
    ConversionUtils.copyToDynaBean(requestParams, planImgDocBean, errors);
    GenericDAO patGenDocDao = new GenericDAO("patient_general_docs");
    if (errors.isEmpty()) {
      Integer patientPolicyId = (Integer) planImgDocBean.get("patient_policy_id");
      if (patientPolicyId == null) {
        String patientId = ((String[]) requestParams.get("patient_id"))[0];
        BasicDynaBean regBean = new VisitDetailsDAO().findByKey(con, "patient_id", patientId);
        patientPolicyId = (Integer) regBean.get("patient_policy_id");
        BasicDynaBean patGenDoc = patGenDocDao.getBean();
        patGenDoc.set("mr_no", regBean.get("mr_no"));
        patGenDoc.set("patient_id", regBean.get("patient_id"));
        patGenDoc.set("doc_id", docid);
        patGenDoc.set("doc_name", "Insurance Card");
        patGenDoc.set("doc_date", DateUtil.getCurrentDate());
        patGenDoc.set("username", regBean.get("user_name"));
      }

      if (errors.isEmpty()) {
        planImgDocBean.set("doc_id", docid);
        planImgDocBean.set("patient_policy_id", patientPolicyId);
        success = policyDocImgDAO.insert(con, planImgDocBean);
      }

      return success;
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException, IOException {
    Map columndata = new HashMap();
    columndata.put("doc_id", null);
    return patientPlanDAO.update(con, columndata, "doc_id", docId) > 0;
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);
    BasicDynaBean patientplandocbean = policyDocImgDAO.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientplandocbean, errors);
    if (errors.isEmpty()) {
      patientplandocbean.set("doc_id", docid);
      // updates the patient document details like doc_date, doc_name, user etc..
      if (policyDocImgDAO.update(con, patientplandocbean.getMap(), keys) > 0) {
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
