/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericForms.
 *
 * @author insta
 */
public class GenericForms extends AbstractInstaForms {

  /** The visit dao. */
  VisitDetailsDAO visitDao = new VisitDetailsDAO();

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getKeys()
   */
  public Map getKeys() {
    Map map = new HashMap();
    map.put("form_type", "Form_Gen");
    map.put("item_type", "GEN");
    map.put("section_item_id", "");
    return map;
  }

  /**
   * Gets the forms.
   *
   * @param patientId the patient id
   * @return the forms
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getForms(String patientId) throws SQLException {
    String getActiveForms =
        "SELECT fc.*,fdd.dept_id, doc_type_name FROM form_components fc "
            + " JOIN form_department_details fdd ON (fdd.id=fc.id) "
            + " LEFT JOIN doc_type dt ON (dt.doc_type_id = fc.doc_type) "
            + " where fdd.dept_id=? and form_type='Form_Gen' "
            + "and fc.status='A' ORDER BY form_name";
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    List formList = null;
    try {
      String deptId =
          (String) new VisitDetailsDAO().findByKey(con, "patient_id", patientId).get("dept_name");
      ps = con.prepareStatement(getActiveForms);
      ps.setString(1, deptId);
      formList = DataBaseUtil.queryToDynaList(ps);
      if (formList == null || formList.isEmpty()) {
        ps.setString(1, "-1");
        formList = DataBaseUtil.queryToDynaList(ps);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return formList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getComponents(java.util.Map)
   */
  @Override
  public BasicDynaBean getComponents(Map params) throws SQLException {
    int formId = Integer.parseInt(ConversionUtils.getParamValue(params, "insta_form_id", "0"));
    int genericFormId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "generic_form_id", "0"));
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (formId == 0) {
        // for the same deptname there can be multiple forms available, so not returing anything.
        return null;
      } else {
        String getFormsFromTx =
            " SELECT DISTINCT psd.section_id, psf.display_order, psf.form_id, fc.form_name "
                + " FROM patient_section_details psd "
                + " JOIN patient_section_forms psf USING (section_detail_id) "
                + " JOIN form_components fc ON (fc.id=psf.form_id) "
                + " WHERE coalesce(generic_form_id, 0)=? AND coalesce(psd.section_item_id, 0)=0 "
                + " AND psf.form_type='Form_Gen' AND item_type='GEN' "
                + " AND psf.form_id=? ORDER BY display_order";

        ps = con.prepareStatement(getFormsFromTx);
        ps.setInt(1, genericFormId);
        ps.setInt(2, formId);

        List<BasicDynaBean> formList = DataBaseUtil.queryToDynaList(ps);
        ps.close();
        if (formList == null || formList.isEmpty()) {
          String getActiveForms =
              " SELECT foo.section_id::int as section_id,"
                  + " foo.id as form_id, form_name, display_order "
                  + " FROM (SELECT id, form_name, regexp_split_to_table(sections, ',')"
                  + " as section_id, "
                  + " generate_series(1, array_upper(regexp_split_to_array(sections, E','), 1))"
                  + " as display_order "
                  + " FROM form_components where id=? and form_type='Form_Gen') as foo "
                  + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
                  + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";
          ps = con.prepareStatement(getActiveForms);
          ps.setInt(1, formId);
          formList = DataBaseUtil.queryToDynaList(ps);
        }

        DynaBeanBuilder builder = new DynaBeanBuilder();
        builder.add("sections");
        builder.add("form_id", Integer.class);
        builder.add("form_name");

        BasicDynaBean bean = builder.build();
        String sections = "";
        boolean first = true;
        int formIdtxn = 0;
        String formName = "";
        for (BasicDynaBean b : formList) {
          if (!first) {
            sections += ",";
          }
          sections += (Integer) b.get("section_id");
          first = false;
          formIdtxn = (Integer) b.get("form_id");
          formName = (String) b.get("form_name");
        }
        bean.set("sections", sections);
        bean.set("form_id", formIdtxn);
        bean.set("form_name", formName);
        return bean;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#saveConsultationNotes(java.sql.Connection,
   * java.util.Map, java.lang.String, int, boolean)
   */
  @Override
  public String saveConsultationNotes(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#savePrescription(java.sql.Connection,
   * java.util.Map, java.lang.String, int, boolean)
   */
  @Override
  public String savePrescription(Connection con, Map params, String userName, int sectionDetailId,
      boolean insert) throws SQLException, IOException, Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionItemId(java.util.Map)
   */
  @Override
  public int getSectionItemId(Map params) {
    return 0;
  }

  /** The Constant GENERIC_FORMS_COUNT. */
  private static final String GENERIC_FORMS_COUNT = " SELECT count(form_id) ";

  /** The Constant GENERIC_FORMS_FIELDS. */
  private static final String GENERIC_FORMS_FIELDS =
      " SELECT generic_form_id, patient_id, form_name, form_id, form_type, "
          + " item_type, user_name, mod_time ,reg_date,doc_type";

  /** The Constant GENERIC_FORMS_TABLES. */
  private static final String GENERIC_FORMS_TABLES =
      " FROM (SELECT psd.mr_no, psd.generic_form_id, psd.patient_id,"
          + " fc.form_name, fc.id as form_id, fc.form_type,fc.doc_type, "
          + " psd.item_type, psd.user_name, date_trunc('minute', psd.mod_time)"
          + " as mod_time, section_item_id ,reg_date"
          + " FROM patient_section_details psd "
          + " JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id) "
          + " JOIN form_components fc ON (fc.id=psf.form_id) "
          + " JOIN patient_registration pr ON(psd.patient_id = pr.patient_id)"
          + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND"
          + " (patient_confidentiality_check(pd.patient_group,pd.mr_no)))"
          + " GROUP BY psd.generic_form_id, psd.patient_id, psd.mr_no,"
          + " psd.section_item_id, fc.form_name, fc.id, "
          + " fc.form_type,fc.doc_type, psd.item_type, psd.user_name,"
          + " date_trunc('minute', psd.mod_time),pr.reg_date) as foo";

  /** The Constant WHERE. */
  private static final String WHERE =
      " WHERE form_type='Form_Gen' AND coalesce(section_item_id, 0)=0 ";

  /**
   * Gets the added forms.
   *
   * @param patientId the patient id
   * @param listingParams the listing params
   * @return the added forms
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public PagedList getAddedForms(String patientId, Map listingParams) throws SQLException,
      IOException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder sqb = null;
    try {
      sqb =
          new SearchQueryBuilder(con, GENERIC_FORMS_FIELDS, GENERIC_FORMS_COUNT,
              GENERIC_FORMS_TABLES, WHERE, listingParams);
      sqb.addFilter(sqb.STRING, "patient_id", "=", patientId);
      sqb.addSecondarySort("mod_time", true);
      sqb.build();

      return sqb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant LIST_BY_VISIT. */
  private static final String LIST_BY_VISIT = GENERIC_FORMS_FIELDS + GENERIC_FORMS_TABLES + WHERE
      + " AND patient_id=? ";

  /**
   * List documents by visit.
   *
   * @param patientId the patient id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List listDocumentsByVisit(String patientId) throws SQLException {
    return DataBaseUtil.queryToDynaList(LIST_BY_VISIT, patientId);
  }

  /** The Constant LIST_BY_MRNO. */
  private static final String LIST_BY_MRNO = GENERIC_FORMS_FIELDS + GENERIC_FORMS_TABLES + WHERE
      + " AND mr_no=? ";

  /**
   * List visit documents for mr no.
   *
   * @param mrNo the mr no
   * @return the list
   * @throws SQLException the SQL exception
   */
  public static List listVisitDocumentsForMrNo(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(LIST_BY_MRNO, mrNo);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionDetails(int, java.lang.String,
   * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int)
   */
  @Override
  public List getSectionDetails(int formId, String formType, String itemType, int sectionId,
      String linkedTo, String mrNo, String patientId, int itemId, int genericFormId)
      throws SQLException, IOException {
    List list = null;
    PatientSectionDetailsDAO sdDAO = new PatientSectionDetailsDAO();
    Connection con = DataBaseUtil.getConnection();
    try {
      if (linkedTo.equals("patient")) {
        BasicDynaBean record =
            PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId,
                sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getPatientLevelSectionValues(mrNo, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
      } else if (linkedTo.equals("visit")) {
        BasicDynaBean record =
            PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId,
                sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getVisitLevelSectionValues(patientId, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
      } else if (linkedTo.equals("order item")) {
        return Collections.EMPTY_LIST;// for ip record there will be no order item level sections.
      } else if (linkedTo.equals("form")) {
        list =
            sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                itemType);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getSectionDetails(int, java.lang.String,
   * java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public List getSectionDetails(int formId, String formType, String itemType, int sectionId,
      String linkedTo, String mrNo, String patientId, int itemId, int genericFormId,
      BasicDynaBean record) throws SQLException, IOException {
    List list = null;
    PatientSectionDetailsDAO sdDAO = new PatientSectionDetailsDAO();
    Connection con = DataBaseUtil.getConnection();
    try {
      if (linkedTo.equals("patient")) {
        if (record == null) {
          list = sdDAO.getPatientLevelSectionValues(mrNo, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
      } else if (linkedTo.equals("visit")) {
        if (record == null) {
          list = sdDAO.getVisitLevelSectionValues(patientId, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
      } else if (linkedTo.equals("order item")) {
        return Collections.EMPTY_LIST;// for ip record there will be no order item level sections.
      } else if (linkedTo.equals("form")) {
        list =
            sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                itemType);
      }

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return list;
  }
}
