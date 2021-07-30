package com.insta.hms.mdm.practitionerconsultationmapping;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PractitionerConsultationMappingRepository extends MasterRepository<Integer> {

  public PractitionerConsultationMappingRepository() {
    super("practitioner_type_consultation_mapping", "practitioner_type_consultation_mapping_id");
  }

  /** The Constant DEFAULT_FIELD_LIST. */
  private static final String FIELD_LIST = "SELECT DISTINCT practitioner_type_id, "
      + "practitioner_name, status ";

  /** The Constant DEFAULT_COUNT_QUERY. */
  private static final String COUNT_QUERY = "SELECT COUNT(DISTINCT practitioner_type_id) ";

  /** The Constant FROM_QUERY. */
  private static final String FROM_QUERY = "FROM (SELECT ptcm.practitioner_type_id, "
      + "pt.practitioner_name, ptcm.status FROM practitioner_type_consultation_mapping ptcm "
      + "JOIN practitioner_types pt ON pt.practitioner_id = ptcm.practitioner_type_id ) AS FOO";

  /**
   * Gets the search query.
   *
   * @return the search query
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(FIELD_LIST, COUNT_QUERY, FROM_QUERY);
  }

  private static final String GET_CONSULTATION_TYPE = "SELECT ptcm.practitioner_type_id, "
      + "ptcm.consultation_type_id, ct.consultation_type, ct.duration "
      + "FROM practitioner_type_consultation_mapping ptcm "
      + "JOIN consultation_types ct ON (ct.consultation_type_id = ptcm.consultation_type_id) "
      + "WHERE ##practitioner_type## ##APPT_CAT_FILTER## AND ct.status='A'";

  /**
   * get consultation types by filters practitioner type/appointment category/or all.
   * @param practitionerTypeId practitioner type id
   * @param apptCat appointment category
   * @return returns consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(Integer practitionerTypeId, String apptCat) {
    String query = GET_CONSULTATION_TYPE;
    if (practitionerTypeId == null) {
      query = query.replace("##practitioner_type##", "");
    } else {
      query = query.replace("##practitioner_type##", " ptcm.practitioner_type_id = ?");
    }
    if (apptCat != null && apptCat != "") {
      if (practitionerTypeId != null) {
        query = query.replace("##APPT_CAT_FILTER##", " AND ct.patient_type = 'o' ");
      } else {
        query = query.replace("##APPT_CAT_FILTER##", " ct.patient_type = 'o' ");
      }
    } else {
      query = query.replace("##APPT_CAT_FILTER##", "");
    }
    if (practitionerTypeId == null) {
      return DatabaseHelper.queryToDynaList(query);
    } else {
      return DatabaseHelper.queryToDynaList(query, new Object[] {practitionerTypeId});
    }
  }

}
