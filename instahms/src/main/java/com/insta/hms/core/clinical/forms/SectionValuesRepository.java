package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SectionValuesRepository.
 *
 * @author krishnat
 */
@Repository
public class SectionValuesRepository extends GenericRepository {

  /**
   * Instantiates a new section values repository.
   */
  public SectionValuesRepository() {
    super("patient_section_fields");
  }

  /**
   * Checks if is image used.
   *
   * @param fieldDetailId the field detail id
   * @param imageId the image id
   * @return true, if is image used
   */
  // is the selected image used by any other field.
  public boolean isImageUsed(int fieldDetailId, int imageId) {
    String query =
        "select field_detail_id from patient_section_fields where image_id=?"
        + " and field_detail_id!=?";
    return DatabaseHelper.queryToDynaBean(query, new Object[] {imageId, fieldDetailId}) != null;
  }

  /**
   * Gets the section values.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return the section values
   */
  public List<BasicDynaBean> getSectionValues(FormParameter parameter, int sectionId) {
    String query =
        " SELECT sm.section_id, coalesce(psd.finalized, 'N') as finalized,"
        + " coalesce(psd.section_detail_id, 0) as section_detail_id, "
        + " psd.revision_number, psd.user_name, psd.mod_time, sfd.field_id, sfd.field_name, "
        + " sfd.field_type, sfd.is_mandatory, sfd.allow_others, sfd.allow_normal, sfd.normal_text,"
        + " sfd.no_of_lines, "
        + " sfd.phrase_category_id as field_phrase_category_id, "
        + " sfd.pattern_id as field_pattern_id, sfd.display_order as field_display_order, "
        + " sfo.option_id, sfo.option_value, sfo.value_code, sfd.default_to_current_datetime, "
        + " sfo.phrase_category_id as option_phrase_category_id,"
        + " sfo.display_order as option_display_order, "
        + " sfo.pattern_id as option_pattern_id, psoptions.available, psoptions.option_remarks,"
        + " psfields.field_remarks, "
        + " psfields.date, psfields.date_time, "
        + " sfd.markers, img.coordinate_x, img.coordinate_y, img.marker_id, img.notes,"
        + " psfields.image_id, psfields.field_detail_id, "
        + " marker_detail_id, option_detail_id "
        + " FROM section_master sm "
        + " JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id and sfd.status='A') "
        + " LEFT JOIN LATERAL "
        + " (SELECT option_id, field_id, option_value, display_order, 'b' as alpha, value_code,"
        + " phrase_category_id, pattern_id "
        + " FROM section_field_options isfo "
        + " WHERE (sfd.field_id=isfo.field_id and isfo.status='A') "
        + " UNION ALL "
        + " SELECT 0 as option_id, sfd.field_id, 'Normal' as option_value, 0 as display_order,"
        + " 'a' as alpha, 'Normal' as value_code, null, null "
        + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
        + " UNION ALL "
        + " SELECT -1 as option_id, sfd.field_id, 'Others' as option_value, 0 as display_order,"
        + " 'c' as alpha, 'Others' as value_code, null, null "
        + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
        + " ) as sfo on sfd.field_id=sfo.field_id "
        + " LEFT JOIN (Select psd.*, psf.display_order as section_display_order"
        + " from patient_section_forms psf LEFT JOIN patient_section_details psd ON "
        + " (psd.mr_no=? and psd.patient_id=? "
        + " AND psd."
        + parameter.getFormFieldName()
        + "=? AND item_type=?) "
        + " WHERE  (psd.section_detail_id=psf.section_detail_id and form_type=?)) as psd"
        + " ON (psd.section_id=sm.section_id) "
        + " LEFT JOIN patient_section_fields psfields ON (psfields.section_detail_id"
        + " = psd.section_detail_id and psfields.field_id=sfd.field_id) "
        + " LEFT JOIN patient_section_options psoptions ON (psfields.field_detail_id"
        + " = psoptions.field_detail_id and psoptions.option_id=sfo.option_id) "
        + " LEFT JOIN patient_section_image_details img ON (psfields.field_detail_id"
        + " = img.field_detail_id) "
        + " WHERE sm.section_id=? order by section_display_order, psd.section_detail_id,"
        + " sfd.display_order, sfo.alpha, sfo.display_order";
    return DatabaseHelper.queryToDynaList(
        query,
        new Object[] {parameter.getMrNo(), parameter.getPatientId(), parameter.getId(),
            parameter.getItemType(), parameter.getFormType(), sectionId});
  }



  /**
   * Gets the patient level section values.
   *
   * @param parameters the parameters
   * @param sectionId the section id
   * @return the patient level section values
   */
  public List<BasicDynaBean> getPatientLevelSectionValues(FormParameter parameters, int sectionId) {
    String query =
        " SELECT sm.section_id, 'N' as finalized, psd.section_detail_id, "
            + " psd.revision_number, psd.user_name, psd.mod_time, sfd.field_id, sfd.field_name, "
            + " sfd.field_type, sfd.is_mandatory, sfd.allow_others, sfd.allow_normal,"
            + " sfd.normal_text, sfd.no_of_lines, "
            + " sfd.phrase_category_id as field_phrase_category_id, "
            + " sfd.pattern_id as field_pattern_id,"
            + " sfd.display_order as field_display_order, "
            + " sfo.option_id, sfo.option_value, sfo.value_code,"
            + " sfd.default_to_current_datetime, "
            + " sfo.phrase_category_id as option_phrase_category_id,"
            + " sfo.display_order as option_display_order, "
            + " sfo.pattern_id as option_pattern_id, psoptions.available,"
            + " psoptions.option_remarks, psfields.field_remarks, "
            + " psfields.date, psfields.date_time, "
            + " sfd.markers, img.coordinate_x, img.coordinate_y, img.marker_id,"
            + " img.notes, psfields.image_id, null AS field_detail_id, "
            + " null AS marker_detail_id, null AS option_detail_id "
            + " FROM section_master sm "
            + " JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id and sfd.status='A') "
            + " LEFT JOIN LATERAL "
            + " (SELECT option_id, field_id, option_value, display_order, 'b' as alpha,"
            + " value_code, phrase_category_id, pattern_id "
            + " FROM section_field_options isfo "
            + " WHERE (sfd.field_id=isfo.field_id and isfo.status='A') "
            + " UNION ALL "
            + " SELECT 0 as option_id, sfd.field_id, 'Normal' as option_value, 0 as display_order,"
            + " 'a' as alpha, 'Normal' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " UNION ALL "
            + " SELECT -1 as option_id, sfd.field_id, 'Others' as option_value, 0 as display_order,"
            + " 'c' as alpha, 'Others' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " ) as sfo on sfd.field_id=sfo.field_id "
            + " LEFT JOIN patient_section_details psd ON (psd.section_id=sm.section_id"
            + " and psd.mr_no=? AND section_status='A') "
            + " LEFT JOIN patient_section_forms psf ON (psd.section_detail_id"
            + " = psf.section_detail_id) "
            + " LEFT JOIN patient_section_fields psfields ON (psfields.section_detail_id"
            + " = psd.section_detail_id and psfields.field_id=sfd.field_id ) "
            + " LEFT JOIN patient_section_options psoptions ON (psfields.field_detail_id"
            + " = psoptions.field_detail_id and psoptions.option_id=sfo.option_id) "
            + " LEFT JOIN patient_section_image_details img ON (psfields.field_detail_id"
            + " = img.field_detail_id) "
            + " WHERE sm.section_id=? and sm.linked_to='patient' "
            + " ORDER BY psf.display_order, psd.section_detail_id, sfd.display_order, sfo.alpha,"
            + " sfo.display_order";
    return DatabaseHelper.queryToDynaList(query, new Object[] {parameters.getMrNo(), sectionId});
  }

  /**
   * Gets the visit level section values.
   *
   * @param parameters the parameters
   * @param sectionId the section id
   * @return the visit level section values
   */
  public List<BasicDynaBean> getVisitLevelSectionValues(FormParameter parameters, int sectionId) {
    String query =
        " SELECT sm.section_id, 'N' as finalized, psd.section_detail_id, "
            + " psd.revision_number, psd.user_name, psd.mod_time, sfd.field_id, sfd.field_name, "
            + " sfd.field_type, sfd.is_mandatory, sfd.allow_others, sfd.allow_normal,"
            + " sfd.normal_text, sfd.no_of_lines, "
            + " sfd.phrase_category_id as field_phrase_category_id, "
            + " sfd.pattern_id as field_pattern_id, sfd.display_order as field_display_order, "
            + " sfo.option_id, sfo.option_value, sfo.value_code, sfd.default_to_current_datetime, "
            + " sfo.phrase_category_id as option_phrase_category_id,"
            + " sfo.display_order as option_display_order, "
            + " sfo.pattern_id as option_pattern_id, psoptions.available,"
            + " psoptions.option_remarks, psfields.field_remarks, "
            + " psfields.date, psfields.date_time, "
            + " sfd.markers, img.coordinate_x, img.coordinate_y, img.marker_id, img.notes,"
            + " psfields.image_id, null AS field_detail_id, "
            + " null AS marker_detail_id, null AS option_detail_id "
            + " FROM section_master sm "
            + " JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id and sfd.status='A') "
            + " LEFT JOIN LATERAL "
            + " (SELECT option_id, field_id, option_value, display_order, 'b' as alpha,"
            + " value_code, phrase_category_id, pattern_id "
            + " FROM section_field_options isfo "
            + " WHERE (sfd.field_id=isfo.field_id and isfo.status='A') "
            + " UNION ALL "
            + " SELECT 0 as option_id, sfd.field_id, 'Normal' as option_value, 0 as display_order,"
            + " 'a' as alpha, 'Normal' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " UNION ALL "
            + " SELECT -1 as option_id, sfd.field_id, 'Others' as option_value, 0 as display_order,"
            + " 'c' as alpha, 'Others' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " ) as sfo on sfd.field_id=sfo.field_id "
            + " LEFT JOIN patient_section_details psd ON (psd.section_id=sm.section_id"
            + " and psd.patient_id=?  "
            + " AND section_status='A') "
            + " LEFT JOIN patient_section_forms psf ON (psd.section_detail_id"
            + " = psf.section_detail_id) "
            + " LEFT JOIN patient_section_fields psfields ON (psfields.section_detail_id"
            + " = psd.section_detail_id and psfields.field_id=sfd.field_id) "
            + " LEFT JOIN patient_section_options psoptions ON (psfields.field_detail_id"
            + " = psoptions.field_detail_id and psoptions.option_id=sfo.option_id) "
            + " LEFT JOIN patient_section_image_details img ON (psfields.field_detail_id"
            + " = img.field_detail_id) "
            + " WHERE sm.section_id=? and sm.linked_to='visit' "
            + " ORDER BY psf.display_order, psd.section_detail_id, sfd.display_order, sfo.alpha,"
            + " sfo.display_order";
    return DatabaseHelper.queryToDynaList(query,
        new Object[] {parameters.getPatientId(), sectionId});
  }

  /**
   * Gets the order item level section values.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return the order item level section values
   */
  public List<BasicDynaBean> getOrderItemLevelSectionValues(FormParameter parameter,
      int sectionId) {
    String query =
        " SELECT sm.section_id, 'N' as finalized, psd.section_detail_id, "
            + " psd.revision_number, psd.user_name, psd.mod_time, sfd.field_id, sfd.field_name, "
            + " sfd.field_type, sfd.is_mandatory, sfd.allow_others, sfd.allow_normal,"
            + " sfd.normal_text, sfd.no_of_lines, "
            + " sfd.phrase_category_id as field_phrase_category_id, "
            + " sfd.pattern_id as field_pattern_id, sfd.display_order as field_display_order, "
            + " sfo.option_id, sfo.option_value, sfo.value_code, sfd.default_to_current_datetime, "
            + " sfo.phrase_category_id as option_phrase_category_id,"
            + " sfo.display_order as option_display_order, "
            + " sfo.pattern_id as option_pattern_id, psoptions.available, psoptions.option_remarks,"
            + " psfields.field_remarks, psfields.date, psfields.date_time, sfd.markers,"
            + " img.coordinate_x, img.coordinate_y, img.marker_id, img.notes, psfields.image_id,"
            + " null AS field_detail_id, "
            + " null AS marker_detail_id, null AS option_detail_id "
            + " FROM section_master sm "
            + " JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id and sfd.status='A') "
            + " LEFT JOIN LATERAL "
            + " (SELECT option_id, field_id, option_value, display_order, 'b' as alpha, value_code,"
            + " phrase_category_id, pattern_id "
            + " FROM section_field_options isfo "
            + " WHERE (sfd.field_id=isfo.field_id and isfo.status='A') "
            + " UNION ALL "
            + " SELECT 0 as option_id, sfd.field_id, 'Normal' as option_value, 0 as display_order,"
            + " 'a' as alpha, 'Normal' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " UNION ALL "
            + " SELECT -1 as option_id, sfd.field_id, 'Others' as option_value, 0 as display_order,"
            + " 'c' as alpha, 'Others' as value_code, null, null "
            + " WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " ) as sfo on sfd.field_id=sfo.field_id "
            + " LEFT JOIN patient_section_details psd ON (psd.section_id=sm.section_id and psd."
            + parameter.getFormFieldName()
            + "=?  "
            + " AND psd.item_type=? AND section_status='A') "
            + " LEFT JOIN patient_section_forms psf ON (psd.section_detail_id"
            + " = psf.section_detail_id) "
            + " LEFT JOIN patient_section_fields psfields ON (psfields.section_detail_id"
            + " = psd.section_detail_id and psfields.field_id=sfd.field_id) "
            + " LEFT JOIN patient_section_options psoptions ON (psfields.field_detail_id"
            + " = psoptions.field_detail_id and psoptions.option_id=sfo.option_id) "
            + " LEFT JOIN patient_section_image_details img ON (psfields.field_detail_id"
            + " = img.field_detail_id) "
            + " WHERE sm.section_id=? and sm.linked_to='order item' "
            + " ORDER BY psf.display_order, psd.section_detail_id, sfd.display_order,"
            + " sfo.alpha, sfo.display_order";
    return DatabaseHelper.queryToDynaList(query,
        new Object[] {parameter.getId(), parameter.getItemType(), sectionId});
  }

  //Removed the  commeted code f1c44ea3f562e242a1d024c13c530e1fcb4cb14f

  /** The triage section data. */
  private static final String TRIAGE_SECTION_DATA =
      "SELECT psd.section_id, psd.section_detail_id, sf.field_id, fd.field_name, fd.field_type, "
          + " sf.date, sf.date_time, sf.image_id, sf.field_remarks, coalesce(fo.option_value,"
          + " fo.value_code, "
          + " (case when so.option_id=0 then 'Normal' when so.option_id=-1"
          + " then 'Others' else null end) ) "
          + " as option_value, so.option_remarks, "
          + " sid.coordinate_x, sid.coordinate_y, sid.marker_id, notes "
          + " FROM patient_section_details psd "
          + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id"
          + " AND psf.form_type='Form_TRI') "
          + " JOIN patient_section_fields sf ON (sf.section_detail_id=psd.section_detail_id) "
          + " JOIN section_field_desc fd ON (fd.field_id=sf.field_id) "
          + " LEFT JOIN patient_section_options so ON (so.field_detail_id=sf.field_detail_id"
          + " AND so.available='Y') "
          + " LEFT JOIN section_field_options fo ON (fo.option_id=so.option_id) "
          + " LEFT JOIN patient_section_image_details sid ON (sid.field_detail_id"
          + " = sf.field_detail_id AND sid.available='Y') "
          + " WHERE psd.section_item_id=? AND psd.section_id > 0 "
          + " ORDER BY psd.section_detail_id, fd.display_order, fo.display_order";

  /**
   * Gets the triage section data.
   *
   * @param sectionItemId the section item id
   * @return the triage section data
   */
  public List<BasicDynaBean> getTriageSectionData(Integer sectionItemId) {
    return DatabaseHelper.queryToDynaList(TRIAGE_SECTION_DATA, new Object[] {sectionItemId});
  }

  /** The get consultation physician form field values. */
  private static String GET_CONSULTATION_PHYSICIAN_FORM_FIELD_VALUES =
      " SELECT textcat_linecat(' ' || section_title || ' :- ' || value) AS cons_fields_value "
          + " FROM (SELECT section_title, value "
          + " FROM section_master sm "
          + " JOIN (SELECT section_det.section_id, textcat_linecat(field_name || ' : ' ||"
          + " COALESCE(option_value,'') ||"
          + " CASE WHEN coalesce(field_remarks, '') != '' then '(' || field_remarks || ')'  "
          + " WHEN coalesce(option_remarks, '') != '' then '(' || option_remarks || ')' "
          + " else '' END) AS value "
          + " FROM patient_section_details section_det "
          + " JOIN patient_section_fields psv USING (section_detail_id)"
          + " LEFT JOIN patient_section_options pso ON (psv.field_detail_id=pso.field_detail_id) "
          + " JOIN section_field_desc sfd "
          + " ON (section_det.section_id = sfd.section_id AND psv.field_id = sfd.field_id) "
          + " LEFT JOIN section_field_options sfo "
          + " ON (sfd.field_id=sfo.field_id AND pso.option_id = sfo.option_id) "
          + " WHERE section_det.section_item_id = ? AND section_det.item_type='CONS' "
          + " AND sfd.use_in_presenting_complaint = 'Y' GROUP BY section_det.section_id "
          + " ) AS foo ON (foo.section_id = sm.section_id) "
          + " JOIN patient_section_details psd ON (psd.section_id=sm.section_id)"
          + " WHERE psd.section_item_id = ? AND psd.item_type='CONS' "
          + " GROUP BY psd.section_id, sm.section_title, foo.value ORDER BY psd.section_id)"
          + " AS foo ";

  /**
   * Gets the cons insta section field values.
   *
   * @param consId the cons id
   * @return the cons insta section field values
   */
  // used to update the presenting complaint.
  public String getConsInstaSectionFieldValues(Integer consId) {
    return DatabaseHelper.getString(GET_CONSULTATION_PHYSICIAN_FORM_FIELD_VALUES, new Object[] {
        consId, consId});
  }

}
