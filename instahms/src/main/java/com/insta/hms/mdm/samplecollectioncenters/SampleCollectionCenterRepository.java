package com.insta.hms.mdm.samplecollectioncenters;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SampleCollectionCenterRepository.
 */
@Repository
public class SampleCollectionCenterRepository extends MasterRepository<Integer> {

  /** The Constant COLLECTION_CENTERS_TABLE. */
  private static final String COLLECTION_CENTERS_TABLE = "FROM (  "
      + " SELECT collection_center_id, collection_center,scc.status,scc.center_id,hcm.center_name "
      + " FROM sample_collection_centers scc "
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = scc.center_id) ) AS foo ";

  /**
   * Instantiates a new sample collection center repository.
   */
  public SampleCollectionCenterRepository() {
    super("sample_collection_centers", "collection_center_id", "collection_center");
  }

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(COLLECTION_CENTERS_TABLE);
  }

  /**
   * List by center.
   *
   * @param centerIds the center ids
   * @return the list
   */
  public List<BasicDynaBean> listByCenter(List<Integer> centerIds) {
    StringBuilder query = new StringBuilder(
        "SELECT * from sample_collection_centers WHERE status='A' ");
    DataBaseUtil.addWhereFieldInList(query, "center_id", centerIds, true);
    query.append("ORDER BY collection_center");
    return DatabaseHelper.queryToDynaList(query.toString(), centerIds.toArray());
  }

}
