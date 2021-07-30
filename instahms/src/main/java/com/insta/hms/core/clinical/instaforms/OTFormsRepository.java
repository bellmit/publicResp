package com.insta.hms.core.clinical.instaforms;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;

/**
 * The Class OTFormsRepository.
 *
 * @author anup vishwas
 */
public class OTFormsRepository {

  /** The Constant GET_FORMS_FROM_TX. */
  private static final String GET_FORMS_FROM_TX = " SELECT DISTINCT psd.section_id,"
      + " psf.display_order, psf.form_id, fc.form_name FROM patient_section_details psd "
      + " JOIN patient_section_forms psf USING (section_detail_id) "
      + " JOIN form_components fc ON (fc.id=psf.form_id) "
      + " WHERE psd.section_item_id=? AND psf.form_type='Form_OT' ORDER BY display_order ";

  /**
   * Gets the section form details.
   *
   * @param consultationId
   *          the consultation id
   * @return the section form details
   */
  public List<BasicDynaBean> getSectionFormDetails(int consultationId) {

    return DatabaseHelper.queryToDynaList(GET_FORMS_FROM_TX, new Object[] { consultationId });
  }

  /** The Constant GET_ACTIVE_FORMS. */
  private static final String GET_ACTIVE_FORMS = " SELECT foo.section_id::int as section_id, "
      + " foo.id as form_id, form_name "
      + " FROM (SELECT fc.id, fc.form_name, regexp_split_to_table(fc.sections, ',') "
      + " as section_id, "
      + " generate_series(1, array_upper(regexp_split_to_array(fc.sections, E','), 1)) "
      + " as display_order "
      + " FROM form_components fc, form_department_details fdd where fdd.dept_id=? "
      + " and form_type='Form_OT' and fc.id=fdd.id) as foo "
      + " LEFT JOIN section_master sm ON (sm.section_id::text=foo.section_id) "
      + " WHERE coalesce(sm.status, 'A')='A' order by display_order ";

  /**
   * Form component details.
   *
   * @param deptId
   *          the dept id
   * @return the list
   */
  public List<BasicDynaBean> formComponentDetails(String deptId) {

    return DatabaseHelper.queryToDynaList(GET_ACTIVE_FORMS, new Object[] { deptId });
  }

}
