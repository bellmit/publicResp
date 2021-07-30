package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PdfUtils;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;
import com.insta.hms.genericdocuments.PatientHVFDocValuesDAO;
import com.insta.hms.imageretriever.PatientImageRetriever;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class InsuranceDAO.
 *
 * @author pragna.p
 */
public class InsuranceDAO extends GenericDAO {

  /**
   * Instantiates a new insurance DAO.
   */
  public InsuranceDAO() {
    super("insurance_case");
  }

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InsuranceDAO.class);

  /** The get case details. */
  private String getCaseDetailsQuery = " SELECT ic.insurance_id, ic.mr_no, "
      + " ic.insurance_no, ic.case_added_date, "
      + " ic.policy_no, ic.policy_holder_name, ic.patient_relationship,"
      + " round(estv.amt,2) as estimate_amt, "
      + " ic.remarks, ic.status_reason, ic.status, ic.tpa_id,  "
      + " ic.finalized_date ,ic.diagnosis," + " tm.claim_template_id, tm.default_claim_template,  "
      + " tm.tpa_name, tm.tpa_pdf_form, ic.preauth_doc_id, " + " icd.insurance_id as claim_id,   "
      + " pr.patient_id, pr.insurance_id AS pat_insurance " + " FROM insurance_case ic   "
      + " LEFT JOIN patient_registration pr on (pr.insurance_id = ic.insurance_id) "
      + " LEFT JOIN tpa_master tm on (tm.tpa_id=ic.tpa_id)   "
      + " LEFT JOIN insurance_estimate_view estv on (estv.insurance_id=ic.insurance_id)   "
      + " LEFT JOIN insurance_claim_docs icd on (icd.insurance_id=ic.insurance_id)  "
      + " WHERE ic.insurance_id= ? ";

  /**
   * Gets the case details.
   *
   * @param insuranceId the insurance id
   * @return the case details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getCaseDetails(int insuranceId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(getCaseDetailsQuery, new Object[] { insuranceId });
  }

  /** The get visit case details. */
  private String getVisitCaseDetailsQuery = " SELECT ic1.insurance_id, "
      + " pd.mr_no, pr.patient_id, ic.insurance_no,"
      + " ic.case_added_date, ic.policy_no," + " ic.policy_holder_name, ic.patient_relationship,"
      + " round(estv.amt,2) as estimate_amt, ic.remarks,"
      + " ic.status_reason, ic.status, coalesce(pr.primary_sponsor_id,ic.tpa_id)as tpa_id, "
      + " ic.finalized_date,ic.diagnosis, tm.claim_template_id, "
      + " tm.default_claim_template, coalesce(tms.tpa_name, tm.tpa_name) as tpa_name,"
      + " tm.tpa_pdf_form, pr.patient_category_id, " + " pr.insurance_id AS pat_insurance "
      + " FROM patient_details pd   "
      + " LEFT JOIN patient_registration pr on pd.mr_no = pr.mr_no   "
      + " LEFT OUTER JOIN salutation_master s ON (pd.salutation = s.salutation_id)   "
      + " LEFT OUTER JOIN insurance_case ic on "
      + " (ic.mr_no = pr.mr_no  and pr.insurance_id is null)"
      + " LEFT OUTER JOIN insurance_case ic1 on (ic1.insurance_id = pr.insurance_id)    "
      + " LEFT JOIN insurance_estimate_view estv ON (estv.insurance_id=ic.insurance_id)   "
      + " LEFT JOIN tpa_master tm ON (tm.tpa_id=ic.tpa_id)   "
      + " LEFT JOIN tpa_master tms ON (tms.tpa_id=pr.primary_sponsor_id)   "
      + " WHERE ( (CASE WHEN pr.patient_id IS NOT NULL THEN 'A' "
      + " WHEN pd.previous_visit_id IS NOT NULL THEN 'I' ELSE 'N'  END)"
      + " IN ('A','N','I')) and pr.patient_id=?";

  /**
   * Gets the visit case details.
   *
   * @param visitId the visit id
   * @return the visit case details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getVisitCaseDetails(String visitId) throws SQLException {
    List list = DataBaseUtil.queryToDynaList(getVisitCaseDetailsQuery, visitId);
    if (list.size() > 0) {
      return (BasicDynaBean) list.get(0);
    }
    return null;
  }

  /** The get patient case details. */
  private String getPatientCaseDetailsQuery = "SELECT ic.insurance_id, "
      + " pd.mr_no, pd.visit_id AS patient_id,"
      + " ic1.insurance_no, ic1.case_added_date, ic1.policy_no,  "
      + " ic1.policy_holder_name, ic1.patient_relationship,"
      + " round(estv.amt,2) as estimate_amt, ic1.remarks,  "
      + " ic1.status_reason, ic1.status, (ic1.tpa_id)as tpa_id,   "
      + " ic1.finalized_date,ic1.diagnosis, tm.claim_template_id,    "
      + " tm.default_claim_template, ( tm.tpa_name) as tpa_name, tm.tpa_pdf_form,"
      + "  null AS patient_category_id,   " + " pr.insurance_id AS pat_insurance "
      + " FROM patient_details pd   "
      + " LEFT OUTER JOIN salutation_master S ON (pd.salutation = s.salutation_id) "
      + " LEFT OUTER JOIN insurance_case ic on (ic.mr_no = pd.mr_no) "
      + " LEFT OUTER JOIN patient_registration pr on "
      + " (pr.mr_no = ic.mr_no AND (pr.insurance_id is null OR pr.insurance_id=0) ) "
      + " LEFT JOIN insurance_estimate_view estv ON (estv.insurance_id=ic.insurance_id) "
      + " LEFT JOIN tpa_master tm ON (tm.tpa_id=ic.tpa_id) "
      + " LEFT JOIN tpa_master tms ON (tms.tpa_id=pr.primary_sponsor_id)   "
      + " LEFT JOIN patient_registration pz ON " + " (pz.insurance_id = ic.insurance_id AND"
      + " (pz.insurance_id is not null OR pz.insurance_id=0))"
      + " LEFT OUTER JOIN insurance_case ic1 on (ic1.insurance_id = pz.insurance_id) "
      + " WHERE ( (CASE WHEN pr.patient_id IS NOT NULL THEN 'A' "
      + " WHEN pd.previous_visit_id IS NOT NULL THEN 'I' ELSE 'N'  END)"
      + " IN ('A','N','I')) and pd.mr_no=?";

  /**
   * Gets the patient case details.
   *
   * @param mrNo the mr no
   * @return the patient case details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPatientCaseDetails(String mrNo) throws SQLException {
    List list = DataBaseUtil.queryToDynaList(getPatientCaseDetailsQuery, mrNo);
    if (list.size() > 0) {
      return (BasicDynaBean) list.get(0);
    }
    return null;
  }

  /**
   * Gets the ins details.
   *
   * @param whereValue     the where value
   * @param whereCondition the where condition
   * @return the ins details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getInsDetails(String whereValue, String whereCondition) throws SQLException {
    BasicDynaBean bean = null;
    List list;

    if (whereCondition.equals("Insurance")) {
      int insuranceId = Integer.parseInt(whereValue);
      list = DataBaseUtil.queryToDynaList(getCaseDetailsQuery, insuranceId);
    } else if (whereCondition.equals("Visit")) {
      list = DataBaseUtil.queryToDynaList(getVisitCaseDetailsQuery, whereValue);
    } else {
      list = DataBaseUtil.queryToDynaList(getPatientCaseDetailsQuery, whereValue);
    }

    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }

    return bean;
  }

  /** The Constant STATUS_FIELD. */
  private static final String STATUS_FIELD = " (CASE WHEN pr.patient_id IS NOT NULL THEN 'A' "
      + " WHEN pd.previous_visit_id IS NOT NULL THEN 'I' " + "      ELSE 'N' " + " END) ";

  /** The Constant FULL_NAME_FIELD. */
  private static final String FULL_NAME_FIELD = " (pdev.salutation || "
      + " ' ' || pd.patient_name || ' ' ||pd.last_name) ";

  /** The Constant INS_QUERY_FIELDS. */
  private static final String INS_QUERY_FIELDS = " SELECT pd.mr_no, pdev.patient_id, "
      + FULL_NAME_FIELD + " AS patient_name, "
      + "  get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + "  as patient_name, " + "  get_patient_age_in(pd.dateofbirth, pd.expected_dob) as agein, "
      + "  get_patient_age(pd.dateofbirth, pd.expected_dob) as age,"
      + "  pd.patient_gender, pd.patient_phone, "
      + "  pd.oldmrno, pdev.referer, pd.patient_phone, pdev.doctor_name, " + STATUS_FIELD
      + "  AS status, pdev.patient_id,"
      + "  pdev.reg_date, pdev.reg_time, pdev.visit_type, b.bill_no, ic.insurance_id ";

  /** The Constant INS_QUERY_COUNT. */
  private static final String INS_QUERY_COUNT = "SELECT count(pd.mr_no) ";

  /** The Constant INS_QUERY_TABLES. */
  private static final String INS_QUERY_TABLES = " FROM patient_details pd "
      + "  LEFT OUTER JOIN patient_visit_details_ext_view pdev on (pdev.mr_no = pd.mr_no) "
      + "  LEFT OUTER JOIN BILL b ON (b.visit_id=pdev.patient_id and b.status not in ('X','C') ) "
      + "  JOIN insurance_case ic on (ic.insurance_id =pr.insurance_id) ";

  /** The Constant PATIENT_CONFIDENTIALITY_FILTER_INIT_WHERE. */
  private static final String PATIENT_CONFIDENTIALITY_FILTER_INIT_WHERE = " WHERE "
      + " (patient_confidentiality_check(pd.patient_group,pd.mr_no)) ";

  /**
   * Gets the all mrnos.
   *
   * @param filter  the filter
   * @param listing the listing
   * @return the all mrnos
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getAllMrnos(Map filter, Map listing) throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, INS_QUERY_FIELDS, INS_QUERY_COUNT,
        INS_QUERY_TABLES, PATIENT_CONFIDENTIALITY_FILTER_INIT_WHERE, sortField, sortReverse,
        pageSize, pageNum);

    qb.addFilter(qb.STRING, "pd.mr_no", "=", filter.get("mr_no"));

    qb.addFilter(qb.STRING, FULL_NAME_FIELD, "ilike", filter.get("name"));
    qb.addFilter(qb.STRING, "pd.patient_phone", "=", filter.get("phone"));
    qb.addFilter(qb.STRING, "pd.oldmrno", "=", filter.get("oldReg"));

    qb.addFilter(qb.STRING, STATUS_FIELD, "IN", filter.get("status"));
    qb.addFilter(qb.STRING, "pdev.visit_type", "IN", filter.get("visit_type"));

    qb.addFilter(qb.STRING, "pdev.doctor_id", "IN", filter.get("doctor_id"));
    qb.addFilter(qb.STRING, "pdev.referer", "IN", filter.get("referrer"));

    qb.addFilter(qb.DATE, "date(pdev.reg_date)", ">=", filter.get("fdate"));
    qb.addFilter(qb.DATE, "date(pdev.reg_date)", "<=", filter.get("tdate"));
    qb.build();

    PagedList pagedList = qb.getMappedPagedList();

    qb.close();
    con.close();

    return pagedList;
  }

  /**
   * Edits the insurance case.
   *
   * @param con  the con
   * @param bean the bean
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int editInsuranceCase(Connection con, BasicDynaBean bean) throws SQLException {
    int resValue = 0;

    Map<String, Integer> keys = new HashMap<String, Integer>();
    keys.put("insurance_id", Integer.parseInt(bean.get("insurance_id").toString()));

    Map fields = bean.getMap();

    resValue = DataBaseUtil.dynaUpdate(con, "insurance_case", fields, keys);
    return resValue;
  }

  /**
   * Adds the insurance case.
   *
   * @param con  the con
   * @param bean the bean
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean addInsuranceCase(Connection con, BasicDynaBean bean) throws SQLException {
    boolean resValue;
    Map fields = bean.getMap();
    resValue = DataBaseUtil.dynaInsert(con, "insurance_case", fields);
    return resValue;
  }

  /**
   * Update patient registration.
   *
   * @param con       the con
   * @param bean      the bean
   * @param patientId the patient id
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int updatePatientRegistration(Connection con, BasicDynaBean bean, String patientId)
      throws SQLException {
    String tpaIsDifferentQuery = "UPDATE "
        + " patient_registration set insurance_id=?,primary_sponsor_id=?, "
        + " plan_id=0, primary_insurance_co=null, category_id=null where patient_id=?";
    String tpaIsSameQuery = "UPDATE patient_registration set insurance_id=? where patient_id=?";
    String tpa = (String) bean.get("tpa_id");

    BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", patientId);
    String currentVisitTpa = visitBean == null ? null
        : (String) visitBean.get("primary_sponsor_id");
    int resValue = 0;

    if (tpa != null && currentVisitTpa != null && tpa.equals(currentVisitTpa)) {
      try (PreparedStatement ps1 = con.prepareStatement(tpaIsSameQuery);) {
        ps1.setInt(1, Integer.parseInt((String.valueOf(bean.get("insurance_id")))));
        ps1.setString(2, patientId);
        resValue = ps1.executeUpdate();
      }
    } else {
      try (PreparedStatement ps1 = con.prepareStatement(tpaIsDifferentQuery)) {
        ps1.setInt(1, Integer.parseInt((String.valueOf(bean.get("insurance_id")))));
        ps1.setString(2, (String) bean.get("tpa_id"));
        ps1.setString(3, patientId);
        resValue = ps1.executeUpdate();
      }
    }

    return resValue;
  }

  /** The Constant ALL_CASES_QUERY_FIELDS. */
  private static final String ALL_CASES_QUERY_FIELDS = "SELECT IC.INSURANCE_ID, "
      + " null::text AS bill_no, " + " IC.INSURANCE_NO, PD.MR_NO,PR.PATIENT_ID," + "  coalesce"
      + " (get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name))"
      + " AS PATIENT_NAME,"
      + " PR.VISIT_TYPE AS VISIT_TYPE ,DEPT.DEPT_NAME,TM.TPA_NAME,TM.TPA_PDF_FORM,"
      + " PR.REG_DATE AS REG_DATE,ISS.STATUS_NAME,IC.STATUS,"
      + " pr.primary_sponsor_id AS PATIENT_TPA ";

  /** The Constant ALL_CASES_QUERY_COUNT. */
  private static final String ALL_CASES_QUERY_COUNT = "SELECT COUNT(IC.INSURANCE_ID)";

  /** The Constant ALL_CASES_QUERY_TABLES. */
  private static final String ALL_CASES_QUERY_TABLES = " FROM INSURANCE_CASE IC"
      + " LEFT OUTER JOIN PATIENT_REGISTRATION PR ON IC.INSURANCE_ID=PR.INSURANCE_ID "
      + " LEFT OUTER JOIN PATIENT_DETAILS PD ON IC.MR_NO=PD.MR_NO "
      + " LEFT OUTER JOIN SALUTATION_MASTER S ON PD.SALUTATION = S.SALUTATION_ID "
      + " LEFT OUTER JOIN DEPARTMENT DEPT on (DEPT.DEPT_ID = PR.DEPT_NAME)"
      + " JOIN TPA_MASTER TM ON TM.TPA_ID=IC.TPA_ID"
      + " LEFT OUTER JOIN INSURANCE_STATUS ISS ON ISS.STATUS_ID = IC.STATUS";

  /** The Constant ALL_CASES_WHERE_CLAUSE. */
  private static final String ALL_CASES_WHERE_CLAUSE = " WHERE "
      + " patient_confidentiality_check(pd.patient_group,pd.mr_no) ";

  /**
   * Gets the all insurance cases.
   *
   * @param filter  the filter
   * @param listing the listing
   * @return the all insurance cases
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getAllInsuranceCases(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, ALL_CASES_QUERY_FIELDS,
        ALL_CASES_QUERY_COUNT, ALL_CASES_QUERY_TABLES, ALL_CASES_WHERE_CLAUSE, sortField,
        sortReverse, pageSize, pageNum);

    qb.addFilter(qb.STRING, "pd.mr_no", "=", filter.get("mr_no"));
    qb.addFilter(qb.STRING, "coalesce(pd.patient_name||' '||pd.last_name)", "ilike",
        filter.get("name"));

    if (filter.get("status") != null) {
      qb.addFilter(qb.STRING, "ic.status", "IN", filter.get("status"));
    }

    if (filter.get("visit_type") != null) {
      qb.addFilter(qb.STRING, "pr.visit_type", "IN", filter.get("visit_type"));
    }

    if (filter.get("tpa_id") != null) {
      qb.addFilter(qb.STRING, "ic.tpa_id", "IN", filter.get("tpa_id"));
    }

    if (filter.get("gen_reg_date0") != null) {
      qb.addFilter(qb.DATE, "pr.reg_date", ">=", filter.get("gen_reg_date0"));
    }

    if (filter.get("gen_reg_date1") != null) {
      qb.addFilter(qb.DATE, "pr.reg_date", "<=", filter.get("gen_reg_date1"));
    }

    if (filter.get("insurance_id") != null && !(filter.get("insurance_id").equals(""))) {
      qb.addFilter(qb.INTEGER, "ic.insurance_id", "=",
          Integer.parseInt((String) filter.get("insurance_id")));
    }

    qb.build();

    PagedList pagedList = qb.getDynaPagedList();

    qb.close();
    con.close();

    return pagedList;
  }

  /** The Constant ALL_UNCONNECTED_CASES_QUERY_FIELDS. */
  private static final String ALL_UNCONNECTED_CASES_QUERY_FIELDS = " SELECT * ";

  /** The Constant ALL_UNCONNECTED_CASES_QUERY_COUNT. */
  private static final String ALL_UNCONNECTED_CASES_QUERY_COUNT = " SELECT COUNT(*) ";

  /** The Constant ALL_UNCONNECTED_CASES_QUERY_TABLE. */
  private static final String ALL_UNCONNECTED_CASES_QUERY_TABLE = " FROM  "
      + " ( SELECT  null::varchar AS bill_no, "
      + " CASE WHEN (SELECT bill_no FROM bill b WHERE status NOT IN ('C','X') AND "
      + " pdev.patient_id= b.visit_id and is_tpa = 'Y' LIMIT 1) IS NOT NULL"
      + " THEN 'Y' ELSE 'N' END AS pending, " + "  pd.mr_no,pdev.patient_id, "
      + " (s.salutation || ' ' || pd.patient_name || ' ' ||pd.last_name) as patient_name,"
      + " pdev.visit_type, " + "  dept.dept_name,tm.tpa_name,pdev.primary_sponsor_id,"
      + " pdev.reg_date,CASE WHEN pdev.status= 'I' THEN 'Inactive' "
      + " WHEN pdev.status='A' THEN 'Active' ELSE 'No Visits' END AS status_name, pdev.status, "
      + " (SELECT array_to_string  (array( " + "  SELECT tpa_name||'-'||insurance_id AS insu_case  "
      + "  FROM insurance_case ic " + "  JOIN tpa_master tm ON tm.tpa_id = ic.tpa_id "
      + "  WHERE ic.mr_no = pdev.mr_no AND ic.insurance_id  " + "  NOT IN ( "
      + " SELECT COALESCE(insurance_id, 0) " + " FROM patient_registration pr " + "  ) ) "
      + "  ,',')) AS insu_case " + "  FROM patient_details pd  "
      + "  LEFT OUTER JOIN patient_registration pdev on (pdev.mr_no = pd.mr_no ) "
      + "  LEFT OUTER JOIN DEPARTMENT DEPT on (DEPT.DEPT_ID = PDEV.DEPT_NAME)  "
      + "  LEFT OUTER JOIN SALUTATION_MASTER S ON PD.SALUTATION = S.SALUTATION_ID  "
      + "  LEFT JOIN TPA_MASTER TM ON TM.TPA_ID=pdev.primary_sponsor_id  "
      + "  WHERE  (op_type='M' AND "
      + " pdev.primary_sponsor_id IS NOT NULL AND pdev.primary_sponsor_id!='') "
      + "  AND (pdev.insurance_id IS NULL OR pdev.insurance_id = 0) AND"
      + "  ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))" + "  AS qry";

  /** The Constant ALL_UNCONNECTED_CASES_QUERY_WHERE. */
  private static final String ALL_UNCONNECTED_CASES_QUERY_WHERE = " WHERE pending = 'Y' ";

  /**
   * Gets the all unconnected insurance cases.
   *
   * @param filter  the filter
   * @param listing the listing
   * @return the all unconnected insurance cases
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getAllUnconnectedInsuranceCases(Map filter, Map listing)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();

    String sortField = (String) listing.get(LISTING.SORTCOL);
    boolean sortReverse = (Boolean) listing.get(LISTING.SORTASC);
    int pageSize = (Integer) listing.get(LISTING.PAGESIZE);
    int pageNum = (Integer) listing.get(LISTING.PAGENUM);

    SearchQueryBuilder qb = new SearchQueryBuilder(con, ALL_UNCONNECTED_CASES_QUERY_FIELDS,
        ALL_UNCONNECTED_CASES_QUERY_COUNT, ALL_UNCONNECTED_CASES_QUERY_TABLE,
        ALL_UNCONNECTED_CASES_QUERY_WHERE, sortField, sortReverse, pageSize, pageNum);

    qb.addFilter(qb.STRING, "mr_no", "=", filter.get("mr_no"));
    qb.addFilter(qb.STRING, "coalesce(pd.patient_name||' '||pd.last_name)", "ilike",
        filter.get("name"));

    qb.addFilter(qb.STRING, "status", "IN", filter.get("status"));

    qb.addFilter(qb.STRING, "visit_type", "IN", filter.get("visit_type"));

    qb.addFilter(qb.STRING, "primary_sponsor_id", "IN", filter.get("tpa_id"));

    if (filter.get("patient_id") != null) {
      qb.addFilter(qb.STRING, "patient_id", "ilike", filter.get("patient_id"));
    }

    if (filter.get("gen_reg_date0") != null) {
      qb.addFilter(qb.DATE, "reg_date", ">=", filter.get("gen_reg_date0"));
    }

    if (filter.get("gen_reg_date1") != null) {
      qb.addFilter(qb.DATE, "reg_date", "<=", filter.get("gen_reg_date1"));
    }

    if (filter.get("insurance_id") != null && !(filter.get("insurance_id").equals(""))) {
      qb.addFilter(qb.INTEGER, "insurance_id", "=",
          Integer.parseInt((String) filter.get("insurance_id")));
    }
    qb.addSecondarySort("mr_no");

    qb.build();

    PagedList pagedList = qb.getDynaPagedList();

    qb.close();
    con.close();

    return pagedList;
  }

  /** The Constant FOLLOW_UP_BILL_AMTS. */
  private static final String FOLLOW_UP_BILL_AMTS = "  SELECT "
      + " COALESCE(sum(bb.approval_amount),0) AS approved_amt,"
      + " COALESCE(sum(bb.total_amount),0) as bill_amt, "
      + "  COALESCE(sum(bb.total_claim),0) AS bill_claim_amt,"
      + " COALESCE(sum(bb.claim_recd_amount),0) AS claim_recd_amt, "
      + "  COALESCE(sum(insurance_deduction),0) AS  insurance_deduction " + "  FROM bill bb  "
      + "  WHERE  bb.is_tpa = 'Y' AND" + " bb.status NOT IN ('X','C')   " + "  AND bb.visit_id IN"
      + "( SELECT  patient_id FROM patient_registration "
      + " WHERE main_visit_id = ? AND patient_id != ? ) ";

  /**
   * Gets the followup amounts.
   *
   * @param visitId the visit id
   * @return the followup amounts
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static BasicDynaBean getFollowupAmounts(String visitId)
      throws SQLException, ParseException {
    List<BasicDynaBean> followupBillAmts = DataBaseUtil.queryToDynaList(FOLLOW_UP_BILL_AMTS,
        visitId, visitId);
    if ((followupBillAmts != null) && (followupBillAmts.size() > 0)) {
      return followupBillAmts.get(0);
    }
    return null;
  }

  /** The Constant CLAIM_BILL_AMTS. */
  private static final String CLAIM_BILL_AMTS = " SELECT foo.*, u.temp_username AS temp_username, "
      + " sp.procedure_code, sp.procedure_name, sp.procedure_limit " + " FROM "
      + " ( SELECT  null::text AS bill_no, " + " MAX(finalized_date) AS finalized_date,  "
      + " MAX(pr.bed_type) AS bed_type, MAX(procedure_no) AS procedure_no, "
      + " SUM(insurance_deduction) AS insurance_deduction,  "
      + " MAX(b.approval_amount) AS approval_amount , "
      + " COALESCE(SUM(b.total_amount),0) AS bill_amt,   "
      + " COALESCE(SUM(b.total_claim),0) AS bill_claim_amt,  "
      + " COALESCE(SUM(b.claim_recd_amount),0) AS claim_recd_amt,  "
      + " COALESCE(SUM(insurance_deduction),0) AS insurance_deduction, "
      + " MAX(b.username) AS username " + " FROM bill b  "
      + " LEFT JOIN patient_registration pr ON (pr.patient_id = b.visit_id) "
      + " WHERE b.bill_no IN ( ? ) " + " ) AS foo "
      + " LEFT JOIN u_user u ON (u.emp_username = foo.username) "
      + " LEFT JOIN sponsor_procedure_limit sp ON (sp.procedure_no = foo.procedure_no) ";

  /**
   * Gets the bill bean.
   *
   * @param indentedBills the indented bills
   * @return the bill bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getBillBean(String indentedBills) throws SQLException {
    BasicDynaBean bean = null;
    List list;
    list = DataBaseUtil.queryToDynaList(CLAIM_BILL_AMTS, indentedBills);
    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /** The Cliam query. */
  private static String CliamQuery = " SELECT "
      + " ic.insurance_id, ic.mr_no, COALESCE(ipr.patient_id, pr.patient_id) AS patient_id, "
      + " (SELECT array_to_string " + " (array( " + " SELECT bill_no " + " FROM bill bb "
      + " WHERE bb.visit_id IN " + " (SELECT patient_id " + " FROM patient_registration "
      + " WHERE " + " patient_id = ipr.patient_id " + " OR main_visit_id = ipr.patient_id ) "
      + " AND bb.status NOT IN ('X','C') " + " AND bb.is_tpa = 'Y' " + " ),',') "
      + " ) AS bill_no, " + " ict.template_type,ict.template_content,ict.template_name,ict.status, "
      + " icd.doc_content_html,icd.insurance_id as claim_docs_id, "
      + " tm.claim_template_id,tm.default_claim_template " + " FROM insurance_case ic "
      + " LEFT JOIN patient_registration pr ON (pr.mr_no = ic.mr_no AND pr.insurance_id IS NULL "
      + " AND op_type = 'M') "
      + " LEFT JOIN patient_registration ipr ON (ipr.insurance_id = ic.insurance_id) "
      + "  LEFT JOIN tpa_master tm ON (tm.tpa_id = ic.tpa_id) "
      + " LEFT JOIN insurance_claim_template ict ON (ict.claim_template_id=tm.claim_template_id) "
      + " LEFT JOIN insurance_claim_docs icd ON (icd.insurance_id=ic.insurance_id) "
      + " JOIN patient_details pd ON (pr.mr_no= pd.mr_no)" + " WHERE ic.insurance_id = ? AND "
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the claim content.
   *
   * @param insuranceId the insurance id
   * @return the claim content
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getClaimContent(int insuranceId) throws SQLException {
    BasicDynaBean bean = null;
    List list;
    list = DataBaseUtil.queryToDynaList(CliamQuery, insuranceId);
    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /** The claim html content query. */
  private static String claimHtmlContentQuery = "select "
      + " icd.doc_content_html,ict.template_name,tm.default_claim_template"
      + " FROM  insurance_claim_docs icd"
      + " JOIN insurance_case ic on ic.insurance_id = icd.insurance_id"
      + " JOIN tpa_master tm on tm.tpa_id = ic.tpa_id"
      + " LEFT OUTER JOIN insurance_claim_template ict "
      + " on ict.claim_template_id = tm.claim_template_id" + " WHERE icd.insurance_id=?";

  /**
   * Gets the claim template content.
   *
   * @param insuranceId the insurance id
   * @return the claim template content
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getClaimTemplateContent(int insuranceId) throws SQLException {
    BasicDynaBean bean = null;
    List list;
    list = DataBaseUtil.queryToDynaList(claimHtmlContentQuery, insuranceId);

    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /** The claim RTF content query. */
  private static String claimRTFContentQuery = "select "
      + " icd.doc_content_rtf,ict.template_name,tm.default_claim_template"
      + " FROM  insurance_claim_docs icd"
      + " JOIN insurance_case ic on ic.insurance_id = icd.insurance_id"
      + " JOIN tpa_master tm on tm.tpa_id = ic.tpa_id"
      + " LEFT OUTER JOIN insurance_claim_template ict on "
      + " ict.claim_template_id = tm.claim_template_id" + " WHERE icd.insurance_id=?";

  /**
   * Gets the claim RTF content.
   *
   * @param insuranceId the insurance id
   * @return the claim RTF content
   * @throws SQLException the SQL exception
   */
  public static List getClaimRTFContent(int insuranceId) throws SQLException {
    Connection con = null;
    ResultSet rs = null;
    PreparedStatement pstm = null;
    byte[] byteData = null;
    ArrayList alReturnValue = new ArrayList();
    try {
      con = DataBaseUtil.getConnection();
      pstm = con.prepareStatement(claimRTFContentQuery);
      pstm.setInt(1, insuranceId);
      rs = pstm.executeQuery();
      if ((rs != null) && rs.next()) {
        byteData = rs.getBytes(1);
        alReturnValue.add(byteData);
        if ((String) rs.getObject(3) == null || (String) rs.getObject(3) == "") {
          alReturnValue.add(rs.getObject(2));
        } else {
          alReturnValue.add("Default RTF Tempalte");
        }
      }
      alReturnValue.add("application/rtf");
    } finally {
      DataBaseUtil.closeConnections(con, pstm, rs);
    }
    return alReturnValue;
  }

  /** The Estimate docs query. */
  private static String EstimateDocsQuery = "select ie.estimate_id,sum(amt),insurance_id,"
      + " ie.updated_date,user_id from insurance_estimate ie"
      + " join estimate_header eh on ie.estimate_id = eh.estimate_id"
      + " where insurance_id=? group by " + " ie.estimate_id,insurance_id,ie.updated_date,user_id";

  /** The Preauth docs query. */
  private static String PreauthDocsQuery = "select ic.insurance_id,ic.preauth_doc_id,"
      + " doc_date,doc_name,ic.preauth_username from insurance_case ic "
      + " join patient_documents pd on pd.doc_id = ic.preauth_doc_id " + " where ic.insurance_id=?";

  /** The Claim docs query. */
  private static String ClaimDocsQuery = " select ic.insurance_id,ic.mr_no,"
      + " coalesce(ict.template_type,tm.default_claim_template )as template_type,"
      + " icd.insurance_id as claim_docs_id," + " ict.template_name,icd.doc_date,icd.username "
      + " FROM insurance_case ic" + " LEFT OUTER JOIN tpa_master tm on(tm.tpa_id=ic.tpa_id)"
      + " LEFT OUTER JOIN INSURANCE_CLAIM_TEMPLATE ict on "
      + " (ict.claim_template_id=tm.claim_template_id)" + " JOIN insurance_claim_docs icd on "
      + " (icd.insurance_id=ic.insurance_id) where ic.insurance_id=?";

  /**
   * Gets the estimate docs.
   *
   * @param insuranceId the insurance id
   * @return the estimate docs
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getEstimateDocs(int insuranceId) throws SQLException {
    return DataBaseUtil.queryToDynaList(EstimateDocsQuery, insuranceId);
  }

  /**
   * Gets the preauth docs.
   *
   * @param insuranceId the insurance id
   * @return the preauth docs
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPreauthDocs(int insuranceId) throws SQLException {
    return DataBaseUtil.queryToDynaList(PreauthDocsQuery, insuranceId);
  }

  /**
   * Gets the claim docs.
   *
   * @param insuranceId the insurance id
   * @return the claim docs
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getClaimDocs(int insuranceId) throws SQLException {
    return DataBaseUtil.queryToDynaList(ClaimDocsQuery, insuranceId);
  }

  /** The tpa docs query. */
  private static String tpa_docs_query = "select "
      + " ic.mr_no,tpa_doc_id,itd.insurance_id,document_name,"
      + " description,doc_recd_date,itd.created_by,content_type,original_extension "
      + " FROM insurance_tpa_docs itd"
      + " JOIN insurance_case ic on ic.insurance_id = itd.insurance_id "
      + " WHERE ic.insurance_id=?";

  /**
   * Gets the tpa docs.
   *
   * @param insuranceId the insurance id
   * @return the tpa docs
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTpaDocs(int insuranceId) throws SQLException {
    return DataBaseUtil.queryToDynaList(tpa_docs_query, insuranceId);
  }

  /** The History query. */
  private static String HistoryQuery = "SELECT "
      + " datetime,user_id,email_subject,'' as doc_title,'S' as doc_type"
      + ",it.transaction_id as docid,null as attachment_id " + " FROM insurance_transaction it"
      + " where insurance_id=? " + " UNION"
      + " SELECT doc_recd_date,created_by,description,document_name,"
      + " 'R' as doc_type,tpa_doc_id as docid"
      + " ,tpa_doc_id as attachment_id from insurance_tpa_docs "
      + " WHERE insurance_id=? order by docid,doc_type";

  /**
   * Gets the ins history.
   *
   * @param insuranceId the insurance id
   * @return the ins history
   * @throws SQLException the SQL exception
   */
  public static List getInsHistory(int insuranceId) throws SQLException {
    List list;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(HistoryQuery);
      ps.setInt(1, insuranceId);
      ps.setInt(2, insuranceId);
      list = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /** The Attachements query. */
  private static String AttachementsQuery = "select "
      + " ita.doc_title,ita.attachment_id,it.transaction_id as docid "
      + " FROM insurance_transaction_attachments ita"
      + " JOIN insurance_transaction it on it.transaction_id= ita.transaction_id"
      + " WHERE insurance_id=?";

  /**
   * Gets the ins attachements.
   *
   * @param insuranceId the insurance id
   * @return the ins attachements
   * @throws SQLException the SQL exception
   */
  public static List getInsAttachements(int insuranceId) throws SQLException {
    List list;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(AttachementsQuery);
      ps.setInt(1, insuranceId);
      list = DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /** The get all trans docs query. */
  private static String getAllTransDocsQuery = "select doc_title,attachment_id,content_type "
      + " from insurance_transaction_attachments where" + " transaction_id=?";

  /**
   * Gets the transaction docs.
   *
   * @param transactionId the transaction id
   * @return the transaction docs
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getTransactionDocs(int transactionId) throws SQLException {
    return DataBaseUtil.queryToDynaList(getAllTransDocsQuery, transactionId);
  }

  /** The get all insurance docs fields. */
  private static String getAllInsuranceDocs_Fields = "SELECT "
      + " ic.mr_no,pr.patient_id as visit_id,id.doc_id, id.doc_name"
      + ",id.doc_date,id.username, id.insurance_id,pd.doc_format,pd.doc_status, pd.template_id,"
      + "dv.status,dv.doc_type,dv.template_name, dv.specialized, dv.access_rights ";

  /** The get all insurance docs tables. */
  private static String getAllInsuranceDocs_Tables = " FROM doc_all_templates_view dv"
      + " JOIN patient_documents pd on"
      + " pd.template_id = dv.template_id and dv.doc_format = pd.doc_format"
      + " JOIN insurance_docs  id on (id.doc_id = pd.doc_id)"
      + " JOIN insurance_case ic on ic.insurance_id = id.insurance_id"
      + " LEFT OUTER JOIN patient_registration pr on pr.insurance_id = ic.insurance_id";

  /** The get all insurance docs count. */
  private static String getAllInsuranceDocs_Count = "select count(ic.mr_no)";

  /**
   * Gets the insurance docs.
   *
   * @param listingParams the listing params
   * @param extraParams   the extra params
   * @param specialized   the specialized
   * @return the insurance docs
   * @throws SQLException the SQL exception
   */
  public static PagedList getInsuranceDocs(Map listingParams, Map extraParams, Boolean specialized)
      throws SQLException {

    int totalRecords = 0;

    SearchQueryBuilder qb = null;

    Connection con = null;
    List list = null;
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, getAllInsuranceDocs_Fields, getAllInsuranceDocs_Count,
          getAllInsuranceDocs_Tables, null, null, null, false, pageSize, pageNum);

      // todo: should convert to int in the caller.
      qb.addFilter(SearchQueryBuilder.INTEGER, "id.insurance_id", "=",
          Integer.parseInt((String) extraParams.get("insurance_id")));
      qb.build();

      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      list = DataBaseUtil.queryToDynaList(psData);
      try (ResultSet rsCount = psCount.executeQuery();) {
        if (rsCount.next()) {
          totalRecords = rsCount.getInt(1);
        }
      }
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
    return new PagedList(list, totalRecords, pageSize, pageNum);
  }

  /** The get mrno details of ins docs. */
  private static String getMrnoDetailsOfInsDocs = "select ic.mr_no ,pr.patient_id as visit_id"
      + " from  insurance_case ic " + " LEFT OUTER JOIN patient_registration pr "
      + " on pr.insurance_id = ic.insurance_id" + " where ic.insurance_id=?";

  /**
   * Gets the mrno details from ins docs.
   *
   * @param insuranceId the insurance id
   * @return the mrno details from ins docs
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getMrnoDetailsFromInsDocs(int insuranceId) throws SQLException {

    BasicDynaBean bean = null;
    List list;
    list = DataBaseUtil.queryToDynaList(getMrnoDetailsOfInsDocs, insuranceId);

    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /**
   * Gets the document bytes.
   *
   * @param docidStr  the docid str
   * @param allFields the all fields
   * @param mrNo      the mr no
   * @param patientId the patient id
   * @return the document bytes
   * @throws SQLException             the SQL exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public static List getDocumentBytes(String docidStr, boolean allFields, String mrNo,
      String patientId) throws SQLException, IllegalArgumentException, IOException,
      DocumentException, XPathExpressionException, TransformerException {

    byte[] pdfbytes = null;

    if (docidStr == null || docidStr.equals("")) {
      throw new IllegalArgumentException("docidStr is null");
    }

    PatientDocumentsDAO dao = new PatientDocumentsDAO();
    BasicDynaBean patientdocbean = dao.getBean();
    List alReturnValue = new ArrayList();

    try {
      dao.loadByteaRecords(patientdocbean, "doc_id", Integer.parseInt(docidStr));
    } catch (NumberFormatException nfe) {
      // is captured but not thrown, thrown as a illegal exception in the
      // following statement in both the
      // conditions when docid is not an integer and when document is not
      // exists for an given docid.
    }

    if (patientdocbean.get("doc_id") == null) {
      throw new IllegalArgumentException("Document not found for: " + docidStr);
    }

    String format = patientdocbean.get("doc_format").toString();
    int docid = Integer.parseInt(docidStr);
    GenericDocumentsDAO generaldocdao = new GenericDocumentsDAO();
    BasicDynaBean generaldocbean = generaldocdao.findByKey("doc_id", docid);

    if (format.equals("doc_hvf_templates")) {

      Map ftlParamMap = new HashMap();
      Map patientDetails = new HashMap();
      GenericDocumentsFields.copyPatientDetails(patientDetails, mrNo, patientId, false);
      ftlParamMap.put("visitdetails", patientDetails);

      ftlParamMap.put("mr_no", mrNo);
      ftlParamMap.put("patient_id", patientId);
      ftlParamMap.put("modules_activated",
          ((Preferences) RequestContext.getSession().getAttribute("preferences"))
              .getModulesActivatedMap());
      ftlParamMap.put("fieldvalues", PatientHVFDocValuesDAO.getHVFDocValues(docid, allFields));
      ftlParamMap.put("patientDocDetails", patientdocbean);
      StringWriter writer = new StringWriter();
      try {
        Template template = AppInit.getFmConfig().getTemplate("PatientHVFDocumentPrint.ftl");
        template.process(ftlParamMap, writer);
      } catch (TemplateException te) {
        throw new IllegalArgumentException(te);
      }
      HtmlConverter hc = new HtmlConverter();
      BasicDynaBean prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
      Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
      pdfbytes = hc.getPdfBytes(writer.toString(), "Patient HVF Document Print",
          PrintConfigurationsDAO.getPatientDefaultPrintPrefs(), repeatPHeader, true, true, true,
          false);

      alReturnValue.add(pdfbytes);
      alReturnValue.add("Insurance Docuements");
      alReturnValue.add("application/pdf");

    } else if (format.equals("doc_rich_templates")) {
      BasicDynaBean prefs = PrintConfigurationsDAO.getPatientDefaultPrintPrefs();
      HtmlConverter hc = new HtmlConverter(new PatientImageRetriever());
      String content = (String) patientdocbean.get("doc_content_text");
      Boolean repeatPHeader = ((String) prefs.get("repeat_patient_info")).equals("Y");
      pdfbytes = hc.getPdfBytes(content, "", prefs, repeatPHeader, true, true, true, false);

      alReturnValue.add(pdfbytes);
      alReturnValue.add("Insurance Docuements");
      alReturnValue.add("application/pdf");

    } else if (format.equals("doc_pdf_form_templates")) {

      GenericDAO templatedao = new GenericDAO("doc_pdf_form_templates");
      BasicDynaBean templatebean = templatedao.getBean();
      templatedao.loadByteaRecords(templatebean, "template_id", patientdocbean.get("template_id"));

      Map<String, String> fields = new HashMap<String, String>();
      GenericDocumentsFields.copyPatientDetails(fields, mrNo, patientId, true);

      GenericDAO pdffieldsvaluesdao = new GenericDAO("patient_pdf_form_doc_values");
      List<BasicDynaBean> fieldslist = pdffieldsvaluesdao.listAll(null, "doc_id", docid);
      for (BasicDynaBean fieldsBean : fieldslist) {
        fields.put(fieldsBean.get("field_name").toString(),
            fieldsBean.get("field_value").toString());
      }
      java.io.InputStream pdf = (java.io.InputStream) templatebean.get("template_content");
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        PdfUtils.sendFillableForm(os, pdf, fields, true, null, null, null);
      } catch (DocumentException de) {
        throw new IllegalArgumentException(de);
      }
      pdfbytes = os.toByteArray();

      alReturnValue.add(pdfbytes);
      alReturnValue.add("Insurance Docuements");
      alReturnValue.add("application/pdf");

    } else if (format.equals("doc_rtf_templates")) {
      pdfbytes = DataBaseUtil
          .readInputStream((java.io.InputStream) patientdocbean.get("doc_content_bytea"));
      alReturnValue.add(pdfbytes);
      alReturnValue.add("Insurance Docuements");
      alReturnValue.add("application/rtf");

    } else {
      throw new IllegalArgumentException("Document not support pdf convertion");
    }
    return alReturnValue;
  }

  /** The tpa email ids. */
  private static String TPA_EMAIL_IDS = "select ic.insurance_id,ic.tpa_id,tm.email_id from "
      + " insurance_case ic" + " join tpa_master tm on ic.tpa_id=tm.tpa_id"
      + " where ic.insurance_id=?";

  /**
   * Gets the TPA email ids.
   *
   * @param insuranceId the insurance id
   * @return the TPA email ids
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getTPAEmailIds(String insuranceId) throws SQLException {

    BasicDynaBean bean = null;
    List list;
    list = DataBaseUtil.queryToDynaList(TPA_EMAIL_IDS, Integer.parseInt(insuranceId));

    if (list.size() > 0) {
      bean = (BasicDynaBean) list.get(0);
    }
    return bean;
  }

  /** The Constant GET_PREAUTH_DETAILS. */
  private static final String GET_PREAUTH_DETAILS = " SELECT "
      + " tm.tpa_id, tm.tpa_name,tm.tpa_pdf_form,"
      + " ip.pre_auth_id,ic.insurance_id,ic.insurance_no,ic.policy_no "
      + " FROM insurance_case ic"
      + " LEFT OUTER JOIN tpa_master tm ON (tm.tpa_id=ic.tpa_id)"
      + " LEFT OUTER JOIN insurance_preauth ip ON "
      + " (ip.insurance_id = ic.insurance_id) WHERE ic.insurance_id=?";

  /**
   * Gets the preauth form.
   *
   * @param insuranceId the insurance id
   * @return the preauth form
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPreauthForm(String insuranceId) throws SQLException {
    List list = DataBaseUtil.queryToDynaList(GET_PREAUTH_DETAILS, Integer.parseInt(insuranceId));
    return (BasicDynaBean) list.get(0);
  }

  /** The get all preauth docs fields. */
  private static String getAllPreauthDocs_Fields = "SELECT "
      + " ic.mr_no,pr.patient_id as visit_id,ic.preauth_doc_id as doc_id, ic.doc_name,"
      + " ic.doc_date, ic.insurance_id,pd.doc_format,pd.doc_status, pd.template_id,"
      + " dv.status,dv.doc_type,dv.template_name, dv.specialized, dv.access_rights ";

  /** The get all preauth docs tables. */
  private static String getAllPreauthDocs_Tables = " FROM doc_all_templates_view dv"
      + " JOIN patient_documents pd on "
      + " pd.template_id = dv.template_id and dv.doc_format = pd.doc_format"
      + " JOIN insurance_case ic on " + " ic.preauth_doc_id = pd.doc_id"
      + " LEFT OUTER JOIN patient_registration pr on " + " pr.insurance_id  = ic.insurance_id ";

  /** The get all preauth docs count. */
  private static String getAllPreauthDocs_Count = "select count(ic.preauth_doc_id)";

  /**
   * Gets the preauth form.
   *
   * @param listingParams the listing params
   * @param extraParams   the extra params
   * @param specialized   the specialized
   * @return the preauth form
   * @throws SQLException the SQL exception
   */
  public static PagedList getPreauthForm(Map listingParams, Map extraParams, Boolean specialized)
      throws SQLException {

    int totalRecords = 0;

    SearchQueryBuilder qb = null;

    Connection con = null;
    List list = null;
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, getAllPreauthDocs_Fields, getAllPreauthDocs_Count,
          getAllPreauthDocs_Tables, null, null, null, false, pageSize, pageNum);

      // todo: should convert to int in the caller.
      qb.addFilter(SearchQueryBuilder.INTEGER, "ic.insurance_id", "=",
          Integer.parseInt((String) extraParams.get("insurance_id")));
      Object userName = extraParams.get("username");
      userName = userName == null ? "" : userName;
      // 11.3 veracode security flaw fix
      // String UNRESTRICTED_AND_AUTHOR_DOCS = " (CASE WHEN access_rights = 'A' THEN
      // preauth_username='"
      // + userName.toString() + "' ELSE true END)";
      // qb.appendToQuery(UNRESTRICTED_AND_AUTHOR_DOCS);
      ArrayList typesList = new ArrayList();
      typesList.add(SearchQueryBuilder.STRING);
      ArrayList valuesList = new ArrayList();
      valuesList.add(userName);
      qb.appendExpression(
          " AND (CASE WHEN access_rights = 'A' THEN preauth_username= ? ELSE true END)", typesList,
          valuesList);
      qb.build();

      PreparedStatement psData = qb.getDataStatement();
      PreparedStatement psCount = qb.getCountStatement();
      list = DataBaseUtil.queryToDynaList(psData);
      try (ResultSet rsCount = psCount.executeQuery();) {
        if (rsCount.next()) {
          totalRecords = rsCount.getInt(1);
        }
      }
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }

    return new PagedList(list, totalRecords, pageSize, pageNum);
  }

  /** The Constant CHECK_IF_UNCONNECTED_CASE_EXISTS. */
  private static final String CHECK_IF_UNCONNECTED_CASE_EXISTS = " SELECT insurance_id "
      + " FROM insurance_case "
      + " WHERE mr_no = ? " + " AND tpa_id= ?  AND insurance_id NOT IN ( "
      + " SELECT insurance_id FROM patient_registration WHERE mr_no =? "
      + " AND insurance_id IS  NOT NULL AND insurance_id!=0 AND tpa_id=?) ";

  /**
   * Check if unconnected case exists.
   *
   * @param mrno  the mrno
   * @param tpaid the tpaid
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean checkIfUnconnectedCaseExists(String mrno, String tpaid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CHECK_IF_UNCONNECTED_CASE_EXISTS);
      ps.setString(1, mrno);
      ps.setString(2, tpaid);
      ps.setString(3, mrno);
      ps.setString(4, tpaid);
      List result = DataBaseUtil.queryToDynaList(ps);
      if (result == null || result.isEmpty()) {
        return true;
      } else {
        return false;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Adds the to dash board.
   *
   * @param con           the con
   * @param mrno          the mrno
   * @param tpaid         the tpaid
   * @param insuranceId   the insurance id
   * @param policyDetsMap the policy dets map
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public boolean addToDashBoard(Connection con, String mrno, String tpaid, int insuranceId,
      Map policyDetsMap) throws SQLException, ParseException {
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(
          "INSERT INTO INSURANCE_CASE (INSURANCE_ID,MR_NO,TPA_ID,CASE_ADDED_DATE,STATUS, "
              + " POLICY_NO,POLICY_HOLDER_NAME,POLICY_VALIDITY_START,POLICY_VALIDITY_END,"
              + " PATIENT_RELATIONSHIP,PRIOR_AUTH_ID, PRIOR_AUTH_MODE_ID)"
              + " VALUES(?,?,?,LOCALTIMESTAMP(0),?,?,?,?,?,?,?,?)");
      ps.setInt(1, insuranceId);
      ps.setString(2, mrno);
      ps.setString(3, tpaid);
      ps.setString(4, "P");
      ps.setString(5, policyDetsMap.get("policy_no") == null ? null
          : policyDetsMap.get("policy_no").toString());
      ps.setString(6, policyDetsMap.get("policy_holder_name") == null ? null
          : policyDetsMap.get("policy_holder_name").toString());
      ps.setDate(7, policyDetsMap.get("policy_validity_start") == null ? null
          : DataBaseUtil.parseDate(policyDetsMap.get("policy_validity_start").toString()));
      ps.setDate(8, policyDetsMap.get("policy_validity_end") == null ? null
          : DataBaseUtil.parseDate(policyDetsMap.get("policy_validity_start").toString()));
      ps.setString(9, policyDetsMap.get("patient_relationship") == null ? null
          : policyDetsMap.get("patient_relationship").toString());
      ps.setString(10, policyDetsMap.get("prior_auth_id") == null ? null
          : policyDetsMap.get("prior_auth_id").toString());
      if (policyDetsMap.get("prior_auth_mode_id") == null
          || policyDetsMap.get("prior_auth_mode_id").equals("")) {
        ps.setNull(11, java.sql.Types.INTEGER);
      } else {
        ps.setInt(11,
            policyDetsMap.get("prior_auth_mode_id") == null
                || policyDetsMap.get("prior_auth_mode_id").equals("") ? null
                    : (Integer) policyDetsMap.get("prior_auth_mode_id"));
      }
      int retValue = ps.executeUpdate();
      success = retValue > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return success;
  }

  /**
   * Gets the TPA document bytes.
   *
   * @param tpaDocId the tpa doc id
   * @return the TPA document bytes
   * @throws SQLException             the SQL exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  public static List getTPADocumentBytes(String tpaDocId)
      throws SQLException, IllegalArgumentException, IOException, DocumentException,
      XPathExpressionException, TransformerException {

    GenericDAO dao = new GenericDAO("insurance_tpa_docs");
    BasicDynaBean bean = dao.getBean();
    List alReturnValue = new ArrayList();
    dao.loadByteaRecords(bean, "tpa_doc_id", Integer.parseInt(tpaDocId));

    byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("doc_content"));

    alReturnValue.add(bytes);
    alReturnValue.add(bean.get("document_name"));

    if (bean.get("content_type") == null) {
      alReturnValue.add("image/gif");
    } else {
      alReturnValue.add(bean.get("content_type").toString());
    }

    return alReturnValue;
  }

  /**
   * Bill str delim cat.
   *
   * @param list  the list
   * @param delim the delim
   * @return the string
   */
  public static String billStrDelimCat(List<BasicDynaBean> list, String delim) {
    StringBuilder sb = new StringBuilder();
    String loopDelim = "";
    for (BasicDynaBean bill : list) {
      String billNumber = (String) bill.get("bill_no");
      sb.append(loopDelim);
      sb.append(billNumber);
      loopDelim = delim;
    }
    return sb.toString();
  }

  /** The get case bills for main visit. */
  private static String GET_CASE_BILLS_FOR_MAIN_VISIT = " SELECT "
      + " CASE WHEN (bb.visit_id = ?) THEN bill_no "
      + " ELSE bill_no || '(Followup) E\r\n' END AS bill_no "
      + " FROM bill bb WHERE bb.visit_id IN "
      + " (SELECT patient_id FROM patient_registration WHERE  "
      + " main_visit_id = ?  OR patient_id = ? )"
      + " AND bb.status NOT IN ('X','C') AND bb.is_tpa = 'Y'  ";

  /**
   * Gets the case bills for main visit.
   *
   * @param visitId the visit id
   * @return the case bills for main visit
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static String getCaseBillsForMainVisit(String visitId) throws SQLException, IOException {
    List<BasicDynaBean> billNosList = new ArrayList<BasicDynaBean>();
    billNosList = DataBaseUtil.queryToDynaList(GET_CASE_BILLS_FOR_MAIN_VISIT,
        new Object[] { visitId, visitId, visitId });
    return billStrDelimCat(billNosList, ",");
  }

  /** The Constant ALL_PATIENT_PLAN_DOCS. */
  public static final String ALL_PATIENT_PLAN_DOCS = " SELECT "
      + " doc_name, doc_date, pdd.doc_id, username, pip.plan_id, "
      + " pr.patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format,"
      + " content_type, pd.doc_type, username  " + " FROM patient_policy_details ppd "
      + " JOIN plan_docs_details pdd ON" + " ppd.patient_policy_id = pdd.patient_policy_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " LEFT JOIN patient_insurance_plans pip ON"
      + " (pip.patient_policy_id= ppd.patient_policy_id AND  " + " pip.plan_id = ppd.plan_id) "
      + " LEFT JOIN patient_registration pr ON (pip.patient_id = pr.patient_id)"
      + " LEFT JOIN patient_details pds ON (pds.mr_no = pr.mr_no)  ";

  /**
   * Gets the all patient plan card documents.
   *
   * @param mrNo the mr no
   * @return the all patient plan card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientPlanCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_PLAN_DOCS
        + " WHERE  pds.mr_no=? AND (pr.patient_id='' || pr.patient_id IS NULL) ", mrNo);
  }

  /**
   * Gets the all visit plan card documents.
   *
   * @param mrNo the mr no
   * @return the all visit plan card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitPlanCardDocuments(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_PLAN_DOCS + " WHERE pr.patient_id =? ", mrNo);
  }

  /**
   * Gets the all visit for mr no plan card documents.
   *
   * @param mrNo the mr no
   * @return the all visit for mr no plan card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitForMrNoPlanCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(
        ALL_PATIENT_PLAN_DOCS + " WHERE pds.mr_no=? and coalesce(pr.patient_id, '')!='' ", mrNo);
  }

  /** The Constant ALL_PATIENT_CORPORATE_DOCS. */
  public static final String ALL_PATIENT_CORPORATE_DOCS = " SELECT * "
      + " FROM (SELECT doc_name, doc_date, pcd.doc_id, username, pds.mr_no, "
      + " patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format,"
      + " content_type, pd.doc_type, username  " + " FROM patient_corporate_details pcdd "
      + " JOIN corporate_docs_details pcd ON"
      + " pcd.patient_corporate_id = pcdd.patient_corporate_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " JOIN patient_registration pr ON(pr.patient_corporate_id= pcdd.patient_corporate_id)"
      + " JOIN patient_details pds ON (pds.mr_no = pr.mr_no)  " + " UNION "
      + " SELECT doc_name, doc_date, pcd.doc_id, username, pds.mr_no, "
      + " patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format,"
      + " content_type, pd.doc_type, username  " + " FROM patient_corporate_details pcdd "
      + " JOIN corporate_docs_details pcd ON"
      + " pcd.patient_corporate_id = pcdd.patient_corporate_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " JOIN patient_registration pr ON"
      + " (pr.secondary_patient_corporate_id= pcdd.patient_corporate_id) "
      + " JOIN patient_details pds ON (pds.mr_no = pr.mr_no)) AS foo ";

  /**
   * Gets the all patient corporate card documents.
   *
   * @param mrNo the mr no
   * @return the all patient corporate card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientCorporateCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_CORPORATE_DOCS
        + " WHERE  foo.mr_no=? AND (foo.patient_id='' || foo.patient_id IS NULL) ", mrNo);
  }

  /**
   * Gets the all visit corporate card documents.
   *
   * @param mrNo the mr no
   * @return the all visit corporate card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitCorporateCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_CORPORATE_DOCS + " WHERE foo.patient_id =? ",
        mrNo);
  }

  /**
   * Gets the all visit for mr no corporate card documents.
   *
   * @param mrNo the mr no
   * @return the all visit for mr no corporate card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitForMrNoCorporateCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(
        ALL_PATIENT_CORPORATE_DOCS + " WHERE foo.mr_no=? and coalesce(foo.patient_id, '')!='' ",
        mrNo);
  }

  /** The Constant ALL_PATIENT_NATIONAL_DOCS. */
  public static final String ALL_PATIENT_NATIONAL_DOCS = " SELECT * "
      + " FROM (SELECT doc_name, doc_date, pnd.doc_id, username, pds.mr_no, "
      + " patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format, content_type,"
      + " pd.doc_type, username  " + " FROM patient_national_sponsor_details pnsd "
      + " JOIN national_sponsor_docs_details pnd ON"
      + " pnd.patient_national_sponsor_id = pnsd.patient_national_sponsor_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " JOIN patient_registration pr ON"
      + " (pr.patient_national_sponsor_id= pnd.patient_national_sponsor_id) "
      + " JOIN patient_details pds ON (pds.mr_no = pr.mr_no)  " + " UNION"
      + " SELECT doc_name, doc_date, pnd.doc_id, username, pds.mr_no, "
      + " patient_id, pr.status, pr.reg_date, pr.visit_type, doc_format,"
      + " content_type, pd.doc_type, username  " + " FROM patient_national_sponsor_details pnsd "
      + " JOIN national_sponsor_docs_details pnd ON"
      + " pnd.patient_national_sponsor_id = pnsd.patient_national_sponsor_id "
      + " JOIN patient_documents pd USING (doc_id)  "
      + " LEFT JOIN doc_all_templates_view dat USING (doc_format, template_id)  "
      + " LEFT JOIN doc_type dt ON (dt.doc_type_id = pd.doc_type)  "
      + " JOIN patient_registration pr ON"
      + " (pr.secondary_patient_national_sponsor_id= pnd.patient_national_sponsor_id) "
      + " JOIN patient_details pds ON (pds.mr_no = pr.mr_no)) AS foo ";

  /**
   * Gets the all patient national card documents.
   *
   * @param mrNo the mr no
   * @return the all patient national card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientNationalCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_NATIONAL_DOCS
        + " WHERE  foo.mr_no=? AND (foo.patient_id='' || foo.patient_id IS NULL) ", mrNo);
  }

  /**
   * Gets the all visit national card documents.
   *
   * @param mrNo the mr no
   * @return the all visit national card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitNationalCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(ALL_PATIENT_NATIONAL_DOCS + " WHERE foo.patient_id =? ",
        mrNo);
  }

  /**
   * Gets the all visit for mr no national card documents.
   *
   * @param mrNo the mr no
   * @return the all visit for mr no national card documents
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllVisitForMrNoNationalCardDocuments(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(
        ALL_PATIENT_NATIONAL_DOCS + " WHERE foo.mr_no=? and coalesce(foo.patient_id, '')!='' ",
        mrNo);
  }
}
