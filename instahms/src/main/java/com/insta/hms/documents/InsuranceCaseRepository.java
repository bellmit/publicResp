package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class InsuranceCaseRepository extends GenericRepository {

  public InsuranceCaseRepository() {
    super("insurance_case");
  }

  private static String getAllInsuranceDocs = "SELECT ic.mr_no,pr.patient_id "
      + "as visit_id,id.doc_id, id.doc_name"
      + ",id.doc_date,id.username, id.insurance_id,pd.doc_format,pd.doc_status, pd.template_id,"
      + "dv.status,dv.doc_type,dv.template_name, dv.specialized, dv.access_rights "
      + " FROM doc_all_templates_view dv"
      + " JOIN patient_documents pd on pd.template_id = dv.template_id and dv.doc_format ="
      + " pd.doc_format"
      + " JOIN insurance_docs  id on (id.doc_id = pd.doc_id)"
      + " JOIN insurance_case ic on ic.insurance_id = id.insurance_id"
      + " LEFT OUTER JOIN patient_registration pr on pr.insurance_id = ic.insurance_id "
      + "where id.insurance_id = ?";

  public static List<BasicDynaBean> getInsuranceDocs(Map listingParams, Map extraParams,
      Boolean specialized) {
    return DatabaseHelper.queryToDynaList(getAllInsuranceDocs, extraParams.get("insurance_id"));
  }

  private static final String GET_CASE_DETAILS = " SELECT ic.insurance_id, ic.mr_no, "
      + " ic.insurance_no, ic.case_added_date, "
      + "   ic.policy_no, ic.policy_holder_name, ic.patient_relationship, "
      + " round(estv.amt,2) as estimate_amt, "
      + "   ic.remarks, ic.status_reason, ic.status, ic.tpa_id,  "
      + "   ic.finalized_date ,ic.diagnosis,   "
      + "   tm.claim_template_id, tm.default_claim_template,  "
      + "   tm.tpa_name, tm.tpa_pdf_form, ic.preauth_doc_id, "
      + "   icd.insurance_id as claim_id,   "
      + "   pr.patient_id, pr.insurance_id AS pat_insurance " + "   FROM insurance_case ic "
      + "   LEFT JOIN patient_registration pr on (pr.insurance_id = ic.insurance_id) "
      + "   LEFT JOIN tpa_master tm on (tm.tpa_id=ic.tpa_id)   "
      + "   LEFT JOIN insurance_estimate_view estv on (estv.insurance_id=ic.insurance_id)   "
      + "   LEFT JOIN insurance_claim_docs icd on (icd.insurance_id=ic.insurance_id)  "
      + "   WHERE ic.insurance_id= ? ";

  public BasicDynaBean getCaseDetails(int insuranceId) {
    return DatabaseHelper.queryToDynaBean(GET_CASE_DETAILS, new Object[] { insuranceId });
  }

  public static final String ALL_PATIENT_PLAN_DOCS = " SELECT doc_name, doc_date, pdd.doc_id,"
      + " username, pip.plan_id, "
      + " pr.patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format, content_type,"
      + " pd.doc_type, username  "
      + " FROM patient_policy_details ppd "
      + " JOIN plan_docs_details pdd ON ppd.patient_policy_id = pdd.patient_policy_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " LEFT JOIN patient_insurance_plans pip ON(pip.patient_policy_id= ppd.patient_policy_id "
      + " AND pip.plan_id = ppd.plan_id) "
      + " LEFT JOIN patient_registration pr ON (pip.patient_id = pr.patient_id)"
      + " LEFT JOIN patient_details pds ON (pds.mr_no = pr.mr_no)  ";

  public static List<BasicDynaBean> getAllPatientPlanCardDocuments(String mrNo) {
    return DatabaseHelper.queryToDynaList(ALL_PATIENT_PLAN_DOCS
        + " WHERE  pds.mr_no=? AND (pr.patient_id='' || pr.patient_id IS NULL) ", mrNo);
  }

  public static List<BasicDynaBean> getAllVisitPlanCardDocuments(String mrNo) {
    return DatabaseHelper.queryToDynaList(ALL_PATIENT_PLAN_DOCS + " WHERE pr.patient_id =? ",
        mrNo);
  }

}
