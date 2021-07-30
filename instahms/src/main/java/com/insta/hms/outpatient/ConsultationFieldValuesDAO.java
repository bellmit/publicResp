package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class ConsultationFieldValuesDAO.
 *
 * @author krishna.t
 */
public class ConsultationFieldValuesDAO extends GenericDAO {

  /**
   * Instantiates a new consultation field values DAO.
   */
  public ConsultationFieldValuesDAO() {
    super("patient_consultation_field_values");
  }

  /** The Constant CONSULT_FIELD_VALUES. */
  private static final String CONSULT_FIELD_VALUES = " SELECT dc.doc_id,"
      + " dc.template_id, dc.patient_id, pcfv.value_id, pcfv.field_id, "
      + " pcfv.field_value, 'Consultation Notes' as field_name, 'Y' as print_column"
      + " FROM doctor_consultation dc " + "   JOIN patient_consultation_field_values pcfv "
      + "   ON (dc.doc_id=pcfv.doc_id and pcfv.field_id = -1) " + " WHERE consultation_id=?";

  /** The Constant VALUE_NOT_EMPTY_COLUMNS. */
  private static final String VALUE_NOT_EMPTY_COLUMNS = " AND coalesce(field_value, '')!=''";

  /**
   * Gets the consultation fields values for print.
   *
   * @param consultationId      the consultation id
   * @param allFields           the all fields
   * @param notEmptyFieldValues the not empty field values
   * @return the consultation fields values for print
   * @throws SQLException the SQL exception
   */
  public static List getConsultationFieldsValuesForPrint(int consultationId, boolean allFields,
      boolean notEmptyFieldValues) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = CONSULT_FIELD_VALUES;
      if (notEmptyFieldValues) {
        fieldsValues += VALUE_NOT_EMPTY_COLUMNS;
      }
      ps = con.prepareStatement(fieldsValues);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CONSULT_FIELD_VALUES1. */
  private static final String CONSULT_FIELD_VALUES1 = " SELECT dc.doc_id, dc.template_id,"
      + " dc.patient_id, pcfv.value_id, pcfv.field_id, pcfv.field_value, dhtf.field_name,"
      + " dhtf.default_value, dhtf.num_lines, dhtf.display_order, dhtf.print_column"
      + " FROM doctor_consultation dc " + "       JOIN doc_hvf_templates dht using (template_id) "
      + "       JOIN doc_hvf_template_fields dhtf using (template_id) "
      + "       JOIN patient_consultation_field_values pcfv "
      + "               ON (dc.doc_id=pcfv.doc_id and dhtf.field_id=pcfv.field_id) "
      + " WHERE consultation_id=?";

  /** The Constant PRINTABLE_COLUMNS1. */
  private static final String PRINTABLE_COLUMNS1 = " AND print_column='Y' ";

  /** The Constant VALUE_NOT_EMPTY_COLUMNS1. */
  private static final String VALUE_NOT_EMPTY_COLUMNS1 = " AND coalesce(field_value, '')!=''";

  /**
   * Gets the consultation fields values.
   *
   * @param consultationId      the consultation id
   * @param allFields           the all fields
   * @param notEmptyFieldValues the not empty field values
   * @return the consultation fields values
   * @throws SQLException the SQL exception
   */
  public static List getConsultationFieldsValues(int consultationId, boolean allFields,
      boolean notEmptyFieldValues) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = CONSULT_FIELD_VALUES1;
      if (!allFields) {
        fieldsValues = fieldsValues + PRINTABLE_COLUMNS1;
      }
      if (notEmptyFieldValues) {
        fieldsValues += VALUE_NOT_EMPTY_COLUMNS1;
      }
      ps = con.prepareStatement(fieldsValues);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CONSULT_FIELD_HISTORY. */
  private static final String CONSULT_FIELD_HISTORY = " SELECT dc.doc_id, dc.template_id,"
      + " dc.consultation_id, dc.visited_date, dc.patient_id, pcfv.value_id, "
      + " pcfv.field_id, pcfv.field_value, dhtf.field_name, dhtf.default_value,"
      + " dhtf.num_lines, dhtf.display_order, dhtf.print_column " + " FROM doctor_consultation dc "
      + "   JOIN doc_hvf_templates dht using (template_id) "
      + "   JOIN doc_hvf_template_fields dhtf using (template_id) "
      + "   JOIN patient_consultation_field_values pcfv "
      + "   ON (dc.doc_id=pcfv.doc_id and dhtf.field_id=pcfv.field_id) "
      + " WHERE consultation_id IN " + " (SELECT consultation_id FROM doctor_consultation dc "
      + "     JOIN patient_registration pr using (patient_id) "
      + " WHERE dc.mr_no=? AND pr.visit_type=? and doctor_name = ? AND consultation_id < ? "
      + " ORDER BY consultation_id desc)  order by consultation_id desc";

  /**
   * Gets the consultation fields history.
   *
   * @param mrNo           the mr no
   * @param visitType      the visit type
   * @param doctorName     the doctor name
   * @param consultationId the consultation id
   * @return the consultation fields history
   * @throws SQLException the SQL exception
   */
  public static List getConsultationFieldsHistory(String mrNo, String visitType, String doctorName,
      int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONSULT_FIELD_HISTORY);
      ps.setString(1, mrNo);
      ps.setString(2, visitType);
      ps.setString(3, doctorName);
      ps.setInt(4, consultationId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
