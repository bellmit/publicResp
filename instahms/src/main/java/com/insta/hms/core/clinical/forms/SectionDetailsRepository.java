package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SectionDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new section details repository.
   */
  public SectionDetailsRepository() {
    super("patient_section_details");
  }

  /** The Constant GET_RECORD. */
  private static final String GET_RECORD = " SELECT psd.* FROM patient_section_details psd "
      + "JOIN patient_section_forms psf USING (section_detail_id) "
      + "WHERE psd.mr_no=? AND psd.patient_id=? AND #filter#=? "
      + " AND psd.item_type=? AND section_id=? AND psf.form_type=? ";

  /**
   * Gets the record.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return the record
   */
  public BasicDynaBean getRecord(FormParameter parameter, Integer sectionId) {
    return DatabaseHelper.queryToDynaBean(
        GET_RECORD.replace("#filter#", parameter.getFormFieldName()), parameter.getMrNo(),
        parameter.getPatientId(), parameter.getId(), parameter.getItemType(), sectionId,
        parameter.getFormType());
  }

  /**
   * Gets the all records.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return the all records
   */
  public List<BasicDynaBean> getAllRecords(FormParameter parameter, Integer sectionId) {
    return DatabaseHelper.queryToDynaList(
        GET_RECORD.replace("#filter#", parameter.getFormFieldName()), parameter.getMrNo(),
        parameter.getPatientId(), parameter.getId(), parameter.getItemType(), sectionId,
        parameter.getFormType());
  }

  /** The Constant FORM_SECTIONS_FROM_TX. */
  private static final String FORM_SECTIONS_FROM_TX = " SELECT "
      + " psf.form_id,"
      + " psd.section_id, "
      + " (case when (2>=? OR psd.section_id IN ( -1, -3, -6, -4)) then true "
      + " else exists (select section_id from insta_section_rights isr Where "
      + " isr.section_id=psd.section_id AND isr.role_id=?) end) AS section_rights, "
      + " coalesce(sys.display_name, sm.section_title) as section_title,"
      + " coalesce(sm.allow_all_normal, 'N') as allow_all_normal,"
      + " sm.linked_to as linked_to,"
      + " coalesce(sys.section_mandatory, sm.section_mandatory) as section_mandatory,"
      + " sys.field_phrase_category_id,"
      + " coalesce(sm.allow_duplicate, false) as allow_duplicate,"
      + " psd.finalized,"
      + " psf.display_order "
      + "FROM patient_section_details psd "
      + "LEFT JOIN system_generated_sections sys USING (section_id) "
      + "LEFT JOIN section_master sm USING (section_id) "
      + "JOIN patient_section_forms psf USING (section_detail_id) "
      + "WHERE #filter#=? AND psf.form_type=? ORDER BY display_order ";

  /**
   * Gets the sections.
   *
   * @param parameter the parameter
   * @param roleId    the role id
   * @return the sections
   */
  public List<BasicDynaBean> getSections(FormParameter parameter, Integer roleId) {
    return DatabaseHelper.queryToDynaList(
        FORM_SECTIONS_FROM_TX.replace("#filter#", parameter.getFormFieldName()),
        new Object[] { roleId, roleId, parameter.getId(), parameter.getFormType() });
  }

  /** The Constant SECTION_FROM_TX_WITH_SAVED_STATUS. */
  private static final String SECTION_FROM_TX_WITH_SAVED_STATUS =
      "SELECT DISTINCT psf.form_id, psd.section_id, "
          + " (case when 2>=:roleId then true else exists (select section_id"
          + " from insta_section_rights isr"
          + " Where isr.section_id=psd.section_id AND isr.role_id=:roleId) end) AS section_rights, "
          + " coalesce(sys.display_name, sm.section_title) as section_title,"
          + " coalesce(sm.allow_all_normal, 'N') as allow_all_normal, sm.linked_to as linked_to, "
          + " coalesce(sys.section_mandatory, sm.section_mandatory) as section_mandatory, "
          + " sys.field_phrase_category_id, coalesce(sm.allow_duplicate, false)"
          + " as allow_duplicate, "
          + " psd.finalized, psf.display_order, "
          + " (case when section_id=-1 then exists (select patient_id from patient_registration"
          + " where patient_id=psd.patient_id AND complaint is not null AND complaint!='') "
          + " when section_id=-2 then exists (select allergy_id from patient_allergies where "
          + " section_detail_id=psd.section_detail_id) "
          + " when section_id=-4 then exists (select patient_id from visit_vitals vv"
          + " where vv.patient_id=psd.patient_id) "
          + " when section_id=-5 then exists (select consultation_id from doctor_consultation dc, "
          + " patient_consultation_field_values dcfv where dc.consultation_id=psd.section_item_id"
          + " AND dcfv.doc_id=dc.doc_id "
          + "    AND dcfv.field_id=-1 AND dcfv.field_value!='' AND dcfv.field_value is not NULL) "
          + " when section_id=-6 then #filter-data# "
          + " when section_id=-7 then #filter-prescription# "
          + " when section_id=-18 then exists (select patient_id from patient_notes"
          + " where patient_id = psd.patient_id)"
          + " when section_id=-13 then exists (select section_detail_id"
          + " from obstetric_headrecords where "
          + " section_detail_id=psd.section_detail_id AND (field_g is not null"
          + " or field_p is not null "
          + " or field_l is not null or field_a is not null) "
          + " UNION ALL "
          + " select section_detail_id from pregnancy_history where section_detail_id"
          + " = psd.section_detail_id) "
          + " when section_id=-14 then exists (select section_detail_id from antenatal_main where "
          + " section_detail_id=psd.section_detail_id) "
          + " when section_id=-15 then exists (select section_detail_id"
          + " from patient_health_maintenance where "
          + " section_detail_id=psd.section_detail_id) "
          + " when section_id=-16 then exists (select section_detail_id from patient_pac where "
          + " section_detail_id=psd.section_detail_id) "
          + " when section_id=-21 then ("
          + " (exists (select section_item_id from patient_section_details"
          + " WHERE section_id=-21 AND patient_id=:visitId))"
          + " OR (exists (select ppl_id from patient_problem_list where mr_no=:mrNo)))"
          + " when section_id>0 then exists (select section_detail_id"
          + " from patient_section_fields psf "
          + " LEFT JOIN patient_section_options pso USING (field_detail_id)"
          + " LEFT JOIN patient_section_image_details psid USING (field_detail_id) where "
          + " section_detail_id=psd.section_detail_id AND  (((field_remarks!=''"
          + " AND field_remarks is not NUll) "
          + " OR date is not Null OR date_time is not Null OR psid.marker_detail_id is not NULL)"
          + " OR pso.available='Y')) "
          + " else false end) as saved, psd.section_detail_id "
          + " FROM patient_section_details psd "
          + " LEFT JOIN system_generated_sections sys USING (section_id) "
          + " LEFT JOIN section_master sm USING (section_id) "
          + " JOIN patient_section_forms psf USING (section_detail_id) "
          + " WHERE #filter#=:id AND psf.form_type=:formType ORDER BY display_order";

  /**
   * Gets the sections with saved status.
   *
   * @param parameter the parameter
   * @param roleId    the role id
   * @return the sections with saved status
   */
  public List<BasicDynaBean> getSectionsWithSavedStatus(FormParameter parameter, Integer roleId) {
    String formType = parameter.getFormType();
    String query = SECTION_FROM_TX_WITH_SAVED_STATUS.replace("#filter#",
        parameter.getFormFieldName());
    if (formType.equals("Form_IP")) {
      query = query
          .replace("#filter-data#",
              " exists (select visit_id from mrd_diagnosis where visit_id = psd.patient_id) ")
          .replace("#filter-prescription#",
              " exists (select visit_id from patient_prescription where visit_id"
                  + " = psd.patient_id) ");
    } else {
      query = query.replace("#filter-data#", " true ").replace("#filter-prescription#",
          " exists (select consultation_id from patient_prescription where consultation_id"
              + " = psd.section_item_id) ");
    }
    MapSqlParameterSource sqlParam = new MapSqlParameterSource();
    sqlParam.addValue("roleId", roleId);
    sqlParam.addValue("visitId", parameter.getPatientId());
    sqlParam.addValue("mrNo", parameter.getMrNo());
    sqlParam.addValue("id", parameter.getId());
    sqlParam.addValue("formType", formType);
    return DatabaseHelper.queryToDynaList(query,sqlParam);
  }

  /** The Constant SECTION_DATA_STATUS. */
  private static final String SECTION_DATA_STATUS = "SELECT (case "
      + " when sm.linked_to='patient' then exists (SELECT psd.section_detail_id"
      + " FROM patient_section_details psd, "
      + " patient_section_fields psf WHERE psd.mr_no=? and psd.section_status='A'"
      + " and psd.section_detail_id=psf.section_detail_id ) "
      + " when sm.linked_to='visit' then exists (SELECT psd.section_detail_id"
      + " FROM patient_section_details psd "
      + " JOIN patient_section_fields psf USING (section_detail_id) "
      + " LEFT JOIN patient_section_options pso USING (field_detail_id)"
      + " LEFT JOIN patient_section_image_details psid USING (field_detail_id)"
      + " WHERE (CASE When 0=? then psd.patient_id=? and psd.section_status='A'"
      + " else psd.section_detail_id =? end)"
      + " AND psd.section_detail_id=psf.section_detail_id "
      + " AND (((field_remarks!='' AND field_remarks is not NUll) OR date is not Null"
      + " OR date_time is not Null "
      + " OR psid.marker_detail_id is not NULL) OR pso.available='Y')"
      + " and psd.section_id = ? limit 1 ) "
      + " when sm.linked_to='order item' then exists (SELECT psd.section_detail_id"
      + " FROM patient_section_details psd "
      + " JOIN patient_section_fields psf USING (section_detail_id) "
      + " LEFT JOIN patient_section_options pso USING (field_detail_id)"
      + " LEFT JOIN patient_section_image_details psid USING (field_detail_id)"
      + " WHERE (CASE When 0=? then psd.#filter#=? and psd.section_status='A'"
      + " else psd.section_detail_id =? end)"
      + " AND psd.section_detail_id=psf.section_detail_id "
      + " AND (((field_remarks!='' AND field_remarks is not NUll) OR date is not Null"
      + " OR date_time is not Null "
      + " OR psid.marker_detail_id is not NULL) OR pso.available='Y') and"
      + " psd.section_id = ? limit 1) "
      + " when sm.linked_to='form' then exists (SELECT psd.section_detail_id"
      + " FROM patient_section_details psd "
      + " JOIN patient_section_fields psf USING (section_detail_id) "
      + " LEFT JOIN patient_section_options pso USING (field_detail_id)"
      + " LEFT JOIN patient_section_image_details psid USING (field_detail_id)"
      + " WHERE psd.section_detail_id =? "
      + " AND psd.section_detail_id=psf.section_detail_id "
      + " AND (((field_remarks!='' AND field_remarks is not NUll) OR date is not Null"
      + " OR date_time is not Null "
      + " OR psid.marker_detail_id is not NULL) OR pso.available='Y')"
      + " and psd.section_id = ? limit 1) "
      + " else false end) as data_status FROM section_master sm WHERE sm.section_id=?";

  /**
   * Gets the section data status.
   *
   * @param parameter       the parameter
   * @param sectionDetailId the section detail id
   * @param sectionId       the section id
   * @return the section data status
   */
  public Boolean getSectionDataStatus(FormParameter parameter, Integer sectionDetailId,
      Integer sectionId) {
    return (Boolean) DatabaseHelper
        .queryToDynaBean(SECTION_DATA_STATUS.replace("#filter#", parameter.getFormFieldName()),
            new Object[] { parameter.getMrNo(), sectionDetailId, parameter.getPatientId(),
                sectionDetailId, sectionId, sectionDetailId, parameter.getId(), sectionDetailId,
                sectionId, sectionDetailId, sectionId, sectionId })
        .get("data_status");
  }

  /** The Constant ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT. */
  private static final String ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT = " SELECT"
      + " field_id, option_id, option_value, "
      + " case when field_type in ('text', 'wide text') then field_remarks"
      + " else option_remarks end as option_remarks, "
      + " field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, "
      + "   'sd_' || section_detail_id as str_section_detail_id, psfv.finalized, "
      + " coalesce(image_id, 0) as image_id, field_detail_id, marker_detail_id "
      + " FROM patient_section_field_values_for_print psfv "
      + " WHERE mr_no=? AND patient_id=? AND coalesce(section_item_id, 0)=? "
      + " AND coalesce(generic_form_id, 0)=? AND form_id=? AND item_type=? AND value_found "
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
   */
  public List<BasicDynaBean> getAllSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return DatabaseHelper.queryToDynaList(ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT,
        new Object[] { mrNo, patientId, itemId, genericFormId, formId, itemType });
  }

  /** The All SECTION S FRO M MASTER. */
  private static String All_SECTIONS_FROM_MASTER = "SELECT section_id::int as section_id, "
      + "(case when 2>=? then true else exists (select section_id from "
      + " insta_section_rights isr Where "
      + " isr.section_id=sm.section_id::integer AND isr.role_id=?) end) AS section_rights, "
      + " sm.section_title as section_title, "
      + "coalesce(sm.allow_all_normal, 'N') as allow_all_normal, "
      + "sm.linked_to as linked_to, "
      + "sm.section_mandatory, "
      + " null as field_phrase_category_id, "
      + "coalesce(sm.allow_duplicate, false) as allow_duplicate, "
      + " 'N' as finalized, "
      + " 0 as display_order "
      + " FROM section_master sm "
      + " WHERE coalesce(sm.status, 'A')='A' "
      + " #linked_fitler#"
      + " UNION ALL "
      + " SELECT section_id::int as section_id, "
      + " (case when 2>=? OR section_id::int IN (-1, -3, -6, -4) then true else"
      + " exists (select section_id from "
      + " insta_section_rights isr Where "
      + " isr.section_id=sgs.section_id::integer AND isr.role_id=?) end) AS section_rights, "
      + " display_name as section_title, "
      + " 'N' as allow_all_normal, "
      + " null as linked_to, "
      + " section_mandatory, "
      + " field_phrase_category_id, "
      + " false as allow_duplicate, "
      + " 'N' as finalized, "
      + " 0 as display_order "
      + " from system_generated_sections sgs WHERE #section_fitler# ";

  /**
   * Gets the all master sections.
   *
   * @param roleId   the role id
   * @param formType the form type
   * @return the all master sections
   */
  public List<BasicDynaBean> getAllMasterSections(Integer roleId, String formType) {
    String query = All_SECTIONS_FROM_MASTER;
    if (formType.equals("Form_IP")) {
      query = query.replace("#linked_fitler#", " AND linked_to IN ('patient', 'visit', 'form') ");
      query = query.replace("#section_fitler#", " ip !='N' ");
    } else {
      query = query.replace("#linked_fitler#", "");
      query = query.replace("#section_fitler#", " op !='N' ");
    }

    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(query,
        new Object[] { roleId, roleId, roleId, roleId });

    return list;
  }

  /** The insta sections details. */
  private static String INSTA_SECTIONS_DETAILS = " SELECT"
      + " sm.section_title, psd.section_detail_id,"
      + " psf.form_id, display_order, psd.section_id, "
      + " psd.finalized, psd.finalized_user, usr.temp_username "
      + " FROM section_master sm "
      + " JOIN patient_section_details psd ON (psd.section_id=sm.section_id)"
      + " JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id) "
      + " LEFT JOIN u_user usr ON (psd.finalized_user=usr.emp_username)"
      + " WHERE psd.mr_no=? AND psd.patient_id=? AND coalesce(psd.section_item_id, 0)=?"
      + " AND coalesce(psd.generic_form_id, 0)=? AND psf.form_id=? AND psd.item_type=?"
      + " ORDER BY display_order, psd.section_detail_id";

  /**
   * Gets the added section master details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the added section master details
   */
  public List<BasicDynaBean> getAddedSectionMasterDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(INSTA_SECTIONS_DETAILS,
        new Object[] { mrNo, patientId, itemId, genericFormId, formId, itemType });

    return list;
  }

  /** The Constant GET_CARRYFORWARD_SECTIONS_BY_SECTION_IDS. */
  private static final String GET_CARRYFORWARD_SECTIONS_BY_SECTION_IDS = "SELECT"
      + " count(*) as count, sm.section_id FROM section_master sm "
      + "JOIN patient_section_details psd ON (psd.mr_no=:mr_no AND psd.section_status='A'"
      + " AND psd.section_id=sm.section_id) "
      + "WHERE sm.linked_to='patient' AND sm.section_id IN (:sectionIds) "
      + "GROUP BY sm.section_id "
      + " UNION ALL "
      + "SELECT count(*) as count, sm.section_id "
      + "FROM section_master sm "
      + "JOIN patient_section_details psd ON (psd.patient_id=:patient_id"
      + " AND psd.section_status='A'"
      + " AND psd.section_id=sm.section_id) "
      + "WHERE sm.linked_to='visit' AND sm.section_id IN (:sectionIds) "
      + "GROUP BY sm.section_id "
      + " UNION ALL "
      + "SELECT count(*) as count, sm.section_id "
      + "FROM section_master sm "
      + "JOIN patient_section_details psd ON (psd.#filter#=:section_item_id "
      + "AND psd.section_status='A'"
      + " AND psd.section_id=sm.section_id AND psd.item_type=:item_type) "
      + "WHERE sm.linked_to='order item' AND sm.section_id IN (:sectionIds) "
      + "GROUP BY sm.section_id ";

  /**
   * Gets the carry forward sections by section ids. This method is used only for TOC
   * generation of forms.
   *
   * @param parameter  the parameter
   * @param sectionIds the section ids
   * @return the carry forward sections by section ids
   */
  public List<BasicDynaBean> getCarryForwardSectionsBySectionIds(FormParameter parameter,
      List<Integer> sectionIds) {
    MapSqlParameterSource queryParameters = new MapSqlParameterSource();
    queryParameters.addValue("mr_no", parameter.getMrNo());
    queryParameters.addValue("item_type", parameter.getItemType());
    queryParameters.addValue("patient_id", parameter.getPatientId());
    queryParameters.addValue("section_item_id", parameter.getId());
    queryParameters.addValue("sectionIds", sectionIds);
    return DatabaseHelper.queryToDynaList(
        GET_CARRYFORWARD_SECTIONS_BY_SECTION_IDS.replace("#filter#", parameter.getFormFieldName()),
        queryParameters);
  }

  private static final String GET_CLINICAL_DATA_BY_PATIENT_ID = "select"
      + " textcat_commacat(psfv.field_name||' : '|| "
      + " COALESCE(psfv.option_value,text(psfv.date_time),text(psfv.date),'') ||' '|| "
      + " COALESCE(psfv.option_remarks,'')||' '||"
      + " COALESCE(psfv.field_remarks,' ')) AS field_name_value,"
      + " stm.section_type as section_type, stm.section_type_id as section_type_id"
      + " FROM patient_section_field_values_for_print psfv"
      + " JOIN section_master sm ON(sm.section_id=psfv.section_id)"
      + " JOIN section_type_master stm ON(stm.section_type_id = sm.section_type_id)"
      + " where psfv.patient_id=? and psfv.value_found "
      + " GROUP BY stm.section_type_id,stm.section_type";

  public List<BasicDynaBean> getClinicalDataByPatientId(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_CLINICAL_DATA_BY_PATIENT_ID, patientId);
  }

  private static final String UPDATE_SECTIONS_FINALIZE_STATUS = "UPDATE"
      + " patient_section_details SET finalized='N' "
      + "WHERE #formTypeKey=:formId AND section_id IN (:sectionIds)"
      + " AND patient_id=:patientId AND mr_no=:mrNo";

  /**
   * Update unfinalize status for multiple sections.
   * @param param parameters
   * @param sectionIds section identifiers
   * @return true or false inidcating status of update
   */
  public boolean updateSectionsUnFinalizeStatus(FormParameter param, List<Integer> sectionIds) {
    MapSqlParameterSource queryParams = new MapSqlParameterSource();
    queryParams.addValue("formId", param.getId());
    queryParams.addValue("sectionIds", sectionIds);
    queryParams.addValue("patientId", param.getPatientId());
    queryParams.addValue("mrNo", param.getMrNo());
    String query = UPDATE_SECTIONS_FINALIZE_STATUS.replace("#formTypeKey",
        param.getFormFieldName());
    return DatabaseHelper.update(query, queryParams) > 0;
  }
}
