package com.insta.hms.mdm.notetypes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public class NoteTypesRepository extends MasterRepository<Integer> {

  public NoteTypesRepository() {
    super("note_type_master", "note_type_id");
  }
  
  /** The Constant NOTE_TYPE_FIELDS. */
  private static final String NOTE_TYPE_FIELDS = "SELECT distinct ntm.note_type_id, "
            + " ntm.note_type_name, ntm.assoc_hosp_role_id,ntm.editable_by,"
            + " ntm.status, ntm.billing_option, ntm.transcribing_role_id , ntm.mod_time ";
          
  
  /** The Constant NOTE_TYPE_FROM. */
  private static final String NOTE_TYPE_TABLES = " FROM note_type_master ntm "
            + " LEFT JOIN note_type_template_master nttm ON (ntm.note_type_id = nttm.note_type_id)";
  
  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(distinct ntm.note_type_id) ";

  /**
   * Gets the note types details.
   *
   * @param paramMap the param map
   * @return the note types details
   */
  public PagedList getNoteTypesDetails( Map<String, String[]> paramMap,
      Date startDate, Date endDate) {
    Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(paramMap);
    int pageNum = ((Integer) listingParams.get(LISTING.PAGENUM)).intValue();
    int pageSize = ((Integer) listingParams.get(LISTING.PAGESIZE)).intValue();
    
    SearchQueryAssembler qb = new SearchQueryAssembler(NOTE_TYPE_FIELDS, COUNT, NOTE_TYPE_TABLES, 
        null, "note_type_name", false,pageSize,pageNum );
    qb.addFilterFromParamMap(paramMap);
    if (startDate != null) {
      qb.addFilter(SearchQueryAssembler.DATE, "created_time", ">=", startDate);
    }
    if  (endDate != null) {
      qb.addFilter(SearchQueryAssembler.DATE, "created_time", "<=", endDate);
    }

    qb.build();
    
    return qb.getMappedPagedList();
  }

  private static final String GET_USER_NOTETYPES = "SELECT distinct ntm.note_type_id,"
         + " ntm.note_type_name, ntm.assoc_hosp_role_id, ntm.transcribing_role_id,"
         + " ntm.billing_option "
         + " FROM note_type_master ntm "
         + " LEFT JOIN user_hosp_role_master uar ON (uar.hosp_role_id = ntm.assoc_hosp_role_id)"
         + " LEFT JOIN user_hosp_role_master utr ON (utr.hosp_role_id = ntm.transcribing_role_id)"
         + " WHERE status = 'A' AND (uar.u_user=:associatedUser OR utr.u_user=:transcribedUser) ";
  
  /**
   * Gets the user notes.
   *
   * @param userName the user name
   * @return the user notes
   */
  public List<BasicDynaBean> getUserNotes(String userName) {
    String query = GET_USER_NOTETYPES;
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("associatedUser", userName);
    parameters.addValue("transcribedUser", userName);
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /**
   * Gets the note type by id.
   *
   * @param name the note type name
   * @return the id of the note type
   */
  public Integer getNotesTypeIdByName(String name) {
    String query = "select note_type_id from note_type_master where note_type_name=? "
        + "order by mod_time desc limit 1";
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(query, name);
    if (list != null && !list.isEmpty() && list.get(0) != null) {
      return (Integer) (((BasicDynaBean) list.get(0)).getMap().get("note_type_id"));
    }
    return null;
  }
  
  /**
   * This function returns getNoteTypeMasterByRoleId from NoteTypeMaster.
   * @param hospitalRoleIds hosptialRoleIds
   * @return list of records
   */
  public List<BasicDynaBean> getNoteTypeMasterByRoleId(List<Integer> hospitalRoleIds) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("hospitalRoleIds", hospitalRoleIds);
    String query = NOTE_TYPE_FIELDS
        + " FROM note_type_master ntm "
        + " WHERE assoc_hosp_role_id IN (:hospitalRoleIds)";
    return DatabaseHelper.queryToDynaList(query, params);
  }
  
}

