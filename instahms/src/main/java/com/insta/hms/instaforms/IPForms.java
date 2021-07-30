/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
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
 * The Class IPForms.
 *
 * @author insta
 */
public class IPForms extends AbstractInstaForms {

  /** The visit DAO. */
  VisitDetailsDAO visitDAO = new VisitDetailsDAO();

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getKeys()
   */
  public Map<String, String> getKeys() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("form_type", "Form_IP");
    map.put("item_type", "");
    map.put("section_item_id", "");
    return map;
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getComponents(java.util.Map)
   */
  public BasicDynaBean getComponents(Map params) throws SQLException {
    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    int componentFormId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "form_component_id", "0"));
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String getFormsFromTx =
          " SELECT DISTINCT psd.section_id, psf.display_order, psf.form_id, fc.form_name "
              + " FROM patient_section_details psd "
              + " JOIN patient_section_forms psf USING (section_detail_id) "
              + " JOIN form_components fc ON (fc.id=psf.form_id) "
              + " WHERE psd.patient_id=? and coalesce(psd.section_item_id, 0)=0 AND "
              + " psf.form_type='Form_IP' ORDER BY display_order ";

      ps = con.prepareStatement(getFormsFromTx);
      ps.setString(1, patientId);

      List<BasicDynaBean> formList = DataBaseUtil.queryToDynaList(ps);
      if (formList == null || formList.isEmpty()) {
        BasicDynaBean visitbean = visitDAO.findByKey(con, "patient_id", patientId);
        String deptId = (String) visitbean.get("dept_name");

        String getActiveForms =
            " SELECT foo.section_id::int as section_id, foo.id as form_id, form_name "
                + " FROM (SELECT fc.id, fc.form_name, regexp_split_to_table(fc.sections, ',')"
                + " as section_id, "
                + " generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1))"
                + " as display_order "
                + " FROM form_components fc, form_department_details fdd where fdd.dept_id=? "
                + " and fc.form_type='Form_IP' and fdd.id=fc.id and istemplate=false) as foo "
                + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
                + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";
        ps = con.prepareStatement(getActiveForms);
        ps.setString(1, deptId);
        formList = DataBaseUtil.queryToDynaList(ps);
        if (formList == null || formList.isEmpty()) {
          ps.setString(1, "-1");
          formList = DataBaseUtil.queryToDynaList(ps);
        }
      }
      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("sections");
      builder.add("form_id", Integer.class);
      builder.add("form_name");
      BasicDynaBean bean = builder.build();
      String sections = "";
      int formId = 0;
      boolean first = true;
      String formName = "";

      for (BasicDynaBean b : formList) {
        if (!first) {
          sections += ",";
        }
        sections += (Integer) b.get("section_id");
        first = false;
        formId = (Integer) b.get("form_id");
        formName = (String) b.get("form_name");
      }
      bean.set("sections", sections);
      bean.set("form_id", formId);
      bean.set("form_name", formName);

      return bean;

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
        list = Collections.EMPTY_LIST; // for ip record there will be no order item level sections.
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
        list = Collections.EMPTY_LIST; // for ip record there will be no order item level sections.
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
