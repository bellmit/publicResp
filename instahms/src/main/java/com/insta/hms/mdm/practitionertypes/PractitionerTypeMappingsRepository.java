package com.insta.hms.mdm.practitionertypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PractitionerTypeMappingsRepository extends GenericRepository {

  /**
   * Constructor.
   */
  public PractitionerTypeMappingsRepository() {
    super("practitioner_types_mapping");
    // TODO Auto-generated constructor stub
  }

  private static final String PRACTITIONER_TYPE_MAPPING_QUERY =
      "SELECT ptm.mapping_id, ptm.practitioner_id, ptm.consultation_type_id, "
      + "ptm.visit_type, ct.consultation_type, "
          + "pt.practitioner_name from practitioner_types_mapping ptm "
          + "JOIN practitioner_types pt ON (ptm.practitioner_id = pt.practitioner_id) "
          + "JOIN consultation_types ct ON (ptm.consultation_type_id = ct.consultation_type_id)"
          + " where ptm.center_id = ? # and pt.status='A' and ct.status='A'";
  
  private static final String PRACTIONER_FILTER = " and  ptm.practitioner_id=? ";

  /**
   * returns practitioner mappings.
   * @param centerId center id
   * @return list
   */
  public List<BasicDynaBean> getPractitionerMappings(Integer centerId) {

    String query = PRACTITIONER_TYPE_MAPPING_QUERY;
    query = query.replace("#", "");
    return DatabaseHelper.queryToDynaList(query, new Object[] {centerId});
  }

  /**
   * returns practitioner mappings.
   * @param centerId center id
   * @param practionerId practioner id
   * @return list
   */
  public List<BasicDynaBean> getPractitionerMappings(Integer centerId, Integer practionerId) {
    String query = PRACTITIONER_TYPE_MAPPING_QUERY;
    query = query.replace("#", PRACTIONER_FILTER);
    return DatabaseHelper.queryToDynaList(query, new Object[] { centerId, practionerId });
  }
}
