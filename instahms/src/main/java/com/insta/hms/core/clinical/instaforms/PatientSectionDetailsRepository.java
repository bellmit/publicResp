package com.insta.hms.core.clinical.instaforms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientSectionDetailsRepository.
 *
 * @author anup vishwas
 */

@Repository
public class PatientSectionDetailsRepository extends GenericRepository {

  /**
   * Instantiates a new patient section details repository.
   */
  public PatientSectionDetailsRepository() {
    super("patient_section_details");
  }

  /** The Constant ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT. */
  private static final String ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT = " SELECT field_id, option_id,"
      + " option_value, "
      + " case when field_type in ('text', 'wide text') then field_remarks else option_remarks end "
      + " as option_remarks, field_name, field_type, date_time, date, "
      + " allow_others, allow_normal, normal_text, section_id, section_title, "
      + " coordinate_x, coordinate_y, marker_id, notes, section_detail_id, "
      + " 'sd_' || section_detail_id as str_section_detail_id, psfv.finalized, "
      + " coalesce(image_id, 0) as image_id, field_detail_id, marker_detail_id "
      + " FROM patient_section_field_values_for_print psfv "
      + " WHERE mr_no=? AND patient_id=? AND coalesce(section_item_id, 0)=? "
      + " AND coalesce(generic_form_id, 0)=? AND form_id=? AND item_type=? AND value_found "
      + " ORDER BY section_detail_id, field_display_order, option_display_order, coordinate_x asc, "
      + " coordinate_y desc";

  /**
   * Gets the all section details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param formId
   *          the form id
   * @param itemType
   *          the item type
   * @return the all section details
   */
  public List<BasicDynaBean> getAllSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return DatabaseHelper.queryToDynaList(ALL_SECTIONS_FIELD_VALUES_FOR_PATIENT, new Object[] {
        mrNo, patientId, itemId, genericFormId, formId, itemType });
  }

  /** The Constant GET_FORMS_FROM_TX. */
  private static final String GET_FORMS_FROM_TX = " SELECT DISTINCT psd.section_id, "
      + " psf.display_order, psf.form_id, fc.form_name "
      + " FROM patient_section_details psd "
      + " JOIN patient_section_forms psf USING (section_detail_id) "
      + " JOIN form_components fc ON (fc.id=psf.form_id) "
      + " WHERE psd.section_item_id=? AND psf.form_type=? ORDER BY display_order ";

  /**
   * Gets the section form details.
   *
   * @param consultationId
   *          the consultation id
   * @param formType
   *          the form type
   * @return the section form details
   */
  public List<BasicDynaBean> getSectionFormDetails(int consultationId, String formType) {

    return DatabaseHelper.queryToDynaList(GET_FORMS_FROM_TX, new Object[] { consultationId,
        formType });
  }

}
