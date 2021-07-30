package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.DuplicateEntityException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FormComponentsRepository.
 *
 * @author anup vishwas
 */

@Repository
public class FormComponentsRepository extends GenericRepository {

  /** The form department details repository. */
  @LazyAutowired
  private FormDepartmentDetailsRepository formDepartmentDetailsRepository;

  /** The form center repository. */
  @LazyAutowired
  private FormComponentsCenterApplicabilityRepository formCenterRepository;

  /** The form names. */
  private Map<String, String> formNames;

  /** The Constant FORM_CONS. */
  private static final String FORM_CONS = "Form_CONS";

  /** The Constant FORM_IP. */
  private static final String FORM_IP = "Form_IP";

  /** The Constant FORM_TRI. */
  private static final String FORM_TRI = "Form_TRI";

  /** The Constant FORM_IA. */
  private static final String FORM_IA = "Form_IA";

  /** The Constant FORM_OT. */
  private static final String FORM_OT = "Form_OT";

  /** The Constant FORM_SERV. */
  private static final String FORM_SERV = "Form_Serv";

  /** The Constant FORM_GEN. */
  private static final String FORM_GEN = "Form_Gen";

  /** The Constant FORM_OP_FOLLOW_UP_CONS. */
  private static final String FORM_OP_FOLLOW_UP_CONS = "Form_OP_FOLLOW_UP_CONS";

  /** The Constant FORM_TYPE. */
  private static final String FORM_TYPE = "form_type";

  /** The Constant FORM_NAME. */
  private static final String FORM_NAME = "form_name";

  /** The Constant DEPT_NAME. */
  private static final String DEPT_NAME = "dept_name";

  /**
   * Instantiates a new form components repository.
   */
  public FormComponentsRepository() {
    super("form_components");
    this.formNames = new HashMap<>();
    this.formNames.put(FORM_CONS, "OP Forms");
    this.formNames.put(FORM_IP, "IP Forms");
    this.formNames.put(FORM_TRI, "Triage Forms");
    this.formNames.put(FORM_IA, "Assessment Forms");
    this.formNames.put(FORM_OT, "Surgery Forms");
    this.formNames.put(FORM_SERV, "Service Forms");
    this.formNames.put(FORM_GEN, "Generic Forms");
    this.formNames.put(FORM_OP_FOLLOW_UP_CONS, "OP Follow Up Consultation Form");
  }

  /** The Constant GET_ACTIVE_FORMS. */
  private static final String GET_ACTIVE_FORMS = " SELECT foo.section_id::int as section_id, "
      + " foo.id as form_id, form_name " + " FROM "
      + " (SELECT fc.id, fc.form_name, regexp_split_to_table(fc.sections, ',') as section_id, "
      + " generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1)) "
      + " as display_order " + " FROM form_components fc, form_department_details fdd "
      + " where fdd.dept_id=? and fc.form_type=? AND istemplate=false and fdd.id=fc.id #) as foo "
      + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
      + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";

  /**
   * Form component details.
   *
   * @param deptId
   *          the dept id
   * @param formatType
   *          the format type
   * @param operationId
   *          the operation id
   * @return the list
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> formComponentDetails(String deptId, String formatType,
      String operationId) {
    StringBuilder query = null;
    List list = new ArrayList();
    list.add(deptId);
    list.add(formatType);
    if (operationId != null) {
      query = new StringBuilder(GET_ACTIVE_FORMS.replace("#", "AND fc.operation_id=?"));
      list.add(operationId);
    } else {
      query = new StringBuilder(GET_ACTIVE_FORMS.replace("#", ""));
    }
    return DatabaseHelper.queryToDynaList(query.toString(), list.toArray());
  }

  /** The Constant GET_COMPONENTS_CENTERWISE. */
  private static final String GET_COMPONENTS_CENTERWISE = " SELECT cc.form_name, cc.id, "
      + " case when form_type = 'Form_Serv' then sd.department else d.dept_name end as dept_name,"
      + " fdd.dept_id, cc.form_type, cc.sections, cc.group_patient_sections, cc.operation_id,"
      + " om.operation_name, cc.service_id, s.service_name, cc.immunization, cc.print_template_id,"
      + " cc.status, cc.doctor_id, doc.doctor_name, cc.istemplate " + " FROM form_components cc"
      + " JOIN form_department_details fdd ON (fdd.id=cc.id)"
      + " LEFT JOIN department d ON (fdd.dept_id=d.dept_id)"
      + " LEFT JOIN services_departments sd ON (fdd.dept_id=sd.serv_dept_id::text)"
      + " LEFT JOIN services s ON (s.service_id=cc.service_id)"
      + " LEFT JOIN operation_master om ON (cc.operation_id=om.op_id) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id=cc.doctor_id) "
      + " LEFT JOIN form_components_center_applicability fcca ON (fcca.form_components_id=cc.id)"
      + " WHERE fcca.status = 'A' AND (fcca.center_id = 0 OR fcca.center_id = ?) "
      + " ORDER BY cc.form_name";

  /**
   * Gets the components center wise.
   *
   * @param centerId
   *          the center id
   * @return the components center wise
   */
  public List<BasicDynaBean> getComponentsCenterWise(int centerId) {
    return DatabaseHelper.queryToDynaList(GET_COMPONENTS_CENTERWISE, new Object[] { centerId });
  }

  /** The Constant GET_COMPONENTS. */
  private static final String GET_COMPONENTS = " SELECT cc.form_name, cc.id, "
      + " case when form_type = 'Form_Serv' then sd.department else d.dept_name end as dept_name,"
      + " fdd.dept_id, cc.form_type, cc.sections, cc.group_patient_sections, cc.operation_id,"
      + " om.operation_name, cc.service_id, s.service_name, cc.immunization, cc.print_template_id, "
      + " cc.status, cc.doctor_id, doc.doctor_name, cc.istemplate " + " FROM form_components cc"
      + " JOIN form_department_details fdd ON (fdd.id=cc.id)"
      + " LEFT JOIN department d ON (fdd.dept_id=d.dept_id)"
      + " LEFT JOIN services_departments sd ON (fdd.dept_id=sd.serv_dept_id::text)"
      + " LEFT JOIN services s ON (s.service_id=cc.service_id)"
      + " LEFT JOIN operation_master om ON (cc.operation_id=om.op_id) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id=cc.doctor_id) " + " ORDER BY cc.form_name";

  /**
   * Gets the components.
   *
   * @return the components
   */
  public List<BasicDynaBean> getComponents() {
    return DatabaseHelper.queryToDynaList(GET_COMPONENTS);
  }

  /** The Constant SECTION_FOR_FORM_ID. */
  private static final String SECTION_FOR_FORM_ID = " SELECT  "
      + " COALESCE(sgs.section_name, sm.section_title) as section_title,"
      + "  section.section_id, sm.status, sm.linked_to, display_order"
      + " FROM (SELECT trim(regexp_split_to_table(sections, E',')) as section_id, id,"
      + "   generate_series(1, array_upper(regexp_split_to_array(sections, E','), 1)) "
      + " as display_order " + "     FROM form_components) as section "
      + " LEFT JOIN section_master sm ON (section.section_id=sm.section_id::text) "
      + " LEFT JOIN system_generated_sections sgs ON (section.section_id=sgs.section_id::text) "
      + " WHERE section.id=? " + " ORDER BY display_order";

  /**
   * Gets the sections.
   *
   * @param formId
   *          the form id
   * @return the sections
   */
  public List<BasicDynaBean> getSections(int formId) {
    return DatabaseHelper.queryToDynaList(SECTION_FOR_FORM_ID, new Object[] { formId });
  }

  /**
   * Checks if is duplicate form.
   *
   * @param bean
   *          the bean
   * @param deptId
   *          the dept id
   * @param newFormRule
   *          the new form rule
   * @return true, if is duplicate form
   */
  public boolean isDuplicateForm(BasicDynaBean bean, String deptId, boolean newFormRule) {
    String formType = (String) bean.get(FORM_TYPE);
    String query = "SELECT * FROM form_components fc, form_department_details fdd "
        + "where fc.id=fdd.id and ";
    BasicDynaBean found = null;

    if (!formType.equals(FORM_GEN)) {
      String formQuery = query;
      formQuery += "form_name=? AND form_type=? ";
      found = DatabaseHelper.queryToDynaBean(formQuery,
          new Object[] { bean.get(FORM_NAME), formType });
    }

    if (found != null && !found.get("id").equals(bean.get("id"))) {
      return true;
    }

    if (formType.equals(FORM_TRI)) {
      query += "dept_id=? AND form_type=? AND istemplate=false ";
      if (newFormRule) {
        return false;
      }
      found = DatabaseHelper.queryToDynaBean(query, new Object[] { deptId, bean.get(FORM_TYPE) });
    } else if (formType.equals(FORM_SERV)) {
      query += "service_id=? AND dept_id=? AND form_type=?";
      found = DatabaseHelper.queryToDynaBean(query,
          new Object[] { bean.get("service_id"), deptId, bean.get(FORM_TYPE) });
    } else if (formType.equals(FORM_OT)) {
      query += "operation_id=? AND dept_id=? AND form_type=?";
      found = DatabaseHelper.queryToDynaBean(query,
          new Object[] { bean.get("operation_id"), deptId, bean.get(FORM_TYPE) });
    } else if (formType.equals(FORM_CONS) || (formType.equals(FORM_OP_FOLLOW_UP_CONS))) {
      if (!newFormRule) {
        String centerQuery = "Select * from form_components_center_applicability"
            + " where form_components_id=?";
        List<BasicDynaBean> centerFormBeans = DatabaseHelper.queryToDynaList(centerQuery,
            bean.get("id"));
        List<Integer> centerIds = new ArrayList<>();
        for (BasicDynaBean record : centerFormBeans) {
          centerIds.add((Integer) record.get("center_id"));
        }
        if (!centerFormBeans.isEmpty()) {
          query = "SELECT fc.id FROM form_components fc "
              + "JOIN form_department_details fdd USING (id) "
              + "JOIN form_components_center_applicability fcca ON"
              + " (fcca.form_components_id=fc.id AND center_id IN (:centerIds)) "
              + "WHERE doctor_id=:doctorId AND dept_id=:deptId AND"
              + " form_type=:formType AND istemplate=false " + "Group BY id";
          MapSqlParameterSource parameters = new MapSqlParameterSource();
          parameters.addValue("doctorId", bean.get("doctor_id"));
          parameters.addValue("deptId", deptId);
          parameters.addValue("formType", formType);
          parameters.addValue("centerIds", centerIds);
          List<BasicDynaBean> temp = DatabaseHelper.queryToDynaList(query, parameters);
          if ((temp.size() == 1 && !temp.get(0).get("id").equals(bean.get("id")))
              || temp.size() > 1) {
            return true;
          }
        } else {
          query = "SELECT fc.id FROM form_components fc "
              + "JOIN form_department_details fdd USING (id) "
              + "JOIN form_components_center_applicability fcca ON"
              + " (fcca.form_components_id=fc.id AND center_id=0) "
              + "WHERE fc.id=fdd.id and doctor_id=? AND dept_id=? AND form_type=?"
              + " AND istemplate=false";
          found = DatabaseHelper.queryToDynaBean(query, bean.get("doctor_id"), deptId, formType);
        }
      }
    } else if (!formType.equals(FORM_GEN) && !newFormRule) {
      //Allow to create multiple IP Template form.
      query += "dept_id=? AND form_type=? AND istemplate=false ";
      found = DatabaseHelper.queryToDynaBean(query, new Object[] { deptId, bean.get(FORM_TYPE) });
    }

    if (found != null && !found.get("id").equals(bean.get("id"))) {
      return true;
    }

    return false;
  }

  /** The Get department. */
  private static final String GET_DEPARTMENT = "SELECT dept_name from department WHERE dept_id=?";

  /** The serv depts. */
  private static final String SERV_DEPTS = "SELECT department as dept_name "
      + "FROM services_departments WHERE serv_dept_id::text=?";

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.mdm.MasterRepository#throwDuplicateEntityException(org.apache.commons.beanutils
   * .BasicDynaBean)
   * 
   * Throwing the custom exception message
   */
  protected void throwDuplicateEntityException(BasicDynaBean bean, String deptId,
      boolean newFormRule) {
    String formType = (String) bean.get(FORM_TYPE);
    if (formType.equals(FORM_SERV)) {
      String deptName = "All";
      if (!deptId.equals("-1")) {
        BasicDynaBean deptBean = DatabaseHelper.queryToDynaBean(SERV_DEPTS,
            new Object[] { deptId });
        deptName = (String) deptBean.get(DEPT_NAME);
      }
      String serName = "All";
      if (!((String) bean.get("service_id")).equals("-1")) {
        BasicDynaBean opBean = DatabaseHelper.queryToDynaBean(
            "SELECT service_name FROM services WHERE servie_id=?",
            new Object[] { (String) bean.get("service_id") });
        serName = (String) opBean.get("service_name");
      }
      throw new DuplicateEntityException("exception.duplicate.service.formcomponents",
          new String[] { (String) bean.get(FORM_NAME), deptName, serName,
              this.formNames.get((String) bean.get(FORM_TYPE)) });
    } else if (formType.equals(FORM_OT)) {
      String deptName = "All";
      if (!deptId.equals("-1")) {
        BasicDynaBean deptBean = DatabaseHelper.queryToDynaBean(GET_DEPARTMENT,
            new Object[] { deptId });
        deptName = (String) deptBean.get(DEPT_NAME);
      }
      String opName = "All";
      if (!((String) bean.get("operation_id")).equals("-1")) {
        BasicDynaBean opBean = DatabaseHelper.queryToDynaBean(
            "SELECT operation_name FROM operation_master WHERE op_id=?",
            new Object[] { (String) bean.get("operation_id") });
        opName = (String) opBean.get("operation_name");
      }
      throw new DuplicateEntityException("exception.duplicate.operation.formcomponents",
          new String[] { (String) bean.get(FORM_NAME), deptName, opName,
              this.formNames.get((String) bean.get(FORM_TYPE)) });

    } else if (formType.equals(FORM_CONS) || (formType.equals(FORM_OP_FOLLOW_UP_CONS))) {
      if (newFormRule) {
        throw new DuplicateEntityException("exception.duplicate.cons.formcomponents.new",
            new String[] { (String) bean.get(FORM_NAME) });
      } else {
        String deptName = "All";
        if (!deptId.equals("-1")) {
          BasicDynaBean deptBean = DatabaseHelper.queryToDynaBean(GET_DEPARTMENT,
              new Object[] { deptId });
          deptName = (String) deptBean.get(DEPT_NAME);
        }
        String doctorName = "All";
        if (!((String) bean.get("doctor_id")).equals("-1")) {
          BasicDynaBean doctorBean = DatabaseHelper.queryToDynaBean(
              "SELECT doctor_name FROM doctors WHERE doctor_id=?",
              new Object[] { (String) bean.get("doctor_id") });
          doctorName = (String) doctorBean.get("doctor_name");
        }
        BasicDynaBean genericBean = DatabaseHelper
            .queryToDynaBean("SELECT max_centers_inc_default from generic_preferences");
        if ((Integer) genericBean.get("max_centers_inc_default") > 1) {
          throw new DuplicateEntityException("exception.duplicate.cons.formcomponents.multicenter",
              new String[] { (String) bean.get(FORM_NAME), deptName, doctorName,
                  this.formNames.get((String) bean.get(FORM_TYPE)) });
        } else {
          throw new DuplicateEntityException("exception.duplicate.cons.formcomponents",
              new String[] { (String) bean.get(FORM_NAME), deptName, doctorName,
                  this.formNames.get((String) bean.get(FORM_TYPE)) });
        }
      }

    } else if (formType.equals(FORM_GEN)) {
      // duplicates are allowed in generic forms.
    } else if (formType.equals(FORM_TRI) || formType.equals(FORM_IA) || formType.equals(FORM_IP)) {
      String deptName = "All";
      if (!deptId.equals("-1")) {
        BasicDynaBean deptBean = DatabaseHelper.queryToDynaBean(GET_DEPARTMENT,
            new Object[] { deptId });
        deptName = (String) deptBean.get(DEPT_NAME);
      }
      throw new DuplicateEntityException("exception.duplicate.formcomponents",
          new String[] { (String) bean.get(FORM_NAME), deptName,
              this.formNames.get((String) bean.get(FORM_TYPE)) });
    }
  }
  
  private static String GET_TEMPLATE_FORMS = "SELECT fc.id, fc.form_name, "
      + " fdd.dept_id, fc.doctor_id, "
      + " (CASE WHEN EXISTS (SELECT form_id FROM form_template_data WHERE form_id=fc.id) "
      + " THEN true ELSE false END) AS has_data, fcca.center_id " + "FROM form_components fc "
      + " JOIN form_components_center_applicability fcca ON (fcca.form_components_id=fc.id "
      + "   AND fcca.status='A' AND fcca.center_id IN (0, ?)) "
      + " JOIN form_department_details fdd ON (fdd.id=fc.id AND fdd.dept_id IN ('-1', ?)) "
      + "WHERE fc.form_type=? AND fc.istemplate=true #template_filter# "
      + "ORDER BY doctor_id DESC, dept_id DESC, center_id DESC";

  /**
   * Gets the cons template forms.
   *
   * @param doctorId the doctor id
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections cons template form
   */
  public List<BasicDynaBean> getConsTemplateForms(String doctorId, String deptId,
      Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", " AND fc.doctor_id IN ('-1', ?)");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { centerId, deptId, FORM_CONS, doctorId });
  }

  /**
   * Gets the triage template forms.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections triage template form
   */
  public List<BasicDynaBean> getTriageTemplateForms(String deptId, Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", "");
    return DatabaseHelper.queryToDynaList(query, new Object[] { centerId, deptId, FORM_TRI });
  }

  /**
   * Gets the assessment template forms.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections assessment template form
   */
  public List<BasicDynaBean> getAssessmentTemplateForms(String deptId, Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", "");
    return DatabaseHelper.queryToDynaList(query, new Object[] { centerId, deptId, FORM_IA, });
  }

  /**
   * Gets the follow up template forms.
   *
   * @param doctorId the doctor id
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections follow up template form
   */
  public List<BasicDynaBean> getFollowUpTemplateForms(String doctorId, String deptId,
      Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", " AND fc.doctor_id IN ('-1', ?)");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { centerId, deptId, FORM_OP_FOLLOW_UP_CONS, doctorId });
  }

  /**
   * Gets the surgery template forms.
   *
   * @param deptId the dept id
   * @param operationId the operation id
   * @param centerId the center id
   * @return sections surgery template forms
   */
  public List<BasicDynaBean> getSurgeryTemplateForms(String deptId, String operationId,
      Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", " AND fc.operation_id IN ('-1', ?)");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { centerId, deptId, FORM_OT, operationId });
  }

  /**
   * Gets the generic template forms.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections generic template form
   */
  public List<BasicDynaBean> getGenericTemplateForms(String deptId, Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", "");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { centerId, deptId, FORM_GEN });
  }

  /**
   * Gets the in patient template forms.
   *
   * @param deptId the dept id
   * @param centerId the center id
   * @return sections inpatient template forms
   */
  public List<BasicDynaBean> getInPatientTemplateForms(String deptId, Integer centerId) {
    String query = GET_TEMPLATE_FORMS;
    query = query.replace("#template_filter#", "");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { centerId, deptId, FORM_IP });
  }

  /** The get consultation template form. */
  private static String GET_CONSULTATION_TEMPLATE_FORM = "SELECT fc.id, fc.form_name, "
      + " fdd.dept_id, fc.doctor_id, "
      + " (CASE WHEN EXISTS (SELECT form_id FROM form_template_data WHERE form_id=fc.id) "
      + " THEN true ELSE false END) AS has_data, fcca.center_id " + "FROM form_components fc "
      + "JOIN form_components_center_applicability fcca ON (fcca.form_components_id=fc.id "
      + "   AND fcca.status='A' AND fcca.center_id IN (0, ?)) "
      + "JOIN form_department_details fdd ON (fdd.id=fc.id AND fdd.dept_id IN ('-1', ?)) "
      + "WHERE fc.doctor_id IN ('-1', ?) AND fc.form_type=? AND fc.istemplate=true "
      + "ORDER BY doctor_id DESC, dept_id DESC, center_id DESC";

  /**
   * Gets the consultation template form.
   *
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param centerId
   *          the center id
   * @param formType
   *          the form type
   * @return the forms for consultation
   */
  public List<BasicDynaBean> getFormTemplatesForConsultation(String doctorId, String deptId,
      Integer centerId, String formType) {
    return DatabaseHelper.queryToDynaList(GET_CONSULTATION_TEMPLATE_FORM,
        new Object[] { centerId, deptId, doctorId, formType });
  }

  /** The get forms for consultation. */
  private static String GET_FORMS_FOR_CONSULTATION = "SELECT fc.id, fc.form_name, "
      + " fdd.dept_id, fc.doctor_id, fcca.center_id " + "FROM form_components fc "
      + "JOIN form_components_center_applicability fcca ON (fcca.form_components_id=fc.id "
      + "   AND fcca.status='A' AND fcca.center_id IN (0, ?)) "
      + "JOIN form_department_details fdd ON (fdd.id=fc.id AND fdd.dept_id IN ('-1', ?)) "
      + "WHERE fc.doctor_id IN ('-1', ?) AND fc.form_type=? AND fc.istemplate=false "
      + "ORDER BY doctor_id DESC, dept_id DESC, center_id DESC";

  /**
   * Gets the forms for consultation.
   *
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param centerId
   *          the center id
   * @param formType
   *          the form type
   * @return the forms for consultation
   */
  public List<BasicDynaBean> getFormsForConsultation(String doctorId, String deptId,
      Integer centerId, String formType) {
    return DatabaseHelper.queryToDynaList(GET_FORMS_FOR_CONSULTATION,
        new Object[] { centerId, deptId, doctorId, formType });
  }

  /** The get sections by id. */
  private static String GET_SECTIONS_BY_ID = " SELECT" + " foo.id as form_id,"
      + " foo.section_id::int as section_id, "
      + " (case when 2>=? OR foo.section_id::int IN (-1, -7, -3, -6, -4) then true else exists "
      + " (select section_id from insta_section_rights isr "
      + " Where isr.section_id=foo.section_id::integer AND isr.role_id=?) end) AS section_rights, "
      + " coalesce(sys.display_name, sm.section_title) as section_title,"
      + " coalesce(sm.allow_all_normal, 'N') as allow_all_normal," + " sm.linked_to as linked_to,"
      + " coalesce(sys.section_mandatory, sm.section_mandatory) as section_mandatory,"
      + " sys.field_phrase_category_id,"
      + " coalesce(sm.allow_duplicate, false) as allow_duplicate," + " 'N' as finalized,"
      + " foo.display_order " + "FROM (SELECT "
      + "       id, regexp_split_to_table(sections, ',') as section_id, "
      + "       generate_series(1, array_upper(regexp_split_to_array(sections, E','), 1)) "
      + "       as display_order " + "      FROM form_components where id=?) as foo "
      + "LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
      + "LEFT JOIN system_generated_sections sys ON  (sys.section_id::text=foo.section_id) "
      + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";

  /**
   * Gets the sectionsby id.
   *
   * @param formId
   *          the form id
   * @param roleId
   *          the role id
   * @return the sectionsby id
   */
  public List<BasicDynaBean> getSectionsbyId(Integer formId, Integer roleId) {
    return DatabaseHelper.queryToDynaList(GET_SECTIONS_BY_ID,
        new Object[] { roleId, roleId, formId });
  }

  private static final String FORM_SECTIONS_FROM_MASTER = " SELECT foo.id as form_id, "
      + " foo.section_id::int as section_id, "
      + " (case when 2>=? OR foo.section_id::integer IN ( -1, -3, -6, -4) then true else "
      + " exists (select section_id from insta_section_rights isr Where "
      + " isr.section_id=foo.section_id::integer AND isr.role_id=?) end) AS section_rights,"
      + " coalesce(sys.display_name, sm.section_title) as section_title,"
      + " coalesce(sm.allow_all_normal, 'N') as allow_all_normal, sm.linked_to as linked_to,"
      + " coalesce(sys.section_mandatory, sm.section_mandatory) as section_mandatory,"
      + " sys.field_phrase_category_id, coalesce(sm.allow_duplicate, false) as allow_duplicate,"
      + " 'N' as finalized, foo.display_order "
      + "FROM (SELECT fc.id, regexp_split_to_table(fc.sections, ',') as section_id, "
      + "    generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1)) "
      + "    as display_order  FROM form_components fc "
      + "     JOIN form_department_details fdd ON (fc.id=fdd.id AND fdd.dept_id=?) "
      + "     JOIN form_components_center_applicability fcca ON (fcca.form_components_id=fc.id "
      + "       AND fcca.center_id IN (0, ?)) "
      + "   where form_type=? and fc.istemplate=false #form_fitler#) as foo "
      + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
      + " LEFT JOIN system_generated_sections sys ON  (sys.section_id::text=foo.section_id) "
      + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";

  /**
   * Gets the OP form.
   *
   * @param deptId the dept id
   * @param doctorId the doctor id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getOPForm(String deptId, String doctorId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", " and doctor_id=? ");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_CONS, doctorId });
  }

  /**
   * Gets the triage form.
   *
   * @param deptId the dept id
   * @param doctorId the doctor id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getTriageForm(String deptId, String doctorId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", "");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_TRI });
  }

  /**
   * Gets the IA form.
   *
   * @param deptId the dept id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getIAForm(String deptId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", "");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_IA });
  }

  /**
   * Gets the follow up form.
   *
   * @param deptId the dept id
   * @param doctorId the doctor id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getFollowUpForm(String deptId, String doctorId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", " and doctor_id=? ");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_OP_FOLLOW_UP_CONS, doctorId });
  }

  /**
   * Gets the service form.
   *
   * @param deptId the dept id
   * @param serviceId the service id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getServiceForm(String deptId, String serviceId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", " and service_id=? ");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_SERV, serviceId });
  }

  /**
   * Gets the OT form.
   *
   * @param deptId the dept id
   * @param operationId the operation id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getOTForm(String deptId, String operationId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", " and operation_id=? ");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_OT, operationId });
  }

  /**
   * Gets the generic form.
   *
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getGenericForm(Integer formId, Integer roleId,
      Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", " and fc.id=? ");
    String deptId = (String) formDepartmentDetailsRepository.findByKey("id", formId).get("dept_id");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] {roleId, roleId, deptId, centerId, FORM_GEN, formId});
  }

  /**
   * Gets the IP form.
   *
   * @param deptId the dept id
   * @param roleId the role id
   * @param centerId the center id
   * @return sections of form
   */
  public List<BasicDynaBean> getIPForm(String deptId, Integer roleId, Integer centerId) {
    String query = FORM_SECTIONS_FROM_MASTER;
    query = query.replace("#form_fitler#", "");
    return DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, deptId, centerId, FORM_IP });
  }

  /** The get forms by id. */
  private static String GET_FORMS_BY_ID = "SELECT sections, fc.id, group_patient_sections, "
      + "form_type, operation_id, service_id, form_name, immunization, print_template_id, "
      + "status, doc_type, doctor_id, fdd.dept_id, fdd.form_department_id, fc.istemplate "
      + "FROM form_components fc " + "JOIN form_department_details fdd ON (fdd.id=fc.id) "
      + "WHERE fc.id =? ";

  /**
   * Gets the form bean.
   *
   * @param id
   *          the id
   * @return the form bean
   */
  public BasicDynaBean getFormBean(Integer id) {
    return DatabaseHelper.queryToDynaBean(GET_FORMS_BY_ID, new Object[] { id });
  }

  /**
   * Gets the dept bean.
   *
   * @return the dept bean
   */
  public BasicDynaBean getDeptBean() {
    return formDepartmentDetailsRepository.getBean();
  }

  /**
   * Insert form dept.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  public Integer insertFormDept(BasicDynaBean bean) {
    return formDepartmentDetailsRepository.insert(bean);
  }

  /**
   * Update form dept.
   *
   * @param bean
   *          the bean
   * @param keys
   *          the keys
   * @return the integer
   */
  public Integer updateFormDept(BasicDynaBean bean, Map<String, Object> keys) {
    return formDepartmentDetailsRepository.update(bean, keys);
  }

  /**
   * Insert form center.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  public Integer insertFormCenter(BasicDynaBean bean) {
    return formCenterRepository.insert(bean);
  }

  /**
   * Gets the form center bean.
   *
   * @return the form center bean
   */
  public BasicDynaBean getFormCenterBean() {
    return formCenterRepository.getBean();
  }

  /**
   * Gets the form component center id.
   *
   * @return the form component center id
   */
  public Integer getFormComponentCenterId() {
    return formCenterRepository.getNextSequence();
  }

  /**
   * Gets the all applicable centers.
   *
   * @param formId
   *          the form id
   * @return the all applicable centers
   */
  public List<BasicDynaBean> getAllApplicableCenters(int formId) {
    return formCenterRepository.getAllApplicableCenters(formId);
  }

  /**
   * Form center batch insert.
   *
   * @param beans
   *          the beans
   * @return the int[]
   */
  public int[] formCenterBatchInsert(List<BasicDynaBean> beans) {
    return formCenterRepository.batchInsert(beans);
  }

  /**
   * Form center batch update.
   *
   * @param updateBeans
   *          the update beans
   * @param updateKeysMap
   *          the update keys map
   * @return the int[]
   */
  public int[] formCenterBatchUpdate(List<BasicDynaBean> updateBeans,
      Map<String, Object> updateKeysMap) {
    return formCenterRepository.batchUpdate(updateBeans, updateKeysMap);
  }

  /**
   * Form center batch delete.
   *
   * @param key
   *          the key
   * @param deleteKeys
   *          the delete keys
   * @return the int[]
   */
  public int[] formCenterBatchDelete(String key, List<Object> deleteKeys) {
    return formCenterRepository.batchDelete(key, deleteKeys);
  }

  /**
   * Gets the all non default centers.
   *
   * @param formId
   *          the form id
   * @return the all non default centers
   */
  public List<BasicDynaBean> getAllNonDefaultCenters(int formId) {
    return formCenterRepository.getAllNonDefaultCenters(formId);
  }

  /**
   * Delete all non default centers.
   *
   * @param formComponentsId
   *          the form components id
   * @return the int
   */
  public int deleteAllNonDefaultCenters(int formComponentsId) {
    return formCenterRepository.deleteAllNonDefaultCenters(formComponentsId);
  }

  /**
   * Form center insert.
   *
   * @param bean
   *          the bean
   * @return the integer
   */
  public Integer formCenterInsert(BasicDynaBean bean) {
    return formCenterRepository.insert(bean);
  }

  /**
   * Form center delete.
   *
   * @param filterMap
   *          the filter map
   * @return the integer
   */
  public Integer formCenterDelete(Map<String, Object> filterMap) {
    return formCenterRepository.delete(filterMap);
  }

  /**
   * Find center by key.
   *
   * @param filterMap
   *          the filter map
   * @return the basic dyna bean
   */
  public BasicDynaBean findCenterByKey(Map<String, Object> filterMap) {
    return formCenterRepository.findByKey(filterMap);
  }
  
  private static final String GET_GEN_FORM_NAMES_BY_CATEGORY =
      "SELECT fc.id as template_id, fc.form_name as template_name, dt.doc_type_name, "
      + " dt.doc_type_id FROM form_components fc"
      + " JOIN doc_type_category_mapping dtcm ON (fc.doc_type = dtcm.doc_type_id) "
      + " JOIN form_department_details fdd ON (fdd.id = fc.id AND fdd.dept_id IN ('-1', ?))"
      + " JOIN doc_type dt ON (dt.doc_type_id=dtcm.doc_type_id)"
      + " WHERE fc.form_type='Form_Gen' AND dtcm.doc_type_category_id=? #docIdFilter"
      + " AND fc.status='A'";

  /**
   * Get generic form names from master.
   * @param docCategory doc category (e.g.: CLN)
   * @param docTypeId doc type
   * @param deptId department id
   * @param searchQuery search query
   * @return records
   */
  public List<BasicDynaBean> getGenericFormNamesByCategory(String docCategory, String docTypeId,
      String deptId, String searchQuery) {
    boolean isDocIdEmpty = StringUtils.isEmpty(docTypeId);
    String docIdFilter = !isDocIdEmpty ? "AND dtcm.doc_type_id = ?" : "";
    StringBuilder query = new StringBuilder(GET_GEN_FORM_NAMES_BY_CATEGORY);
    if (StringUtils.isEmpty(searchQuery) && !isDocIdEmpty) {
      return DatabaseHelper.queryToDynaList(query.toString().replace("#docIdFilter", docIdFilter),
          new Object[] {deptId, docCategory, docTypeId});
    }
    int inc = 0;
    String[] searchWords = searchQuery.split(" ");
    int wordsLength = searchWords.length;
    String searchFilter =
        "AND (fc.form_name ILIKE ? OR fc.form_name ILIKE ? OR fc.form_name ILIKE ?) ";
    Object[] parameterSource = new Object[(wordsLength) * 3 + (isDocIdEmpty ? 2 : 3)];
    parameterSource[inc++] = deptId;
    parameterSource[inc++] = docCategory;
    if (!isDocIdEmpty) {
      parameterSource[inc++] = docTypeId;
    }
    for (int i = 0; i < searchWords.length; i++) {
      parameterSource[inc++] = searchQuery + "%";
      parameterSource[inc++] = "%" + searchQuery + "%";
      parameterSource[inc++] = "%" + searchQuery;
      query.append(searchFilter);
    }
    query.append(" ORDER BY template_name LIMIT 50");
    return DatabaseHelper
        .queryToDynaList(query.toString().replace("#docIdFilter", docIdFilter), parameterSource);
  }
}
