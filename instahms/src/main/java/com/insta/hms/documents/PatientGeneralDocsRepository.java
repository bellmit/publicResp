package com.insta.hms.documents;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

// RC TODO: This should not be GenericDocumentsRepository,
/**
 * The Class PatientGeneralDocsRepository.
 */
// instead should be GeneralDocumentsRepository
@Repository
public class PatientGeneralDocsRepository extends GenericRepository {

  /**
   * Instantiates a new patient general docs repository.
   */
  public PatientGeneralDocsRepository() {
    super("patient_general_docs");
  }

  /** The Constant ALL_GENERAL_DOC_FIELDS. */
  private static final String ALL_GENERAL_DOC_FIELDS =
      "SELECT pgd.doc_name, pgd.doc_id, pgd.doc_date, pgd.username, pgd.patient_id, "
          + " pr.status, pr.reg_date, pr.visit_type, "
          + " (CASE WHEN dat.template_id IS NULL THEN pd.doc_format "
          + "ELSE dat.doc_format END) as doc_format, "
          + " pd.doc_status, dat.template_id, dat.template_name, pd.doc_type,"
          + " dat.status, pd.content_type, "
          + " dt.doc_type_name,dat.access_rights, pd.doc_location,pr.reg_date, pd.center_id ";

  /** The Constant ALL_GENERAL_DOC_TABLES. */
  private static final String ALL_GENERAL_DOC_TABLES =
      "FROM patient_general_docs pgd " 
          + " JOIN patient_documents pd USING (doc_id) "
          + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id) "
          + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type) "
          + " LEFT JOIN patient_registration pr USING (patient_id) ";

  /** The Constant ALL_GENERAL_DOCS_WHERE_COND. */
  private static final String ALL_GENERAL_DOCS_WHERE_COND = " WHERE (dat.specialized=false or" 
          + " pd.template_id is null) ";

  /** The Constant ALL_PATIENT_DOCS. */
  public static final String ALL_PATIENT_DOCS = ALL_GENERAL_DOC_FIELDS + ALL_GENERAL_DOC_TABLES
      + ALL_GENERAL_DOCS_WHERE_COND + " AND pgd.mr_no=? AND pgd.patient_id='' ";

  /**
   * Gets the all patient documents.
   *
   * @param mrNo
   *          the mr no
   * @return the all patient documents
   */
  public static List<BasicDynaBean> getAllPatientDocuments(String mrNo) {
    return DatabaseHelper.queryToDynaList(ALL_PATIENT_DOCS, mrNo);
  }

  /** The Constant ALL_VISIT_DOCS. */
  public static final String ALL_VISIT_DOCS = ALL_GENERAL_DOC_FIELDS + ALL_GENERAL_DOC_TABLES
      + ALL_GENERAL_DOCS_WHERE_COND + " AND pgd.patient_id=?";

  /**
   * Gets the all visit documents.
   *
   * @param patientId
   *          the patient id
   * @return the all visit documents
   */
  public static List<BasicDynaBean> getAllVisitDocuments(String patientId) {
    return DatabaseHelper.queryToDynaList(ALL_VISIT_DOCS, patientId);
  }

  /**
   * Input stream to file.
   *
   * @param is
   *          the is
   * @param imgFile
   *          the img file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void inputStreamToFile(InputStream is, File imgFile) throws IOException {
    byte[] bytes = new byte[is.available()];
    try (FileOutputStream fos = new FileOutputStream(imgFile)) {
      while (true) {
        int res = is.read(bytes);
        if (res <= 0) {
          break;
        }
        fos.write(bytes, 0, res);
      }
    }
  }

  private static final String LIST_PATIENT_DOCS_BY_VISIT =
      " SELECT pgd.doc_id, pd.template_id, pd.doc_format, "
          + "COALESCE(pd.created_at, pgd.doc_date::timestamp) AS created_date, pgd.doc_name, "
          + "COALESCE(pd.mod_time, pgd.doc_date::timestamp) AS mod_date, "
          + "pgd.username, 'D' as item_type, pd.doc_type, dt.doc_type_name, ur.role_name, "
          + "pd.doc_status AS save_status, COALESCE(pgd.created_by, pgd.username) as created_by" 
          + "  FROM  patient_general_docs pgd"
          + "   JOIN patient_documents pd ON (pgd.doc_id = pd.doc_id)"
          + "   JOIN patient_registration pr ON (pr.patient_id = pgd.patient_id)"
          + "   JOIN doc_type dt ON (pd.doc_type = dt.doc_type_id)"
          + "   JOIN doc_type_category_mapping dtcm ON (dt.doc_type_id = dtcm.doc_type_id)"
          + "   JOIN u_user uu ON (uu.emp_username = pgd.username)"
          + "   JOIN u_role ur USING (role_id)"
          + " WHERE pr.patient_id=? AND dtcm.doc_type_category_id = ?"
          + " ORDER BY created_date DESC";

  /**
   * List patient documents.
   *
   * @param visitId the visit id
   * @param docCategory the doc category
   * @return the list
   */
  public List<BasicDynaBean> listPatientDocuments(String visitId, String docCategory) {
    List<BasicDynaBean> resultList =
        DatabaseHelper.queryToDynaList(LIST_PATIENT_DOCS_BY_VISIT, new Object[] {
            visitId, docCategory});
    if (resultList == null) {
      return Collections.emptyList();
    }
    return resultList;
  }

  private static final String GET_UPLOADED_DOC_LIST = 
      "Select " + "pd.doc_id," + "pgd.patient_id,"
      + "pgd.username," + "content_type," + "doc_type," + "original_extension,"
      + "doc_name, patient_id, username, center_id " + "FROM patient_documents pd "
      + "JOIN patient_general_docs pgd ON (pd.doc_id = pgd.doc_id) "
      + "WHERE doc_format = 'doc_fileupload' ";

  private static final String MrNoFilter = " AND mr_no = ? ";
  private static final String PatientIdFilter = " AND patient_id = ? AND center_id = ? ";

  /**
   * Get uploaded document types.
   * 
   * @param key
   *          key value
   * @param value
   *          value
   * @return returns list of uploaded documents
   */
  @SuppressWarnings({ "rawtypes" })
  public List getUploadedDocList(String key, String value) {
    String query = "";
    if (key.equals("mrNo")) {
      query = GET_UPLOADED_DOC_LIST + MrNoFilter;
      return DatabaseHelper.queryToDynaList(query, new Object[] { value });
    }
    query = GET_UPLOADED_DOC_LIST + PatientIdFilter;
    Integer centerId = RequestContext.getCenterId();
    return DatabaseHelper.queryToDynaList(query, new Object[] { value, centerId });

  }

  private static final String GET_UPLOADED_DOC_CONTENT = "SELECT doc_content_bytea FROM "
      + " patient_documents where doc_id = ? ";

  public BasicDynaBean getDocumentContent(Integer docId) {
    return DatabaseHelper.queryToDynaBean(GET_UPLOADED_DOC_CONTENT, new Object[] { docId });
  }

}
