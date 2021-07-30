package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PlanDocsDetailsRepository.
 */
@Repository
public class PlanDocsDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new plan docs details repository.
   */
  public PlanDocsDetailsRepository() {
    super("plan_docs_details");
  }

  /** The Constant GET_PLAN_DOCUMENT. */
  private static final String GET_PLAN_DOCUMENT =
      " SELECT pdc.doc_content_bytea, pdc.content_type " + " FROM patient_insurance_plans "
          + " JOIN plan_docs_details pdd USING(patient_policy_id) "
          + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
          + " WHERE patient_policy_id = ? ORDER BY pdd.doc_id DESC  ";

  /**
   * Gets the plan document list.
   *
   * @param patientPolicyId the patient policy id
   * @return the plan document list
   */
  public List<BasicDynaBean> getPlanDocumentList(Integer patientPolicyId) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_DOCUMENT, new Object[] {patientPolicyId});
  }

  /** The Constant GET_PLAN_DOCUMENTS_FROM_VISIT_ID. */
  private static final String GET_PLAN_DOCUMENTS_FROM_VISIT_ID =
      " SELECT pdc.doc_content_bytea, pdc.content_type " + " FROM patient_insurance_plans  pip"
          + " JOIN plan_docs_details pdd USING(patient_policy_id) "
          + " JOIN patient_documents pdc ON(pdc.doc_id = pdd.doc_id) "
          + " WHERE patient_id = ? AND pip.priority=?";

  /**
   * Gets the plan documents from visit ID.
   *
   * @param visitID the visit ID
   * @param priority the priority
   * @return the plan documents from visit ID
   */
  public List<BasicDynaBean> getPlanDocumentsFromVisitID(String visitID, Integer priority) {
    return DatabaseHelper.queryToDynaList(GET_PLAN_DOCUMENTS_FROM_VISIT_ID,
        new Object[] {visitID, priority});
  }
}
