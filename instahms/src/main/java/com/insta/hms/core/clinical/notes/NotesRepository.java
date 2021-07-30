package com.insta.hms.core.clinical.notes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class NotesRepository extends GenericRepository {

  public NotesRepository() {
    super("patient_notes");
  }

  private static final String NOTE_FIELDS = "SELECT * ";


  private static final String NOTE_TABLES = " FROM ( SELECT distinct pn.note_id,"
      + " pn.patient_id, pn.note_type_id, pn.note_content, pn.billable_consultation,"
      + " pn.save_status, pn.original_note_id, pn.on_behalf_doctor_id, pn.on_behalf_user,"
      + " pn.created_by, pn.created_time, pn.new_note_id, ntm.note_type_name,"
      + " ntm.editable_by, pn.consultation_type_id, pn.documented_date,"
      + " to_char(pn.documented_time, 'hh24:mi') as documented_time "
      + " FROM patient_notes pn "
      + " JOIN note_type_master ntm ON (ntm.note_type_id = pn.note_type_id)" + " #draftnotes"
      + " ) AS foo";

  private static final String COUNT = " SELECT count(distinct(note_id)) ";

  /**
   * Gets patient notes.
   * 
   * @param paramMap the map
   * @param startDate the date
   * @param endDate the date
   * @param roleId the role
   * @param userName the usename
   * @return paged list
   */
  public PagedList getPatientNotesDetails(Map<String, String[]> paramMap, Date startDate,
      Date endDate, Integer roleId, String userName) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(paramMap);
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();
    String noteTables = NOTE_TABLES;
    if (roleId > 2) {
      noteTables = noteTables.replace("#draftnotes", "WHERE CASE WHEN pn.save_status='D' "
          + "THEN pn.created_by = '" + userName + "' ELSE true END");
    } else {
      noteTables = noteTables.replace("#draftnotes", " ");
    }
    String sortField = "documented_date";
    if (paramMap.containsKey("sort_date") && paramMap.get("sort_date") != null) {
      sortField = ((String[]) paramMap.get("sort_date"))[0];
    }
    SearchQueryAssembler qb = new SearchQueryAssembler(NOTE_FIELDS, COUNT, noteTables, null,
        sortField, true, pageSize, pageNum);
    if ("documented_date".equals(sortField)) {
      qb.addSecondarySort("documented_time",true);
    }
    qb.addFilterFromParamMap(paramMap);
    if (startDate != null) {
      qb.addFilter(SearchQueryAssembler.DATE, "DATE(created_time)", ">=", startDate);
    }
    if (endDate != null) {
      qb.addFilter(SearchQueryAssembler.DATE, "DATE(created_time)", "<=", endDate);
    }

    qb.build();

    return qb.getMappedPagedList();
  }

  private static final String GET_USER_HOSP_ROLE = "SELECT u_user"
      + " FROM user_hosp_role_master where u_user = :userName and hosp_role_id IN (:hospRoleIds)";

  /**
   * Gets editable notes.
   * 
   * @param hospRoleIds the hosp role
   * @param userName the username
   * @return list of beans
   */
  public List<BasicDynaBean> getEditiableNoteUsers(List<Integer> hospRoleIds, String userName) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    String query = GET_USER_HOSP_ROLE;
    parameters.addValue("userName", userName);
    parameters.addValue("hospRoleIds", hospRoleIds);
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  private static final String GET_PATIENT_NOTES_PRINT = "SELECT distinct pn.note_id,"
      + " pn.patient_id, pn.note_type_id, pn.note_content, pn.billable_consultation,"
      + " pn.save_status, pn.original_note_id, pn.on_behalf_doctor_id, pn.on_behalf_user,"
      + " pn.created_by, pn.created_time, pn.new_note_id, ntm.note_type_name,"
      + " ntm.editable_by, pn.consultation_type_id, pn.documented_date,"
      + " to_char(pn.documented_time, 'hh24:mi') as documented_time"
      + " FROM patient_notes pn "
      + " JOIN note_type_master ntm ON (ntm.note_type_id = pn.note_type_id)"
      + " WHERE pn.new_note_id IS NULL AND pn.patient_id=:patientId AND NOT pn.save_status='D' "
      + " #noteTypeIdFilter#"
      + " ORDER BY documented_date ASC, documented_time ASC";

  /**
   * Gets patient notes.
   * 
   * @param patientId the visit id
   * @return list of beans
   */
  public List<BasicDynaBean> getPatientNotesForPrint(String patientId, List<Integer> noteTypeIds) {
    String query = GET_PATIENT_NOTES_PRINT;
    MapSqlParameterSource params = new MapSqlParameterSource();
    if (!noteTypeIds.isEmpty()) {
      query = query.replace("#noteTypeIdFilter#"," AND pn.note_type_id IN (:noteTypeIds)");
      params.addValue("noteTypeIds", noteTypeIds);
    } else {
      query = query.replace("#noteTypeIdFilter#", "");
    }
    params.addValue("patientId", patientId);
    return DatabaseHelper.queryToDynaList(query, params);
  }

  private static final String GET_SAME_NOTE_TYPE_DRAFTS = "SELECT count(note_type_id)"
      + " FROM patient_notes" + " WHERE patient_id =? AND save_status = 'D' AND "
      + " created_by =? GROUP BY note_type_id, patient_id,save_status,created_by ";

  public List<BasicDynaBean> getSameNoteTypeDrafts(String patientId, String userName) {
    return DatabaseHelper.queryToDynaList(GET_SAME_NOTE_TYPE_DRAFTS,
        new Object[] {patientId, userName});
  }

  private static final String GET_BILLED_CONSULTATIONS =
      "SELECT count(on_behalf_doctor_id),d.doctor_id" + " FROM doctors d"
          + " JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id)"
          + " LEFT JOIN patient_notes pn ON (pn.on_behalf_doctor_id = d.doctor_id"
          + " AND billable_consultation='Y' AND created_time::date = date(current_timestamp)"
          + " AND pn.patient_id =?)" + " WHERE d.status = 'A'";

  /**
   * Gets billed cons count.
   * 
   * @param patientId the visit id
   * @param centerId the center id
   * @return list of beans
   */
  public List<BasicDynaBean> getBilledConsCountPerDay(String patientId, Integer centerId) {
    String query = GET_BILLED_CONSULTATIONS;
    List<Object> queryParams = new ArrayList<>();
    queryParams.add(patientId);
    if (centerId != null && centerId != 0) {
      query += " AND (center_id = ? OR center_id=0) GROUP BY d.doctor_id";
      queryParams.add(centerId);
    } else {
      query += " GROUP BY d.doctor_id";
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

  private static final String GET_BILLABLE_DAY_NOTES = "SELECT count(*) FROM patient_notes"
      + " WHERE billable_consultation='Y' AND created_time::date = date(current_timestamp)"
      + " AND  patient_id= ? AND on_behalf_doctor_id= ? ";

  public Integer getBillableNotesForDay(String patientId, String doctorId) {

    return DatabaseHelper.getInteger(GET_BILLABLE_DAY_NOTES, patientId, doctorId);
  }

  private static final String GET_TOTAL_NOTES_COUNT =
      "SELECT count(*)" + " FROM patient_notes WHERE patient_id= ? #draftnotes";

  /**
   * Gets total notes count.
   * 
   * @param patientId the visit id
   * @param roleId the user role id
   * @param userName the username
   * @return integer
   */
  public Integer getTotalNotesCount(String patientId, Integer roleId, String userName) {
    String query = GET_TOTAL_NOTES_COUNT;
    if (roleId > 2) {
      query = query.replace("#draftnotes",
          "AND CASE WHEN save_status='D' " + "THEN created_by = '" + userName + "' ELSE true END");
    } else {
      query = query.replace("#draftnotes", " ");
    }
    return DatabaseHelper.getInteger(query, new Object[] {patientId});
  }
  
  private static final   StringBuilder FETCH_NOTES_BY_HOSPITAL_ROLE = new StringBuilder()
      .append("SELECT ")
      .append("hrm.hosp_role_id, ")
      .append("ntm.note_type_id, ")
      .append("ntm.note_type_name, ")
      .append("hrm.hosp_role_name ")
      .append("FROM ")
      .append("patient_notes pn ")
      .append("LEFT JOIN note_type_master ntm USING (note_type_id) ")
      .append("LEFT JOIN hospital_roles_master hrm ON (hrm.hosp_role_id = ntm.assoc_hosp_role_id) ")
      .append("WHERE ")
      .append("pn.patient_id = ?")
      .append("GROUP BY hrm.hosp_role_id, ntm.note_type_id");
  
  public List<BasicDynaBean> getNotesByHospitalRolesOrNoteType(String patientId) {
    return DatabaseHelper.queryToDynaList(FETCH_NOTES_BY_HOSPITAL_ROLE.toString(), patientId);
  }
  
}
