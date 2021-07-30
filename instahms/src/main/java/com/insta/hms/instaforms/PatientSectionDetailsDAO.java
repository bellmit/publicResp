/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class PatientSectionDetailsDAO.
 *
 * @author insta
 */
public class PatientSectionDetailsDAO extends GenericDAO {

  /**
   * Instantiates a new patient section details DAO.
   */
  public PatientSectionDetailsDAO() {
    super("patient_section_details");
  }

  /**
   * Gets the record.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param sectionId     the section id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the record
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static BasicDynaBean getRecord(String mrNo, String patientId, int itemId,
      int genericFormId, int sectionId, int formId, String itemType)
      throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return getRecord(con, mrNo, patientId, itemId, genericFormId, sectionId, formId, itemType);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the record.
   *
   * @param con             the con
   * @param mrNo            the mr no
   * @param patientId       the patient id
   * @param itemId          the item id
   * @param genericFormId   the generic form id
   * @param sectionId       the section id
   * @param sectionDetailId the section detail id
   * @param formId          the form id
   * @param itemType        the item type
   * @return the record
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static BasicDynaBean getRecord(Connection con, String mrNo, String patientId, int itemId,
      int genericFormId, int sectionId, int sectionDetailId, int formId, String itemType)
      throws SQLException, IOException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT psd.* FROM patient_section_details psd "
          + " JOIN patient_section_forms psf USING (section_detail_id) "
          + " WHERE psd.mr_no=? AND psd.patient_id=? " + " AND coalesce(psd.section_item_id, 0)=?"
          + " AND coalesce(psd.generic_form_id, 0)=? " + " AND psd.item_type=? AND section_id=? AND"
          + " section_detail_id = ? AND psf.form_id=?");
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setObject(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setString(5, itemType);
      ps.setInt(6, sectionId);
      ps.setInt(7, sectionDetailId);
      ps.setInt(8, formId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the record.
   *
   * @param con           the con
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param sectionId     the section id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the record
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static BasicDynaBean getRecord(Connection con, String mrNo, String patientId, int itemId,
      int genericFormId, int sectionId, int formId, String itemType)
      throws SQLException, IOException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT psd.* FROM patient_section_details psd "
          + " JOIN patient_details pd ON (pd.mr_no = psd.mr_no"
          + " AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
          + " JOIN patient_section_forms psf USING (section_detail_id) "
          + " WHERE psd.mr_no=? AND psd.patient_id=? "
          + " AND coalesce(psd.section_item_id, 0)=? AND coalesce(psd.generic_form_id, 0)=? "
          + " AND psd.item_type=? AND section_id=? AND psf.form_id=?");
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setObject(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setString(5, itemType);
      ps.setInt(6, sectionId);
      ps.setInt(7, formId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the sections.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @return the sections
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getSections(String mrNo, String patientId, int itemId,
      int genericFormId, int formId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT distinct psd.section_id, psd.finalized, section_status"
          + " FROM patient_section_details psd "
          + " JOIN patient_details pd ON (pd.mr_no = psd.mr_no"
          + " AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
          + " JOIN patient_section_forms psf USING (section_detail_id) "
          + " WHERE psd.mr_no=? AND psd.patient_id=? "
          + " AND coalesce(psd.section_item_id, 0)=? AND coalesce(psd.generic_form_id, 0)=? "
          + " AND psf.form_id=?");
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setInt(5, formId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ACTIVE_FIELD_VALUES_FOR_PATIENT. */
  private static final String ACTIVE_FIELD_VALUES_FOR_PATIENT = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text')"
      + " then field_remarks else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, image_id,"
      + " 0 as field_detail_id, 0 as marker_detail_id "
      + " FROM patient_section_field_values_view psfv  "
      + " WHERE mr_no = ? AND section_id=? AND section_status='A'"
      + " AND linked_to='patient' AND coalesce(available, 'Y')='Y' "
      + " ORDER BY section_detail_id, field_display_order, option_display_order";

  /**
   * Gets the patient level section values.
   *
   * @param mrNo      the mr no
   * @param sectionId the section id
   * @return the patient level section values
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPatientLevelSectionValues(String mrNo, int sectionId)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ACTIVE_FIELD_VALUES_FOR_PATIENT);
      ps.setString(1, mrNo);
      ps.setInt(2, sectionId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant ACTIVE_FIELD_VALUES_FOR_VISIT. */
  private static final String ACTIVE_FIELD_VALUES_FOR_VISIT = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text') then"
      + " field_remarks else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, image_id,"
      + " 0 as field_detail_id, 0 as marker_detail_id "
      + " FROM patient_section_field_values_view psfv  "
      + " WHERE patient_id = ? AND section_id=? AND section_status='A' "
      + " AND linked_to='visit' AND coalesce(available, 'Y')='Y' "
      + " ORDER BY section_detail_id, field_display_order, option_display_order";

  /**
   * Gets the visit level section values.
   *
   * @param visitId   the visit id
   * @param sectionId the section id
   * @return the visit level section values
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getVisitLevelSectionValues(String visitId, int sectionId)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ACTIVE_FIELD_VALUES_FOR_VISIT);
      ps.setString(1, visitId);
      ps.setInt(2, sectionId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant ACTIVE_FIELD_VALUES_FOR_ORDER_ITEM. */
  private static final String ACTIVE_FIELD_VALUES_FOR_ORDER_ITEM = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text') then"
      + " field_remarks else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, image_id,"
      + " 0 as field_detail_id, 0 as marker_detail_id "
      + " FROM patient_section_field_values_view psfv  "
      + " WHERE section_item_id = ? AND item_type=? AND section_id=? AND section_status='A'"
      + " AND linked_to='order item' AND coalesce(available, 'Y')='Y' "
      + " ORDER BY section_detail_id, field_display_order, option_display_order";

  /**
   * Gets the item level section values.
   *
   * @param itemId    the item id
   * @param itemType  the item type
   * @param sectionId the section id
   * @return the item level section values
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getItemLevelSectionValues(int itemId, String itemType, int sectionId)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ACTIVE_FIELD_VALUES_FOR_ORDER_ITEM);
      ps.setInt(1, itemId);
      ps.setString(2, itemType);
      ps.setInt(3, sectionId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant SECTION_WISE_FIELD_VALUES_FOR_PATIENT_USING_FORMID. */
  private static final String SECTION_WISE_FIELD_VALUES_FOR_PATIENT_USING_FORMID = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text') then"
      + " field_remarks else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id,"
      + " psfv.finalized, image_id, field_detail_id, marker_detail_id "
      + " FROM patient_section_field_values_view psfv "
      + " WHERE mr_no=? AND patient_id=? AND coalesce(section_item_id, 0)=? "
      + " AND coalesce(generic_form_id, 0)=? AND form_id=? AND section_id=?"
      + " AND item_type=? AND coalesce(available, 'Y')='Y' "
      + " ORDER BY section_detail_id, field_display_order, option_display_order";

  /**
   * Gets the section details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param sectionId     the section id
   * @param itemType      the item type
   * @return the section details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, int sectionId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SECTION_WISE_FIELD_VALUES_FOR_PATIENT_USING_FORMID);
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setInt(5, formId);
      ps.setInt(6, sectionId);
      ps.setString(7, itemType);

      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT. */
  private static final String ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text') then"
      + " field_remarks else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, "
      + " 'sd_' || section_detail_id as str_section_detail_id, psfv.finalized,"
      + " coalesce(image_id, 0) as image_id," + " field_detail_id, marker_detail_id "
      + " FROM patient_section_field_values_for_print psfv "
      + " WHERE mr_no=? AND patient_id=? AND coalesce(section_item_id, 0)=? "
      + " AND coalesce(generic_form_id, 0)=? AND form_id=? AND item_type=? " + " AND value_found "
      + " ORDER BY section_detail_id, field_display_order, option_display_order,"
      + " coordinate_x asc, coordinate_y desc";

  /**
   * Gets the all section details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all section details
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getAllSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT);
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setInt(5, formId);
      ps.setString(6, itemType);

      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The get consultation physician form field values. */
  public static final String GET_CONSULTATION_PHYSICIAN_FORM_FIELD_VALUES = " SELECT"
      + " textcat_linecat(' ' || section_title || ' :- ' || value) AS cons_fields_value "
      + " FROM (SELECT section_title, value " + "   FROM section_master sm "
      + "   JOIN (SELECT section_det.section_id, "
      + "   textcat_linecat(field_name || ' : ' || COALESCE(option_value,'') ||"
      + "   CASE WHEN coalesce(field_remarks, '') != '' then '(' || field_remarks || ')'  "
      + "   WHEN coalesce(option_remarks, '') != '' then"
      + "   '(' || option_remarks || ')' else '' END) AS value "
      + "   FROM patient_section_details section_det "
      + "   JOIN patient_section_fields psv USING (section_detail_id)"
      + "   LEFT JOIN patient_section_options pso ON (psv.field_detail_id=pso.field_detail_id) "
      + "   JOIN section_field_desc sfd " + "   ON (section_det.section_id = sfd.section_id "
      + "   AND psv.field_id = sfd.field_id) " + "   LEFT JOIN section_field_options sfo "
      + "   ON (sfd.field_id=sfo.field_id AND pso.option_id = sfo.option_id) "
      + "   WHERE section_det.section_item_id = ? AND section_det.item_type='CONS' "
      + "   AND sfd.use_in_presenting_complaint = 'Y' GROUP BY section_det.section_id "
      + "   ) AS foo ON (foo.section_id = sm.section_id) "
      + " JOIN patient_section_details psd ON (psd.section_id=sm.section_id) "
      + " WHERE psd.section_item_id = ? AND psd.item_type='CONS' "
      + " GROUP BY psd.section_id, sm.section_title, foo.value"
      + " ORDER BY psd.section_id) AS foo ";

  /**
   * Gets the cons insta section field values.
   *
   * @param con            the con
   * @param consultationId the consultation id
   * @return the cons insta section field values
   * @throws SQLException the SQL exception
   */
  // used to update the presenting complaint.
  public String getConsInstaSectionFieldValues(Connection con, int consultationId)
      throws SQLException {
    PreparedStatement ps = null;
    String consultationValues = null;
    try {
      ps = con.prepareStatement(GET_CONSULTATION_PHYSICIAN_FORM_FIELD_VALUES);
      ps.setInt(1, consultationId);
      ps.setInt(2, consultationId);
      consultationValues = DataBaseUtil.getStringValueFromDb(ps);
      return consultationValues;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID. */
  private static final String UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID = "UPDATE"
      + " patient_section_details psd SET patient_id=? FROM doctor_consultation dc "
      + " WHERE dc.consultation_id=psd.section_item_id AND dc.patient_id = ? "
      + " AND psd.patient_id=?";

  /**
   * Update visit id.
   *
   * @param con       the con
   * @param opVisitId the op visit id
   * @param ipVisitId the ip visit id
   * @throws SQLException the SQL exception
   */
  public static void updateVisitId(Connection con, String opVisitId, String ipVisitId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID);
      ps.setString(1, ipVisitId);
      ps.setString(2, ipVisitId);
      ps.setString(3, opVisitId);
      ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant GET_GENERIC_FORM_LIST. */
  private static final String GET_GENERIC_FORM_LIST = " SELECT"
      + " psd.*, usr.temp_username FROM patient_section_details psd "
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.patient_id=? AND coalesce(generic_form_id, 0)=? AND psd.item_type=? ";

  /**
   * Gets the generic form list.
   *
   * @param patientId     the patient id
   * @param genericFormId the generic form id
   * @param formType      the form type
   * @return the generic form list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getGenericFormList(String patientId, int genericFormId,
      String formType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_GENERIC_FORM_LIST);
      ps.setString(1, patientId);
      ps.setInt(2, genericFormId);
      ps.setString(3, formType);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SECTION_VALUES. */
  private static final String SECTION_VALUES = " SELECT psf.*, pso.* "
      + " FROM patient_section_fields psf "
      + " LEFT JOIN patient_section_options pso using (field_detail_id) "
      + " WHERE psf.section_detail_id=? ";

  /**
   * Gets the section values.
   *
   * @param con             the con
   * @param sectionDetailId the section detail id
   * @return the section values
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getSectionValues(Connection con, int sectionDetailId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SECTION_VALUES);
      ps.setInt(1, sectionDetailId);
      return DataBaseUtil.queryToDynaList(ps);

    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete markers.
   *
   * @param sectionDetailId the section detail id
   * @throws SQLException the SQL exception
   */
  public void deleteMarkers(int sectionDetailId) throws SQLException {
    String query = "delete from patient_section_image_details psid USING patient_section_fields psf"
        + " WHERE (psf.field_detail_id=psid.field_detail_id) AND psf.section_detail_id=? ";
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      ps.setInt(1, sectionDetailId);
      ps.executeUpdate();
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PATIENT_NOTES_PRINT. */
  private static final String GET_PATIENT_NOTES_PRINT = "SELECT distinct pn.note_id,"
      + " pn.patient_id, pn.note_type_id, pn.note_content, pn.billable_consultation,"
      + " pn.save_status, pn.original_note_id, pn.on_behalf_doctor_id, pn.on_behalf_user,"
      + " pn.created_by, pn.created_time, pn.new_note_id, ntm.note_type_name,"
      + " ntm.editable_by, pn.consultation_type_id " + " FROM patient_notes pn "
      + " JOIN note_type_master ntm ON (ntm.note_type_id = pn.note_type_id)"
      + " WHERE pn.new_note_id IS NULL AND pn.patient_id=? #draftnotes "
      + " ORDER BY created_time DESC";

  /**
   * Gets the patient final notes.
   *
   * @param patientId the patient id
   * @return the patient final notes
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getPatientFinalNotes(String patientId) throws SQLException {
    Integer roleId = (Integer) RequestContext.getRoleId();
    String userName = (String) RequestContext.getUserName();
    String query = GET_PATIENT_NOTES_PRINT;
    if (roleId > 2) {
      query = query.replace("#draftnotes",
          "AND CASE WHEN pn.save_status='D' " + "THEN pn.created_by = ? ELSE true END");
    } else {
      query = query.replace("#draftnotes", " ");
    }
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      ps.setString(1, patientId);
      if (roleId > 2) {
        ps.setString(2, userName);
      }
      return DataBaseUtil.queryToDynaList(ps);
    }
  }
}
