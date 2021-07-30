package com.insta.hms.documentpersitence;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.ConsultationForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OutPatientDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpDocumentAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    consultation_id
  }

  GenericDAO opdoc = new GenericDAO("outpatient_docs");
  DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
  FormComponentsDAO consCompDao = new FormComponentsDAO();

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> allKeys = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      allKeys.put(key.name(), null);
    }
    return allKeys;
  }

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    BasicDynaBean opdocbean = opdoc.getBean();
    ConversionUtils.copyToDynaBean(requestParams, opdocbean, errors);

    int consultationId = (Integer) opdocbean.get("consultation_id");
    BasicDynaBean consRecord = consultDao.findConsultationExt(consultationId);
    String status = (String) consRecord.get("status");
    if (errors.isEmpty()) {
      opdocbean.set("doc_id", docid);
      boolean flag = opdoc.insert(con, opdocbean);
      if (flag && status.equals("A")) {
        BasicDynaBean consultationBean = consultDao.getBean();
        consultationBean.set("status", "P"); // marking consultation is done partially.

        AbstractInstaForms formdao = new ConsultationForms();
        PatientSectionDetailsDAO pfdDAO = new PatientSectionDetailsDAO();
        Map params = new HashMap();
        params.put("consultation_id", new String[] { consultationId + "" });
        params.put("patient_id", new String[] { (String) consRecord.get("patient_id") });
        params.put("mr_no", new String[] { (String) consRecord.get("mr_no") });

        BasicDynaBean form = formdao.getComponents(params);
        params.put("insta_form_id", new String[] { form.get("form_id").toString() });
        String userName = (String) RequestContext.getSession().getAttribute("userid");

        try {
          if (formdao.save(con, params) != null) {
            return false;
          }
        } catch (SQLException se) {
          throw se;
        } catch (IOException ie) {
          throw ie;
        } catch (Exception exp) {
          // throwing the io exception by wrapping the actual exception,
          // this is just because not to modify the generic documents framework method signature
          throw new IOException(exp);
        }

        return consultDao.update(con, consultationBean.getMap(), "consultation_id",
            consultationId) > 0;
      }
      return flag;
    } else {
      requestParams.put("error", "incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean opdocbean = opdoc.getBean();
    ConversionUtils.copyToDynaBean(requestParams, opdocbean, errors);

    if (errors.isEmpty()) {
      opdocbean.set("doc_id", docid);
      if (opdoc.update(con, opdocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;
    } else {
      requestParams.put("errors", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return opdoc.delete(con, "doc_id", docId);
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    if (key.equalsIgnoreCase("patient_id")) {
      return OutPatientDAO.getAllVisitDocuments((String) valueCol);
    }

    return null;
  }

  public List<BasicDynaBean> getVisitDocumentsForMrNo(String mrNo) throws Exception {
    return OutPatientDAO.getAllVisitsDocumentsForMrNo(mrNo);
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return OutPatientDAO.getOutpatientDocs(listingParams, extraParams, specialized);
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {

    if (to == null || keyParams == null || keyParams.isEmpty()) {
      return;
    }
    int consultationId = Integer.parseInt(keyParams.get("consultation_id").toString());
    GenericDAO consultDAO = new GenericDAO("doctor_consultation");
    String patientId = (String) consultDAO.findByKey("consultation_id", consultationId)
        .get("patient_id");
    ;
    GenericDocumentsFields.copyPatientDetails(to, null, patientId, underscore);

  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {

    BasicDynaBean documentdetailsbean = opdoc.findByKey("doc_id", docid);
    HashMap<String, Object> hashMap = new HashMap<>();
    Integer consultationId = (Integer) documentdetailsbean.get("consultation_id");
    hashMap.put("consultation_id", consultationId);

    return hashMap;
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = opdoc.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());

  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}