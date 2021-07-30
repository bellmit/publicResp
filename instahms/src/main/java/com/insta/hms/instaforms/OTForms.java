/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.OTServices.OtRecord.OtRecordDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class OTForms.
 *
 * @author insta
 */
public class OTForms extends AbstractInstaForms {

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getKeys()
   */
  public Map<String, String> getKeys() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("form_type", "Form_OT");
    map.put("item_type", "SUR");
    map.put("section_item_id", "operation_proc_id");
    return map;
  }

  /**
   * Gets the form names.
   *
   * @param operationDetailId the operation detail id
   * @param patientId the patient id
   * @return the form names
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getFormNames(int operationDetailId, String patientId)
      throws SQLException {
    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("operation_name");
      builder.add("operation_id");
      builder.add("operation_proc_id", Integer.class);
      builder.add("operation_details_id", Integer.class);
      builder.add("form_id", Integer.class);
      builder.add("form_name");

      List<BasicDynaBean> opprocidlist =
          new OtRecordDAO().getOperations(patientId, operationDetailId);
      for (BasicDynaBean b : opprocidlist) {
        int procId = (Integer) b.get("operation_proc_id");
        String operationId = (String) b.get("operation_id");

        ps =
            con.prepareStatement("SELECT fc.form_name, psf.form_id"
                + " FROM patient_section_details psd "
                + " JOIN patient_section_forms psf "
                + " ON (psd.section_detail_id=psf.section_detail_id) "
                + " JOIN form_components fc ON (fc.id=psf.form_id) "
                + " WHERE section_item_id=? AND item_type='SUR'"
                + " AND psf.form_type='Form_OT' "
                + " GROUP BY fc.form_name, psf.form_id ");
        ps.setInt(1, procId);
        BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
        if (bean == null) {
          String deptId = (String) b.get("dept_id");

          String getActiveForms =
              " SELECT fc.id as form_id, fc.form_name "
                  + " FROM form_components fc, "
                  + " form_department_details fdd"
                  + " WHERE fc.form_type='Form_OT' "
                  + " AND fc.operation_id=? AND fdd.dept_id=? and fdd.id=fc.id ";

          ps = con.prepareStatement(getActiveForms);
          ps.setString(1, operationId);
          ps.setString(2, deptId);

          bean = DataBaseUtil.queryToDynaBean(ps);
          if (bean == null) {
            ps.setString(1, "-1");
            ps.setString(2, deptId);
            bean = DataBaseUtil.queryToDynaBean(ps);
            if (bean == null) {
              ps.setString(1, "-1");
              ps.setString(2, "-1");
              bean = DataBaseUtil.queryToDynaBean(ps);
            }
          }
        }

        BasicDynaBean record = builder.build();
        record.set("operation_name", b.get("operation_name"));
        record.set("operation_id", b.get("operation_id"));
        record.set("operation_proc_id", b.get("operation_proc_id"));
        record.set("operation_details_id", operationDetailId);
        record.set("form_id", bean.get("form_id"));
        record.set("form_name", bean.get("form_name"));

        list.add(record);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.instaforms.AbstractInstaForms#getComponents(java.util.Map)
   */
  public BasicDynaBean getComponents(Map params) throws SQLException {

    int operationProcId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "operation_proc_id", "0"));
    Connection con = DataBaseUtil.getConnection();
    BasicDynaBean otBean = OtRecordDAO.getOperation(operationProcId);
    String operationId = (String) otBean.get("operation_id");

    PreparedStatement ps = null;
    try {
      String getFormsFromsTx =
          " SELECT DISTINCT psd.section_id, psf.display_order, psf.form_id, fc.form_name "
              + " FROM patient_section_details psd "
              + " JOIN patient_section_forms psf USING (section_detail_id) "
              + " JOIN form_components fc ON (fc.id=psf.form_id) "
              + " WHERE psd.section_item_id=? AND psf.form_type='Form_OT'"
              + " ORDER BY display_order";

      ps = con.prepareStatement(getFormsFromsTx);
      ps.setInt(1, operationProcId);

      List<BasicDynaBean> formList = DataBaseUtil.queryToDynaList(ps);
      if (formList == null || formList.isEmpty()) {
        String deptId = (String) otBean.get("dept_id");

        String getActiveForms =
            " SELECT foo.section_id::int as section_id, foo.id as form_id, form_name "
                + " FROM (SELECT fc.id, fc.form_name, regexp_split_to_table(fc.sections, ',')"
                + " as section_id, "
                + " generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1))"
                + " as display_order "
                + " FROM form_components fc, form_department_details fdd"
                + " WHERE fc.form_type='Form_OT' AND fc.operation_id=? "
                + " AND fdd.dept_id=? and fdd.id=fc.id) as foo "
                + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
                + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";

        ps = con.prepareStatement(getActiveForms);
        ps.setString(1, operationId);
        ps.setString(2, deptId);

        formList = DataBaseUtil.queryToDynaList(ps);
        if (formList == null || formList.isEmpty()) {
          ps.setString(1, "-1");
          ps.setString(2, deptId);
          formList = DataBaseUtil.queryToDynaList(ps);
          if (formList == null || formList.isEmpty()) {
            ps.setString(1, "-1");
            ps.setString(2, "-1");
            formList = DataBaseUtil.queryToDynaList(ps);
          }
        }
      }
      DynaBeanBuilder builder = new DynaBeanBuilder();
      builder.add("sections");
      builder.add("form_id", Integer.class);
      builder.add("form_name");

      BasicDynaBean bean = builder.build();
      String sections = "";
      boolean first = true;
      int formId = 0;
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
    return Integer.parseInt(ConversionUtils.getParamValue(params, "operation_proc_id", "0"));
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
        BasicDynaBean record =
            PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId,
                sectionId, formId, itemType);
        if (record == null) {
          list = sdDAO.getItemLevelSectionValues(itemId, itemType, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
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
        if (record == null) {
          list = sdDAO.getItemLevelSectionValues(itemId, itemType, sectionId);
        } else {
          list =
              sdDAO.getSectionDetails(mrNo, patientId, itemId, genericFormId, formId, sectionId,
                  itemType);
        }
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

  /**
   * Delete form details.
   *
   * @param con the con
   * @param operationProcId the operation proc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean deleteFormDetails(Connection con, int operationProcId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps =
          con.prepareStatement("DELETE FROM patient_section_values ppfv"
              + " USING patient_section_details psd "
              + " WHERE psd.section_detail_id=ppfv.section_detail_id"
              + " AND section_item_id = ? AND item_type='SUR'");
      ps.setInt(1, operationProcId);
      ps.executeUpdate();
      ps.close();

      ps =
          con.prepareStatement("DELETE FROM patient_section_image_details psid"
              + " USING patient_section_fields psf, patient_section_details psd"
              + " WHERE psid.field_detail_id=psf.field_detail_id"
              + " AND psd.section_detail_id=psf.section_detail_id "
              + " AND section_item_id = ? AND item_type='SUR'");
      ps.setInt(1, operationProcId);
      ps.executeUpdate();
      ps.close();

      ps =
          con.prepareStatement("DELETE FROM patient_section_forms psf"
              + " USING patient_section_details psd "
              + " WHERE psd.section_detail_id=psf.section_detail_id AND"
              + " section_item_id = ? AND item_type='SUR'");
      ps.setInt(1, operationProcId);
      ps.executeUpdate();
      ps.close();

      ps =
          con.prepareStatement("DELETE FROM patient_section_details"
              + " where section_item_id=? AND item_type='SUR'");
      ps.setInt(1, operationProcId);
      ps.executeUpdate();

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return true;
  }

}
