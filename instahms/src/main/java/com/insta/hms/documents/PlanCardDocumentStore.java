package com.insta.hms.documents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientInsurancePlansRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePolicyDetailsRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlanCardDocumentStore extends AbstractDocumentStore {

  public PlanCardDocumentStore() {
    super("SYS_RG", true);
    // TODO Auto-generated constructor stub
  }

  public static enum KEYS { patient_policy_id }

  @LazyAutowired
  private PatientInsurancePolicyDetailsRepository patientPlanRepo;
  @LazyAutowired
  private PlanDocsDetailsRepository policyDocImgRepo;
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;
  @LazyAutowired
  private InsuranceCaseRepository insuranceCaseRepository;
  @LazyAutowired
  private PatientGeneralDocsRepository patGenDocRepo;
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;
  @LazyAutowired
  private PatientInsurancePlansRepository patientInsurancePlansRepository;

  @Override
  public void copyDocumentDetails(int docid, Map to) {
    if (to == null) {
      return;
    }
    BasicDynaBean docDetailsBean = policyDocImgRepo.findByKey("doc_id", docid);
    to.putAll(docDetailsBean.getMap());
  }

  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore) {
    if (fields == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    String mrno = (String) keyParams.get("mr_no");
    genericDocumentsUtil.copyPatientDetails(fields, mrno, patientId, underscore);
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) {
    BasicDynaBean docDetailsBean = policyDocImgRepo.findByKey("doc_id", docid);
    HashMap hashmap = new HashMap();
    hashmap.putAll(docDetailsBean.getMap());
    return hashmap;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) {
    // TODO Auto-generated method stub
    if (key.equalsIgnoreCase("mr_no")) {
      return InsuranceCaseRepository.getAllPatientPlanCardDocuments((String) valueCol);
    } else if (key.equalsIgnoreCase("patient_id")) {
      return InsuranceCaseRepository.getAllVisitPlanCardDocuments((String) valueCol);
    }

    return null;
  }

  @Override
  public Map<String, Object> getKeys() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean postCreate(int docid, Map requestParams, List errors) {

    // logic to insert in plan_docs_details table moved to RegistrationService
    if (errors.isEmpty()) {
      return true;
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
      return false;
    }
  }

  @Override
  public boolean postDelete(Object docId, String format) {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    BasicDynaBean bean = patientPlanRepo.getBean();
    bean.set("doc_id", null);
    return patientPlanRepo.update(bean, keys) > 0;
  }

  @Override
  public boolean postUpdate(int docid, Map requestParams, List errors) {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);
    BasicDynaBean patientplandocbean = policyDocImgRepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientplandocbean, errors);
    if (errors.isEmpty()) {
      patientplandocbean.set("doc_id", docid);
      // updates the patient document details like doc_date, doc_name,
      // user etc..
      if (policyDocImgRepo.update(patientplandocbean, keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public List<BasicDynaBean> searchDocuments(Map listingParams, Map extraParams,
      Boolean specialized) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getCenterId(Map requestParams) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Boolean isMigratedToMinio() {
    return true;
  }

}
