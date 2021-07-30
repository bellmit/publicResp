package com.insta.hms.documents;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientDocumentRepository.
 */
@Repository
public class PatientDocumentRepository extends GenericRepository {

  /**
   * Instantiates a new patient document repository.
   */
  public PatientDocumentRepository() {
    super("patient_documents");
    // TODO Auto-generated constructor stub
  }

  /** The patientpdfformvaluesrepo. */
  @LazyAutowired
  private static PatientPdfFormValuesRepository patientpdfformvaluesrepo;

  /** The RedisTemplate. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /** The Constant GET_PATIENT_DOCUMENT. */
  public static final String GET_PATIENT_DOCUMENT = "SELECT doc_type, doc_status, center_id FROM "
      + "patient_documents WHERE doc_id = ?";

  // RC TODO : This method is not required. The caller can use findByKey()
  /**
   * Gets the patient document.
   *
   * @param docId the doc id
   * @return the patient document
   */
  // instead.
  public static BasicDynaBean getPatientDocument(int docId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_DOCUMENT, new Object[] { docId });
  }

  /** The Constant INSERT_PDF_FORM_FIELD_VALUES. */
  private static final String INSERT_PDF_FORM_FIELD_VALUES = "insert into "
      + "patient_pdf_form_doc_values(doc_id, field_name, field_value, username) "
      + " values(?, ?, ?, ?)";

  /** The Constant UPDATE_PDF_FORM_FIELD_VALUES. */
  private static final String UPDATE_PDF_FORM_FIELD_VALUES = "update patient_pdf_form_doc_values "
      + "set field_value=? where doc_id=? and field_name=?";

  /** The Constant GET_PDF_FORM_FIELD_VALUES. */
  private static final String GET_PDF_FORM_FIELD_VALUES = "select field_name from "
      + "patient_pdf_form_doc_values where doc_id = ?";

  /**
   * Update PDF form field values.
   *
   * @param params the params
   * @param docId the doc id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updatePDFFormFieldValues(Map params, Object docId) throws IOException {

    Map allFields = ConversionUtils.flatten(params);
    allFields.remove("mr_no");
    allFields.remove("patient_id");
    allFields.remove("pat_name");
    allFields.remove("dept_name");
    allFields.remove("method");
    allFields.remove("doc_date");
    allFields.remove("template_id");
    allFields.remove("doc_name");
    allFields.remove("format");
    allFields.remove("doc_id");

    Integer templateId = (params.get("template_id") != null
        && !((String[]) params.get("template_id"))[0].equals(""))
            ? Integer.parseInt(((String[]) params.get("template_id"))[0])
            : null;
    List<BasicDynaBean> imageTemplateFieldvalues = PatientPdfDocImagesRepository
        .getPDFTemplateImageValues(templateId);
    String userName = params.get("user_name") == null ? ""
        : ((String[]) params.get("user_name"))[0];
    userName = userName == null ? "" : userName;

    if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {
      for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
        allFields.remove("field_id" + "_" + i);
        allFields.remove("field_input" + "_" + i);
        allFields.remove("device_ip" + "_" + i);
        allFields.remove("device_info" + "_" + i);
        allFields.remove("fieldImgText" + "_" + i);
      }
    }

    BasicDynaBean insertBean = patientpdfformvaluesrepo.getBean();
    List<BasicDynaBean> insertBeanList = new ArrayList<>();
    BasicDynaBean updateBean = patientpdfformvaluesrepo.getBean();
    List<BasicDynaBean> updateBeanList = new ArrayList<>();
    Map<String, Object> updateKeys = new HashMap<>();
    boolean exists = false;
    Map<String, Boolean> savedFields = new HashMap<>();
    List<BasicDynaBean> fieldList = DatabaseHelper.queryToDynaList(GET_PDF_FORM_FIELD_VALUES,
        new Object[] { docId });
    if (fieldList.size() > 0) {
      for (int i = 0; i < fieldList.size(); i++) {
        BasicDynaBean bean = fieldList.get(i);
        savedFields.put((String) bean.get("field_name"), false);
      }
      for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
        if (((String) e.getKey()).startsWith("_")) {
          // _ names are supposed to be temp variables and skipped.
          continue;
        }
        for (int i = 0; i < fieldList.size(); i++) {
          BasicDynaBean bean = fieldList.get(i);

          if (bean.get("field_name").equals(e.getKey())) {
            updateBean.set("field_value", e.getValue());
            updateKeys.put("doc_id", docId);
            updateKeys.put("field_name", e.getKey());
            updateBeanList.add(updateBean);
            savedFields.put((String) bean.get("field_name"), true); // updated
            // fields
            exists = true;
            break;
          }
        }
        if (!exists) {
          insertBean.set("field_value", e.getValue());
          insertBean.set("doc_id", docId);
          insertBean.set("username", userName);
          insertBean.set("field_name", e.getKey());
          insertBeanList.add(updateBean);
        }
        exists = false;
      }
      // updating checkbox fields with no, which were saved earlier
      // transaction, and want to uncheck the values now.
      for (Map.Entry<String, Boolean> e : savedFields.entrySet()) {
        if (!e.getValue()) {
          updateBean.set("field_value", "");
          updateKeys.put("doc_id", docId);
          updateKeys.put("field_name", e.getKey());
          updateBeanList.add(updateBean);
        }
      }

    } else {
      for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
        if (((String) e.getKey()).startsWith("_")) {
          // _ names are supposed to be temp variables and skipped.
          continue;
        }
        insertBean.set("field_value", e.getValue());
        insertBean.set("doc_id", docId);
        insertBean.set("username", userName);
        insertBean.set("field_name", e.getKey());
        insertBeanList.add(updateBean);
      }
    }
    int[] updateResults = batchUpdate(updateBeanList, updateKeys);
    int[] insertResults = batchInsert(insertBeanList);

    return true;
  }

  /** The Constant GET_DOC_FORMAT. */
  private static final String GET_DOC_FORMAT = " select pd.doc_format from patient_documents pd "
      + " JOIN doc_all_templates_view datv ON (pd.template_id = datv.template_id) "
      + " where doc_id = ? ";

  /**
   * Gets the doc format from doc id.
   *
   * @param docId the doc id
   * @return the doc format from doc id
   */
  public BasicDynaBean getDocFormatFromDocId(Integer docId) {
    return DatabaseHelper.queryToDynaBean(GET_DOC_FORMAT, new Object[] { docId });
  }

  /** The Constant CHECK_ISR_PATIENT. */
  private static final String CHECK_ISR_PATIENT = "SELECT incoming_visit_id FROM test_documents td"
      + " JOIN tests_prescribed tp USING(prescribed_id)"
      + " JOIN incoming_sample_registration isr ON(isr.incoming_visit_id = tp.pat_id)"
      + " where doc_id = :docid"
      + " UNION ALL" + " SELECT incoming_visit_id FROM service_documents sd"
      + " JOIN services_prescribed sp USING(prescription_id)"
      + " JOIN incoming_sample_registration isr ON(isr.incoming_visit_id = sp.patient_id)"
      + " WHERE doc_id = :docid";
  
  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_GENERAL_DOCS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_GENERAL_DOCS = "SELECT mr_no"
      + " FROM patient_general_docs WHERE doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_REGISTRATION. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_REGISTRATION = "SELECT mr_no"
      + " FROM patient_registration "
      + " WHERE doc_id = :docid OR discharge_doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_PATIENT_REGISTRATION_CARDS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_PATIENT_REGISTRATION_CARDS = "SELECT"
      + " mr_no FROM patient_registration_cards prc"
      + " JOIN patient_registration using (patient_id) " + " WHERE prc.doc_id = :docid";
  
  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_TEST_DOCUMENTS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_TEST_DOCUMENTS = "SELECT pr.mr_no "
      + "FROM test_documents td "
      + " JOIN tests_prescribed tp USING(prescribed_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
      + " WHERE td.doc_id = :docid ;";
  
  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_SERVICE_DOCUMENTS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_SERVICE_DOCUMENTS = "SELECT "
      + "pr.mr_no FROM service_documents sd "
      + " JOIN services_prescribed sp using(prescription_id) "
      + " JOIN patient_registration pr using(patient_id) " + " WHERE sd.doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_OPERATION_DOCUMENTS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_OPERATION_DOCUMENTS = "SELECT "
      + " mr_no FROM operation_documents od "
      + " JOIN bed_operation_schedule bos ON (bos.prescribed_id = od.prescription_id)"
      + " WHERE doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_CORPORATE_DOCS_DETAILS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_CORPORATE_DOCS_DETAILS = "SELECT "
      + " mr_no FROM corporate_docs_details "
      + " JOIN patient_corporate_details USING(patient_corporate_id)" + " WHERE doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_NATIONAL_SPONSOR_DD. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_NATIONAL_SPONSOR_DD = "SELECT mr_no "
      + " FROM national_sponsor_docs_details nsdd "
      + " JOIN patient_registration pr using(patient_national_sponsor_id) "
      + " WHERE nsdd.doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_PLAN_DOC_DETAILS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_PLAN_DOC_DETAILS = "SELECT mr_no"
      + " FROM plan_docs_details "
      + " JOIN patient_insurance_plans using(patient_policy_id) " + " WHERE doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_INSURANCE_DOCS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_INSURANCE_DOCS = "SELECT mr_no"
      + " FROM insurance_docs "
      + " JOIN insurance_case USING(insurance_id)" + " WHERE doc_id = :docid";

  /** The Constant GET_ASSOCIATED_MR_NO_LIST_FROM_DIET_CHART_DOCUMENTS. */
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_DIET_CHART_DOCUMENTS = "SELECT"
      + " mr_no FROM diet_chart_documents dcd " + " JOIN patient_registration pr USING(patient_id)"
      + " WHERE dcd.doc_id = :docid";

  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_OUTPATIENT_DOCS = 
      "SELECT mr_no FROM outpatient_docs od "
      + " JOIN doctor_consultation dc on od.consultation_id=dc.consultation_id "
      + " WHERE od.doc_id=:docid";  
  
  private static final String GET_ASSOCIATED_MR_NO_LIST_FROM_MRD_OBSERVATIONS = 
      "SELECT mr_no FROM patient_registration pr "
      + "JOIN bill b ON (b.visit_id = pr.patient_id) "
      + "JOIN bill_charge bc on (bc.bill_no = b.bill_no) "
      + "JOIN mrd_observations mo ON (mo.charge_id = bc.charge_id) WHERE mo.document_id = :docid";

  /**
   * Gets the associated mr no.
   *
   * @param docId the doc id
   * @return the associated mr no
   */
  public List<String> getAssociatedMrNo(List<String> docId) {

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("docid", docId, java.sql.Types.INTEGER);

    List<BasicDynaBean> listBeanMrNo = DatabaseHelper
        .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_GENERAL_DOCS, parameters);
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_REGISTRATION,
          parameters);
    }

    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_PATIENT_REGISTRATION_CARDS, parameters);
    }

    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_TEST_DOCUMENTS,
          parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_SERVICE_DOCUMENTS, parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_NATIONAL_SPONSOR_DD, parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_OPERATION_DOCUMENTS, parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_CORPORATE_DOCS_DETAILS, parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(
          GET_ASSOCIATED_MR_NO_LIST_FROM_PLAN_DOC_DETAILS,parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_INSURANCE_DOCS,
          parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper
          .queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_DIET_CHART_DOCUMENTS, parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_OUTPATIENT_DOCS,
          parameters);
    }
    if (listBeanMrNo.isEmpty()) {
      listBeanMrNo = DatabaseHelper.queryToDynaList(GET_ASSOCIATED_MR_NO_LIST_FROM_MRD_OBSERVATIONS,
          parameters);
    }
    List<String> mrNos = null;
    if (!listBeanMrNo.isEmpty()) {
      mrNos = new ArrayList<>(listBeanMrNo.size());
      for (BasicDynaBean bean : listBeanMrNo) {
        String thisMrNo = (String) bean.get("mr_no");
        mrNos.add(thisMrNo);
      }
    } else {
      List<BasicDynaBean> listBeanISR = DatabaseHelper.queryToDynaList(CHECK_ISR_PATIENT,
          parameters);
      if (!listBeanISR.isEmpty()) {
        mrNos = Arrays.asList("ISR");
      }
      HttpSession session = RequestContext.getSession();
      if (session != null) {
        String schema = RequestContext.getSchema();
        String userid = (String)session.getAttribute("userId");
        String redisKey = "docId:" + docId.get(0) + "userId:" + userid + "sch:" + schema;
        String redisData = (String) redisTemplate.opsForValue().get(redisKey);
        if (redisData != null) {
          mrNos = Arrays.asList("NEWDOCUPLOAD");
        }
      }
    }
    return mrNos;
  }

}
