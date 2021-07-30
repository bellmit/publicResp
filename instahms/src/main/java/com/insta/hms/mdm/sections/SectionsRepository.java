package com.insta.hms.mdm.sections;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/** The Class SectionsRepository. */
@Repository
public class SectionsRepository extends MasterRepository<Integer> {

  /** Instantiates a new sections repository. */
  public SectionsRepository() {
    super("section_master", "section_id", "section_title");
  }

  /**
   * it is used to get sections searchquery.
   * 
   */
  @Override
  public SearchQuery getSearchQuery() {
    SearchQuery query = super.getSearchQuery();
    query.setSecondarySortColumn("section_title");
    return query;
  }

  /** The Constant GET_ADDED_SECTION_MASTER_DETAILS. */
  private static final String GET_ADDED_SECTION_MASTER_DETAILS =
      "SELECT sm.section_title, psd.section_detail_id,"
          + "   psf.form_id, display_order, psd.section_id, psd.finalized, psd.finalized_user, "
          + " usr.temp_username "
          + " FROM section_master sm "
          + "   JOIN patient_section_details psd ON (psd.section_id=sm.section_id)"
          + "   JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id)"
          + "   LEFT JOIN u_user usr ON (psd.finalized_user=usr.emp_username)"
          + " WHERE psd.mr_no=? AND psd.patient_id=? AND coalesce(psd.section_item_id, 0)=?"
          + "   AND coalesce(psd.generic_form_id, 0)=? AND psf.form_id=? AND psd.item_type=?"
          + " ORDER BY display_order, psd.section_detail_id";

  /**
   * Gets the added section master details.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the added section master details
   */
  public List<BasicDynaBean> getAddedSectionMasterDetails(
      String mrNo, String patientId, int itemId, int genericFormId, int formId, String itemType) {

    return DatabaseHelper.queryToDynaList(
        GET_ADDED_SECTION_MASTER_DETAILS,
        new Object[] {mrNo, patientId, itemId, genericFormId, formId, itemType});
  }

  /**
   * Gets the sections list.
   *
   * @param formType the form type
   * @return the sections list
   */
  public List<BasicDynaBean> getSectionsList(String formType) {
    String where = "";
    if (formType.equals("Form_OT")) {
      where = " AND linked_to IN ('order item')";
    } else if (formType.equals("Form_IP") || formType.equals("Form_Gen")) {
      where = " AND linked_to IN ('patient', 'visit', 'form')";
    } else if (formType.equals("Form_Serv")) {
      where = " AND linked_to IN ('patient', 'visit', 'order item')";
    }
    String query = "select * from section_master sm where status='A' " + where;
    return DatabaseHelper.queryToDynaList(query);
  }

  /**
   * Gets the section definition.
   *
   * @param sectionId the section id
   * @return the section definition
   */
  public List<BasicDynaBean> getSectionDefinition(int sectionId) {
    String query =
        " SELECT sm.section_id, sfd.field_id, sfd.field_name, "
            + " sfd.field_type, sfd.is_mandatory, sfd.allow_others, sfd.allow_normal,"
            + "  sfd.normal_text, sfd.no_of_lines, "
            + " sfd.phrase_category_id as field_phrase_category_id, "
            + " sfd.pattern_id as field_pattern_id, sfd.display_order as field_display_order, "
            + " sfo.option_id, sfo.option_value, sfd.default_to_current_datetime, "
            + " sfo.phrase_category_id as option_phrase_category_id, "
            + " sfo.display_order as option_display_order, "
            + " sfo.pattern_id as option_pattern_id, sfd.markers, null as marker_id, "
            + " null as marker_detail_id, null as image_id, "
            + " null as coordinate_x, null as coordinate_y, null as notes, null as available,"
            + " null as date, null as date_time, null as field_remarks, null as option_remarks, "
            + " null as field_detail_id, null as option_detail_id "
            + " FROM section_master sm "
            + "  JOIN section_field_desc sfd ON (sm.section_id=sfd.section_id and sfd.status='A')"
            + "  LEFT JOIN LATERAL "
            + "    (SELECT option_id, field_id, option_value, display_order, 'b' as alpha, "
            + "     value_code, phrase_category_id, pattern_id "
            + "   FROM section_field_options isfo "
            + "   WHERE ( sfd.field_id=isfo.field_id and isfo.status='A') "
            + "   UNION ALL "
            + "   SELECT 0 as option_id, sfd.field_id, 'Normal' as option_value, "
            + "   0 as display_order, 'a' as alpha, null, null, null "
            + "   WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + "   UNION ALL "
            + "   SELECT -1 as option_id, sfd.field_id, 'Others' as option_value, "
            + "   0 as display_order, 'c' as alpha, null, null, null "
            + "   WHERE sfd.field_type in ('checkbox', 'dropdown') "
            + " ) as sfo on sfd.field_id=sfo.field_id "
            + " WHERE sm.section_id=? order by  sfd.display_order, sfo.alpha, sfo.display_order";
    return DatabaseHelper.queryToDynaList(query, new Object[] {sectionId});
  }
}
